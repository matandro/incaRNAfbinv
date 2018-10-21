package bgu.bioinf.rnaDesign.model;

import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.JobEntity;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by matan on 15/10/15.
 */
public class JobInfoModel {
    private String queryName;
    private String email;
    private String queryStructure;
    private String querySequence;
    private String jobId;
    private Integer outputAmount;
    private Float gcContent;
    private Float targetEnergy;
    private Float targetMR;
    private String seedSequence;
    private Integer noIterations;
    private Float GcError;

    public void setJobStatus(JobEntity.Status jobStatus) {
        this.jobStatus = jobStatus;
    }

    public JobEntity.Status getJobStatus() {
        return jobStatus;
    }

    private JobEntity.Status jobStatus;

    public String getJobError() {
        return jobError;
    }

    private String jobError;

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    private Date startTime;

    public String getMotifConstraint() {
        return motifConstraint;
    }

    private String motifConstraint;

    private Date endTime;
    public JobInfoModel() {
        motifConstraint = null;
        queryStructure = null;
        querySequence = null;
        seedSequence = null;
        targetEnergy = null;
        outputAmount = null;
        queryName = null;
        gcContent = null;
        targetMR = null;
        email = null;
        jobId = null;
        jobError = null;
        GcError = null;
        jobStatus = null;
    }

    public void updateModelFromEntity(JobEntity jobEntity) {
        this.setMotifConstraint(jobEntity.getMotifConstraint());
        this.setQueryStructure(jobEntity.getQueryStructure());
        this.setQuerySequence(jobEntity.getQuerySequence());
        this.setNoIterations(jobEntity.getNoIterations());
        this.setOutputAmount(jobEntity.getOutputAmount());
        this.setSeedSequence(jobEntity.getSeedSequence());
        this.setTargetEnergy(jobEntity.getTargetEnergy());
        this.setQueryName(jobEntity.getQueryName());
        this.setGcContent(jobEntity.getGcContent());
        this.setStartTime(jobEntity.getStartTime());
        this.setTargetMR(jobEntity.getTargetMr());
        this.setEndTime(jobEntity.getEndTime());
        this.setGcError(jobEntity.getGcError());
        this.setJobId(jobEntity.getJobId());
        this.setEmail(jobEntity.getEmail());
        this.setJobStatus(jobEntity.getJobStatus());
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setQueryStructure(String queryStructure) {
        this.queryStructure = queryStructure;
    }

    public void setQuerySequence(String querySequence) {
        this.querySequence = querySequence;
    }

    public String getQuerySequence() {
        return querySequence;
    }

    public String getQueryStructure() {
        return queryStructure;
    }

    public String getJobId() {
        return jobId;
    }

    public String getEmail() {
        return email;
    }

    public void setOutputAmount(Integer outputAmount) {
        this.outputAmount = outputAmount;
    }

    public Integer getOutputAmount() {
        return outputAmount;
    }

    public void setGcContent(Float GCContent) {
        this.gcContent = GCContent;
    }

    public void setTargetEnergy(float targetEnergy) {
        this.targetEnergy = targetEnergy;
    }

    public void setTargetMR(float targetMR) {
        this.targetMR = targetMR;
    }

    public Float getTargetEnergy() {
        return targetEnergy;
    }

    public Float getTargetMR() {
        return targetMR;
    }

    public Float getGcContent() {
        return gcContent;
    }

    public void setSeedSequence(String seedSequence) {
        this.seedSequence = seedSequence;
    }

    public String getSeedSequence() {
        return seedSequence;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Integer getNoIterations() {
        return noIterations;
    }

    public void setNoIterations(Integer noIterations) {
        this.noIterations = noIterations;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Float getGcContentDecimal() {
        return this.getGcContent() / 100;
    }

    public Float getGcErrorDecimal() {
        return this.getGcError() / 100;
    }

    public boolean isJobReady() {
        return (this.endTime != null);
    }

    public String getFormattedStartTime() {
        return (getStartTime() == null) ? "" : Utils.getFormmatedTime(getStartTime());
    }

    public String getFormattedEndTime() {
        return (getEndTime() == null) ? "" : Utils.getFormmatedTime(getEndTime());
    }

    public void setJobError(String jobError) {
        this.jobError = jobError;
    }

    public SEEDTYPE getSeedType () {
        return SEEDTYPE.getSeedType(this);
    }

    public String getSeedTypeString () {
        return SEEDTYPE.getSeedTypeName(SEEDTYPE.getSeedType(this));
    }

    public void setGcError(Float GCError) {
        this.GcError = GCError;
    }

    public Float getGcError(){
        return this.GcError;
    }

    public void setMotifConstraint(String motifConstraint) {
        this.motifConstraint = motifConstraint;
    }

    public enum SEEDTYPE {
        INCARNATION,
        RANDOM,
        INITIAL;

        public static SEEDTYPE getSeedType(JobInfoModel jobInfoModel) {
            SEEDTYPE result = null;
            if (jobInfoModel.getGcContent() != null) {
                result = INCARNATION;
            } else if (jobInfoModel.getSeedSequence() != null) {
                result = INITIAL;
            } else {
                result = RANDOM;
            }
            return result;
        }

        public static String getSeedTypeName(SEEDTYPE seedType) {
            String result = null;
            switch (seedType) {
                case INCARNATION:
                    result = "incaRNAtion";
                    break;
                case RANDOM:
                    result = "Random";
                    break;
                case INITIAL:
                    result = "Initial Seed";
                    break;
                default:
                    Utils.log("SEVERE", true, " SEEDTYPE.getSeedTypeName input [" + seedType + "] is not supported");
                    break;
            }
            return result;
        }
    }
}
