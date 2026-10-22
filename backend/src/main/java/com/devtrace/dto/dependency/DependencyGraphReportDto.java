package com.devtrace.dto.dependency;

import java.util.ArrayList;
import java.util.List;

public class DependencyGraphReportDto {
    private String repositoryId;
    private int totalModules;
    private int totalDependencies;
    private boolean hasCircularDependencies;
    private int circularDependenciesCount;
    private int highBlastRadiusCount;
    private int fragileComponentsCount;
    private List<DependencyNodeDto> nodes = new ArrayList<>();
    private List<DependencyEdgeDto> edges = new ArrayList<>();
    private List<CircularDependencyDto> circularDependencies = new ArrayList<>();
    private String architecturalSummary;

    public DependencyGraphReportDto() {}

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }
    public int getTotalModules() { return totalModules; }
    public void setTotalModules(int totalModules) { this.totalModules = totalModules; }
    public int getTotalDependencies() { return totalDependencies; }
    public void setTotalDependencies(int totalDependencies) { this.totalDependencies = totalDependencies; }
    public boolean isHasCircularDependencies() { return hasCircularDependencies; }
    public void setHasCircularDependencies(boolean hasCircularDependencies) { this.hasCircularDependencies = hasCircularDependencies; }
    public int getCircularDependenciesCount() { return circularDependenciesCount; }
    public void setCircularDependenciesCount(int circularDependenciesCount) { this.circularDependenciesCount = circularDependenciesCount; }
    public int getHighBlastRadiusCount() { return highBlastRadiusCount; }
    public void setHighBlastRadiusCount(int highBlastRadiusCount) { this.highBlastRadiusCount = highBlastRadiusCount; }
    public int getFragileComponentsCount() { return fragileComponentsCount; }
    public void setFragileComponentsCount(int fragileComponentsCount) { this.fragileComponentsCount = fragileComponentsCount; }
    public List<DependencyNodeDto> getNodes() { return nodes; }
    public void setNodes(List<DependencyNodeDto> nodes) { this.nodes = nodes; }
    public List<DependencyEdgeDto> getEdges() { return edges; }
    public void setEdges(List<DependencyEdgeDto> edges) { this.edges = edges; }
    public List<CircularDependencyDto> getCircularDependencies() { return circularDependencies; }
    public void setCircularDependencies(List<CircularDependencyDto> circularDependencies) { this.circularDependencies = circularDependencies; }
    public String getArchitecturalSummary() { return architecturalSummary; }
    public void setArchitecturalSummary(String architecturalSummary) { this.architecturalSummary = architecturalSummary; }
}
