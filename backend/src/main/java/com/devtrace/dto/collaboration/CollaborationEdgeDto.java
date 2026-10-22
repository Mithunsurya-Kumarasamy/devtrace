package com.devtrace.dto.collaboration;

import java.util.ArrayList;
import java.util.List;

public class CollaborationEdgeDto {
    private String source;
    private String target;
    private double weight;
    private List<String> interactionTypes = new ArrayList<>();

    public CollaborationEdgeDto() {}

    public CollaborationEdgeDto(String source, String target, double weight, List<String> interactionTypes) {
        this.source = source;
        this.target = target;
        this.weight = weight;
        this.interactionTypes = interactionTypes;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public List<String> getInteractionTypes() { return interactionTypes; }
    public void setInteractionTypes(List<String> interactionTypes) { this.interactionTypes = interactionTypes; }
}
