//package com.example.cafemanagementsystemgit.Controllers;
//
//public class LoginController {
//}

package com.example.cafemanagementsystemgit.Controllers;

import com.cafe.Main;
import com.cafe.database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    protected void onLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (authenticate(username, password)) {
            try {
                // Load the new FXML file for the order page
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("/com/cafe/views/order.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Cafe Management System - Order");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid login!");
            // You can add a pop-up alert for invalid login
        }
    }

    private boolean authenticate(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // Note: In a real app, you would use hashed passwords
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true if a user is found
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
