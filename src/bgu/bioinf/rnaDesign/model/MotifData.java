package bgu.bioinf.rnaDesign.model;

import java.util.List;

/**
 * Created by matan on 28/12/15.
 */
public class MotifData {
    private String name;
    private String value;
    private int startIndex;
    private int endIndex;
    private List<Integer> indexList;

    public MotifData(String name, String value, int start, int end) {
        this.name = name;
        this.value = value;
        this.startIndex = start;
        this.endIndex = end - 1;
        this.indexList = null;
    }

    public void setIndexList(List<Integer> indexList) {
        this.indexList = indexList;
    }

    public List<Integer> getIndexList() {
        return this.indexList;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public int getEndIndex() {
        return this.endIndex;
    }

    public String toString() {
        return "Name: " + this.name + ", Value: " + this.value + "\n";
    }

    public String toFullString() {
        return "Name: " + this.name + ", Value: " + this.value + ", Start Index: "
                + this.startIndex + ", End Index: " + this.endIndex + "\n";
    }
}
