package bgu.bioinf.rnaDesign.Producers;

import bgu.bioinf.rnaDesign.model.MotifData;

import java.util.*;

/**
 * Created by matan on 21/04/16.
 * Not thread safe, generated shapiro with indexes
 */
public class ShapiroGenerator {
    private String structure;
    private String aux;
    private String shapiro;
    private String shapiroIndex;
    private Map<Integer, Integer> closreMap;
    private Map<Integer, Integer> treeIndexMap;

    public ShapiroGenerator(String structure) {
        closreMap = new HashMap<Integer, Integer>(structure.length());
        this.structure = structure;
        aux = null;
        shapiro = null;
        shapiroIndex = null;
        treeIndexMap = null;

        Stack<Integer> stack = new Stack<Integer>();
        for (int i = 0; i < structure.length(); ++i) {
            switch (structure.charAt(i)) {
                case '(':
                    stack.push(i);
                    break;
                case ')':
                    closreMap.put(i, stack.pop());
                    break;
                case '.':
                default:
            }
        }
    }

    private static char[] copyStrToArray(String str) {
        char[] arr = new char[str.length() + 1];
        for (int i = 0; i < str.length(); ++i) {
            arr[i] = str.charAt(i);
        }
        return arr;
    }

    private String aux_struct() {
        short[] match_paren = new short[structure.length() / 2 + 1];
        int i, o, p;
        char[] string = copyStrToArray(this.structure);

        i = o = 0;
        while (string[i] != 0) {
            switch (string[i]) {
                case '.':
                    break;
                case '(':
                    match_paren[++o] = (short) i;
                    break;
                case ')':
                    p = i;
                    while ((string[p + 1] == ')') && (match_paren[o - 1] == match_paren[o] - 1)) {
                        p++;
                        o--;
                    }
                    string[p] = ']';
                    i = p;
                    string[match_paren[o]] = '[';
                    o--;
                    break;
                default:
                    //nrerror("Junk in structure at aux_structure\n");
            }
            i++;
        }
        aux = new String(string);
        return aux;
    }

    private static void addToIndexes(Map<Integer, List<Integer>> map, int arrIndex, int strIndex) {
        if (map.get(arrIndex) == null) {
            map.put(arrIndex, new ArrayList<Integer>());
        }
        map.get(arrIndex).add(strIndex);
    }

    private static String getIndexes(Map<Integer, List<Integer>> map, int arrIndex) {
        String res = "[]";
        if (map.get(arrIndex) != null) {
            res = map.get(arrIndex).toString();
        }
        return res;
    }

    private static void mapPlusOne(Map<Integer, Integer> map, int index) {
        if (map.get(index) == null)
            map.put(index, 1);
        else
            map.put(index, map.get(index) + 1);
    }

    private String getIndexStrByShapiroIndex(MotifData motifData) {
        String numbers = "";
        int shapiroIndexCur = 0;
        int shapiroCur = 0;
        // Find the index (in both strings)
        for (; shapiroCur < getShapiro().length() && shapiroCur < motifData.getStartIndex(); ++shapiroCur) {
            if (getShapiro().charAt(shapiroCur) == '(' || getShapiro().charAt(shapiroCur) == ')') {
                for (; shapiroIndexCur < getShapiroIndex().length(); ++shapiroIndexCur) {
                    if (getShapiroIndex().charAt(shapiroIndexCur) == '(' || getShapiroIndex().charAt(shapiroIndexCur) == ')') {
                        ++shapiroIndexCur;
                        break;
                    }
                }
            }
        }
        // read until end of motif
        for (; shapiroCur < getShapiro().length() && shapiroCur <= motifData.getEndIndex() + 1; ++shapiroCur) {
            if (getShapiro().charAt(shapiroCur) == '(' || getShapiro().charAt(shapiroCur) == ')') {
                for (; shapiroIndexCur < getShapiroIndex().length(); ++shapiroIndexCur) {
                    char c = getShapiroIndex().charAt(shapiroIndexCur);
                    if (c == ',' || c == ']')
                        numbers += ",";
                    else if (c >= '0' && c <= '9')
                        numbers += c;
                    else if (c == '(' || c == ')') {
                        ++shapiroIndexCur;
                        break;
                    }
                }
            }
        }
        return numbers;
    }

    public List<Integer> getIndexsByMotif(MotifData motifData) {
        List<Integer> indexes = new ArrayList<Integer>();

        String numbersWithQ = getIndexStrByShapiroIndex(motifData);
        String[] numbers = numbersWithQ.split(",");
        for (int i = 0 ; i < numbers.length ; ++i) {
            if (numbers[i] != "") {
                indexes.add(Integer.valueOf(numbers[i]) + 1);
            }
        }

        Collections.sort(indexes);
        return indexes;
    }

    private String b2Shapiro() {
        // was global
        Map<Integer, Integer> loop_size = new HashMap<Integer, Integer>();       /* loop sizes of a structure */
        Map<Integer, Integer> helix_size = new HashMap<Integer, Integer>();      /* helix sizes of a structure */
        Map<Integer, Integer> loop_degree = new HashMap<Integer, Integer>();      /* loop degrees of a structure */
        int loops;                             /* n of loops and stacks */
        int unpaired;
        int pairs;

        Map<Integer, List<Integer>> loopIndexes = new HashMap<Integer, List<Integer>>();
        Map<Integer, List<Integer>> stemIndexes = new HashMap<Integer, List<Integer>>();

        int[] bulge = new int[structure.length() / 3 + 1];
        int[] loop = new int[structure.length() / 3 + 1];

        int i, lp, p;
        char[] string;
        this.shapiro = "";
        String temp = "";
        String testIndexes = "";


        loop_degree.put(0, 0);         /* open structure has degree 0 */
        pairs = unpaired = loops = lp = 0;
        loop[0] = 0;

        string = copyStrToArray(getAux());

        i = p = 0;
        temp += '(';    /* root */
        testIndexes += '(';
        while (string[i] != 0) {
            switch (string[i]) {
                case '.':
                    unpaired++;
                    mapPlusOne(loop_size, loop[lp]);
                    addToIndexes(loopIndexes, loop[lp], i);
                    break;
                case '[':
                    testIndexes += "((";
                    temp += "((";
                    if ((i > 0) && (string[i - 1] == '(' || string[i - 1] == '['))
                        bulge[lp] = 1;
                    lp++;
                    loop_degree.put(++loops, 1);
                    loop[lp] = loops;
                    bulge[lp] = 0;
                    break;
                case ')':
                    if (string[i - 1] == ']') bulge[lp] = 1;
                    p++;
                    addToIndexes(stemIndexes, loop[lp], i);
                    addToIndexes(stemIndexes, loop[lp], closreMap.get(i));
                    break;
                case ']':
                    if (string[i - 1] == ']') bulge[lp] = 1;
                    switch (loop_degree.get(loop[lp])) {
                        case 1:
                            temp += 'H';
                            break;           /* hairpin */
                        case 2:
                            if (bulge[lp] == 1)
                                temp += 'B';                    /* bulge */
                            else
                                temp += 'I';                    /* internal loop */
                            break;
                        default:
                            temp += 'M';                /* multiloop */
                    }
                    helix_size.put(loop[lp], p + 1);
                    addToIndexes(stemIndexes, loop[lp], i);
                    addToIndexes(stemIndexes, loop[lp], closreMap.get(i));

                    temp += loop_size.get(loop[lp]) + ")";
                    testIndexes += getIndexes(loopIndexes, loop[lp]) + ")";
                    temp += "S" + helix_size.get(loop[lp]) + ")";
                    testIndexes += getIndexes(stemIndexes, loop[lp]) + ")";


                    pairs += p + 1;
                    p = 0;
                    mapPlusOne(loop_degree, loop[--lp]);
                    break;
            }
            i++;
        }

        if (loop_size.get(0) != null && loop_size.get(0) != 0) {
            temp += "E" + loop_size.get(0) + ")";
            testIndexes += getIndexes(loopIndexes, 0) + ")";
        }
        temp += "R)";
        testIndexes += ")";
        if (loop_size.get(0) != null && loop_size.get(0) != 0) {
            shapiro += '(';
            testIndexes = "(" + testIndexes;
        }
        shapiro += temp;
        shapiroIndex = testIndexes;
        return shapiro;
    }

    public String getAux() {
        if (aux == null)
            aux_struct();
        return aux;
    }

    public String getShapiro() {
        if (shapiro == null)
            b2Shapiro();
        return shapiro;
    }

    public String getShapiroIndex() {
        if (shapiroIndex == null)
            b2Shapiro();
        return shapiroIndex;
    }
}