package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.model.JobInfoModel;

public class DesignRunnerFactory {
    public static DesignRunner get(JobInfoModel jobInfoModel){
        DesignRunner res = null;
        if (jobInfoModel.getVersion() == 1)
            res = new RNAfbinvRunner(jobInfoModel);
        else
            res = new RNAfbinvTwoRunner(jobInfoModel);
        return res;
    }
}
