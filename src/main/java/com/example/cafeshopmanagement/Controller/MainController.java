package com.example.cafeshopmanagement.Controller;
import com.example.cafeshopmanagement.App;
import com.example.cafeshopmanagement.Model.UserDetail;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    public Label username;
    @FXML
    public Button dashboard_button;
    @FXML
    public Button inventory_button;
    @FXML
    public Button menu_button;
    @FXML
    public Button customers_button;
    @FXML
    public Button log_out_button;
    @FXML
    public StackPane centralStackPane;

    private Alert alert;
    private DashboardController dashboardController;
    private InventoryController inventoryController;
    private MenuController menuController;
    private CustomersController customersController;
    private Parent dashboardView;
    private Parent inventoryView;
    private Parent menuView;
    private Parent customersView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getUsername();
        log_out_button.setOnAction(event -> logout());

        // Eagerly load all FXML files and their controllers to prevent errors on subsequent clicks
        try {
            FXMLLoader dashboardLoader = new FXMLLoader(App.class.getResource("FXML/Dashboard.fxml"));
            dashboardView = dashboardLoader.load();
            dashboardController = dashboardLoader.getController();

            FXMLLoader inventoryLoader = new FXMLLoader(App.class.getResource("FXML/Inventory.fxml"));
            inventoryView = inventoryLoader.load();
            inventoryController = inventoryLoader.getController();

            FXMLLoader menuLoader = new FXMLLoader(App.class.getResource("FXML/Menu.fxml"));
            menuView = menuLoader.load();
            menuController = menuLoader.getController();

            FXMLLoader customersLoader = new FXMLLoader(App.class.getResource("FXML/Customers.fxml"));
            customersView = customersLoader.load();
            customersController = customersLoader.getController();

            // Load the dashboard view by default
            centralStackPane.getChildren().setAll(dashboardView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Switch Form
    public void switchForm(ActionEvent event) {
        if (event.getSource() == dashboard_button) {
            centralStackPane.getChildren().setAll(dashboardView);
            dashboardController.refreshData(); // Add this method to DashboardController
        } else if (event.getSource() == inventory_button) {
            centralStackPane.getChildren().setAll(inventoryView);
            inventoryController.inventoryShowData(); // Ensure the table is refreshed
        } else if (event.getSource() == menu_button) {
            centralStackPane.getChildren().setAll(menuView);
            menuController.refreshMenu(); // New method to refresh menu content
        } else if (event.getSource() == customers_button) {
            centralStackPane.getChildren().setAll(customersView);
            customersController.showCustomerData(); // Ensure the table is refreshed
        }
    }

    public void logout() {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> optional = alert.showAndWait();
        if (optional.isPresent() && optional.get().equals(ButtonType.OK)) {
            Stage currentStage = (Stage) log_out_button.getScene().getWindow();
            currentStage.close();

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Login.fxml"));
                Stage mainStage = new Stage();
                Scene scene = new Scene(fxmlLoader.load());
                mainStage.setScene(scene);
                mainStage.setTitle("Cafe Shop Management");
                mainStage.setMinHeight(430);
                mainStage.setMinWidth(610);
                mainStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void getUsername() {
        String user = UserDetail.getUsername();
        if (user != null && !user.isEmpty()) {
            user = user.substring(0, 1).toUpperCase() + user.substring(1);
            username.setText(user);
        }
    }
}