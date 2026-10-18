package com.devtrace.service.issuenlp;

import com.devtrace.dto.issuenlp.*;
import com.devtrace.entity.IssueEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.IssueEntityRepository;
import com.devtrace.repository.RepositoryEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class IssueNlpService {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-zA-Z]{3,}\\b");

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "have", "not", "are",
            "was", "were", "will", "would", "can", "could", "should", "all", "any", "but",
            "about", "into", "some", "when", "then", "there", "their", "they", "them"
    );

    private static final Map<String, Set<String>> CATEGORY_LEXICONS = Map.of(
            "BUG_DEFECT", Set.of(
                    "bug", "defect", "fix", "fixed", "crash", "error", "exception", "failure",
                    "failed", "broken", "wrong", "issue", "nullpointer", "npe", "panic", "fatal",
                    "hang", "timeout", "regression", "invalid", "unexpected", "flaky", "leak"
            ),
            "FEATURE_ENHANCEMENT", Set.of(
                    "feature", "enhancement", "add", "added", "support", "implement", "implemented",
                    "allow", "provide", "request", "enable", "new", "enhance", "capability",
                    "extend", "introduce", "option", "propose", "proposal"
            ),
            "DOCUMENTATION", Set.of(
                    "doc", "docs", "documentation", "readme", "guide", "tutorial", "manual",
                    "explanation", "typo", "comment", "javadoc", "markdown", "example", "changelog"
            ),
            "TECH_DEBT_REFACTOR", Set.of(
                    "refactor", "refactored", "rewrite", "clean", "cleanup", "debt", "technical",
                    "modernize", "simplify", "optimize", "optimization", "restructure", "deprecate",
                    "performance", "memory", "prune", "chore", "reorganize"
            ),
            "QUESTION_SUPPORT", Set.of(
                    "how", "why", "question", "help", "can", "clarify", "clarification",
                    "understand", "usage", "support", "assist", "assistance", "advice", "faq"
            )
    );

    private static final Map<String, String> CATEGORY_NAMES = Map.of(
            "BUG_DEFECT", "Bug / Defect",
            "FEATURE_ENHANCEMENT", "Feature / Enhancement",
            "DOCUMENTATION", "Documentation",
            "TECH_DEBT_REFACTOR", "Technical Debt / Refactor",
            "QUESTION_SUPPORT", "Question / Support"
    );

    private final RepositoryEntityRepository repositoryRepo;
    private final IssueEntityRepository issueRepo;

    public IssueNlpService(RepositoryEntityRepository repositoryRepo, IssueEntityRepository issueRepo) {
        this.repositoryRepo = repositoryRepo;
        this.issueRepo = issueRepo;
    }

    @Transactional
    public IssueNlpReportDto analyzeIssuesNlp(String repositoryId) {
        if (!repositoryRepo.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }

        List<IssueEntity> issues = issueRepo.findByRepositoryId(repositoryId);

        IssueNlpReportDto report = new IssueNlpReportDto();
        report.setRepositoryId(repositoryId);
        report.setTotalAnalyzedIssues(issues.size());

        List<String> categoryKeys = List.of(
                "BUG_DEFECT", "FEATURE_ENHANCEMENT", "DOCUMENTATION", "TECH_DEBT_REFACTOR", "QUESTION_SUPPORT"
        );

        if (issues.isEmpty()) {
            report.setLabeledIssuesCount(0);
            report.setUnlabeledIssuesCount(0);
            report.setCategories(createEmptyCategorySummaries(categoryKeys));
            report.setEvaluation(createEmptyEvaluation(categoryKeys));
            return report;
        }

        // Categorize issues
        int labeledCount = 0;
        int unlabeledCount = 0;
        int agreementsCount = 0;

        Map<String, List<Double>> resolutionTimesPerCategory = new HashMap<>();
        Map<String, Integer> openCountsPerCategory = new HashMap<>();
        Map<String, Integer> totalCountsPerCategory = new HashMap<>();
        Map<String, Map<String, Integer>> categoryTermFreqs = new HashMap<>();

        for (String cat : categoryKeys) {
            resolutionTimesPerCategory.put(cat, new ArrayList<>());
            openCountsPerCategory.put(cat, 0);
            totalCountsPerCategory.put(cat, 0);
            categoryTermFreqs.put(cat, new HashMap<>());
        }

        // Confusion matrix and metrics
        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();
        for (String actual : categoryKeys) {
            Map<String, Integer> row = new HashMap<>();
            for (String pred : categoryKeys) {
                row.put(pred, 0);
            }
            confusionMatrix.put(actual, row);
        }

        Map<String, Integer> truePositives = new HashMap<>();
        Map<String, Integer> falsePositives = new HashMap<>();
        Map<String, Integer> falseNegatives = new HashMap<>();
        for (String cat : categoryKeys) {
            truePositives.put(cat, 0);
            falsePositives.put(cat, 0);
            falseNegatives.put(cat, 0);
        }

        List<IssueClassificationItemDto> sampleItems = new ArrayList<>();

        for (IssueEntity issue : issues) {
            String labelCategory = inferCategoryFromLabels(issue.getLabels());
            ClassificationResult textResult = classifyFromText(issue.getTitle(), issue.getBody());

            String effectiveCategory = labelCategory != null ? labelCategory : textResult.predictedCategory;

            // Update entity if not set
            if ("OTHER".equals(issue.getCategory()) || issue.getCategory() == null) {
                issue.setCategory(effectiveCategory);
                issue.setCategoryConfidence(textResult.confidence);
            }

            totalCountsPerCategory.put(effectiveCategory, totalCountsPerCategory.get(effectiveCategory) + 1);

            boolean isOpen = "open".equalsIgnoreCase(issue.getState());
            if (isOpen) {
                openCountsPerCategory.put(effectiveCategory, openCountsPerCategory.get(effectiveCategory) + 1);
            }

            Double resolutionHours = null;
            if (issue.getClosedAt() != null && issue.getCreatedAt() != null && !issue.getClosedAt().isBefore(issue.getCreatedAt())) {
                resolutionHours = round(Duration.between(issue.getCreatedAt(), issue.getClosedAt()).toMillis() / 3600000.0);
                resolutionTimesPerCategory.get(effectiveCategory).add(resolutionHours);
            }

            // Word frequencies for top keywords
            extractTokens(issue.getTitle() + " " + (issue.getBody() != null ? issue.getBody() : "")).forEach(token -> {
                categoryTermFreqs.get(effectiveCategory).merge(token, 1, Integer::sum);
            });

            // Evaluation against weak supervision (label ground truth)
            boolean isAgreed = false;
            if (labelCategory != null) {
                labeledCount++;
                String pred = textResult.predictedCategory;
                confusionMatrix.get(labelCategory).put(pred, confusionMatrix.get(labelCategory).get(pred) + 1);

                if (labelCategory.equals(pred)) {
                    agreementsCount++;
                    isAgreed = true;
                    truePositives.put(labelCategory, truePositives.get(labelCategory) + 1);
                } else {
                    falseNegatives.put(labelCategory, falseNegatives.get(labelCategory) + 1);
                    falsePositives.put(pred, falsePositives.get(pred) + 1);
                }
            } else {
                unlabeledCount++;
            }

            IssueClassificationItemDto item = new IssueClassificationItemDto();
            item.setIssueNumber(issue.getIssueNumber());
            item.setTitle(issue.getTitle());
            item.setState(issue.getState());
            item.setLabels(issue.getLabels());
            item.setLabelCategory(labelCategory);
            item.setPredictedCategory(textResult.predictedCategory);
            item.setConfidence(textResult.confidence);
            item.setAgreement(isAgreed);
            item.setResolutionHours(resolutionHours);
            sampleItems.add(item);
        }

        report.setLabeledIssuesCount(labeledCount);
        report.setUnlabeledIssuesCount(unlabeledCount);

        // Build category summaries
        int totalIssuesCount = issues.size();
        List<IssueCategorySummaryDto> categorySummaries = new ArrayList<>();

        for (String cat : categoryKeys) {
            int totalCat = totalCountsPerCategory.get(cat);
            int openCat = openCountsPerCategory.get(cat);
            int closedCat = totalCat - openCat;
            double pct = totalIssuesCount > 0 ? round((totalCat * 100.0) / totalIssuesCount) : 0.0;

            List<Double> resTimes = resolutionTimesPerCategory.get(cat);
            double medianRes = calculateMedian(resTimes);
            double p75Res = calculateP75(resTimes);
            double avgRes = calculateAverage(resTimes);

            List<String> topKeywords = categoryTermFreqs.get(cat).entrySet().stream()
                    .filter(e -> !STOP_WORDS.contains(e.getKey()) && e.getKey().length() >= 4)
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(6)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            categorySummaries.add(new IssueCategorySummaryDto(
                    cat,
                    CATEGORY_NAMES.get(cat),
                    totalCat,
                    openCat,
                    closedCat,
                    pct,
                    medianRes,
                    p75Res,
                    avgRes,
                    topKeywords
            ));
        }
        report.setCategories(categorySummaries);

        // Build Classification Evaluation
        ClassificationEvaluationDto evaluation = new ClassificationEvaluationDto();
        evaluation.setTotalEvaluatedIssues(totalIssuesCount);
        evaluation.setTotalLabeledIssues(labeledCount);
        evaluation.setAgreementRate(labeledCount > 0 ? round(((double) agreementsCount / labeledCount) * 100.0) : 0.0);
        evaluation.setCategories(categoryKeys);
        evaluation.setConfusionMatrix(confusionMatrix);

        List<CategoryEvaluationMetricDto> metrics = new ArrayList<>();
        for (String cat : categoryKeys) {
            int tp = truePositives.get(cat);
            int fp = falsePositives.get(cat);
            int fn = falseNegatives.get(cat);

            double precision = (tp + fp) > 0 ? round(((double) tp / (tp + fp)) * 100.0) : 0.0;
            double recall = (tp + fn) > 0 ? round(((double) tp / (tp + fn)) * 100.0) : 0.0;
            double f1 = (precision + recall) > 0 ? round((2 * precision * recall) / (precision + recall)) : 0.0;

            metrics.add(new CategoryEvaluationMetricDto(cat, tp, fp, fn, precision, recall, f1));
        }
        evaluation.setCategoryMetrics(metrics);
        report.setEvaluation(evaluation);

        // Limit sample items to 25
        report.setClassifiedIssuesSample(sampleItems.stream().limit(25).collect(Collectors.toList()));

        return report;
    }

    private String inferCategoryFromLabels(String labels) {
        if (labels == null || labels.isBlank()) return null;
        String lower = labels.toLowerCase();

        if (lower.contains("bug") || lower.contains("defect") || lower.contains("crash") || lower.contains("error")) {
            return "BUG_DEFECT";
        }
        if (lower.contains("feature") || lower.contains("enhancement") || lower.contains("proposal") || lower.contains("request")) {
            return "FEATURE_ENHANCEMENT";
        }
        if (lower.contains("doc") || lower.contains("documentation") || lower.contains("readme") || lower.contains("typo")) {
            return "DOCUMENTATION";
        }
        if (lower.contains("refactor") || lower.contains("tech-debt") || lower.contains("cleanup") || lower.contains("debt") || lower.contains("chore")) {
            return "TECH_DEBT_REFACTOR";
        }
        if (lower.contains("question") || lower.contains("support") || lower.contains("help") || lower.contains("faq")) {
            return "QUESTION_SUPPORT";
        }
        return null;
    }

    private ClassificationResult classifyFromText(String title, String body) {
        List<String> titleTokens = extractTokens(title);
        List<String> bodyTokens = extractTokens(body);

        Map<String, Double> scores = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : CATEGORY_LEXICONS.entrySet()) {
            String cat = entry.getKey();
            Set<String> lexicon = entry.getValue();

            double score = 0.0;
            for (String token : titleTokens) {
                if (lexicon.contains(token)) score += 2.5;
            }
            for (String token : bodyTokens) {
                if (lexicon.contains(token)) score += 1.0;
            }
            scores.put(cat, score);
        }

        String bestCategory = "FEATURE_ENHANCEMENT";
        double maxScore = -1.0;
        double sumScores = 0.0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            sumScores += entry.getValue();
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        double confidence;
        if (maxScore > 0.0 && sumScores > 0.0) {
            confidence = round(Math.min(0.99, Math.max(0.55, maxScore / sumScores)));
        } else {
            confidence = 0.50;
        }

        return new ClassificationResult(bestCategory, confidence);
    }

    private List<String> extractTokens(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        List<String> tokens = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(text.toLowerCase());
        while (matcher.find()) {
            String word = matcher.group();
            if (!STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    private List<IssueCategorySummaryDto> createEmptyCategorySummaries(List<String> categories) {
        return categories.stream()
                .map(cat -> new IssueCategorySummaryDto(cat, CATEGORY_NAMES.get(cat), 0, 0, 0, 0.0, 0.0, 0.0, 0.0, Collections.emptyList()))
                .collect(Collectors.toList());
    }

    private ClassificationEvaluationDto createEmptyEvaluation(List<String> categories) {
        ClassificationEvaluationDto eval = new ClassificationEvaluationDto();
        eval.setTotalEvaluatedIssues(0);
        eval.setTotalLabeledIssues(0);
        eval.setAgreementRate(0.0);
        eval.setCategories(categories);
        return eval;
    }

    private double calculateMedian(List<Double> values) {
        return calculatePercentile(values, 50.0);
    }

    private double calculateP75(List<Double> values) {
        return calculatePercentile(values, 75.0);
    }

    private double calculatePercentile(List<Double> values, double percentile) {
        if (values == null || values.isEmpty()) return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        if (sorted.size() == 1) return round(sorted.get(0));

        double rank = (percentile / 100.0) * (sorted.size() - 1);
        int lower = (int) Math.floor(rank);
        int upper = (int) Math.ceil(rank);
        if (lower == upper) return round(sorted.get(lower));

        double weight = rank - lower;
        return round(sorted.get(lower) + weight * (sorted.get(upper) - sorted.get(lower)));
    }

    private double calculateAverage(List<Double> values) {
        if (values == null || values.isEmpty()) return 0.0;
        return round(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static class ClassificationResult {
        final String predictedCategory;
        final double confidence;

        ClassificationResult(String predictedCategory, double confidence) {
            this.predictedCategory = predictedCategory;
            this.confidence = confidence;
        }
    }
}
