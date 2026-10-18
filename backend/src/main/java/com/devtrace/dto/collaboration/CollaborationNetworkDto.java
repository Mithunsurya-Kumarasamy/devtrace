package com.devtrace.dto.collaboration;

import java.util.ArrayList;
import java.util.List;

public class CollaborationNetworkDto {
    private String repositoryId;
    private int totalContributors;
    private int totalInteractions;
    private double networkDensity;
    private double modularityScore;
    private int communitiesCount;
    private List<CollaborationNodeDto> nodes = new ArrayList<>();
    private List<CollaborationEdgeDto> edges = new ArrayList<>();
    private List<KnowledgeSiloDto> knowledgeSilos = new ArrayList<>();
    private String collaborationHealthAssessment;

    public CollaborationNetworkDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTotalContributors() { return totalContributors; }
    public void setTotalContributors(int totalContributors) { this.totalContributors = totalContributors; }
    public int getTotalInteractions() { return totalInteractions; }
    public void setTotalInteractions(int totalInteractions) { this.totalInteractions = totalInteractions; }
    public double getNetworkDensity() { return networkDensity; }
    public void setNetworkDensity(double networkDensity) { this.networkDensity = networkDensity; }
    public double getModularityScore() { return modularityScore; }
    public void setModularityScore(double modularityScore) { this.modularityScore = modularityScore; }
    public int getCommunitiesCount() { return communitiesCount; }
    public void setCommunitiesCount(int communitiesCount) { this.communitiesCount = communitiesCount; }
    public List<CollaborationNodeDto> getNodes() { return nodes; }
    public void setNodes(List<CollaborationNodeDto> nodes) { this.nodes = nodes; }
    public List<CollaborationEdgeDto> getEdges() { return edges; }
    public void setEdges(List<CollaborationEdgeDto> edges) { this.edges = edges; }
    public List<KnowledgeSiloDto> getKnowledgeSilos() { return knowledgeSilos; }
    public void setKnowledgeSilos(List<KnowledgeSiloDto> knowledgeSilos) { this.knowledgeSilos = knowledgeSilos; }
    public String getCollaborationHealthAssessment() { return collaborationHealthAssessment; }
    public void setCollaborationHealthAssessment(String collaborationHealthAssessment) { this.collaborationHealthAssessment = collaborationHealthAssessment; }
}
