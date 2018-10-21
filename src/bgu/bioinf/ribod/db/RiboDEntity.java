package bgu.bioinf.ribod.db;

import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.DBConnector;
import bgu.bioinf.ribod.model.RiboDResult;

import javax.persistence.*;

@Entity
@Table(name = "RiboD", schema = "RNADesign", catalog = "")
public class RiboDEntity {
    private String seqId;
    private String sequence;
    private String energyStructure;
    private String cmStructure;

    @Id
    @Column(name = "SeqId")
    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    @Basic
    @Column(name = "Sequence")
    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Basic
    @Column(name = "EnergyStructure")
    public String getEnergyStructure() {
        return energyStructure;
    }

    public void setEnergyStructure(String energyStructure) {
        this.energyStructure = energyStructure;
    }

    @Basic
    @Column(name = "CmStructure")
    public String getCmStructure() {
        return cmStructure;
    }

    public void setCmStructure(String cmStructure) {
        this.cmStructure = cmStructure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RiboDEntity that = (RiboDEntity) o;

        if (seqId != null ? !seqId.equals(that.seqId) : that.seqId != null) return false;
        if (sequence != null ? !sequence.equals(that.sequence) : that.sequence != null) return false;
        if (energyStructure != null ? !energyStructure.equals(that.energyStructure) : that.energyStructure != null)
            return false;
        if (cmStructure != null ? !cmStructure.equals(that.cmStructure) : that.cmStructure != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = seqId != null ? seqId.hashCode() : 0;
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        result = 31 * result + (energyStructure != null ? energyStructure.hashCode() : 0);
        result = 31 * result + (cmStructure != null ? cmStructure.hashCode() : 0);
        return result;
    }


    public static RiboDResult retrieveDbObject(String seqId) {
        String error = null;
        EntityManager em = null;
        RiboDEntity riboDEntity = null;
        if (seqId == null || seqId.length() > 50) {
            error = "muster enter legal sequence id.";
        } else {
            try {
                em = DBConnector.getEntityManager();
                riboDEntity = em.find(RiboDEntity.class, seqId);
                if (riboDEntity == null) {
                    error = "Could not sequence ID " + seqId;
                }
            } catch (Exception e) {
                error = "Failed to connect to the database, please try again later.";
                Utils.log("SEVERE", e, "ImageController.retrieveDbObject - " + error);
            } finally {
                if (em != null)
                    em.close();
            }
        }
        RiboDResult ribodResult = new RiboDResult(riboDEntity, error);
        return ribodResult;
    }
}
