package cs420.cs420finalproject;

public class PestData {
    private String timestamp;
    private int pestLevel;
    private String fieldId; // Field ID to associate pest data with a specific field
    private static final int MIN_PEST_LEVEL = 0;
    private static final int MAX_PEST_LEVEL = 10;

    public PestData(String timestamp, String fieldId, int pestLevel) {
        this.timestamp = timestamp;
        this.fieldId = fieldId;
        this.pestLevel = pestLevel; // Set pest level from the database
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getPestLevel() {
        return pestLevel;
    }

    public String getFieldId() {
        return fieldId; // Getter for field ID
    }

    // Method to update the pest level
    public void increasePestLevel() {
        int increase = (int) (Math.random() * 4); // Random increase between 0 and 3
        this.pestLevel = Math.min(this.pestLevel + increase, MAX_PEST_LEVEL); // Ensure max pest level is 10
    }

    public void setPestLevel(int pestLevel) {
        this.pestLevel = pestLevel;
    }
}