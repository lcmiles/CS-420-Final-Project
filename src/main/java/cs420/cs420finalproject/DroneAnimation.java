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
        TranslateTransition moveTransition = new TranslateTransition();
        moveTransition.setNode(drone);
        moveTransition.setToX(targetX - drone.getLayoutX());  // Movement on X axis
        moveTransition.setToY(targetY - drone.getLayoutY());  // Movement on Y axis
        moveTransition.setInterpolator(Interpolator.EASE_BOTH);
        moveTransition.setCycleCount(1);
        moveTransition.setDuration(Duration.seconds(2));  // Adjust as needed
        SequentialTransition sequentialTransition = new SequentialTransition(moveTransition);
        return sequentialTransition;
    }

}