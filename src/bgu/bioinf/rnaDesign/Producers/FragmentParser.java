package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.model.MotifData;

import java.util.Collections;
import java.util.Map;

/**
 * Created by matan on 28/12/15.
 */
public class FragmentParser {
    private java.util.TreeMap<Integer, MotifData> motifs;
    private MotifData selectedMotif;

    public FragmentParser(ShapiroGenerator shapiroGenerator, int version) {
        this.selectedMotif = null;
        String shapiro = shapiroGenerator.getShapiro();
        this.motifs = new java.util.TreeMap<>(Collections.reverseOrder());
        String mainReg = "E[0-9]+|R|I[0-9]+|M[0-9]+|B[0-9]+|S[0-9]+|H[0-9]+";
        java.util.regex.Matcher matcher;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\(\\(H[0-9]+\\)S[0-9]+\\)");

        if (version == 1) {
            mainReg = "E[0-9]+|R|I[0-9]+|M[0-9]+|B[0-9]+";
            matcher = pattern.matcher(shapiro);
            while (matcher.find()) {
                MotifData motifData = new MotifData("Stem-loop", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
        }

        pattern = java.util.regex.Pattern.compile(mainReg);
        matcher = pattern.matcher(shapiro);
        while (matcher.find()) {
            MotifData motifData = null;
            if (matcher.group().matches("E[0-9]+")) {
                motifData = new MotifData("External", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
            if (matcher.group().matches("R")) {
                /*motifData = new MotifData("5'-3'", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(new Integer(matcher.start()), motifData);*/
            }
            if (matcher.group().matches("M[0-9]+")) {
                motifData = new MotifData("Multi-loop", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
            if (matcher.group().matches("I[0-9]+")) {
                motifData = new MotifData("Internal-loop", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
            if (matcher.group().matches("B[0-9]+")) {
                motifData = new MotifData("Buldge", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
            // picked up on Version 2 where multiple motifs are possible
            if (matcher.group().matches("S[0-9]+")) {
                motifData = new MotifData("Stem", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
            if (matcher.group().matches("H[0-9]+")) {
                motifData = new MotifData("Hairpin-loop", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
        }

        if (version == 1) {
            pattern = java.util.regex.Pattern.compile("S[0-9]+");
            matcher = pattern.matcher(shapiro);
            while (matcher.find()) {
                Map.Entry entry = motifs.higherEntry(matcher.start());
                if (entry != null) {
                    MotifData motif = (MotifData) entry.getValue();
                    if (motif.getEndIndex() > matcher.start()) {
                        continue;
                    }
                }
                MotifData motifData = new MotifData("Stem", matcher.group(), matcher.start(), matcher.end());
                motifData.setIndexList(shapiroGenerator.getIndexsByMotif(motifData));
                motifs.put(matcher.start(), motifData);
            }
        }
    }

    public java.util.TreeMap<Integer, MotifData> getMotifs() {
        return this.motifs;
    }

    public void printMotifs() {
        for (Map.Entry<Integer, MotifData> entry : this.motifs.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ". Value: " + entry.getValue().toFullString());
        }
    }

    public MotifData getSelectedMotif() {
        return selectedMotif;
    }
}
