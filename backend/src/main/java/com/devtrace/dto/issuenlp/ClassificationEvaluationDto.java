package com.devtrace.dto.issuenlp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassificationEvaluationDto {
    private int totalEvaluatedIssues;
    private int totalLabeledIssues;
    private double agreementRate;
    private List<CategoryEvaluationMetricDto> categoryMetrics = new ArrayList<>();
    private Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();
    private List<String> categories = new ArrayList<>();

    public ClassificationEvaluationDto() {}

    public int getTotalEvaluatedIssues() { return totalEvaluatedIssues; }
    public void setTotalEvaluatedIssues(int totalEvaluatedIssues) { this.totalEvaluatedIssues = totalEvaluatedIssues; }
    public int getTotalLabeledIssues() { return totalLabeledIssues; }
    public void setTotalLabeledIssues(int totalLabeledIssues) { this.totalLabeledIssues = totalLabeledIssues; }
    public double getAgreementRate() { return agreementRate; }
    public void setAgreementRate(double agreementRate) { this.agreementRate = agreementRate; }
    public List<CategoryEvaluationMetricDto> getCategoryMetrics() { return categoryMetrics; }
    public void setCategoryMetrics(List<CategoryEvaluationMetricDto> categoryMetrics) { this.categoryMetrics = categoryMetrics; }
    public Map<String, Map<String, Integer>> getConfusionMatrix() { return confusionMatrix; }
    public void setConfusionMatrix(Map<String, Map<String, Integer>> confusionMatrix) { this.confusionMatrix = confusionMatrix; }
    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }
}
