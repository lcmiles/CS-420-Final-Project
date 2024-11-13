package cs420.cs420finalproject;
import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
public class DroneAnimation {
    private Circle drone;
    public DroneAnimation(Circle drone) {
        this.drone = drone;
    }
    public SequentialTransition moveDrone(double targetX, double targetY) {
        // Create a timeline for the animation
        TranslateTransition moveTransition = new TranslateTransition();
        moveTransition.setNode(drone);
        moveTransition.setToX(targetX - drone.getLayoutX());
        moveTransition.setToY(targetY - drone.getLayoutY());
        moveTransition.setInterpolator(Interpolator.EASE_BOTH);
        moveTransition.setCycleCount(1);
        moveTransition.setDuration(Duration.seconds(2)); // Adjust as needed
        // Wrap in a SequentialTransition to ensure it can be chained
        SequentialTransition sequentialTransition = new SequentialTransition(moveTransition);
        return sequentialTransition;
    }
}