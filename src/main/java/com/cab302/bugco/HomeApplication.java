package com.cab302.bugco;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.Objects;

public class HomeApplication extends Application {
    // Constants defining the window title and size
    public static final String TITLE = "BugCo Industries ™ Bug Finder Tool";
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HomeApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm()
        );
        String iconUrl = Objects.requireNonNull(getClass().getResource("image.png")).toExternalForm();
        stage.getIcons().addAll(
                new Image(iconUrl, 16, 16, true, true),
                new Image(iconUrl, 32, 32, true, true),
                new Image(iconUrl, 48, 48, true, true),
                new Image(iconUrl, 128, 128, true, true),
                new Image(iconUrl, 256, 256, true, true)
        );
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}