package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.db.JobResultEntity;

public interface DesignRunner {
    public boolean generateSingleResult(String seed, JobResultEntity jobResultEntity);
    public void cleanRunner();
}
