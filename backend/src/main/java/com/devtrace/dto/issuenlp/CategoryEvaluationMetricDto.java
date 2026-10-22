package com.devtrace.dto.issuenlp;

public class CategoryEvaluationMetricDto {
    private String categoryKey;
    private int truePositives;
    private int falsePositives;
    private int falseNegatives;
    private double precision;
    private double recall;
    private double f1Score;

    public CategoryEvaluationMetricDto() {}

    public CategoryEvaluationMetricDto(String categoryKey, int truePositives, int falsePositives,
                                       int falseNegatives, double precision, double recall, double f1Score) {
        this.categoryKey = categoryKey;
        this.truePositives = truePositives;
        this.falsePositives = falsePositives;
        this.falseNegatives = falseNegatives;
        this.precision = precision;
        this.recall = recall;
        this.f1Score = f1Score;
    }

    public String getCategoryKey() { return categoryKey; }
    public void setCategoryKey(String categoryKey) { this.categoryKey = categoryKey; }
    public int getTruePositives() { return truePositives; }
    public void setTruePositives(int truePositives) { this.truePositives = truePositives; }
    public int getFalsePositives() { return falsePositives; }
    public void setFalsePositives(int falsePositives) { this.falsePositives = falsePositives; }
    public int getFalseNegatives() { return falseNegatives; }
    public void setFalseNegatives(int falseNegatives) { this.falseNegatives = falseNegatives; }
    public double getPrecision() { return precision; }
    public void setPrecision(double precision) { this.precision = precision; }
    public double getRecall() { return recall; }
    public void setRecall(double recall) { this.recall = recall; }
    public double getF1Score() { return f1Score; }
    public void setF1Score(double f1Score) { this.f1Score = f1Score; }
}
