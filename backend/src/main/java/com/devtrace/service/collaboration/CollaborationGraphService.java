package com.devtrace.service.collaboration;

import com.devtrace.dto.collaboration.*;
import com.devtrace.entity.CommitEntity;
import com.devtrace.entity.PullRequestEntity;
import com.devtrace.entity.PullRequestReviewEntity;
import com.devtrace.exception.ResourceNotFoundException;
import com.devtrace.repository.*;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CollaborationGraphService {

    private final RepositoryEntityRepository repositoryRepo;
    private final CommitEntityRepository commitRepo;
    private final CommitFileEntityRepository commitFileRepo;
    private final PullRequestEntityRepository pullRequestRepo;
    private final PullRequestReviewEntityRepository reviewRepo;

    public CollaborationGraphService(RepositoryEntityRepository repositoryRepo,
                                     CommitEntityRepository commitRepo,
                                     CommitFileEntityRepository commitFileRepo,
                                     PullRequestEntityRepository pullRequestRepo,
                                     PullRequestReviewEntityRepository reviewRepo) {
        this.repositoryRepo = repositoryRepo;
        this.commitRepo = commitRepo;
        this.commitFileRepo = commitFileRepo;
        this.pullRequestRepo = pullRequestRepo;
        this.reviewRepo = reviewRepo;
    }

    public CollaborationNetworkDto getCollaborationNetwork(String repositoryId) {
        if (!repositoryRepo.existsById(repositoryId)) {
            throw new ResourceNotFoundException("Repository not found with ID: " + repositoryId);
        }

        CollaborationNetworkDto response = new CollaborationNetworkDto();
        response.setRepositoryId(repositoryId);

        // 1. Gather all activity
        List<PullRequestEntity> pullRequests = pullRequestRepo.findByRepositoryIdOrderByCreatedAtAsc(repositoryId);
        List<PullRequestReviewEntity> reviews = reviewRepo.findByPullRequestRepositoryIdOrderBySubmittedAtAsc(repositoryId);
        List<Object[]> fileCommitDetails = commitFileRepo.findFileCommitDetailsAllTime(repositoryId);

        // Contributor stats tracking: login -> stats
        Map<String, ContributorStats> contributorMap = new HashMap<>();

        // Process repository commits to ensure all commit authors are registered
        List<CommitEntity> allCommits = commitRepo.findByRepositoryIdAndBranchAndCommitDateBetweenOrderByCommitDateAsc(
                repositoryId, null, Instant.EPOCH, Instant.now());
        for (CommitEntity c : allCommits) {
            String author = (c.getAuthorName() != null && !c.getAuthorName().isBlank()) ? c.getAuthorName() : c.getAuthorEmail();
            if (author != null && !author.isBlank()) {
                ContributorStats stats = contributorMap.computeIfAbsent(author, ContributorStats::new);
                if (fileCommitDetails.isEmpty()) {
                    stats.commitsCount++;
                }
            }
        }

        // Process PR authors and reviews
        Map<String, PullRequestEntity> prById = new HashMap<>();
        for (PullRequestEntity pr : pullRequests) {
            prById.put(pr.getId(), pr);
            String author = pr.getAuthorLogin();
            if (author != null && !author.isBlank()) {
                contributorMap.computeIfAbsent(author, ContributorStats::new).prsCreated++;
            }
        }

        // Collaboration interactions: (userA, userB) -> InteractionDetails
        Map<String, EdgeAccumulator> edgeMap = new HashMap<>();

        for (PullRequestReviewEntity rev : reviews) {
            String reviewer = rev.getReviewerLogin();
            if (reviewer != null && !reviewer.isBlank()) {
                contributorMap.computeIfAbsent(reviewer, ContributorStats::new).reviewsGiven++;
            }

            if (rev.getPullRequest() != null) {
                PullRequestEntity pr = prById.get(rev.getPullRequest().getId());
                if (pr != null && pr.getAuthorLogin() != null && !pr.getAuthorLogin().isBlank()) {
                    String author = pr.getAuthorLogin();
                    if (!author.equalsIgnoreCase(reviewer)) {
                        addEdgeInteraction(edgeMap, author, reviewer, 2.0, "REVIEW");
                    }
                }
            }
        }

        // Process File Commit Details for co-editing and knowledge silos
        Map<String, FileContributorHistory> fileHistoryMap = new HashMap<>();

        for (Object[] row : fileCommitDetails) {
            String filename = (String) row[0];
            String author = (String) row[4];
            int adds = row[5] != null ? ((Number) row[5]).intValue() : 0;
            int dels = row[6] != null ? ((Number) row[6]).intValue() : 0;

            if (author != null && !author.isBlank()) {
                contributorMap.computeIfAbsent(author, ContributorStats::new).commitsCount++;
            }

            fileHistoryMap.computeIfAbsent(filename, FileContributorHistory::new)
                    .recordModification(author, adds, dels);
        }

        // Build co-editing edges: if multiple authors modified the same file, add an edge
        for (FileContributorHistory fileHist : fileHistoryMap.values()) {
            if (fileHist.authors.size() > 1) {
                List<String> authorList = new ArrayList<>(fileHist.authors);
                for (int i = 0; i < authorList.size(); i++) {
                    for (int j = i + 1; j < authorList.size(); j++) {
                        addEdgeInteraction(edgeMap, authorList.get(i), authorList.get(j), 1.0, "CO_EDIT");
                    }
                }
            }
        }

        if (contributorMap.isEmpty()) {
            response.setTotalContributors(0);
            response.setTotalInteractions(0);
            response.setNetworkDensity(0.0);
            response.setModularityScore(0.0);
            response.setCommunitiesCount(0);
            response.setCollaborationHealthAssessment("No contributor activity detected for this repository.");
            return response;
        }

        // 2. Build JGraphT Graph
        Graph<String, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (String contributor : contributorMap.keySet()) {
            graph.addVertex(contributor);
        }

        for (EdgeAccumulator edgeAcc : edgeMap.values()) {
            if (graph.containsVertex(edgeAcc.u) && graph.containsVertex(edgeAcc.v)) {
                DefaultWeightedEdge edge = graph.addEdge(edgeAcc.u, edgeAcc.v);
                if (edge != null) {
                    graph.setEdgeWeight(edge, edgeAcc.weight);
                }
            }
        }

        int n = graph.vertexSet().size();
        int e = graph.edgeSet().size();

        // 3. Centrality and Community Detection
        BetweennessCentrality<String, DefaultWeightedEdge> bcScorer = new BetweennessCentrality<>(graph, true);

        // Community detection via connected components
        ConnectivityInspector<String, DefaultWeightedEdge> inspector = new ConnectivityInspector<>(graph);
        List<Set<String>> communities = inspector.connectedSets();

        Map<String, Integer> nodeCommunityMap = new HashMap<>();
        for (int cIdx = 0; cIdx < communities.size(); cIdx++) {
            for (String member : communities.get(cIdx)) {
                nodeCommunityMap.put(member, cIdx);
            }
        }

        // Calculate modularity
        double modularityScore = calculateModularity(graph, communities);

        // Network density
        double networkDensity = 0.0;
        if (n > 1) {
            double potentialEdges = (double) (n * (n - 1)) / 2.0;
            networkDensity = round((double) e / potentialEdges);
        }

        // Build nodes
        List<CollaborationNodeDto> nodeList = new ArrayList<>();
        for (String login : graph.vertexSet()) {
            ContributorStats stats = contributorMap.get(login);
            int degree = graph.degreeOf(login);
            double normDegree = n > 1 ? round((double) degree / (n - 1)) : 0.0;
            double normBetweenness = round(bcScorer.getVertexScore(login));
            int commId = nodeCommunityMap.getOrDefault(login, 0);

            String role;
            if (stats.reviewsGiven >= 5 || (stats.reviewsGiven > 0 && stats.commitsCount >= 10)) {
                role = "MAINTAINER";
            } else if (stats.reviewsGiven > 0) {
                role = "REVIEWER";
            } else {
                role = "CONTRIBUTOR";
            }

            nodeList.add(new CollaborationNodeDto(
                    login,
                    login,
                    role,
                    normDegree,
                    normBetweenness,
                    commId,
                    stats.getTotalActivity()
            ));
        }

        nodeList.sort(Comparator.comparingDouble(CollaborationNodeDto::getDegreeCentrality).reversed());

        // Build edges
        List<CollaborationEdgeDto> edgeList = new ArrayList<>();
        for (EdgeAccumulator acc : edgeMap.values()) {
            edgeList.add(new CollaborationEdgeDto(
                    acc.u,
                    acc.v,
                    round(acc.weight),
                    new ArrayList<>(acc.types)
            ));
        }

        // 4. Detect Knowledge Silos
        List<KnowledgeSiloDto> silos = detectKnowledgeSilos(fileHistoryMap);

        // 5. Overall Collaboration Assessment
        String assessment = generateAssessment(networkDensity, communities.size(), silos.size(), n);

        response.setTotalContributors(n);
        response.setTotalInteractions(e);
        response.setNetworkDensity(networkDensity);
        response.setModularityScore(round(modularityScore));
        response.setCommunitiesCount(communities.size());
        response.setNodes(nodeList);
        response.setEdges(edgeList);
        response.setKnowledgeSilos(silos);
        response.setCollaborationHealthAssessment(assessment);

        return response;
    }

    private double calculateModularity(Graph<String, DefaultWeightedEdge> graph, List<Set<String>> communities) {
        double m = 0.0;
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            m += graph.getEdgeWeight(edge);
        }
        if (m == 0.0) return 0.0;

        double q = 0.0;
        for (Set<String> comm : communities) {
            double internalWeight = 0.0;
            double degreeSum = 0.0;

            for (String u : comm) {
                double uDeg = 0.0;
                for (DefaultWeightedEdge edge : graph.edgesOf(u)) {
                    uDeg += graph.getEdgeWeight(edge);
                }
                degreeSum += uDeg;

                for (String v : comm) {
                    if (u.compareTo(v) < 0 && graph.containsEdge(u, v)) {
                        DefaultWeightedEdge edge = graph.getEdge(u, v);
                        internalWeight += graph.getEdgeWeight(edge);
                    }
                }
            }

            q += (internalWeight / m) - Math.pow(degreeSum / (2.0 * m), 2);
        }
        return Math.max(0.0, q);
    }

    private List<KnowledgeSiloDto> detectKnowledgeSilos(Map<String, FileContributorHistory> fileHistoryMap) {
        List<KnowledgeSiloDto> silos = new ArrayList<>();

        for (FileContributorHistory hist : fileHistoryMap.values()) {
            if (hist.authors.size() == 1 && (hist.commitCount >= 3 || hist.totalChanges >= 50)) {
                String soleContributor = hist.authors.iterator().next();

                String riskSeverity;
                if (hist.commitCount >= 5 || hist.totalChanges >= 300) {
                    riskSeverity = "CRITICAL";
                } else if (hist.commitCount >= 3 || hist.totalChanges >= 100) {
                    riskSeverity = "HIGH";
                } else {
                    riskSeverity = "MEDIUM";
                }

                String recommendation = String.format("File '%s' is exclusively authored by %s (%d commits, %d lines). Schedule cross-training or peer code reviews.",
                        hist.filename, soleContributor, hist.commitCount, hist.totalChanges);

                silos.add(new KnowledgeSiloDto(
                        hist.filename,
                        "SINGLE_CONTRIBUTOR_FILE",
                        soleContributor,
                        hist.commitCount,
                        hist.totalChanges,
                        riskSeverity,
                        recommendation
                ));
            }
        }

        silos.sort((a, b) -> {
            int sevComp = severityWeight(b.getRiskSeverity()) - severityWeight(a.getRiskSeverity());
            if (sevComp != 0) return sevComp;
            return Long.compare(b.getTotalChanges(), a.getTotalChanges());
        });

        return silos.stream().limit(25).collect(Collectors.toList());
    }

    private int severityWeight(String severity) {
        if ("CRITICAL".equals(severity)) return 3;
        if ("HIGH".equals(severity)) return 2;
        if ("MEDIUM".equals(severity)) return 1;
        return 0;
    }

    private String generateAssessment(double density, int communitiesCount, int siloCount, int totalContributors) {
        if (totalContributors <= 1) {
            return "Single developer repository: all knowledge is concentrated with 1 contributor.";
        }
        if (density >= 0.40 && siloCount == 0) {
            return "Resilient collaboration network: high interaction density across contributors with no single-contributor knowledge silos.";
        }
        if (siloCount > 5) {
            return String.format("Knowledge concentration risk: detected %d single-contributor knowledge silos. Recommended cross-training and pairing to improve team bus factor.", siloCount);
        }
        if (communitiesCount > 2 && density < 0.20) {
            return String.format("Siloed community structure: contributors are partitioned into %d isolated clusters with limited cross-group review interaction.", communitiesCount);
        }
        return "Healthy collaboration baseline with balanced peer reviews and active cross-functional co-editing.";
    }

    private void addEdgeInteraction(Map<String, EdgeAccumulator> edgeMap, String u, String v, double weight, String type) {
        String key = u.compareTo(v) < 0 ? u + "::" + v : v + "::" + u;
        edgeMap.computeIfAbsent(key, k -> {
            String first = u.compareTo(v) < 0 ? u : v;
            String second = u.compareTo(v) < 0 ? v : u;
            return new EdgeAccumulator(first, second);
        }).addInteraction(weight, type);
    }

    private double round(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0.0;
        return BigDecimal.valueOf(val).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    private static class ContributorStats {
        final String login;
        int commitsCount = 0;
        int prsCreated = 0;
        int reviewsGiven = 0;

        ContributorStats(String login) {
            this.login = login;
        }

        int getTotalActivity() {
            return commitsCount + prsCreated + reviewsGiven;
        }
    }

    private static class EdgeAccumulator {
        final String u;
        final String v;
        double weight = 0.0;
        final Set<String> types = new HashSet<>();

        EdgeAccumulator(String u, String v) {
            this.u = u;
            this.v = v;
        }

        void addInteraction(double w, String type) {
            this.weight += w;
            this.types.add(type);
        }
    }

    private static class FileContributorHistory {
        final String filename;
        long commitCount = 0;
        long totalChanges = 0;
        final Set<String> authors = new HashSet<>();

        FileContributorHistory(String filename) {
            this.filename = filename;
        }

        void recordModification(String author, int adds, int dels) {
            this.commitCount++;
            this.totalChanges += (adds + dels);
            if (author != null && !author.isBlank()) {
                this.authors.add(author);
            }
        }
    }
}
