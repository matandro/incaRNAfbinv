package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by matan on 29/11/15.
 */
public class RNAfoldRunner {
    private static final String RNAFOLD_LOCATION = "RNAfold"; // installed version
    private static final String RNASHAPIRO_LOCATION = WebappContextListener.ALGORITHM_LOCATION + "RNAinv/RNAfbinv/RNAshapiroSeq";

    private String sequence;
    private String structure;
    private float energy;
    private String shapiroCoarseStructure;
    private Integer shapiroDistance;
    private Integer bpDistance;

    public String getShapiroStructure() {
        return shapiroStructure;
    }

    // Only for motif
    private String shapiroStructure;

    public RNAfoldRunner(String sequence) {
        this.sequence = sequence;
    }

    public boolean generateInfo(String queryStructure) {
        boolean result = generateRNAfoldInfo();
        if (result) {
            result = generateShapiro(queryStructure);
        }
        return result;
    }

    public boolean generateShapiroLocal(String structure) {
        this.structure = structure;
        return generateShapiro(structure);
    }

    private boolean generateShapiro(String queryStructure) {
        boolean result = false;
        try {
            ProcessBuilder pb = new ProcessBuilder(new String[]{RNASHAPIRO_LOCATION, queryStructure});
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            PrintWriter pw = new PrintWriter(p.getOutputStream());
            pw.println(structure);
            pw.flush();
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("shapiro clean")) {
                    shapiroCoarseStructure = line.split(" = ")[1].trim();
                } else if (line.contains("shapiro distance")) {
                    shapiroDistance = Integer.valueOf(line.split(" = ")[1].trim());
                } else if (line.contains("bp distance")) {
                    bpDistance = Integer.valueOf(line.split(" = ")[1].trim());
                } else if (line.startsWith("shapiro")) {
                    shapiroStructure = line.split(" = ")[1].trim();
                }
            }
            p.waitFor();
            result = true;
        } catch (Exception e) {
            Utils.log("ERROR", e, " RNAfoldRunner.generateShapiro: Failed to generate shapiro for structure [" + structure + "]");
            e.printStackTrace();
        }

        return result;
    }

    private boolean generateRNAfoldInfo() {
        boolean result = false;
        try {
            ProcessBuilder pb = new ProcessBuilder(new String[]{RNAFOLD_LOCATION});
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            PrintWriter pw = new PrintWriter(p.getOutputStream());
            pw.println(sequence);
            pw.println("@");
            pw.flush();
            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                switch (lineNo) {
                    case 0: // ignore, sequence again
                        break;
                    case 1: // Structure + energy
                        //String[] test = line.split(" ");
                        structure = line.split(" ")[0];
                        String energyStr = line.substring(line.lastIndexOf('(')+1, line.lastIndexOf(')')).trim();
                        energy = Float.valueOf(energyStr);
                        break;
                    default:
                        break;
                }
                ++lineNo;
            }
            p.waitFor();
            result = true;
        } catch (Exception e) {
            Utils.log("ERROR", e, " RNAfoldRunner.generateShapiro: Failed to run RNAfold for sequence [" + sequence + "]");
        }
        return result;
    }

    private File generateInput(String content) {
        File rnaFoldInput = null;
        try {
            rnaFoldInput = File.createTempFile("RNAfold", "txt");
            PrintWriter pw = new PrintWriter(rnaFoldInput);
            pw.println(content + "\n");
            pw.flush();
        } catch (Exception e) {
            if (rnaFoldInput != null)
                rnaFoldInput.delete();
            rnaFoldInput = null;
        }
        return rnaFoldInput;
    }

    public String getStructure() {
        return structure;
    }

    public Float getEnergy() {
        return energy;
    }

    public String getShapiroCoarseStructure() {
        return shapiroCoarseStructure;
    }

    public Integer getShapiroDistance() {
        return shapiroDistance;
    }

    public Integer getBpDistance() {
        return bpDistance;
    }
}
