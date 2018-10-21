package bgu.bioinf.rnaDesign.db;

import javax.persistence.*;

/**
 * Created by matan on 01/12/15.
 */
@Entity
@Table(name = "JobError", schema = "RNADesign", catalog = "")
public class JobErrorEntity {
    private String jobId;
    private String errorStr;

    @Id
    @Column(name = "JobId")
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Basic
    @Column(name = "ErrorStr")
    public String getErrorStr() {
        return errorStr;
    }

    public void setErrorStr(String errorStr) {
        this.errorStr = errorStr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JobErrorEntity that = (JobErrorEntity) o;

        if (errorStr != null ? !errorStr.equals(that.errorStr) : that.errorStr != null) return false;
        if (jobId != null ? !jobId.equals(that.jobId) : that.jobId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jobId != null ? jobId.hashCode() : 0;
        result = 31 * result + (errorStr != null ? errorStr.hashCode() : 0);
        return result;
    }
}
