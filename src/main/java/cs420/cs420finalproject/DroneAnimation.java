package cs420.cs420finalproject;

import javafx.animation.Interpolator;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
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

    // Scan the entire pane in a grid-like pattern
    public SequentialTransition scanEntirePane(Pane pane) {
        double paneWidth = pane.getPrefWidth();
        double paneHeight = pane.getPrefHeight();
        double stepSize = 800; // Distance between each step (adjustable)

        SequentialTransition sequentialTransition = new SequentialTransition();

        // Traverse the pane horizontally and vertically
        for (double y = 0; y < paneHeight; y += stepSize) {
            for (double x = 0; x < paneWidth; x += stepSize) {
                sequentialTransition.getChildren().add(moveDrone(x, y));
            }
        }

        return sequentialTransition;
    }

}