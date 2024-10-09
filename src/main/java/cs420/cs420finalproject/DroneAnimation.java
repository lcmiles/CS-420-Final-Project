package cs420.cs420finalproject;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
public class DroneAnimation {
    private Circle droneView; // Visual representation of the drone
    private double startX;    // Initial X position
    private double startY;    // Initial Y position
    public DroneAnimation(Circle droneView) {
        this.droneView = droneView;
        this.startX = droneView.getLayoutX();
        this.startY = droneView.getLayoutY();
    }
    public SequentialTransition moveDrone(double taskX, double taskY) {
        TranslateTransition moveToTask = new TranslateTransition();
        moveToTask.setDuration(Duration.seconds(2));
        moveToTask.setToX(taskX - droneView.getLayoutX()); // Relative to current position
        moveToTask.setToY(taskY - droneView.getLayoutY());
        moveToTask.setNode(droneView);
        // Sequential transition for single movement
        SequentialTransition sequentialTransition = new SequentialTransition(moveToTask);
        return sequentialTransition;
    }
}