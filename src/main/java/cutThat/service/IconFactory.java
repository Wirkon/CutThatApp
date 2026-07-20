package cutThat.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import javax.imageio.ImageIO;

public class IconFactory {
    private static final Map<String, Image> imageCache = new HashMap<>();
    private static final Map<String, java.awt.Image> awtCache = new HashMap<>();

    public static ImageView createIcon(String resourcePath, int size) {
        Image image = imageCache.computeIfAbsent(resourcePath, path ->
                new Image(Objects.requireNonNull(IconFactory.class.getResourceAsStream(path)))
        );

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public static java.awt.Image createAwtImage(String resourcePath) {
        return awtCache.computeIfAbsent(resourcePath, path -> {
            try {
                return ImageIO.read(Objects.requireNonNull(IconFactory.class.getResourceAsStream(path)));
            } catch (Exception e) {
                throw new RuntimeException("Не удалось загрузить AWT-иконку для трея: " + path, e);
            }
        });
    }
}