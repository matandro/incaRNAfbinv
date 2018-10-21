package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.model.JobInfoModel;

import java.io.*;
import java.util.List;

/**
 * Created by matan on 25/11/15.
 */
public class IncaRNAtionRunner {
    private static String INCARNATION_LOCATION = WebappContextListener.ALGORITHM_LOCATION + "incaRNAtion/IncaRNAtion-master/src/IncaRNAtion.py";

    private JobInfoModel jobInformation;

    public IncaRNAtionRunner(JobInfoModel jobInformation) {
        this.jobInformation = jobInformation;
    }

    private File generateOutputFile() throws IOException {
        File temp = File.createTempFile("incaRNAtionInput_", ".txt");
        try {
            PrintWriter pw = new PrintWriter(temp);
            pw.println(jobInformation.getQueryStructure());
            pw.flush();
        } catch (Exception e) {
            temp.delete();
        }
        return temp;
    }

    private static final int MAX_FAILED_ATTEMPTS = 3;

    public void populateList(List<String> seedList) {
        File incaRNAtionInput = null;
        try {
            incaRNAtionInput = generateOutputFile();
            if (incaRNAtionInput != null) {
                int failedAttempt = 0;
                while (seedList.size() < jobInformation.getOutputAmount()) {
                    ProcessBuilder pb = new ProcessBuilder(new String[]{"python", INCARNATION_LOCATION, "-a", "1",
                            "-d", incaRNAtionInput.getAbsolutePath(), "-c", jobInformation.getQuerySequence(),
                            "-gc_max_err", jobInformation.getGcErrorDecimal().toString(), "-no_profile",
                            "-s_gc", jobInformation.getGcContentDecimal().toString()
                            , (jobInformation.getOutputAmount() - seedList.size()) + ""});
                    Process p = pb.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    int linesAdded = 0;
                    while (seedList.size() < jobInformation.getOutputAmount() && (line = br.readLine()) != null) {
                        seedList.add(line.trim());
                        linesAdded++;
                    }

                    p.destroy();
                    /*if (exitCode != 0) {
                        throw new Exception("Failed to generate new seeds. Exit code: " + exitCode + "\nRun info: " + pb.command());
                    } else*/ if (linesAdded == 0) {
                        if (++failedAttempt == MAX_FAILED_ATTEMPTS) {
                            throw new Exception("Failed to generate enough seeds.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.log("SEVERE",e,"IncaRNAtionRunner.populateList - Failed to generate lines list");
            seedList.clear();
        } finally {
            if (incaRNAtionInput != null) {
                try {
                    incaRNAtionInput.delete();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
