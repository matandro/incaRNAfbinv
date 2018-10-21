package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.db.DBConnector;
import bgu.bioinf.rnaDesign.db.JobEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matan on 23/12/15.
 */
public class JobListProducer {
    private String jobName;

    public JobListProducer(String jobName) {
        this.jobName = jobName;
    }

    public List<JobInfoModel> getJobList() {
        List<JobInfoModel> results = new ArrayList<JobInfoModel>();
        if (jobName == null || "".equals(jobName))
            return results;

        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            TypedQuery<JobEntity> allByJobName = em.createNamedQuery("Job.GetAllByQname", JobEntity.class);
            allByJobName.setParameter("queryNamePatt","%" + jobName + "%");
            for (JobEntity job: allByJobName.getResultList()) {
                JobInfoModel jobResult = new JobInfoModel();
                jobResult.updateModelFromEntity(job);
                results.add(jobResult);
            }
        } catch (Exception e) {
            Utils.log("SEVERE", e, "JobListProducer.getJobList - Failed to generate job results");
            results = null;
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {}
            }
        }
        return results;
    }
}
