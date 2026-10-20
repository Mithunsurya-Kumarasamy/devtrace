package com.devtrace.dto.collaboration;

public class CollaborationNodeDto {
    private String id;
    private String label;
    private String role; // MAINTAINER, CONTRIBUTOR, REVIEWER
    private double degreeCentrality;
    private double betweennessCentrality;
    private int communityId;
    private int activityCount;

    public CollaborationNodeDto() {}

    public CollaborationNodeDto(String id, String label, String role, double degreeCentrality,
                                double betweennessCentrality, int communityId, int activityCount) {
        this.id = id;
        this.label = label;
        this.role = role;
        this.degreeCentrality = degreeCentrality;
        this.betweennessCentrality = betweennessCentrality;
        this.communityId = communityId;
        this.activityCount = activityCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public String getUsername() { return label != null ? label : id; }
    public void setLabel(String label) { this.label = label; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public double getDegreeCentrality() { return degreeCentrality; }
    public void setDegreeCentrality(double degreeCentrality) { this.degreeCentrality = degreeCentrality; }
    public double getBetweennessCentrality() { return betweennessCentrality; }
    public void setBetweennessCentrality(double betweennessCentrality) { this.betweennessCentrality = betweennessCentrality; }
    public int getCommunityId() { return communityId; }
    public void setCommunityId(int communityId) { this.communityId = communityId; }
    public int getActivityCount() { return activityCount; }
    public void setActivityCount(int activityCount) { this.activityCount = activityCount; }
}
