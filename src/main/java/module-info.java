module com.example.cafemanagementsystemgit {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.compiler;
    requires java.sql;


    opens com.example.cafemanagementsystemgit to javafx.fxml;
    exports com.example.cafemanagementsystemgit;
}