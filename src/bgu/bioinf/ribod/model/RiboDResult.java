package bgu.bioinf.ribod.model;

import bgu.bioinf.ribod.db.RiboDEntity;

public class RiboDResult {
    public String error;
    public RiboDEntity riboDEntity;

    public RiboDResult(RiboDEntity riboDEntity, String error) {
        this.error = error;
        this.riboDEntity = riboDEntity;
    }
}
