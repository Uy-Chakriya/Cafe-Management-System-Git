module com.example.cafemanagementsystemgit {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.cafemanagementsystemgit to javafx.fxml;
    exports com.example.cafemanagementsystemgit;
}