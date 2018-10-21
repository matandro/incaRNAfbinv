package bgu.bioinf.rnaDesign.db;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matan on 01/12/15.
 */
@Entity
@Table(name = "Job", schema = "RNADesign", catalog = "")
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "Job.GetByNameStartTime",
                query = "Select Job.* " +
                        "From Job " +
                        "Where Job.QueryName=? And Job.StartTime=?",
                resultClass = JobEntity.class
        ),
        @NamedNativeQuery(
                name = "Job.GetAllByQname",
                query = "Select Job.* " +
                        "From Job " +
                        "Where Job.QueryName LIKE :queryNamePatt " +
                        "Order By Job.StartTime DESC",
                resultClass = JobEntity.class
        ),
        @NamedNativeQuery(name = "Job.GetWeekOldJobs",
                query = "SELECT Job.* " +
                        "FROM Job " +
                        "WHERE EndTime < NOW() - INTERVAL 1 WEEK",
                resultClass = JobEntity.class)})
public class JobEntity {
    private String jobId;
    private String queryName;
    private String email;
    private String querySequence;
    private String queryStructure;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer outputAmount;
    private String seedSequence;
    private Float gcContent;
    private float targetEnergy;
    private float targetMr;
    private int noIterations;
    private Float gcError;
    private String motifConstraint;
    private Integer jobStatus;

    @Id
    @Column(name = "JobId")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Basic
    @Column(name = "QueryName")
    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    @Basic
    @Column(name = "Email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Basic
    @Column(name = "QuerySequence")
    public String getQuerySequence() {
        return querySequence;
    }

    public void setQuerySequence(String querySequence) {
        this.querySequence = querySequence;
    }

    @Basic
    @Column(name = "QueryStructure")
    public String getQueryStructure() {
        return queryStructure;
    }

    public void setQueryStructure(String queryStructure) {
        this.queryStructure = queryStructure;
    }

    @Basic
    @Column(name = "StartTime")
    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    @Basic
    @Column(name = "EndTime")
    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    @Basic
    @Column(name = "OutputAmount")
    public Integer getOutputAmount() {
        return outputAmount;
    }

    public void setOutputAmount(Integer outputAmount) {
        this.outputAmount = outputAmount;
    }

    @Basic
    @Column(name = "SeedSequence")
    public String getSeedSequence() {
        return seedSequence;
    }

    public void setSeedSequence(String seedSequence) {
        this.seedSequence = seedSequence;
    }

    @Basic
    @Column(name = "GCContent")
    public Float getGcContent() {
        return gcContent;
    }

    public void setGcContent(Float gcContent) {
        this.gcContent = gcContent;
    }

    @Basic
    @Column(name = "TargetEnergy")
    public float getTargetEnergy() {
        return targetEnergy;
    }

    public void setTargetEnergy(float targetEnergy) {
        this.targetEnergy = targetEnergy;
    }

    @Basic
    @Column(name = "TargetMR")
    public float getTargetMr() {
        return targetMr;
    }

    public void setTargetMr(float targetMr) {
        this.targetMr = targetMr;
    }

    @Basic
    @Column(name = "NoIterations")
    public int getNoIterations() {
        return noIterations;
    }

    public void setNoIterations(int noIterations) {
        this.noIterations = noIterations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobEntity jobEntity = (JobEntity) o;

        if (noIterations != jobEntity.noIterations) return false;
        if (Float.compare(jobEntity.targetEnergy, targetEnergy) != 0) return false;
        if (Float.compare(jobEntity.targetMr, targetMr) != 0) return false;
        if (email != null ? !email.equals(jobEntity.email) : jobEntity.email != null) return false;
        if (endTime != null ? !endTime.equals(jobEntity.endTime) : jobEntity.endTime != null) return false;
        if (gcContent != null ? !gcContent.equals(jobEntity.gcContent) : jobEntity.gcContent != null) return false;
        if (gcError != null ? !gcError.equals(jobEntity.gcError) : jobEntity.gcError != null) return false;
        if (jobId != null ? !jobId.equals(jobEntity.jobId) : jobEntity.jobId != null) return false;
        if (outputAmount != null ? !outputAmount.equals(jobEntity.outputAmount) : jobEntity.outputAmount != null)
            return false;
        if (queryName != null ? !queryName.equals(jobEntity.queryName) : jobEntity.queryName != null) return false;
        if (querySequence != null ? !querySequence.equals(jobEntity.querySequence) : jobEntity.querySequence != null)
            return false;
        if (queryStructure != null ? !queryStructure.equals(jobEntity.queryStructure) : jobEntity.queryStructure != null)
            return false;
        if (seedSequence != null ? !seedSequence.equals(jobEntity.seedSequence) : jobEntity.seedSequence != null)
            return false;
        if (startTime != null ? !startTime.equals(jobEntity.startTime) : jobEntity.startTime != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + (queryName != null ? queryName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (querySequence != null ? querySequence.hashCode() : 0);
        result = 31 * result + (queryStructure != null ? queryStructure.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        result = 31 * result + (outputAmount != null ? outputAmount.hashCode() : 0);
        result = 31 * result + (seedSequence != null ? seedSequence.hashCode() : 0);
        result = 31 * result + (gcContent != null ? gcContent.hashCode() : 0);
        result = 31 * result + (gcError != null ? gcError.hashCode() : 0);
        result = 31 * result + (targetEnergy != +0.0f ? Float.floatToIntBits(targetEnergy) : 0);
        result = 31 * result + (targetMr != +0.0f ? Float.floatToIntBits(targetMr) : 0);
        result = 31 * result + noIterations;
        return result;
    }

    @Basic
    @Column(name = "GCError")
    public Float getGcError() {
        return gcError;
    }

    public void setGcError(Float gcError) {
        this.gcError = gcError;
    }

    @Basic
    @Column(name = "MotifConstraint")
    public String getMotifConstraint() {
        return motifConstraint;
    }

    public void setMotifConstraint(String motifConstraint) {
        this.motifConstraint = motifConstraint;
    }

    @Basic
    @Column(name = "JobStatus")
    public Status getJobStatus() {
        return Status.valueOf(jobStatus);
    }


    public enum Status {
        QUEUED,
        PREP_SEED,
        GENERATE_SEQ,
        SUCCESS,
        FAILURE;

        private static Map<Integer, Status> map = new HashMap<Integer, Status>();

        static {
            for (Status statusEnum : Status.values()) {
                map.put(statusEnum.ordinal(), statusEnum);
            }
        }

        public static Status valueOf(int value) {
            return map.get(value);
        }
    }

    public void setJobStatus(Status jobStatus) {
        this.jobStatus = jobStatus.ordinal();
    }
}
