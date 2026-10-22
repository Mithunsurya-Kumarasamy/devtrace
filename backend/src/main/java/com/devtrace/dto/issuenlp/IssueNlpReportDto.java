package com.devtrace.dto.issuenlp;

import java.util.ArrayList;
import java.util.List;

public class IssueNlpReportDto {
    private String repositoryId;
    private int totalAnalyzedIssues;
    private int labeledIssuesCount;
    private int unlabeledIssuesCount;
    private List<IssueCategorySummaryDto> categories = new ArrayList<>();
    private ClassificationEvaluationDto evaluation;
    private List<IssueClassificationItemDto> classifiedIssuesSample = new ArrayList<>();

    public IssueNlpReportDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTotalAnalyzedIssues() { return totalAnalyzedIssues; }
    public void setTotalAnalyzedIssues(int totalAnalyzedIssues) { this.totalAnalyzedIssues = totalAnalyzedIssues; }
    public int getLabeledIssuesCount() { return labeledIssuesCount; }
    public void setLabeledIssuesCount(int labeledIssuesCount) { this.labeledIssuesCount = labeledIssuesCount; }
    public int getUnlabeledIssuesCount() { return unlabeledIssuesCount; }
    public void setUnlabeledIssuesCount(int unlabeledIssuesCount) { this.unlabeledIssuesCount = unlabeledIssuesCount; }
    public List<IssueCategorySummaryDto> getCategories() { return categories; }
    public void setCategories(List<IssueCategorySummaryDto> categories) { this.categories = categories; }
    public ClassificationEvaluationDto getEvaluation() { return evaluation; }
    public void setEvaluation(ClassificationEvaluationDto evaluation) { this.evaluation = evaluation; }
    public List<IssueClassificationItemDto> getClassifiedIssuesSample() { return classifiedIssuesSample; }
    public void setClassifiedIssuesSample(List<IssueClassificationItemDto> classifiedIssuesSample) { this.classifiedIssuesSample = classifiedIssuesSample; }
}
