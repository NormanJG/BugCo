module com.cab302.bugco {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cab302.bugco to javafx.fxml;
    exports com.cab302.bugco;
}