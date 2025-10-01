package com.cab302.bugco;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class HomeApplication extends Application {
    // Constant defining the window title
    public static final String TITLE = "BugCo Industries ™ Bug Finder Tool";

    @Override
    public void start(Stage stage) throws IOException {
        com.cab302.bugco.db.Database.init();
        FXMLLoader fxmlLoader = new FXMLLoader(HomeApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
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

        MusicService.init();
        MusicService.playLogin();

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
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.show();
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                event.consume();
                showExitConfirmation(stage);
            }
        });
    }

    private void showExitConfirmation(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setContentText("WOULD YOU LIKE TO CONFIRM EXIT?");

        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm()
        );
        dp.getStyleClass().addAll("confirm-exit");
        dp.setPrefSize(600, 400);

        Circle red   = new Circle(6);  red.getStyleClass().add("dot-red");
        Circle amber = new Circle(6);  amber.getStyleClass().add("dot-amber");
        Circle green = new Circle(6);  green.getStyleClass().add("dot-green");
        HBox dots = new HBox(6, red, amber, green);
        dots.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("EXIT TERMINAL");
        title.getStyleClass().add("panel-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, dots, title, spacer);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(8, 10, 8, 10));
        header.getStyleClass().add("panel-titlebar");

        dp.setHeader(header);

        Button okBtn = (Button) dp.lookupButton(ButtonType.OK);
        okBtn.setText("EXIT");
        okBtn.getStyleClass().addAll("term-button", "danger");

        Button cancelBtn = (Button) dp.lookupButton(ButtonType.CANCEL);
        cancelBtn.setText("STAY");
        cancelBtn.getStyleClass().addAll("primary-btn", "success");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                MusicService.dispose();
                stage.close();
            }
        });

    }
    public static void main(String[] args) {
        launch();
    }
}