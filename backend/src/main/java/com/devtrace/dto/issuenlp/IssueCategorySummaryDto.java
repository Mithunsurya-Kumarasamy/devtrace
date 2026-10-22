package com.devtrace.dto.issuenlp;

import java.util.ArrayList;
import java.util.List;

public class IssueCategorySummaryDto {
    private String categoryKey;
    private String categoryName;
    private int totalIssues;
    private int openIssues;
    private int closedIssues;
    private double percentage;
    private double medianResolutionHours;
    private double p75ResolutionHours;
    private double averageResolutionHours;
    private List<String> topKeywords = new ArrayList<>();

    public IssueCategorySummaryDto() {}

    public IssueCategorySummaryDto(String categoryKey, String categoryName, int totalIssues,
                                   int openIssues, int closedIssues, double percentage,
                                   double medianResolutionHours, double p75ResolutionHours,
                                   double averageResolutionHours, List<String> topKeywords) {
        this.categoryKey = categoryKey;
        this.categoryName = categoryName;
        this.totalIssues = totalIssues;
        this.openIssues = openIssues;
        this.closedIssues = closedIssues;
        this.percentage = percentage;
        this.medianResolutionHours = medianResolutionHours;
        this.p75ResolutionHours = p75ResolutionHours;
        this.averageResolutionHours = averageResolutionHours;
        this.topKeywords = topKeywords;
    }

    public String getCategoryKey() { return categoryKey; }
    public void setCategoryKey(String categoryKey) { this.categoryKey = categoryKey; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getTotalIssues() { return totalIssues; }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
    public int getOpenIssues() { return openIssues; }
    public void setOpenIssues(int openIssues) { this.openIssues = openIssues; }
    public int getClosedIssues() { return closedIssues; }
    public void setClosedIssues(int closedIssues) { this.closedIssues = closedIssues; }
    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public double getMedianResolutionHours() { return medianResolutionHours; }
    public void setMedianResolutionHours(double medianResolutionHours) { this.medianResolutionHours = medianResolutionHours; }
    public double getP75ResolutionHours() { return p75ResolutionHours; }
    public void setP75ResolutionHours(double p75ResolutionHours) { this.p75ResolutionHours = p75ResolutionHours; }
    public double getAverageResolutionHours() { return averageResolutionHours; }
    public void setAverageResolutionHours(double averageResolutionHours) { this.averageResolutionHours = averageResolutionHours; }
    public List<String> getTopKeywords() { return topKeywords; }
    public void setTopKeywords(List<String> topKeywords) { this.topKeywords = topKeywords; }
}
