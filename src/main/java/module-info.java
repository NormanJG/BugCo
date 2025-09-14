module com.cab302.bugco {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires jbcrypt;


    opens com.cab302.bugco to javafx.fxml;
    exports com.cab302.bugco;
}