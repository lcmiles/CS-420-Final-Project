package cs420.cs420finalproject;

public class SoilMoistureData {
    private String timestamp;
    private int moistureLevel;
    private String fieldId; // Field ID to associate soil moisture data with a specific field
    private static final int MIN_MOISTURE_LEVEL = 0;
    private static final int MAX_MOISTURE_LEVEL = 10;

    public SoilMoistureData(String timestamp, String fieldId, int moistureLevel) {
        this.timestamp = timestamp;
        this.fieldId = fieldId;
        this.moistureLevel = moistureLevel; // Set moisture level from the database
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getMoistureLevel() {
        return moistureLevel;
    }

    public String getFieldId() {
        return fieldId; // Getter for field ID
    }

    // Method to update the soil moisture level
    public void decreaseMoistureLevel() {
        int decrease = (int) (Math.random() * 4); // Random decrease between 0 and 3
        this.moistureLevel = Math.max(this.moistureLevel - decrease, MIN_MOISTURE_LEVEL); // Ensure min moisture level is 0
    }

    public void setMoistureLevel(int moistureLevel) {
        this.moistureLevel = moistureLevel;
    }
}