package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.db.*;
import bgu.bioinf.rnaDesign.model.JobInfoModel;
import bgu.bioinf.rnaDesign.model.JobResultModel;
import bgu.bioinf.rnaDesign.model.SingleResultModel;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 01/12/15.
 */
public class ResultProducer {
    private JobInfoModel jobInfoModel;
    private String error;
    private JobResultModel jobResultModel;

    public ResultProducer(String jobId, int page, String sortBy, List<Float> filters, long maxResultsInPage) {
        this.jobInfoModel = new JobInfoModel();
        this.jobResultModel = new JobResultModel();
        this.jobResultModel.setMaxResultsInPage(maxResultsInPage);
        this.jobResultModel.setFilters(filters);
        this.jobResultModel.setSortBy(sortBy);
        this.jobResultModel.setPage(page);
        this.jobInfoModel.setJobId(jobId);
        this.error = "";
    }

    public ResultProducer(String jobId) {
        this.jobInfoModel = new JobInfoModel();
        this.jobResultModel = new JobResultModel();
        this.jobInfoModel.setJobId(jobId);
        this.error = "";
    }

    public boolean isResultsReady() {
        boolean result = false;
        EntityManager em = null;
        if (jobInfoModel.getJobId() == null || "".equals(jobInfoModel.getJobId())) {
            error = "Job ID was not attached to the request.";
        } else {
            try {
                em = DBConnector.getEntityManager();
                JobEntity jobEntity = em.find(JobEntity.class, jobInfoModel.getJobId());
                if (jobEntity == null) {
                    error = "Could not find job " + jobInfoModel.getJobId();
                } else {
                    jobInfoModel.updateModelFromEntity(jobEntity);
                    if (jobEntity.getEndTime() != null) {
                        result = true;

                        JobErrorEntity jobErrorEntity = em.find(JobErrorEntity.class, jobInfoModel.getJobId());
                        if (jobErrorEntity != null)
                            jobInfoModel.setJobError(jobErrorEntity.getErrorStr());
                    }
                }
            } catch (Exception e) {
                error = "Failed to connect to the database, please try again later.";
                Utils.log("SEVERE", e, "ResultProducer.isResultReady - " + error);
            } finally {
                if (em != null)
                    em.close();
            }
        }
        return result;
    }

    public SingleResultModel getSingleResult(int runNo) {
        SingleResultModel singleResultModel = null;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            JobResultEntityPK jobResultEntityPK = new JobResultEntityPK();
            jobResultEntityPK.setResultNo(runNo);
            jobResultEntityPK.setJobId(jobInfoModel.getJobId());
            JobResultEntity jobResultEntity = em.find(JobResultEntity.class, jobResultEntityPK);
            if (jobResultEntity == null) {
                error = "Failed to find run " + runNo + " for job " + jobInfoModel.getJobId();
            } else {
                singleResultModel = new SingleResultModel(jobResultEntity);
            }
        } catch (Exception e) {
            error = "Failed to connect to the database, please try again later.";
            Utils.log("SEVERE", e, "ResultProducer.getSingleResult " + error);
        } finally {
            if (em != null)
                em.close();
        }
        return singleResultModel;
    }

    public JobInfoModel getJobInformation() {
        return jobInfoModel;
    }

    public String getError() {
        return error;
    }

    private Predicate getFilterPredicate(Predicate where, CriteriaBuilder criteriaBuilder, Root<JobResultEntity> root) {
        if (jobResultModel.getFilters() != null) {
            Map<Expression<Boolean>, ParameterExpression<Integer>> addedWheres = new HashMap<Expression<Boolean>, ParameterExpression<Integer>>();
            for (int i = 0; i < jobResultModel.getFilters().size() && i < JobResultModel.FILTER_NAMES.length; ++i) {
                if (jobResultModel.getFilters().get(i) != null) {
                    ParameterExpression<Float> matrixParam = criteriaBuilder.parameter(Float.class);
                    if (where != null) {
                        where = criteriaBuilder.and(where,
                                criteriaBuilder.le(root.<Float>get(JobResultModel.FILTER_COLUMN_NAMES[i]),
                                        jobResultModel.getFilters().get(i)));
                    }
                }
            }
        }
        return where;
    }

    public boolean initiateResults() {
        boolean result = false;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<JobResultEntity> criteriaQuery = criteriaBuilder.createQuery(JobResultEntity.class);
            Root<JobResultEntity> root = criteriaQuery.from(JobResultEntity.class);
            ParameterExpression<String> jobIdParam = criteriaBuilder.parameter(String.class);
            criteriaQuery.select(root);
            Predicate where = criteriaBuilder.equal(root.get("jobId"), jobInfoModel.getJobId());
            where = getFilterPredicate(where, criteriaBuilder, root);
            criteriaQuery.where(where);
            List<Order> orderBy = analyzeSortBy(criteriaBuilder, root, jobResultModel.getSortBy());
            criteriaQuery.orderBy(orderBy);
            TypedQuery<JobResultEntity> query = em.createQuery(criteriaQuery);
            //em.createNamedQuery("JobResult.GetAllByJobID", JobResultEntity.class);
            if (jobResultModel.getMaxResultsInPage() > 0) {
                query.setFirstResult((int) (jobResultModel.getPage() * jobResultModel.getMaxResultsInPage()));
                query.setMaxResults((int) jobResultModel.getMaxResultsInPage());
            }
            //query.setParameter("jobId", jobEntity.getJobId());

            for (JobResultEntity jobResultsEntity : query.getResultList()) {
                jobResultModel.addResult(jobResultsEntity);
            }
            result = true;
        } catch (Exception e) {
            error = "Failed to load results from Database, pleas try again later.";
            Utils.log("SEVERE", e, "ResultProducer.initiateResults - " + error);
        } finally {
            if (em != null)
                em.close();
        }
        return result;
    }

    public long getNoOfResults(boolean useFilters) {
        long results;
        EntityManager em = null;
        try {
            em = DBConnector.getEntityManager();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<JobResultEntity> root = criteriaQuery.from(JobResultEntity.class);
            ParameterExpression<String> jobIdParam = criteriaBuilder.parameter(String.class);
            criteriaQuery.select(criteriaBuilder.count(root));
            Predicate where = criteriaBuilder.equal(root.get("jobId"), jobInfoModel.getJobId());
            if (useFilters) {
                where = getFilterPredicate(where, criteriaBuilder, root);
            }
            criteriaQuery.where(where);
            TypedQuery<Long> query = em.createQuery(criteriaQuery);
            results = query.getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            results = 0;
        } finally {
            if (em != null && em.isOpen()) {
                try {
                    em.close();
                } catch (Exception ignore) {
                }
            }
        }
        return results;
    }

    private List<Order> analyzeSortBy(CriteriaBuilder cb, Root<JobResultEntity> root, String sortBy) {
        List<Order> orderBy = new ArrayList<Order>();

        if (sortBy == null || "".equals(sortBy)) {
            if (jobInfoModel.getVersion() == 2) {
                orderBy.add(0, cb.asc(root.get("designScore")));
            }
            orderBy.add(0, cb.asc(root.get("structureDistance")));
            orderBy.add(0, cb.asc(root.get("shapiroDistance")));
        } else {
            try {
                String[] sort = sortBy.split("_");
                if (!"DESC".equals(sort[1])) {
                    orderBy.add(cb.asc(root.get(sort[0])));
                } else {
                    orderBy.add(cb.desc(root.get(sort[0])));
                }
            } catch (Exception e) {
                if (sortBy != null && !"".equals(sortBy)) {
                    Utils.log("SEVERE", e, "unknown sort type: \"" + sortBy + "\"");
                }
            }
        }

        orderBy.add(cb.asc(root.get("resultNo")));

        return orderBy;
    }

    public JobResultModel getJobResults() {
        return jobResultModel;
    }

    public JobEntity.Status getJobStatus() {
        return jobInfoModel.getJobStatus();
    }
}
