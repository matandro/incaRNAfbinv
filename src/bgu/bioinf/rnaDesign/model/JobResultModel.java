package bgu.bioinf.rnaDesign.model;

import bgu.bioinf.rnaDesign.Producers.Utils;
import bgu.bioinf.rnaDesign.db.JobResultEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matan on 01/12/15.
 */
public class JobResultModel {
    private List<SingleResultModel> results;
    private int page;
    private String sortBy;
    private List<Float> filters;
    private long maxResultsInPage;
    private long totalNoOfResults;
    private boolean filtered;

    public JobResultModel() {
        results = new ArrayList<SingleResultModel>();
        filtered = false;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<SingleResultModel> getResults() {
        return results;
    }

    public void addResult(JobResultEntity jobResultEntity) {
        results.add(new SingleResultModel(jobResultEntity));
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setFilters(List<Float> filters) {
        this.filters = filters;
        for (int i = 0; i < this.filters.size(); ++i) {
            if (this.filters.get(i) != null) {
                this.filtered = true;
                return;
            }
        }
        this.filtered = false;
    }

    public boolean getFiltered() {
        return this.filtered;
    }

    public List<Float> getFilters() {
        return filters;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setMaxResultsInPage(long maxResults) {
        this.maxResultsInPage = maxResults;
    }

    public long getMaxResultsInPage() {
        return maxResultsInPage;
    }

    public void setTotalNoOfResults(long totalNoOfResults) {
        this.totalNoOfResults = totalNoOfResults;
    }

    public long getTotalNoOfResults() {
        return totalNoOfResults;
    }

    public int getTotalPages() {
        return (int) Math.ceil((float) getTotalNoOfResults() / maxResultsInPage);
    }

    //TODO: replace filter information to a hash map?
    public static final String[] FILTER_NAMES = {"maxEnergy", "maxBpDistance", "maxShapiroDistance", "maxDesignScore"};
    public static final String[] FILTER_COLUMN_NAMES = {"energyScore", "structureDistance", "shapiroDistance", "designScore"};

    public String getFilterURL() {
        String result = "";
        if (getFilters().size() >= 3 && getFilters().size() <= 4) {
            for (int i = 0; i < FILTER_NAMES.length; ++i) {
                if (getFilters().get(i) != null) {
                    result += "&" + FILTER_NAMES[i] + "=" + getFilters().get(i);
                }
            }
        } else {
            Utils.log("SEVERE", true, "JobResultModel.getFilterURL - filter not initialized properly, size="
                    + getFilters().size() + "!");
        }
        return result;
    }
}
