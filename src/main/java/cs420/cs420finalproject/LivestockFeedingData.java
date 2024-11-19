package cs420.cs420finalproject;

public class LivestockFeedingData {
    private String timestamp;
    private int feedingLevel;
    private String pastureId; // Pasture ID to associate livestock feeding data with a specific pasture
    private static final int MIN_FEEDING_LEVEL = 0;
    private static final int MAX_FEEDING_LEVEL = 10;

    public LivestockFeedingData(String timestamp, String pastureId, int feedingLevel) {
        this.timestamp = timestamp;
        this.pastureId = pastureId;
        this.feedingLevel = feedingLevel; // Set feeding level from the database
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getFeedingLevel() {
        return feedingLevel;
    }

    public String getPastureId() {
        return pastureId; // Getter for pasture ID
    }

    // Method to update the livestock feeding level
    public void decreaseFeedingLevel() {
        int decrease = (int) (Math.random() * 4); // Random decrease between 0 and 3
        this.feedingLevel = Math.max(this.feedingLevel - decrease, MIN_FEEDING_LEVEL); // Ensure min feeding level is 0
    }

    public void setFeedingLevel(int feedingLevel) {
        this.feedingLevel = feedingLevel;
    }
}