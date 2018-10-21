package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.JobResultEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by matan on 25/11/15.
 */
public class RNAfbinvRunner {
    private static final int EXIT_MOTIF_FAILURE = 246;
    private String RNAFBINV_LOCATION = WebappContextListener.ALGORITHM_LOCATION + "RNAinv/RNAfbinv/RNAexinv";
    private JobInfoModel jobInformation;
    private File inputRNAfbinv;

    public RNAfbinvRunner(JobInfoModel jobInformation) {
        this.jobInformation = jobInformation;
        inputRNAfbinv = null;
    }

    public boolean generateSingleResult(String seed, JobResultEntity jobResultEntity) {
        boolean success = false;
        if (inputRNAfbinv != null) {
            try {
                String motif = jobInformation.getMotifConstraint();
                String[] call;
                if (motif == null || "".equals(motif)) {
                    call = new String[]{RNAFBINV_LOCATION
                            , "-f", inputRNAfbinv.getAbsolutePath()
                            , "-p", WebappContextListener.rnGesus.nextLong() + ""
                            , "-i", jobInformation.getNoIterations() + ""
                            , "-S", seed};
                } else {
                    String[] motifInfo = motif.split("_");
                    call = new String[]{RNAFBINV_LOCATION
                            , "-f", inputRNAfbinv.getAbsolutePath()
                            , "-p", WebappContextListener.rnGesus.nextLong() + ""
                            , "-i", jobInformation.getNoIterations() + ""
                            , "-S", seed
                            , "-m", motifInfo[1], motifInfo[0]};
                }
                Utils.log("INFO", false,
                        " RNAfbinvRunner.generateSingleResult: Running RNAexinv, call: " + call);
                ProcessBuilder pb = new ProcessBuilder(call);
                Process p = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                boolean gotResult = false;
                while ((line = br.readLine()) != null) {
                    if (line.contains("Output sequence") && line.contains("=")) {
                        String info = line.substring(line.indexOf(" = ") + 3).trim();
                        jobResultEntity.setResultSequence(info);
                        Utils.log("INFO", false,
                                " RNAfbinvRunner.generateSingleResult: Generated result - Output sequence [" + info + "]");
                        gotResult = true;
                    } else if (line.contains("mutational robustness") && line.contains("=")) {
                        String info = line.substring(line.indexOf(" = ") + 3).trim();
                        jobResultEntity.setMutationalRobustness(Float.valueOf(info));
                        Utils.log("INFO", false,
                                " RNAfbinvRunner.generateSingleResult: Generated result - mutational robustness [" + info + "]");
                    }
                }
                int exitValue = p.waitFor();
                if (exitValue == 246) {
                    Utils.log("WARNING", false, "RNAfbinvRunner.generateSingleResult failed to keep motif in result: "
                            + motif);
                } else if (exitValue != 0 || !gotResult) {
                    throw new Exception("Issue running RNAfbinv exitVal=" + exitValue + "\nRun line:" + pb.command());
                }
                success = true;
            } catch (Exception e) {
                Utils.log("ERROR", e,
                        "RNAfbinvRunner.generateSingleResult: Failed to generate sequence for seed [" + seed + "]");
            }
        }
        return success;
    }

    public boolean generateInputFile() {
        boolean result = false;
        PrintWriter pw = null;
        File input = null;
        try {
            input = File.createTempFile("rnafbinv_", ".txt");
            pw = new PrintWriter(input);
            pw.println(jobInformation.getQueryStructure());
            pw.println(jobInformation.getQuerySequence());
            pw.println(jobInformation.getTargetEnergy());
            pw.println(jobInformation.getTargetMR());
            pw.flush();
            result = true;
        } catch (Exception e) {
            if (input != null) {
                try {
                    input.delete();
                } catch (Exception ignore) {
                }
            }
        } finally {
            if (pw != null)
                pw.close();
        }
        inputRNAfbinv = input;
        return result;
    }

    public void cleanInputFile() {
        if (inputRNAfbinv != null)
            inputRNAfbinv.delete();
    }
}
