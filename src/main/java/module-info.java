module leadershipboard.leadershipboard {
    requires javafx.controls;
    requires javafx.fxml;


    opens leadershipboard.leadershipboard to javafx.fxml;
    exports leadershipboard.leadershipboard;
}