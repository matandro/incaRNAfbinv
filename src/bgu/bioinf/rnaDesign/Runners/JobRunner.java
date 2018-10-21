package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.DBConnector;
import bgu.bioinf.rnaDesign.db.JobEntity;
import bgu.bioinf.rnaDesign.db.JobErrorEntity;
import bgu.bioinf.rnaDesign.db.JobResultEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;
import bgu.bioinf.rnaDesign.model.JobResultModel;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * Created by matan on 18/11/15.
 */
public class JobRunner implements Runnable {
    private static final int MAX_RETRY=10;

    private JobInfoModel jobInformation;

    public JobRunner(JobInfoModel jobInformation) {
        this.jobInformation = jobInformation;
    }

    private void setStatus(JobEntity.Status jobStatus, EntityManager em) {
        EntityTransaction et = null;
        try {
            et = em.getTransaction();
            et.begin();
            JobEntity jobEntity = em.find(JobEntity.class, jobInformation.getJobId());
            jobEntity.setJobStatus(jobStatus);
            em.persist(jobEntity);
            et.commit();
        } catch (Exception e) {
            Utils.log("SEVERE", e," JobRunner.setStatus Failed to change job status");
            et.rollback();
            reportError("Failed to change job status.");
        }
    }

    @Override
    public void run() {
        int resultNo = 0;
        EntityManager em = null;
        EntityTransaction et = null;
        RNAfbinvRunner rnafbinvRunner = new RNAfbinvRunner(jobInformation);
        try {
            em = DBConnector.getEntityManager();

            Utils.log("INFO", false, " JobRunner.run: JobId "
                    + jobInformation.getJobId() + " started seed preperation.");
            setStatus(JobEntity.Status.PREP_SEED, em);
            List<String> seeds = generateSeeds();
            if (seeds == null || seeds.size() == 0) {
                String seedError = "Failed to generate seeds.";
                if (jobInformation.getGcContent() != null) {
                    seedError += " Your incaRNAtion setting might be too strict.";
                }
                reportError(seedError);
                return;
            }

            Utils.log("INFO", false, " JobRunner.run: JobId "
                    + jobInformation.getJobId() + " started sequence generation.");
            setStatus(JobEntity.Status.GENERATE_SEQ, em);

            HashSet<String> generatedSequences = new HashSet<String>(seeds.size());
            int retryLeft = MAX_RETRY;
            et = em.getTransaction();
            rnafbinvRunner.generateInputFile();
            ExecutorCompletionService<SingleResultCallable> ecs =
                    new ExecutorCompletionService<SingleResultCallable>(WebappContextListener.designExecutor);
            // Maybe this should be done by multiple threads?
            for (String seed : seeds) {
                ecs.submit(new SingleResultCallable(jobInformation,
                        ++resultNo, seed, rnafbinvRunner));
            }

            et.begin();

            while(resultNo-- > 0) {
                SingleResultCallable current = null;
                String runError = null;
                try {
                     current = ecs.take().get();
                }
                catch (ExecutionException ee) {
                    runError = ee.getMessage();
                }
                if (current != null) {
                    if (current.getError() == null) {
                        String sequence = current.getJobResultEntity().getResultSequence();
                        // If generated an existing sequence, retry (up to MAX_RETRY)
                        if (generatedSequences.contains(sequence)){
                            if (retryLeft-- > 0) {
                                resultNo++;
                                ecs.submit(current);
                            } else {
                                runError = "Repeating result";
                            }
                        } else {
                            generatedSequences.add(sequence);
                            persistResult(em, current);
                            try {
                                et.commit();
                            } catch (Exception e) {
                                Utils.log("ERROR", e, "Failed to commit result: "
                                        + current.getJobResultEntity().printResult());
                            }
                            et.begin();
                        }
                    } else {
                        runError = current.getError();
                    }
                }
                if (runError != null) {
                    Utils.log("ERROR", true, " jobRunner.run: Failed on run "
                            + jobInformation.getJobId() + " error: " + runError);
                }
            }

            JobEntity jobEntity = em.find(JobEntity.class, jobInformation.getJobId());
            jobEntity.setEndTime(new Timestamp(new Date().getTime()));
            jobEntity.setJobStatus(JobEntity.Status.SUCCESS);
            em.persist(jobEntity);
            et.commit();
        } catch (Exception e) {
            Utils.log("SEVERE", e," JobRunner.run failed to calculate results rollback");
            et.rollback();
            reportError("Failed to add result information to DB.");
        } finally {
            if (em != null)
                em.close();
            rnafbinvRunner.cleanInputFile();
        }
    }

    public void persistResult(EntityManager em, SingleResultCallable singleResultCallable) {
        JobResultEntity jobResultEntity = singleResultCallable.getJobResultEntity();
        em.persist(jobResultEntity);
    }

    /**
     * Attaches error to job in DB and write log
     *
     * @param error the error string
     */
    private void reportError(String error) {
        String finalError = error + " please try again, if the problem persist contact the admin.";
        Utils.log("ERROR", true, " jobRunner.reportError " + finalError);

        EntityTransaction et = null;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            et = em.getTransaction();
            et.begin();
            // Add error
            JobErrorEntity jobErrorEntity = new JobErrorEntity();
            jobErrorEntity.setJobId(jobInformation.getJobId());
            jobErrorEntity.setErrorStr(finalError);
            em.persist(jobErrorEntity);
            // Marks run for end
            JobEntity jobEntity = em.find(JobEntity.class, jobInformation.getJobId());
            jobEntity.setEndTime(new Timestamp(new Date().getTime()));
            jobEntity.setJobStatus(JobEntity.Status.FAILURE);
            em.persist(jobEntity);
            et.commit();
        } catch (Exception e) {
            Utils.log("SEVERE", e, " jobRunner.reportError - Failed to add error to job");
            if (et != null && et.isActive()) {
                et.rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Generate seeds for RNAfbinv
     *
     * @return A list of seeds, size of output amount
     */
    private List<String> generateSeeds() {
        List<String> seeds = new ArrayList<String>(jobInformation.getOutputAmount());
        if (jobInformation.getGcContent() != null) {
            // Run incaRNA to generate seeds
            IncaRNAtionRunner incaRNAtionRunner = new IncaRNAtionRunner(jobInformation);
            incaRNAtionRunner.populateList(seeds);
        } else if (jobInformation.getSeedSequence() != null) {
            // Create a repeat of random seeds
            for (int i = 0; i < jobInformation.getOutputAmount(); ++i) {
                seeds.add(jobInformation.getSeedSequence());
            }
        } else {
            // Generate random seeds
            for (int i = 0; i < jobInformation.getOutputAmount(); ++i) {
                seeds.add(Utils.generateSingleSeed(jobInformation.getQuerySequence()));
            }
        }
        return seeds;
    }
}
