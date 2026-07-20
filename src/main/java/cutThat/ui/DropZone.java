package cutThat.ui;

import cutThat.service.Logger;
import cutThat.service.ProcessFile;
import cutThat.service.SoundManager;
import javafx.animation.*;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import static cutThat.service.IconFactory.createIcon;
public class DropZone extends VBox {
    private final GradientAnimator gradientAnimator;
    private final Label mainLabel;
    private final String MAIN_LABEL = "Перетащите файл сюда";
    private final ImageView zoneIcon;
    private final ImageView encodingIcon;
    private final ImageView successIcon;
    private final ComboBox<String> codecSelector;
    private final Label countLabel;
    private PauseTransition timer;
    private FadeTransition fadeTransition;
    private Queue<File> fileQueue = new LinkedList<>();
    private Boolean PROCESSING_LOCK = false;

    private final VBox centerBox;
    private final VBox progressBox;
    private final ProgressBar progressBar;
    private final Label timeLabel;
    private String selectedFilePath;

    public DropZone(){
        setSpacing(0);
        setAlignment(Pos.CENTER);

        getStyleClass().add("drop-zone");
        getStyleClass().add("drop-zone-normal");

        successIcon = createIcon("/icons/success.png", 48);
        zoneIcon = createIcon("/icons/dpzone.png", 48);
        encodingIcon = createIcon("/icons/processing.png", 48);

        setPadding(new Insets(20, 40, 20, 40));
        VBox.setMargin(zoneIcon, new Insets(0, 0, 15, 0));

        mainLabel = new Label(MAIN_LABEL);
        mainLabel.getStyleClass().add("drop-zone-title");

        Label subLabel = new Label("Поддерживаемые форматы: MP4, MKV, AVI");
        subLabel.getStyleClass().add("drop-zone-subtitle");
        VBox.setMargin(subLabel, new Insets(0, 0, 10, 0));

        countLabel = new Label("Подготовка...");
        countLabel.getStyleClass().add("drop-zone-subtitle");

        codecSelector = new ComboBox<>();
        codecSelector.getItems().addAll("Авто");
        if (ProcessFile.isNvidiaSupported) codecSelector.getItems().add("NVIDIA NVENC");
        if (ProcessFile.isAmdSupported)    codecSelector.getItems().add("AMD AMF");
        if (ProcessFile.isIntelSupported)  codecSelector.getItems().add("INTEL QSV");

        codecSelector.getItems().add("CPU(x264)");
        codecSelector.setValue("Авто");
        codecSelector.getStyleClass().add("drop-zone-codec-selector");

        HBox codecContainer = new HBox(codecSelector);
        codecContainer.setAlignment(Pos.TOP_RIGHT);

        HBox.setMargin(codecSelector, new Insets(-5, -25, 0, 0));

        gradientAnimator = new GradientAnimator(this, Duration.seconds(1));
        gradientAnimator.play();

        centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setOpacity(0.6);
        centerBox.getChildren().addAll(zoneIcon, successIcon, mainLabel);

        progressBox = new VBox(25);
        progressBox.setAlignment(Pos.CENTER);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(260);
        progressBar.getStyleClass().add("drop-zone-progress-bar");
        VBox.setMargin(progressBar, new Insets(15, 0, 0, 0));

        timeLabel = new Label("Начинается сжатие...");
        timeLabel.getStyleClass().add("drop-zone-subtitle");

        progressBox.getChildren().addAll(encodingIcon, progressBar, countLabel, timeLabel);

        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        getChildren().addAll(codecContainer, topSpacer, centerBox, progressBox, bottomSpacer, subLabel);

        zoneIcon.managedProperty().bind(zoneIcon.visibleProperty());
        successIcon.managedProperty().bind(successIcon.visibleProperty());
        centerBox.managedProperty().bind(centerBox.visibleProperty());
        progressBox.managedProperty().bind(progressBox.visibleProperty());
        codecSelector.managedProperty().bind(codecSelector.visibleProperty());

        successIcon.setVisible(false);
        progressBox.setVisible(false);
        centerBox.setVisible(true);
        codecSelector.setVisible(true);

        setupEvents();
    }

    private void toggleVisibility(boolean visible, Node... nodes) {
        for (Node node : nodes) {
            node.setVisible(visible);
        }
    }

    public void showEncodingProgress(){
        Platform.runLater(() -> {
            toggleVisibility(false, centerBox);
            progressBar.setProgress(0);
            timeLabel.setText("Анализ видео...");
            toggleVisibility(true, progressBox);
        });
    }

    public void updateProgress(double progress, String timeRemaining){
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            timeLabel.setText(timeRemaining);
        });
    }

    public void updateCountLabel(int current, int total) {
        Platform.runLater(() -> countLabel.setText("Сжатие " + current + " / " + total));
    }

    public void showSuccess(){
        Platform.runLater(() -> {
            PROCESSING_LOCK = false;
            toggleVisibility(false, progressBox, zoneIcon);
            SoundManager.playNotification();
            mainLabel.setText("Сжатие завершено");
            timer = new PauseTransition(Duration.seconds(5));
            timer.setOnFinished(event1 -> resetToIdleState());
            timer.play();

            toggleVisibility(true, successIcon, centerBox);
        });
    }

    private void resetToIdleState(){
        Platform.runLater(() -> {
            gradientAnimator.setRate(1.0);
            getStyleClass().remove("drop-zone-hover");
            if (!getStyleClass().contains("drop-zone-normal")) {
                getStyleClass().add("drop-zone-normal");
            }

            mainLabel.getStyleClass().remove("drop-zone-unsupported-file");
            mainLabel.getStyleClass().remove("drop-zone-supported-file");
            mainLabel.setText(MAIN_LABEL);

            centerBox.setOpacity(0.6);
            codecSelector.setDisable(false);
            progressBar.setProgress(0);

            toggleVisibility(true, zoneIcon, centerBox, codecSelector);
            toggleVisibility(false, successIcon, progressBox);
        });
    }

    private void setupEvents(){
        setOnDragOver(event -> {
            if(!PROCESSING_LOCK) {
                if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                gradientAnimator.setRate(2.0);
                event.consume();
            }
        });

        setOnDragEntered(event -> {
            if(!PROCESSING_LOCK) {
                if (event.getDragboard().hasFiles()) {
                    animateOpacity(1.0);
                    getStyleClass().remove("drop-zone-normal");
                    if (!getStyleClass().contains("drop-zone-hover")) {
                        getStyleClass().add("drop-zone-hover");
                    }
                }
                event.consume();
            }
        });

        setOnDragExited(event -> {
            if(!PROCESSING_LOCK){
                animateOpacity(0.6);
                getStyleClass().remove("drop-zone-hover");
                if (!getStyleClass().contains("drop-zone-normal")) {
                    getStyleClass().add("drop-zone-normal");
                }
                gradientAnimator.setRate(1.0);
                event.consume();
            }
        });

        setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            animateOpacity(0.6);
            gradientAnimator.setRate(1.0);
            getStyleClass().remove("drop-zone-hover");
            if (!getStyleClass().contains("drop-zone-normal")) {
                getStyleClass().add("drop-zone-normal");
            }

            if (dragboard.hasFiles() && !PROCESSING_LOCK) {
                if(timer != null){
                    timer.stop();
                }

                fileQueue = new LinkedList<>(dragboard.getFiles());
                int totalFileCount = fileQueue.size();
                String path = fileQueue.element().getAbsolutePath();

                if (path.endsWith(".mp4") || path.endsWith(".mkv") || path.endsWith(".avi")) {
                    this.selectedFilePath = path;
                    PROCESSING_LOCK = true;

                    codecSelector.setDisable(true);
                    toggleVisibility(false, codecSelector);

                    ProcessFile.processDroppedFile(fileQueue, this, 1, totalFileCount,codecSelector.getValue());

                    mainLabel.getStyleClass().remove("drop-zone-unsupported-file");
                    if (!mainLabel.getStyleClass().contains("drop-zone-supported-file")) {
                        mainLabel.getStyleClass().add("drop-zone-supported-file");
                    }
                    mainLabel.setText("Файл получен: " + fileQueue.element().getName());
                    Logger.log("Путь сохранён в DropZone: " + selectedFilePath);
                    success = true;
                } else {
                    mainLabel.getStyleClass().remove("drop-zone-supported-file");
                    if (!mainLabel.getStyleClass().contains("drop-zone-unsupported-file")) {
                        mainLabel.getStyleClass().add("drop-zone-unsupported-file");
                    }
                    mainLabel.setText("Формат файла не поддерживается");
                    success = false;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void animateOpacity(double targetOpacity) {
        if(fadeTransition != null){
            fadeTransition.stop();
        }
        fadeTransition = new FadeTransition(Duration.millis(250), centerBox);
        fadeTransition.setFromValue(centerBox.getOpacity());
        fadeTransition.setToValue(targetOpacity);
        fadeTransition.play();
    }
}