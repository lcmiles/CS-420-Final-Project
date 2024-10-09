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
    public void moveDrone(double taskX, double taskY) {
        // First transition: move to the task location
        TranslateTransition moveToTask = new TranslateTransition();
        moveToTask.setDuration(Duration.seconds(2));
        moveToTask.setToX(taskX - startX); // Relative to the starting point
        moveToTask.setToY(taskY - startY);
        moveToTask.setNode(droneView);
        // Second transition: move back to the starting position
        TranslateTransition returnToStart = new TranslateTransition();
        returnToStart.setDuration(Duration.seconds(2));
        returnToStart.setToX(0); // Return to original X (relative to the task position)
        returnToStart.setToY(0); // Return to original Y
        returnToStart.setNode(droneView);
        // Sequential transition to first move to task, then return
        SequentialTransition sequentialTransition = new SequentialTransition(moveToTask, returnToStart);
        sequentialTransition.play();
    }
}