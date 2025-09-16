package leadershipboard.leadershipboard;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class HelloController {


    private List<Players> players = new ArrayList<>();

    @FXML
    private ImageView logoImage;

    @FXML
    private TableView<Players> leaderboardTable;

    @FXML
    private TableColumn<Players, String> usernameColumn;

    @FXML
    private TableColumn<Players, String> achievementColumn;




    public List<Players> getPlayers() {
        return new ArrayList<>(players);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public void addPlayer(String username, String achievement) {
        if (username == null || username.isEmpty()) throw new IllegalArgumentException();


        for (Players p : players) {
            if (p.getUsername().equals(username)) {
                p.setAchievement(achievement);
                return;
            }
        }


        players.add(new Players(username, achievement));
    }


    @FXML
    public void initialize() {
        // Setup TableView columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        achievementColumn.setCellValueFactory(new PropertyValueFactory<>("achievement"));


        addPlayer("Alice", "Lock Picker");
        addPlayer("Bob", "Dystopian Survivor");
        addPlayer("Charlie", "Wasteland Nuke");

        leaderboardTable.getItems().addAll(getPlayers());

        System.out.println(getClass().getResource("/leadershipboard/leadershipboard/logo.png"));



        try {
            Image img = new Image(HelloController.class.getResourceAsStream("/leadershipboard/leadershipboard/logo.png"));
            if (img.isError()) {
                System.out.println("Error loading logo image!");
            }
            logoImage.setImage(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}




