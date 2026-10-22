package com.devtrace.dto.dependency;

public class DependencyEdgeDto {
    private String source;
    private String target;
    private int weight;

    public DependencyEdgeDto() {}

    public DependencyEdgeDto(String source, String target, int weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
