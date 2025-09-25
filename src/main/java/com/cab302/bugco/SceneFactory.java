package com.cab302.bugco;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.Objects;

// load fxml and apply gameplay css
public final class SceneFactory {
    private SceneFactory() {}

    public static Scene make(String fxmlName) throws Exception {
        URL theUrl = Objects.requireNonNull(SceneFactory.class.getResource(fxmlName),
                "FXML NOT FOUND: " + fxmlName);
        Parent theRoot = FXMLLoader.load(theUrl);

        Scene theScene = new Scene(theRoot, HomeApplication.WIDTH, HomeApplication.HEIGHT);

        String css = Objects.requireNonNull(SceneFactory.class.getResource("gameplay.css"))
                .toExternalForm();
        theScene.getStylesheets().setAll(css);

        theScene.setFill(Color.BLACK);
        return theScene;
    }
}
