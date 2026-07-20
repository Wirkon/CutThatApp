package cutThat.ui;


import cutThat.service.WindowDragger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import cutThat.service.TrayManager;


import static cutThat.service.IconFactory.createIcon;


public class CustomTileBar extends HBox {
    private double xOffset = 0;
    private double yOffset = 0;

    public CustomTileBar(Stage stage, TrayManager trayManager) {
        setAlignment(Pos.CENTER_RIGHT);
        setPadding(new Insets(10,15,10,15));
        this.getStyleClass().add("custom-title-bar");
        Label appTitle = new Label("CutThat");
        appTitle.getStyleClass().add("title-bar-text");


        Button btnMinimize = new Button();
        btnMinimize.setGraphic(createIcon("/icons/minimize.png",16));
        styleButton(btnMinimize,"#94a3b8");
        btnMinimize.setOnAction(e -> WindowAnimator.minimize(stage));

        Button btnClose = new Button();
        btnClose.setGraphic(createIcon("/icons/close.png",16));
        styleButton(btnClose, "#ef4444");
        btnClose.setOnAction(e -> {
            WindowAnimator.minimizeToTray(stage);
            trayManager.showNotification("Приложение свернуто","Я в трее");
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(appTitle,spacer, btnMinimize, btnClose);


        WindowDragger.makeDraggable(stage, this);
    }

    private void styleButton(Button button, String color) {
            button.getStyleClass().add("title-bar-button");
            button.setStyle("-btn-hover-color: " + color + ";");
          }

}
