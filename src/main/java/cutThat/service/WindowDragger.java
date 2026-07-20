package cutThat.service;

import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowDragger {
    private static double xOffset = 0;
    private static double yOffset = 0;

    public static void makeDraggable(Stage stage, Node node) {
        node.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        node.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}