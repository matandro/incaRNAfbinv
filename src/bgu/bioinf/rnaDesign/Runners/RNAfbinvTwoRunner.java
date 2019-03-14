package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.ShapiroGenerator;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.JobResultEntity;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matan on 25/11/15.
 * Changed to call new version of RNAfbinv 2.0
 */
public class RNAfbinvTwoRunner implements DesignRunner {
    private static final int EXIT_MOTIF_FAILURE = 246;
    private static final String RNAFBINV_LOCATION = WebappContextListener.ALGORITHM_LOCATION + "RNAsfbinv/";
    private static final String RNAFBINV_SCRIPT_NAME = "RNAfbinvCL.py";
    private JobInfoModel jobInformation;
    private File inputRNAfbinv;

    public RNAfbinvTwoRunner(JobInfoModel jobInformation) {
        this.jobInformation = jobInformation;
        inputRNAfbinv = null;
    }

    public boolean analyzeResult(String outputText, JobResultEntity jobResultEntity) {
        boolean result = false;
        try {
            String[] lines = outputText.split("\n");
            int resStart = 0;
            for (; resStart < lines.length; ++resStart) {
                String line = lines[resStart];
                if (line.trim().startsWith("Result:")) {
                    resStart += 1;
                    break;
                }
            }
            String sequence = lines[resStart].trim();
            jobResultEntity.setResultSequence(sequence);
            Utils.log("INFO", false,
                    " RNAfbinvRunner.analyzeResult: Generated result - Output sequence [" + sequence + "]");
            float mutationalRobustness = Float.valueOf(lines[resStart + 3].split(":", 2)[1].trim());
            jobResultEntity.setMutationalRobustness(mutationalRobustness);
            Utils.log("INFO", false,
                    " RNAfbinvRunner.analyzeResult: Generated result - mutational robusntess [" + mutationalRobustness + "]");
            String tempScore = lines[resStart + 7].split(":", 2)[0].trim().split(" ",3)[2].trim();
            float designScore = Float.valueOf(tempScore.substring(1, tempScore.length() - 1));
            jobResultEntity.setDesignScore(designScore);
            Utils.log("INFO", false,
                    " RNAfbinvRunner.analyzeResult: Generated result - Design score [" + designScore + "]");
            result = true;
        } catch (RuntimeException e) {
            result = false;
        }
        return result;
    }

    public boolean generateSingleResult(String seed, JobResultEntity jobResultEntity) {
        boolean success = false;
        if (this.inputRNAfbinv == null)
            generateInputFile();
        if (this.inputRNAfbinv != null) {
            try {
                String motif = jobInformation.getMotifConstraint();
                ArrayList<String> callList = new ArrayList<>();
                callList.add(WebappContextListener.PYTHON_LOCATION);
                callList.add(RNAFBINV_LOCATION + RNAFBINV_SCRIPT_NAME);
                callList.add("-f");
                callList.add(inputRNAfbinv.getAbsolutePath());
                callList.add("--seed");
                callList.add(WebappContextListener.rnGesus.nextLong() + "");
                callList.add("-i");
                callList.add(jobInformation.getNoIterations() + "");
                callList.add("-s");
                callList.add(seed);
                if (jobInformation.getVaryingSize() != null && jobInformation.getVaryingSize() > 0) {
                    callList.add("--length");
                    callList.add(jobInformation.getVaryingSize() + "");
                }

                if (motif != null && !"".equals(motif)) {
                    String motifStr = "";
                    ShapiroGenerator sg = new ShapiroGenerator(jobInformation.getQueryStructure());
                    String shapiroStr = sg.getShapiro();
                    String[] motifs = motif.split(",");
                    for (String singleMotif: motifs) {
                        String[] motifInfo = singleMotif.split("_");
                        int index = RNAfbinvTwoRunner.findMotifIndex(shapiroStr, Integer.valueOf(motifInfo[0]));
                        motifStr += String.valueOf(index) + motifInfo[1] + ",";
                    }
                    if (!"".equals(motifStr))
                        motifStr = motifStr.substring(0, motifStr.length() - 1);
                    callList.add("-m");
                    callList.add(motifStr);
                }
                String[] call = new String[callList.size()];
                callList.toArray(call);
                Utils.log("INFO", false,
                        " RNAfbinvRunner.generateSingleResult: Running RNAfbinv2.0, call: " +
                                callToPrint(callList));
                ProcessBuilder pb = new ProcessBuilder(call);
                // IMPORTANT redirect! without it, software will hang because of full stderr pipe
                pb.redirectError(new File("/dev/null"));
                pb.directory(new File(RNAFBINV_LOCATION));
                Process p = pb.start();
                String data;
                String all = "";
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((data = br.readLine()) != null) {
                    all += data + "\n";
                }
                int exitValue = p.waitFor();
                boolean gotResult = analyzeResult(all, jobResultEntity);
                if (exitValue != 0 || !gotResult) {
                    throw new Exception("Issue running RNAfbinv exitVal=" + exitValue + "\nRun line:" + pb.command() + "\nOutput:\n" + all);
                }
                success = true;
            } catch (Exception e) {
                Utils.log("ERROR", e,
                        "RNAfbinvRunner.generateSingleResult: Failed to generate sequence for seed [" + seed + "]");
            }
        }
        return success;
    }


    private static String callToPrint(List<String> call) {
        String res = "";
        for (String item : call) {
            res += item +", ";
        }
        if (!"".equals(res))
            res = res.substring(0, res.lastIndexOf(","));
        return res;
    }

    private static int findMotifIndex(String shapiro, int motifIndex) {
        int count = 0;
        for (int i = shapiro.length() -1 ; i >= motifIndex; --i) {
            if (shapiro.charAt(i) == 'R' || shapiro.charAt(i) == 'E' || shapiro.charAt(i) == 'S' ||
                    shapiro.charAt(i) == 'H' || shapiro.charAt(i) == 'M' || shapiro.charAt(i) == 'I' ||
                    shapiro.charAt(i) == 'B')
                count++;
        }
        return count - 1;
    }

    public boolean generateInputFile() {
        boolean result = false;
        PrintWriter pw = null;
        File input = null;
        try {
            input = File.createTempFile("rnafbinv_", ".txt");
            pw = new PrintWriter(input);
            pw.println("Target_structure=" + jobInformation.getQueryStructure());
            pw.println("Target_sequence=" + jobInformation.getQuerySequence());
            pw.println("Target_energy=" + jobInformation.getTargetEnergy());
            pw.println("Target_mr=" + jobInformation.getTargetMR());
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

    public void cleanRunner() {
        if (inputRNAfbinv != null)
            inputRNAfbinv.delete();
    }
}
