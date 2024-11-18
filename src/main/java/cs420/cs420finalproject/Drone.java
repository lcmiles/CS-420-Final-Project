package cs420.cs420finalproject;

public class Drone {

    private String status;
    private String currentLocation;

    public Drone(int droneID, String status, String currentLocation) {
        this.status = status;
        this.currentLocation = currentLocation;
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