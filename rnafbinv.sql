# Drop user if exists
DROP USER 'rnaDesign'@'localhost';
# Drop schema if exists
DROP SCHEMA IF EXISTS RNADesign;
# create schema
CREATE SCHEMA RNADesign;
# Assumes user named rnaDesign
# give user permissions
GRANT USAGE ON *.* TO 'rnaDesign'@'localhost';
GRANT SELECT, UPDATE, DELETE, INSERT ON RNADesign.* TO 'rnaDesign'@'localhost';
# Start Creating tables
# Create job table
CREATE TABLE RNADesign.Job
(
  JobId           VARCHAR(8) NOT NULL,
  QueryName       VARCHAR(128),
  Email           VARCHAR(56),
  QuerySequence   VARCHAR(512) NOT NULL,
  QueryStructure  VARCHAR(512) NOT NULL,
  StartTime       TIMESTAMP    NOT NULL,
  EndTime         TIMESTAMP    NULL     DEFAULT NULL,
  OutputAmount    INT          NULL,
  SeedSequence    VARCHAR(512) NULL, # If null use random sequence
  GCContent       FLOAT        NULL, # Means incaRNAtion, if no value -> RNAfbinv alone
  GCError         FLOAT        NULL,
  TargetEnergy    FLOAT        NOT NULL,
  TargetMR        FLOAT        NOT NULL,
  NoIterations    INT          NOT NULL,
  MotifConstraint VARCHAR(128),
  JobStatus		    INT, # Notation for real time status check, Prepering seeds / Generating sequences
  Version         INT, # enable multi version web-server
  VaryingSize     INT, # for version 2.0, results size +- VaryingSize for original
  PRIMARY KEY (JobId),
  INDEX Job_QueryName_Index (QueryName)
);
# ALTER TABLE RNADesign.Job ADD COLUMN VaryingSize INT;

# Create error table
CREATE TABLE RNADesign.JobError
(
  JobId     VARCHAR(8) NOT NULL,
  ErrorStr  VARCHAR(1028) NOT NULL,
  PRIMARY KEY (JobId),
  FOREIGN KEY JobError_JobId_FK (JobId)
  REFERENCES RNADesign.Job (JobId)
);
# Create Result table
CREATE TABLE RNADesign.JobResult
(
  JobId                   VARCHAR(8)  NOT NULL,
  ResultNo                INT         NOT NULL,
  EnergyScore             FLOAT,
  MutationalRobustness    FLOAT,
  SeedSequence            VARCHAR(512),
  ResultSequence          VARCHAR(512),
  ResultStructure         VARCHAR(512),
  ShapiroCoarseStructure  VARCHAR(256),
  ShapiroStructure        VARCHAR(256),
  StructureDistance       INT,
  ShapiroDistance         INT,
  GcContent               FLOAT,
  DesignScore             FLOAT,
  PRIMARY KEY (JobId, ResultNo)
);
# Create RiboD support table (requires running of filler script)
CREATE TABLE RNADesign.RiboD
(
  SeqId           VARCHAR(50) NOT NULL,
  Sequence        VARCHAR(512),
  EnergyStructure VARCHAR(512),
  CmStructure     VARCHAR(512),
  PRIMARY KEY (SeqId)
);