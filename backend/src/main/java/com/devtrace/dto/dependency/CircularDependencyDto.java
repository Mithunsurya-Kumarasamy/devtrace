package com.devtrace.dto.dependency;

import java.util.ArrayList;
import java.util.List;

public class CircularDependencyDto {
    private int cycleLength;
    private List<String> modules = new ArrayList<>();
    private String recommendation;

    public CircularDependencyDto() {}

    public CircularDependencyDto(int cycleLength, List<String> modules, String recommendation) {
        this.cycleLength = cycleLength;
        this.modules = modules;
        this.recommendation = recommendation;
    }

    public int getCycleLength() { return cycleLength; }
    public void setCycleLength(int cycleLength) { this.cycleLength = cycleLength; }
    public List<String> getModules() { return modules; }
    public void setModules(List<String> modules) { this.modules = modules; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
}
