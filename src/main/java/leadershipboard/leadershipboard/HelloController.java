package leadershipboard.leadershipboard;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.InputStream;

public class HelloController {

    @FXML
    private ImageView logoImage;

    @FXML
    private TableView<Players> leaderboardTable;

    @FXML
    private TableColumn<Players, String> usernameColumn;

    @FXML
    private TableColumn<Players, String> achievementColumn;

    @FXML
    public void initialize() {
        System.out.println("initialize() called");

        // Loading the Bugco logo
        if (logoImage.getImage() == null) {
            InputStream is = getClass().getResourceAsStream("/leadershipboard/leadershipboard/logo.png");
            System.out.println("InputStream is " + (is == null ? "null" : "NOT null"));

            if (is != null) {
                logoImage.setImage(new Image(is));
                System.out.println("Image set successfully");
            } else {
                System.out.println("logo.png not found!");
            }
        }

        //Setting the leaderboard up
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        achievementColumn.setCellValueFactory(new PropertyValueFactory<>("achievement"));  // Fixed typo

        //Testing static data to ensure the leaderboard works correctly
        leaderboardTable.getItems().add(new Players("Alice", "Lock Picker"));
        leaderboardTable.getItems().add(new Players("Bob", "Dystopian Survivor"));
        leaderboardTable.getItems().add(new Players("Charlie", "Wasteland Nuke"));
    }
}

