package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.Listeners.WebappContextListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by matan on 17/11/15.
 */
public class Utils {
    public static final String FASTA_WILDCARD = "RYKMSWBDHVN";
    public static final String FASTA_XNA = "AGCTU" + FASTA_WILDCARD;
    public static final String FASTA_DNA = "AGCT";
    public static final String FASTA_RNA = "AGCU";

    public static void log(String type, Exception e, String message) {
        log(type, true, message);
        e.printStackTrace();
    }

    public static void log(String type, boolean isError, String message) {
        String fullMessage = type + " " + getFormmatedNow() + ":" + message;
        if (isError) {
            System.err.println(fullMessage);
        }
        else {
            System.out.println(fullMessage);
        }
    }

    /**
     * Recives a sequence in DNA / RNA form and turns it into RNA
     *
     * @param sequence The sequence to test
     * @return An upppercase string in RNA form if legal, null otherwise
     */
    public static String verifySequence(String sequence, boolean includeWildCard) {
        String result = sequence.toUpperCase();
        // If DNA or RNA - build uppercase RNA

        if (result.matches("^[" + FASTA_DNA + "]+$") ||
                result.matches("^[" + FASTA_RNA + "]+$") ||
                (includeWildCard && result.matches("^[" + FASTA_XNA + "]+$"))) {
            result = result.replaceAll("T", "U");
        } else {
            result = null;
        }
        return result;
    }

    public static String createRepeat(String pattern, int repeatNo) {
        String result = "";
        for (int i = 0; i < repeatNo; ++i) {
            result += pattern;
        }
        return result;
    }

    /**
     * Check if a structure is in legal (/./) form
     *
     * @param structure the structure to test
     * @return True if legal, false otherwise
     */
    public static boolean verifyStructure(String structure) {
        boolean result = true;
        int openCounter = 0;
        for (int i = 0; result && i < structure.length(); ++i) {
            switch (structure.charAt(i)) {
                case '.':
                    break;
                case '(':
                    openCounter++;
                    break;
                case ')':
                    if (--openCounter < 0)
                        result = false;
                    break;
                default:
                    result = false;
                    break;
            }
        }
        if (openCounter != 0)
            result = false;
        return result;
    }

    private static final DecimalFormat SINGLE_DIGIT_FORMAT = new DecimalFormat("###.0");

    public static String getFormattedNumber(Float number) {
        return SINGLE_DIGIT_FORMAT.format(number);
    }

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy ss:mm:HH");

    public static String getFormmatedNow() {
        return getFormmatedTime(new Date());
    }

    public static String getFormmatedTime(Date time) {
        return SIMPLE_DATE_FORMAT.format(time);
    }

    public static String generateSingleSeed(String constraints) {
        int length = constraints.length();
        String result = "";
        String upperCaseConstraints = constraints.toUpperCase();
        for (int i = 0; i < length; ++i) {
            String constraintValues = FASTA_MAP.get(upperCaseConstraints.charAt(i) + "");
            int letter = WebappContextListener.rnGesus.nextInt(constraintValues.length());
            result += constraintValues.charAt(letter);
        }
        return result;
    }

    /**
     * calculate the % of GC in sequence. only for 100% GC (G / C / S which is either G or C)
     *
     * @param sequence the sequence to test
     * @return the % of GC in sequence, only if the letter must be G or C
     */
    public static float getGCcontent(String sequence) {
        int result = 0;
        String upperCase = sequence.toUpperCase();
        for (int i = 0; i < sequence.length(); ++i) {
            char c = upperCase.charAt(i);
            if (c == 'C' || c == 'G' || c == 'S')
                result++;
        }
        return (float) result / sequence.length();
    }

    /**
     * calculate the % of non GC in sequence. only for 100% GC (A / U / T / Any thing that cannot be G or C)
     *
     * @param sequence the sequence to test
     * @return the % of non GC in sequence, only if the letter must not be G and C
     */
    public static float getNonGCcontent(String sequence) {
        int result = 0;
        String upperCase = sequence.toUpperCase();
        for (int i = 0; i < sequence.length(); ++i) {
            char c = upperCase.charAt(i);
            if (c == 'A' || c == 'U' || c == 'T' || c == 'W')
                result++;
        }
        return (float) result / sequence.length();
    }


    private static final Map<String, String> FASTA_MAP = new HashMap<String, String>();

    /**
     * Used to initiate FASTA_MAP, Called when tomcat deploys the site
     */
    public static void initiateFastaMap() {
        FASTA_MAP.put("A", "A");
        FASTA_MAP.put("G", "G");
        FASTA_MAP.put("C", "C");
        FASTA_MAP.put("U", "U");
        FASTA_MAP.put("N", "ACGU");
        FASTA_MAP.put("R", "AG");
        FASTA_MAP.put("Y", "CU");
        FASTA_MAP.put("K", "GU");
        FASTA_MAP.put("M", "AC");
        FASTA_MAP.put("S", "CG");
        FASTA_MAP.put("W", "AU");
        FASTA_MAP.put("B", "CGU");
        FASTA_MAP.put("D", "AGU");
        FASTA_MAP.put("H", "ACU");
        FASTA_MAP.put("V", "ACG");
        FASTA_MAP.put("T", "U");
    }

    /**
     * Checks if an RNA sequence agree with a constraint fasta sequence
     *
     * @param rnaSequence
     * @param fastaSequence
     * @return True if the agree, false otehrwise
     */
    public static boolean isSequenceAgreement(String fastaSequence, String rnaSequence) {
        boolean result = true;

        for (int i = 0; i < fastaSequence.length(); ++i) {
            if (!FASTA_MAP.get("" + fastaSequence.charAt(i)).contains("" + rnaSequence.charAt(i)) &&
                !FASTA_MAP.containsKey("" + rnaSequence.charAt(i))) {
                result = false;
                break;
            }
        }

        return result;
    }

    private static final String LEGAL_ID_LETTERS = "QWERTYUIOPLKJHGFDSAZXCVBNM1234567890";

    public static String generateRandomId() {
        String randomId = "";
        for (int i = 0; i < 8; ++i) {
            int index = WebappContextListener.rnGesus.nextInt(LEGAL_ID_LETTERS.length());
            randomId += LEGAL_ID_LETTERS.charAt(index);
        }
        return randomId;
    }

    public static byte[] fileIntoByteArray(File file) {
        byte[] fileArray = new byte[(int)file.length()];
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            int index = 0;
            int read;
            while (file.length() > index &&
                    (read = in.read(fileArray, index, (int)file.length() - index)) >= 0) {
                index += read;
            }
        } catch (Exception e) {
            Utils.log("ERROR", e, "Utils.fileIntoByteArray - Failed to read " + file.getAbsolutePath());
            fileArray = null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore){}
            }
        }
        return fileArray;
    }

    public static String removeAllWhitespaces(String toRemove) {
        if (toRemove != null) {
            toRemove = toRemove.replaceAll("\\s","");
        }
        return toRemove;
    }

    public static String replaceBracketType(String structure) {
        if (structure != null) {
            structure = structure.replaceAll("<","(").replaceAll(">",")");
        }
        return structure;
    }
}
