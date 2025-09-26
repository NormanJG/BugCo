package com.cab302.bugco;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.net.URL;
import java.util.Objects;

public final class SceneFactory {
    private SceneFactory() {}

    // Loads an FXML in this package, applies styles.css, and uses app size.
    public static Scene loadScene(String fxmlName) throws Exception {
        URL url = Objects.requireNonNull(SceneFactory.class.getResource(fxmlName),
                "FXML not found: " + fxmlName);
        Parent root = FXMLLoader.load(url);

        Scene scene = new Scene(root);
        String css = Objects.requireNonNull(SceneFactory.class.getResource("styles.css"))
                .toExternalForm();
        scene.getStylesheets().add(css);
        return scene;
    }
}
