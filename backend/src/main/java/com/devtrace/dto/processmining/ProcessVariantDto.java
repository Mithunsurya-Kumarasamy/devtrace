package com.devtrace.dto.processmining;

public class ProcessVariantDto {
    private String variantKey;
    private String variantName;
    private int count;
    private double percentage;
    private double medianCycleTimeHours;
    private String description;

    public ProcessVariantDto() {}

    public ProcessVariantDto(String variantKey, String variantName, int count, double percentage,
                             double medianCycleTimeHours, String description) {
        this.variantKey = variantKey;
        this.variantName = variantName;
        this.count = count;
        this.percentage = percentage;
        this.medianCycleTimeHours = medianCycleTimeHours;
        this.description = description;
    }

    public String getVariantKey() { return variantKey; }
    public void setVariantKey(String variantKey) { this.variantKey = variantKey; }
    public String getVariantName() { return variantName; }
    public String getDisplayName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public int getCount() { return count; }
    public int getPrCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public double getPercentage() { return percentage; }
    public double getPercentageOfTotal() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public double getMedianCycleTimeHours() { return medianCycleTimeHours; }
    public void setMedianCycleTimeHours(double medianCycleTimeHours) { this.medianCycleTimeHours = medianCycleTimeHours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
