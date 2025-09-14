package leadershipboard.leadershipboard;

import javafx.fxml.FXML;
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

    public void addPlayer(String username, String achievement) {
        if (username == null || username.isEmpty()) throw new IllegalArgumentException();
        players.add(new Players(username, achievement));
    }

    public List<Players> getPlayers() {
        return new ArrayList<>(players); // return a copy
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    @FXML
    public void initialize() {
        // JavaFX UI setup
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        achievementColumn.setCellValueFactory(new PropertyValueFactory<>("achievement"));

        // Sample static data
        addPlayer("Alice", "Lock Picker");
        addPlayer("Bob", "Dystopian Survivor");
        addPlayer("Charlie", "Wasteland Nuke");

        // Display data in TableView
        leaderboardTable.getItems().addAll(getPlayers());
    }
}
