
package com.example.cafemanagementsystemgit.Controllers;

import com.cafe.database.DBConnection;
import com.cafe.models.MenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MenuController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    private File selectedImageFile;

    @FXML
    public void onAddProduct() {
        String name = nameField.getText();
        double price = Double.parseDouble(priceField.getText());
        String imagePath = selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null;

        // Save to database
        String query = "INSERT INTO menu_items (name, price, image_path) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, price);
            pstmt.setString(3, imagePath);
            pstmt.executeUpdate();
            System.out.println("Product added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            System.out.println("Image selected: " + selectedImageFile.getName());
        }
    }
}
