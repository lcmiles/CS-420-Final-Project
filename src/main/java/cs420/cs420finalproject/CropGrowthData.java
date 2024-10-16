package cs420.cs420finalproject;

public class CropGrowthData {
    private String timestamp;
    private int growthLevel;
    private String fieldId; // Field ID to associate growth data with a specific field
    private static final int MAX_GROWTH_LEVEL = 10;

    public CropGrowthData(String timestamp, String fieldId, int growthLevel) {
        this.timestamp = timestamp;
        this.fieldId = fieldId;
        this.growthLevel = growthLevel; // Set growth level from the database
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getGrowthLevel() {
        return growthLevel;
    }

    public String getFieldId() {
        return fieldId; // Getter for field ID
    }

    // Method to update the growth level
    public void increaseGrowthLevel() {
        int increase = (int) (Math.random() * 3); // Random increase between 0 and 2
        this.growthLevel = Math.min(this.growthLevel + increase, MAX_GROWTH_LEVEL); // Ensure max growth level is 10
    }

    public void setGrowthLevel(int growthLevel) {
        this.growthLevel = growthLevel;
    }
}