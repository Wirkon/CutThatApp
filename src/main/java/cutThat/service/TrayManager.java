package cutThat.service;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;

import static cutThat.service.IconFactory.createAwtImage;

public class TrayManager {
    private final Stage stage;
    private TrayIcon trayIcon;

    public TrayManager(Stage stage) {
        this.stage = stage;
    }

    public void setupTray(){
        if(!SystemTray.isSupported()) return;

        try{
            SystemTray tray = SystemTray.getSystemTray();
            PopupMenu popup = getPopupMenu(tray);

            trayIcon = new TrayIcon(createAwtImage("/icons/tray.png"),"CutThat", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> Platform.runLater(this::restoreStage));

            tray.add(trayIcon);
        }catch (AWTException e){
            System.err.println("Ошибка трея" + e.getMessage());
        }
    }

    private PopupMenu getPopupMenu(SystemTray tray) {
        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem("Открыть");
        openItem.addActionListener(e-> Platform.runLater(this::restoreStage));

        MenuItem exitItem = new MenuItem("Закрыть");
        exitItem.addActionListener(e->Platform.runLater(()->{
            if(trayIcon != null) tray.remove(trayIcon);
            Platform.exit();
            System.exit(0);
        }));

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        return popup;
    }


    public void restoreStage(){
        if(stage != null){
            stage.show();
            stage.toFront();
            stage.setIconified(false);
        }
    }


    public void showNotification(String title, String message){
        if(trayIcon != null){
            trayIcon.displayMessage(title,message,TrayIcon.MessageType.INFO);
        }
    }

}
