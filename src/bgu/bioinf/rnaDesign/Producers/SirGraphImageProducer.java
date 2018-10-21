package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Created by matan on 03/12/15.
 */
public class SirGraphImageProducer implements ImageProducer {
    private static final String MFOLD_SIG_GRAPH = WebappContextListener.ALGORITHM_LOCATION + "mfold/bin/sir_graph";
    private String alignedStructure;
    private String sequence;
    private String topic;
    private int runNo;

    public SirGraphImageProducer(String sequence, String alignedStructure, String topic, int runNo) {
        this.alignedStructure = alignedStructure;
        this.sequence = sequence;
        this.topic = topic;
        this.runNo = runNo;
    }

    @Override
    public String getImage() {
        String imageName = null;
        File tempCTFile = null;
        String tempFileName = null;
        Process p;
        try {
            tempCTFile = generateCTfile();
            tempFileName = WebappContextListener.TEMP_LOCATION
                    + tempCTFile.getName().substring(0, tempCTFile.getName().lastIndexOf('.'));
            String fullRunningLine = MFOLD_SIG_GRAPH + " -p -o "
                    + tempFileName + " " + tempCTFile.getPath();
            p = Runtime.getRuntime().exec(fullRunningLine);
            Utils.log("INFO", false, "SirGraphImageProducer.getImage - executing: " + fullRunningLine);
            int exitVal = p.waitFor();
            if (exitVal < 0)
                throw new Exception();
            p = Runtime.getRuntime().exec("convert " + tempFileName + ".ps " + tempFileName + ".jpg");
            exitVal = p.waitFor();
            if (exitVal < 0)
                throw new Exception();
            imageName = tempFileName + ".jpg";
        } catch (Exception e) {
            imageName = null;
            Utils.log("ERROR", e, "SirGraphImageProducer.getImage - failed generating image");
        } finally {
            if (tempCTFile != null) {
                try {
                    tempCTFile.delete();
                } catch (Exception ignore) {
                }
            }
            if (tempFileName != null) {
                File file = new File(tempFileName + ".ps");
                try {
                    file.delete();
                } catch (Exception ignore) {
                }
            }
        }
        return imageName;
    }

    public static void writeCTfile (BufferedWriter bufferedWriter, String sequence, String alignedStructure) throws IOException{
        Map<Integer, Integer> complementaryMap = new HashMap<Integer, Integer>();
        Stack<Integer> closing = new Stack<Integer>();
        for (int i = 0; i < alignedStructure.length(); ++i) {
            if (alignedStructure.charAt(i) == '(') {
                closing.push(i);
            } else if (alignedStructure.charAt(i) == ')') {
                Integer close = closing.pop();
                complementaryMap.put(close, i);
                complementaryMap.put(i, close);
            }
        }

        for (int i = 0; i < alignedStructure.length(); ++i) {
            String line = (i + 1) + "\t" + sequence.charAt(i) + "\t" + i + "\t";
            Integer next = i + 2;
            if (next > alignedStructure.length()) {
                next = 0;
            }
            line += next + "\t";
            Integer complementary = complementaryMap.get(i);
            if (complementary == null) {
                complementary = 0;
            } else {
                complementary += 1;
            }
            line += complementary + "\t" + (i);
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
    }

    public File generateCTfile() throws IOException {
        File ctFile = null;
        BufferedWriter bufferedWriter = null;
        try {
            ctFile = File.createTempFile("CT_", ".ct");

            bufferedWriter = new BufferedWriter(new FileWriter(ctFile));
            bufferedWriter.write(sequence.length() + "\t" + topic + " - Run No: " + runNo);
            bufferedWriter.newLine();

            SirGraphImageProducer.writeCTfile(bufferedWriter,sequence,alignedStructure);
        } finally {
            bufferedWriter.close();
        }
        return ctFile;
    }

}
