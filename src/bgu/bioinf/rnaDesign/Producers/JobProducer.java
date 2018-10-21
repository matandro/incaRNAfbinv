package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Runners.JobRunner;
import bgu.bioinf.rnaDesign.db.DBConnector;
import bgu.bioinf.rnaDesign.db.JobEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by matan on 17/11/15.
 */
public class JobProducer {
    private JobInfoModel jobInformation;
    private String error;

    public JobProducer(JobInfoModel jobInformation) {
        this.jobInformation = jobInformation;
    }

    public boolean resolveJob() {
        boolean result = false;

        Timestamp now = new Timestamp(new Date().getTime());
        EntityManager em = null;
        EntityTransaction et = null;
        try {
            em = DBConnector.getEntityManager();
            et = em.getTransaction();
            et.begin();

            JobEntity jobEntity = new JobEntity();
            jobEntity.setStartTime(now);
            jobEntity.setQueryName(jobInformation.getQueryName());
            jobEntity.setEmail(jobInformation.getEmail());
            jobEntity.setQuerySequence(jobInformation.getQuerySequence());
            jobEntity.setQueryStructure(jobInformation.getQueryStructure());
            jobEntity.setOutputAmount(jobInformation.getOutputAmount());
            jobEntity.setTargetEnergy(jobInformation.getTargetEnergy());
            jobEntity.setTargetMr(jobInformation.getTargetMR());
            jobEntity.setGcContent(jobInformation.getGcContent());
            jobEntity.setSeedSequence(jobInformation.getSeedSequence());
            jobEntity.setGcError(jobInformation.getGcError());
            jobEntity.setMotifConstraint(jobInformation.getMotifConstraint());
            jobEntity.setJobStatus(JobEntity.Status.QUEUED);
            synchronized (WebappContextListener.rnGesus) {
                jobEntity.setJobId(generateRandomUnusedId(em));
                em.persist(jobEntity);
                et.commit();
            }
            result = true;
            jobInformation.setJobId(jobEntity.getJobId());
        } catch (Exception e) {
            Utils.log("ERROR", e, "JobProducer.resolveJob failed to resolve job: "
                    + jobInformation.getJobId());
            if (et != null && et.isActive()) {
                try {
                    et.rollback();
                } catch (Exception ignore) {
                }
            }
            error = "Failed to create job, If problem persist please inform site manager";
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }

        if (result) {
            result = submitTask();
            // If failed to submit remove from DB
            if (!result) {
                try {
                    em = DBConnector.getEntityManager();
                    et = em.getTransaction();
                    et.begin();

                    JobEntity jobEntity = em.find(JobEntity.class, jobInformation.getJobId());
                    em.remove(jobEntity);
                    et.commit();
                } finally {
                    if (em != null && em.isOpen()) {
                        try {
                            em.close();
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean submitTask() {
        boolean submitted = false;
        try {
            JobRunner jobRunner = new JobRunner(jobInformation);
            WebappContextListener.jobExecutor.execute(jobRunner);
            submitted = true;
            Utils.log("INFO", false, " JobProducer.submitTask: JobId "
                    + jobInformation.getJobId() + " submitted.");
        } catch (Exception e) {
            error = "Failed to submit job for calculation";
            Utils.log("ERROR", e, "JobProducer.submitTask "
                    + jobInformation.getJobId());
        }
        return submitted;
    }

    public String getError() {
        return error;
    }

    public boolean sendEmail() {
        return MailDispatcher.submissionMail(jobInformation);
    }

    private String generateRandomUnusedId(EntityManager em) {
        String randomId = null;
        boolean success = false;
        while (!success) {
            randomId = Utils.generateRandomId();
            JobEntity je = em.find(JobEntity.class, randomId);
            if (je == null) {
                success = true;
            }
        }
        return randomId;
    }
}
