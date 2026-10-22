package com.devtrace.dto.issuenlp;

public class IssueClassificationItemDto {
    private int issueNumber;
    private String title;
    private String state;
    private String labels;
    private String labelCategory;
    private String predictedCategory;
    private double confidence;
    private boolean agreement;
    private Double resolutionHours;

    public IssueClassificationItemDto() {}

    public int getIssueNumber() { return issueNumber; }
    public void setIssueNumber(int issueNumber) { this.issueNumber = issueNumber; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getLabels() { return labels; }
    public void setLabels(String labels) { this.labels = labels; }
    public String getLabelCategory() { return labelCategory; }
    public void setLabelCategory(String labelCategory) { this.labelCategory = labelCategory; }
    public String getPredictedCategory() { return predictedCategory; }
    public void setPredictedCategory(String predictedCategory) { this.predictedCategory = predictedCategory; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public boolean isAgreement() { return agreement; }
    public void setAgreement(boolean agreement) { this.agreement = agreement; }
    public Double getResolutionHours() { return resolutionHours; }
    public void setResolutionHours(Double resolutionHours) { this.resolutionHours = resolutionHours; }
}
