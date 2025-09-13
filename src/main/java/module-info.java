module com.example.cafeshopmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires de.jensd.fx.glyphs.fontawesome;
    requires java.sql;
    requires  org.xerial.sqlitejdbc;

    opens com.example.cafeshopmanagement to javafx.fxml;
    exports com.example.cafeshopmanagement;
    exports com.example.cafeshopmanagement.Controller;
    exports com.example.cafeshopmanagement.Database;
    exports com.example.cafeshopmanagement.Model;
    opens com.example.cafeshopmanagement.Model to javafx.base;
}