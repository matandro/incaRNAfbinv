package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.db.JobResultEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import javax.persistence.EntityManager;
import java.util.concurrent.Callable;

/**
 * Created by matan on 14/12/15.
 */
public class SingleResultCallable implements Callable<SingleResultCallable> {

    private EntityManager em;
    private JobInfoModel jobInformation;
    private int runNo;
    private String seedSequence;
    private RNAfbinvRunner rnafbinvRunner;
    private String error;
    private JobResultEntity jobResultEntity;

    public SingleResultCallable(JobInfoModel jobInformation, int runNo,
                                String seedSequence, RNAfbinvRunner rnAfbinvRunner) {
        this.jobInformation = jobInformation;
        this.runNo = runNo;
        this.seedSequence = seedSequence;
        this.rnafbinvRunner = rnAfbinvRunner;
        this.error = null;
        jobResultEntity = null;
    }


    @Override
    public SingleResultCallable call() throws Exception {
        // TODO: extend error to carry actual errors
        SingleResultCallable result = null;
        jobResultEntity = new JobResultEntity();
        try {
            jobResultEntity.setJobId(jobInformation.getJobId());
            jobResultEntity.setResultNo(runNo);
            boolean rnafbinvSuccess = rnafbinvRunner.generateSingleResult(seedSequence, jobResultEntity);
            if (rnafbinvSuccess) {
                RNAfoldRunner rnaFoldRunner = new RNAfoldRunner(jobResultEntity.getResultSequence());
                rnaFoldRunner.generateInfo(jobInformation.getQueryStructure());
                jobResultEntity.setEnergyScore(rnaFoldRunner.getEnergy());
                jobResultEntity.setShapiroCoarseStructure(rnaFoldRunner.getShapiroCoarseStructure());
                jobResultEntity.setShapiroStructure(rnaFoldRunner.getShapiroStructure());
                jobResultEntity.setSeedSequence(seedSequence);
                jobResultEntity.setResultStructure(rnaFoldRunner.getStructure());
                jobResultEntity.setShapiroDistance(rnaFoldRunner.getShapiroDistance());
                jobResultEntity.setStructureDistance(rnaFoldRunner.getBpDistance());
                jobResultEntity.setGcContent(calcGC(jobResultEntity.getResultSequence()));
                result = this;
            }
        } catch (Exception e) {
            error = e.getMessage() + "\n" + e.toString();
        }
        return result;
    }

    private Float calcGC(String resultSequence) {
        int gcCount = 0;
        for (int i = 0 ; i < resultSequence.length() ; ++i) {
            if (resultSequence.charAt(i) == 'c' || resultSequence.charAt(i) == 'C'
                    || resultSequence.charAt(i) == 'g' || resultSequence.charAt(i) == 'G')
                gcCount++;
        }
        return (float)(gcCount * 100.0 / resultSequence.length());
    }

    public JobResultEntity getJobResultEntity() {
        return jobResultEntity;
    }

    public String getError() {
        return error;
    }
}
