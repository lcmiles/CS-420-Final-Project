package cs420.cs420finalproject;

public class Drone {

    private int droneID;
    private String status;
    private String currentLocation;

    public Drone(int droneID, String status, String currentLocation) {
        this.droneID = droneID;
        this.status = status;
        this.currentLocation = currentLocation;
    }

    // Getters and setters
    public int getDroneID() {
        return droneID;
    }

    public void setDroneID(int droneID) {
        this.droneID = droneID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
}