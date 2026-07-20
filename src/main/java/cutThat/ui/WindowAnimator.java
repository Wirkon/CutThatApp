package cutThat.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;

public class WindowAnimator {
    private static void animateCollapse(Stage stage, Runnable onFinished) {
        Node root = stage.getScene().getRoot();

        ScaleTransition st = new ScaleTransition(Duration.millis(300), root);
        st.setToX(0.01);
        st.setToY(0.01);

        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setToValue(0);

        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(event -> {
            root.setScaleX(1);
            root.setScaleY(1);
            root.setOpacity(1);

            onFinished.run();
        });
        pt.play();
    }

    public static void minimize(Stage stage) {
        animateCollapse(stage, () -> stage.setIconified(true));
    }


    public static void minimizeToTray(Stage stage) {
        animateCollapse(stage, stage::hide);
    }
}