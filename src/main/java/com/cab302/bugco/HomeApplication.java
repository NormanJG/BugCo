package com.cab302.bugco;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.util.Objects;

public class HomeApplication extends Application {
    // Constants defining the window title and size
    public static final String TITLE = "BugCo Industries ™ Bug Finder Tool";
    public static final int WIDTH = 1200;
    public static final int HEIGHT = 720;
    private MediaPlayer backgroundPlayer;

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
        try {
            String url = Objects.requireNonNull(getClass().getResource("music.wav")).toExternalForm();
            backgroundPlayer = new MediaPlayer(new Media(url));
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.setVolume(0.35);
            backgroundPlayer.play();
        } catch (Exception e) {
            e.printStackTrace(); // If file missing/unsupported
        }
        final AudioClip click = new AudioClip(
                Objects.requireNonNull(getClass().getResource("click.mp3")).toExternalForm()
        );
        click.setVolume(0.3); //

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            // only left clicks
            if (e.isPrimaryButtonDown()) {
                click.play();
            }
        });
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}