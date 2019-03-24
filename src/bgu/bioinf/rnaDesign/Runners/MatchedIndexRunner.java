package bgu.bioinf.rnaDesign.Runners;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;
import bgu.bioinf.rnaDesign.Producers.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchedIndexRunner {
    private static final String GENERATOR_LOCATION = WebappContextListener.ALGORITHM_LOCATION +
            "RNAsfbinv/gather_index.py";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private String sourceSequence;
    private String sourceStructure;
    private String targetSequence;
    private String targetStructure;
    private List<Integer> matchedIndexes;
    private List<Integer> unmatchedIndexes;

    public MatchedIndexRunner(String sourceSequence, String sourceStructure,
                              String targetSequence, String targetStructure) {
        this.sourceSequence = sourceSequence;
        this.sourceStructure = sourceStructure;
        this.targetSequence = targetSequence;
        this.targetStructure = targetStructure;
        this.matchedIndexes = null;
        this.unmatchedIndexes = null;
    }

    public boolean init() {
        boolean success = false;
        try {
            ProcessBuilder pb = new ProcessBuilder(new String[]{WebappContextListener.PYTHON_LOCATION,
                    GENERATOR_LOCATION, this.sourceSequence, this.sourceStructure, this.targetSequence,
                    this.targetStructure});
            Utils.log("INFO", false, "MatchedIndexRunner.init - executing: " + pb.command());
            pb.redirectError(new File("/dev/null"));
            Process p = pb.start();
            boolean returned = p.waitFor(60, TimeUnit.SECONDS);
            if (!returned || p.exitValue() != 0)
                throw new Exception("gather_index.py " + (returned ? "failed with -1" :
                        "hanged for more then 60 seconds"));
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            this.matchedIndexes = getIndexListFromText(line);
            line = br.readLine();
            this.unmatchedIndexes = getIndexListFromText(line);
            success = true;
        } catch (Exception e) {
            Utils.log("ERROR", e, "MatchedIndexRunner.init - failed generating matched indexes");
        }
        return success;
    }

    private List<Integer> getIndexListFromText(String line) {
        List<Integer> indexList = new ArrayList<>();
        Matcher m = NUMBER_PATTERN.matcher(line);
        while(m.find()) {
            indexList.add(Integer.valueOf(m.group()));
        }
        return indexList;
    }

    public List<Integer> getMatchedIndexes() {
        return matchedIndexes;
    }

    public List<Integer> getUnmatchedIndexes() {
        return unmatchedIndexes;
    }
}
