package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by matan on 21/12/15.
 */
public class VarnaRNAImageProducer implements ImageProducer {
    private static final String VARNA_RNA = WebappContextListener.ALGORITHM_LOCATION + "VARNArna/VARNAv3-93.jar";
    private String alignedStructure;
    private String sequence;
    private String topic;
    private List<Integer> markedIndex;
    private Map<Integer, Integer> markedBPs;

    public VarnaRNAImageProducer(String sequence, String alignedStructure, String topic) {
        this.alignedStructure = alignedStructure;
        this.sequence = sequence;
        this.topic = topic;
        if (topic == null) {
            this.topic = "";
        }
        this.markedIndex = null;
        this.markedBPs = null;
    }

    public VarnaRNAImageProducer(String sequence, String alignedStructure, String topic, List<Integer> markedIndex) {
        this(sequence, alignedStructure, topic);
        this.markedIndex = markedIndex;
    }

    public VarnaRNAImageProducer(String sequence, String alignedStructure, String topic, List<Integer> markedIndex,
                                 Map<Integer, Integer> markedBPs) {
        this(sequence, alignedStructure, topic);
        this.markedIndex = markedIndex;
        this.markedBPs = markedBPs;
    }

    public VarnaRNAImageProducer(String sequence, String alignedStructure, String topic,
                                 Map<Integer, Integer> markedBPs) {
        this(sequence, alignedStructure, topic);
        this.markedBPs = markedBPs;
    }

    public File generateCTfile() throws IOException {
        File ctFile = null;
        BufferedWriter bufferedWriter = null;
        try {
            ctFile = File.createTempFile("CT_", ".ct");

            bufferedWriter = new BufferedWriter(new FileWriter(ctFile));
            bufferedWriter.write(sequence.length() + "\t" + topic);
            bufferedWriter.newLine();

            SirGraphImageProducer.writeCTfile(bufferedWriter, sequence, alignedStructure);
        } finally {
            bufferedWriter.close();
        }
        return ctFile;
    }

    private String[] generateCall(File ctTempFile) {
        List<String> call = new ArrayList<String>();
        call.add("java");
        call.add("-cp");
        call.add(VARNA_RNA);
        call.add("fr.orsay.lri.varna.applications.VARNAcmd");
        call.add("-i");
        call.add(ctTempFile.getAbsolutePath());
        call.add("-o");
        call.add(ctTempFile.getAbsolutePath() + ".jpg");
        call.add("-resolution");
        call.add(5.0 + "");
        if (markedIndex != null && !markedIndex.isEmpty()) {
            /* //HighlightRegion
            call.add("-highlightRegion");
            call.add(getGroupIndexString());
            */
            // BaseStyle
            call.add("-basesStyle1");
            call.add("fill=#00FF00,outline=#FF0000");
            call.add("-applyBasesStyle1on");
            call.add(getListIndexString());
        }
        if (markedBPs != null && !markedBPs.isEmpty()) {
            call.add("-auxBPs");
            call.add(getListBPsString());
        }
        if (!"".equals(topic)) {
            call.add("-title");
            call.add("\"" + topic + "\"");
            call.add("-titleSize");
            call.add("16");
            call.add("-titleColor");
            call.add("#000000");
        }
        return call.toArray(new String[0]);
    }

    private String getListBPsString() {
        String res = "";
        for (Map.Entry<Integer, Integer> entry : this.markedBPs.entrySet()) {
            res += "(" + entry.getKey() + "," + entry.getValue() + "):thickness=3,color=#B22222;";
        }
        res = res.substring(0, res.length() - 1);
        return res;
    }

    private String getListIndexString() {
        String result = "";
        int lastNum = markedIndex.get(0);
        int streakLength = 0;
        result += lastNum;
        for (int i = 1; i < markedIndex.size(); ++i) {
            int current = markedIndex.get(i);
            int diff = Math.abs(current - lastNum);
            if (diff == 1) { // keep the streak
                streakLength++;
            } //diff > 1
            else {
                if (streakLength > 0)
                    result += "-" + lastNum;
                streakLength = 0;
                result += "," + current;
            }
            lastNum = current;
        }
        if (streakLength > 0) {
            result += "-" + lastNum;
        }
        if (result.endsWith(","))
            result = result.substring(0, result.length() - 1);
        return result;
    }

    /**
     * Works with highlightRegion option
     * @return
     */
    private String getGroupIndexString() {
        String result = "";
        int lastNum = markedIndex.get(0);
        int streakLength = 0;


        result += lastNum;
        for (int i = 1; i < markedIndex.size(); ++i) {
            int current = markedIndex.get(i);
            int diff = Math.abs(current - lastNum);
            if (diff == 1) { // keep the streak
                streakLength++;
            } //diff > 1
            else {
                if (streakLength > 0)
                    result += "-" + lastNum;
                streakLength = 0;
                result += ":fill=#bcffdd;" + current;
            }
            lastNum = current;
        }

        if (streakLength > 0) {
            result += "-" + lastNum;
        }
        result += ":fill=#bcffdd";

        if (result.endsWith(";"))
            result = result.substring(0, result.length() - 1);
        return result;
    }

    @Override
    public String getImage() {
        String imageName = null;
        Process p;
        File tempViennaFile = null;
        String[] call = {};
        try {
            tempViennaFile = generateCTfile();
            call = generateCall(tempViennaFile);
            ProcessBuilder pb = new ProcessBuilder(call);
            p = pb.start();
            Utils.log("INFO", false, "VarnaRNAImageProducer.getImage - executing: " + pb.command());
            int exitVal = p.waitFor();
            if (exitVal < 0)
                throw new Exception();
            imageName = tempViennaFile.getAbsolutePath() + ".jpg";
        } catch (Exception e) {
            imageName = null;
            Utils.log("ERROR", e, "VarnaRNAImageProducer.getImage - failed generating image call=" + call);
        } finally {
            if (tempViennaFile != null) {
                try {
                    tempViennaFile.delete();
                } catch (Exception ignore) {
                }
            }
        }
        return imageName;
    }
}
