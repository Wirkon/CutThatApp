package cutThat.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.layout.Region;

public class GradientAnimator {
    private final Region targetRegion;
    private final ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();
    private Timeline timeline;
    private Duration duration;

    public GradientAnimator(Region targetRegion, Duration duration) {
        this.targetRegion = targetRegion;
        this.duration = duration;
        initTimeline();
    }

    private void initTimeline() {
        Color color1 = Color.web("#38bdf8");
        Color color2 = Color.web("#8a31e0");

        KeyValue keyValue1 = new KeyValue(baseColor, color1);
        KeyValue keyValue2 = new KeyValue(baseColor, color2);

        KeyFrame keyFrame1 = new KeyFrame(Duration.ZERO, keyValue1);
        KeyFrame keyFrame2 = new KeyFrame(duration, keyValue2);

        timeline = new Timeline(keyFrame1, keyFrame2);
        timeline.setAutoReverse(true);
        timeline.setCycleCount(Animation.INDEFINITE);

        baseColor.addListener((obs, oldColor, newColor) -> {
            if (newColor != null) {
                String hex = String.format("#%02x%02x%02x",
                        (int) (newColor.getRed() * 255),
                        (int) (newColor.getGreen() * 255),
                        (int) (newColor.getBlue() * 255));

                targetRegion.setStyle("-gradient-base: " + hex + ";");
            }
        });
    }

    public void play() {
        if (timeline != null) {
            timeline.play();
        }
    }


    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void setRate(double rate) {
        if (timeline != null) {
            timeline.setRate(rate);
        }
    }
}