package bgu.bioinf.rnaDesign.model;

import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.JobResultEntity;

/**
 * Created by matan on 02/12/15.
 */
public class SingleResultModel {
    private final Integer shapiroDistance;
    private final Float gcContent;

    public Integer getBpDistance() {
        return bpDistance;
    }

    private Integer bpDistance;

    public String getResultSequence() {
        return resultSequence;
    }

    public String getResultShapiro() {
        return resultShapiro;
    }

    public String getSeedSequence() {
        return seedSequence;
    }

    public Float getEnergyScore() {
        return energyScore;
    }

    public Integer getResultNo() {
        return resultNo;
    }

    public String getResultStructure() {
        return resultStructure;
    }

    private final String resultStructure;
    private final String resultSequence;
    private final String resultShapiro;

    public String getResultShapiroCoarse() {
        return resultShapiroCoarse;
    }

    private final String resultShapiroCoarse;
    private final String seedSequence;
    private final Float energyScore;
    private final Integer resultNo;
    private final Float mutationalRobustness;
    private final Float designScore;

    public SingleResultModel(JobResultEntity jobResultEntity) {
        this.resultShapiroCoarse = jobResultEntity.getShapiroCoarseStructure();
        this.resultStructure = jobResultEntity.getResultStructure();
        this.resultShapiro = jobResultEntity.getShapiroStructure();
        this.resultSequence = jobResultEntity.getResultSequence();
        this.seedSequence = jobResultEntity.getSeedSequence();
        this.energyScore = jobResultEntity.getEnergyScore();
        this.resultNo = jobResultEntity.getResultNo();
        this.bpDistance = jobResultEntity.getStructureDistance();
        this.shapiroDistance = jobResultEntity.getShapiroDistance();
        this.mutationalRobustness = jobResultEntity.getMutationalRobustness();
        this.gcContent = jobResultEntity.getGcContent();
        this.designScore = jobResultEntity.getDesignScore();
    }

    public Float getGcContent() {
        return gcContent;
    }

    public String getGcContentPrintable() {
        return Utils.getFormattedNumber(getGcContent());
    }

    public Integer getShapiroDistance() {
        return shapiroDistance;
    }

    public Float getMutationalRobustness() {
        return this.mutationalRobustness;
    }

    public Float getDesignScore() {
        return designScore;
    }
}
