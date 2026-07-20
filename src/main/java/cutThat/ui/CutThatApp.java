package cutThat.ui;

import cutThat.service.Logger;
import cutThat.service.ProcessFile;
import cutThat.service.TrayManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


import java.util.Objects;

import static javax.swing.SwingUtilities.invokeLater;

public class CutThatApp extends Application {
    private final String cssPath = getClass().getResource("/style.css").toExternalForm();

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        ProcessFile.detectHardware();
        Platform.setImplicitExit(false);
        TrayManager trayManager = new TrayManager(primaryStage);
        invokeLater(trayManager::setupTray);

        BorderPane mainBorder = new BorderPane();
        mainBorder.getStyleClass().add("main-window");
        CustomTileBar titleBar = new CustomTileBar(primaryStage, trayManager);


        DropZone dropZone = new DropZone();
        BorderPane.setMargin(dropZone, new Insets(20, 20, 20, 20));
        mainBorder.setTop(titleBar);
        mainBorder.setCenter(dropZone);

        Scene scene = new Scene(mainBorder, 500, 400);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(cssPath);


        try {
            Image appIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/mainIcon.png")));
            primaryStage.getIcons().add(appIcon);
        } catch (Exception e) {
            Logger.error("Не удалось загрузить иконку приложения", e);
        }
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("CutThat");
        primaryStage.show();

    }
}
