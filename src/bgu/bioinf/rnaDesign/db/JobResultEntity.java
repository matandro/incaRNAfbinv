package bgu.bioinf.rnaDesign.db;

import javax.persistence.*;

/**
 * Created by matan on 01/12/15.
 */
@Entity
@Table(name = "JobResult", schema = "RNADesign", catalog = "")
@IdClass(JobResultEntityPK.class)
@NamedNativeQueries({
        @NamedNativeQuery(name = "JobResult.GetResultsByJobId",
                query = "SELECT JobResult.* " +
                        "FROM JobResult " +
                        "WHERE JobId = :jobId",
                resultClass = JobResultEntity.class)})
public class JobResultEntity {
    private String jobId;
    private int resultNo;
    private Float energyScore;
    private String resultSequence;
    private String seedSequence;
    private String shapiroStructure;
    private String resultStructure;
    private Integer structureDistance;
    private Integer shapiroDistance;
    private Float mutationalRobustness;
    private String shapiroCoarseStructure;
    private Float gcContent;
    private Float designScore;

    @Id
    @Column(name = "JobId")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Id
    @Column(name = "ResultNo")
    public int getResultNo() {
        return resultNo;
    }

    public void setResultNo(int resultNo) {
        this.resultNo = resultNo;
    }

    @Basic
    @Column(name = "EnergyScore")
    public Float getEnergyScore() {
        return energyScore;
    }

    public void setEnergyScore(Float energyScore) {
        this.energyScore = energyScore;
    }

    @Basic
    @Column(name = "ResultSequence")
    public String getResultSequence() {
        return resultSequence;
    }

    public void setResultSequence(String resultSequence) {
        this.resultSequence = resultSequence;
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
    @Column(name = "ShapiroStructure")
    public String getShapiroStructure() {
        return shapiroStructure;
    }

    public void setShapiroStructure(String shapiroStructure) {
        this.shapiroStructure = shapiroStructure;
    }

    @Basic
    @Column(name = "ResultStructure")
    public String getResultStructure() {
        return resultStructure;
    }

    public void setResultStructure(String resultStructure) {
        this.resultStructure = resultStructure;
    }

    @Basic
    @Column(name = "StructureDistance")
    public Integer getStructureDistance() {
        return structureDistance;
    }

    public void setStructureDistance(Integer structureDistance) {
        this.structureDistance = structureDistance;
    }

    @Basic
    @Column(name = "DesignScore")
    public Float getDesignScore() {
        return designScore;
    }

    public void setDesignScore(Float designScore) {
        this.designScore = designScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobResultEntity that = (JobResultEntity) o;

        if (resultNo != that.resultNo) return false;
        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;
        if (energyScore != null ? !energyScore.equals(that.energyScore) : that.energyScore != null) return false;
        if (resultSequence != null ? !resultSequence.equals(that.resultSequence) : that.resultSequence != null)
            return false;
        if (seedSequence != null ? !seedSequence.equals(that.seedSequence) : that.seedSequence != null) return false;
        if (shapiroStructure != null ? !shapiroStructure.equals(that.shapiroStructure) : that.shapiroStructure != null)
            return false;
        if (resultStructure != null ? !resultStructure.equals(that.resultStructure) : that.resultStructure != null)
            return false;
        if (structureDistance != null ? !structureDistance.equals(that.structureDistance) : that.structureDistance != null)
            return false;
        if (shapiroDistance != null ? !shapiroDistance.equals(that.shapiroDistance) : that.shapiroDistance != null)
            return false;
        if (gcContent != null ? !gcContent.equals(that.gcContent): that.gcContent != null)
            return false;
        if (designScore != null ? !designScore.equals(that.designScore) : that.designScore != null) return false;
        return mutationalRobustness != null ? mutationalRobustness.equals(that.mutationalRobustness) : that.mutationalRobustness == null;

    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + resultNo;
        result = 31 * result + (energyScore != null ? energyScore.hashCode() : 0);
        result = 31 * result + (resultSequence != null ? resultSequence.hashCode() : 0);
        result = 31 * result + (seedSequence != null ? seedSequence.hashCode() : 0);
        result = 31 * result + (shapiroStructure != null ? shapiroStructure.hashCode() : 0);
        result = 31 * result + (resultStructure != null ? resultStructure.hashCode() : 0);
        result = 31 * result + (structureDistance != null ? structureDistance.hashCode() : 0);
        result = 31 * result + (shapiroDistance != null ? shapiroDistance.hashCode() : 0);
        result = 31 * result + (mutationalRobustness != null ? mutationalRobustness.hashCode() : 0);
        result = 31 * result + (gcContent != null ? gcContent.hashCode() : 0);
        result = 31 * result + (designScore != null ? designScore.hashCode() : 0);
        return result;
    }

    @Basic
    @Column(name = "ShapiroDistance")

    public Integer getShapiroDistance() {
        return shapiroDistance;
    }

    public void setShapiroDistance(Integer shapiroDistance) {
        this.shapiroDistance = shapiroDistance;
    }

    @Basic
    @Column(name = "MutationalRobustness")
    public Float getMutationalRobustness() {
        return mutationalRobustness;
    }

    public void setMutationalRobustness(Float mutationalRobustness) {
        this.mutationalRobustness = mutationalRobustness;
    }

    @Basic
    @Column(name = "ShapiroCoarseStructure")
    public String getShapiroCoarseStructure() {
        return shapiroCoarseStructure;
    }

    public void setShapiroCoarseStructure(String shapiroCoarseStructure) {
        this.shapiroCoarseStructure = shapiroCoarseStructure;
    }

    @Transient
    public String printResult() {
        String result = "[";
        result += "jobId: " + jobId + ", ";
        result += "resultNo: " + resultNo + ", ";
        result += "energyScore: " + (energyScore != null ? energyScore : "") + ", ";
        result += "designScore: " + (designScore != null ? designScore : "") + ", ";
        result += "resultSequence: " + (resultSequence != null ? resultSequence : "");
        result += "seedSequence: " + (seedSequence != null ? seedSequence : "");
        result += "shapiroStructure: " + (shapiroStructure != null ? shapiroStructure : "");
        result += "resultStructure: " + (resultStructure != null ? resultStructure : "");
        result += "structureDistance: " + (structureDistance != null ? structureDistance : "");
        result += "shapiroDistance: " + (shapiroDistance != null ? shapiroDistance : "");
        result += "mutationalRobustness: " + (mutationalRobustness != null ? mutationalRobustness : "");
                result += "]";
        return result;
    }

    @Basic
    @Column(name = "GcContent")
    public Float getGcContent() {
        return gcContent;
    }

    public void setGcContent(Float gcContent) {
        this.gcContent = gcContent;
    }
}
