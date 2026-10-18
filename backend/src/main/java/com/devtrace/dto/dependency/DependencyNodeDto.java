package com.devtrace.dto.dependency;

public class DependencyNodeDto {
    private String id;
    private String name;
    private String moduleType; // JAVA, TYPESCRIPT, PYTHON, MODULE
    private int afferentCoupling; // Ca (in-degree)
    private int efferentCoupling; // Ce (out-degree)
    private double instability; // Ce / (Ca + Ce)
    private long totalChurn;
    private String hotspotLevel; // CRITICAL, HIGH, MEDIUM, LOW
    private String architecturalRole; // HIGH_BLAST_RADIUS, FRAGILE_VOLATILE, STABLE_CORE, NORMAL
    private boolean inCycle;

    public DependencyNodeDto() {}

    public DependencyNodeDto(String id, String name, String moduleType, int afferentCoupling,
                             int efferentCoupling, double instability, long totalChurn,
                             String hotspotLevel, String architecturalRole, boolean inCycle) {
        this.id = id;
        this.name = name;
        this.moduleType = moduleType;
        this.afferentCoupling = afferentCoupling;
        this.efferentCoupling = efferentCoupling;
        this.instability = instability;
        this.totalChurn = totalChurn;
        this.hotspotLevel = hotspotLevel;
        this.architecturalRole = architecturalRole;
        this.inCycle = inCycle;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }
    public int getAfferentCoupling() { return afferentCoupling; }
    public void setAfferentCoupling(int afferentCoupling) { this.afferentCoupling = afferentCoupling; }
    public int getEfferentCoupling() { return efferentCoupling; }
    public void setEfferentCoupling(int efferentCoupling) { this.efferentCoupling = efferentCoupling; }
    public double getInstability() { return instability; }
    public void setInstability(double instability) { this.instability = instability; }
    public long getTotalChurn() { return totalChurn; }
    public void setTotalChurn(long totalChurn) { this.totalChurn = totalChurn; }
    public String getHotspotLevel() { return hotspotLevel; }
    public void setHotspotLevel(String hotspotLevel) { this.hotspotLevel = hotspotLevel; }
    public String getArchitecturalRole() { return architecturalRole; }
    public void setArchitecturalRole(String architecturalRole) { this.architecturalRole = architecturalRole; }
    public boolean isInCycle() { return inCycle; }
    public void setInCycle(boolean inCycle) { this.inCycle = inCycle; }
}
