package com.example.cafeshopmanagement.Controller;

import com.example.cafeshopmanagement.App;
import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.CustomerModel;
import com.example.cafeshopmanagement.Model.ProductData;
import com.example.cafeshopmanagement.Model.UserDetail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;
import java.text.SimpleDateFormat;

public class MenuController implements Initializable {
    public ScrollPane menu_scroll_pane;
    public GridPane menu_grid_pane;
    public TableView<CustomerModel> menu_table_view;
    public TableColumn<CustomerModel, String> menu_product_name;
    public TableColumn<CustomerModel, String> menu_price;
    public TableColumn<CustomerModel, String> menu_quantity;
    public Label menu_total;
    public TextField menu_amount_textfield;
    public Label menu_change;
    public Button menu_pay_btn;
    public Button menu_remove_btn;
    public Button menu_receipt_btn;
    private Alert alert;
    private final ObservableList<ProductData> cardListData = FXCollections.observableArrayList();
    private int customerID;
    private double totalPrice = 0.0;
    private double change = 0.0;
    private double amount = 0.0;

    // refresh the menu
    public void refreshMenu() {
        menuDisplayCard();
        menuShowData();
    }

    public ObservableList<ProductData> menuGetData() {
        ObservableList<ProductData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Product";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ProductData productData = new ProductData(
                        rs.getInt("id"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("type"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("image"),
                        rs.getString("date")
                );
                listData.add(productData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listData;
    }

    public void menuDisplayCard() {
        cardListData.clear();
        cardListData.addAll(menuGetData());
        int row = 0;
        int column = 0;
        menu_grid_pane.getChildren().clear();
        for (ProductData cardListDatum : cardListData) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("FXML/CardProduct.fxml"));
                AnchorPane anchorPane = fxmlLoader.load();
                CardProductController cardProductController = fxmlLoader.getController();
                cardProductController.setData(cardListDatum, this);
                if (column == 3) {
                    column = 0;
                    row += 1;
                }
                menu_grid_pane.add(anchorPane, column++, row);
                GridPane.setMargin(anchorPane, new Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void menuShowData() {
        ObservableList<CustomerModel> menuListData = menuDisplayOrder();
        menu_product_name.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        menu_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        menu_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        menu_table_view.setItems(menuListData);
        menuGetTotal();
    }

    public void menuGetTotal() {
        String total = "SELECT SUM(price) FROM Customer WHERE em_username = ?";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(total)) {
            pst.setString(1, UserDetail.getUsername());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                totalPrice = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        menu_total.setText("$" + String.format("%.2f", totalPrice));
    }

    public void menuAmount() {
        if (menu_amount_textfield.getText().isEmpty() || totalPrice == 0.0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid: Please enter an amount.");
        } else {
            try {
                amount = Double.parseDouble(menu_amount_textfield.getText());
                if (amount < totalPrice) {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid: Insufficient amount.");
                    menu_amount_textfield.setText("");
                } else {
                    change = (amount - totalPrice);
                    menu_change.setText("$" + String.format("%.2f", change));
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid: Please enter a valid number.");
                menu_amount_textfield.setText("");
            }
        }
    }

    public void menuPayBtn() {
        // This method now runs to ensure amount is updated.
        if (menu_amount_textfield.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please enter a value for Amount.");
            return;
        }

        try {
            amount = Double.parseDouble(menu_amount_textfield.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid amount format. Please enter a number.");
            return;
        }

        if (totalPrice == 0.0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please choose your order first!");
            return;
        }
        if (amount < totalPrice) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid amount. Insufficient funds.");
            return;
        }

        String insertPay = "INSERT INTO Receipt (customer_id, total, date, em_username) VALUES(?,?,?,?)";
        String clearCustomer = "DELETE FROM Customer WHERE customer_id = ?";

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation Message");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Are you sure?");
        Optional<ButtonType> optional = confirmationAlert.showAndWait();

        if (optional.isPresent() && optional.get().equals(ButtonType.OK)) {
            try (Connection conn = Database.connectionDB()) {
                conn.setAutoCommit(false); // Start transaction

                try (PreparedStatement insertStmt = conn.prepareStatement(insertPay);
                     PreparedStatement deleteStmt = conn.prepareStatement(clearCustomer)) {

                    // Use SimpleDateFormat to format the date as a String without timezone
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = sdf.format(new Date());

                    insertStmt.setString(1, String.valueOf(UserDetail.getCustomerID()));
                    insertStmt.setDouble(2, totalPrice);
                    insertStmt.setString(3, formattedDate);
                    insertStmt.setString(4, UserDetail.getUsername());
                    insertStmt.executeUpdate();

                    deleteStmt.setString(1, String.valueOf(UserDetail.getCustomerID()));
                    deleteStmt.executeUpdate();

                    conn.commit(); // Commit transaction

                    showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successful.");
                    menuReceiptBtn();
                    menuRestart();
                    menuShowData();

                } catch (SQLException e) {
                    conn.rollback(); // Rollback transaction on error
                    e.printStackTrace();
                    showAlert(Alert.AlertType.WARNING, "Information Message", "An error occurred. Transaction rolled back.");
                } finally {
                    conn.setAutoCommit(true); // Restore default
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Menu restart
    public void menuRestart() {
        totalPrice = 0.0;
        change = 0.0;
        amount = 0.0;
        menu_amount_textfield.setText("");
        menu_total.setText("$0.0");
        menu_change.setText("$0.0");
        menuShowData();
    }

    // Display menu that order
    public ObservableList<CustomerModel> menuDisplayOrder() {
        getCustomerID();
        ObservableList<CustomerModel> listData = FXCollections.observableArrayList();
        String sql = "SELECT c.id, c.customer_id, c.product_id, c.product_name, p.type, c.quantity, c.price, c.date, c.em_username FROM Customer c JOIN Product p ON c.product_id = p.product_id WHERE c.customer_id = ?";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, String.valueOf(UserDetail.getCustomerID()));
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                CustomerModel customerModel = new CustomerModel(
                        rs.getInt("id"),
                        rs.getString("customer_id"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("type"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getString("date"),
                        rs.getString("em_username")
                );
                listData.add(customerModel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listData;
    }

    public void getCustomerID() {
        String sqlCustomer = "SELECT MAX(customer_id) FROM Customer";
        String sqlReceipt = "SELECT MAX(customer_id) FROM Receipt";

        try (Connection conn = Database.connectionDB()) {
            try (PreparedStatement pstCustomer = conn.prepareStatement(sqlCustomer);
                 ResultSet rsCustomer = pstCustomer.executeQuery()) {
                if (rsCustomer.next()) {
                    customerID = rsCustomer.getInt(1);
                }
            }

            try (PreparedStatement pstReceipt = conn.prepareStatement(sqlReceipt);
                 ResultSet rsReceipt = pstReceipt.executeQuery()) {
                int checkID = 0;
                if (rsReceipt.next()) {
                    checkID = rsReceipt.getInt(1);
                }

                if (customerID == checkID) {
                    customerID += 1;
                } else if (customerID == 0) {
                    customerID = checkID + 1;
                }

                UserDetail.setCustomerID(customerID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void menuReceiptBtn() {
        if (totalPrice > 0) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("FXML/Receipt.fxml"));
                Parent root = fxmlLoader.load();
                ReceiptController receiptController = fxmlLoader.getController();
                receiptController.setReceiptData(customerID, totalPrice);

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setTitle("Receipt");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Cannot generate receipt without an order.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        menu_pay_btn.setOnAction(event -> menuPayBtn());
        menu_receipt_btn.setOnAction(event -> menuReceiptBtn());
        menu_amount_textfield.setOnAction(event -> menuAmount());

        refreshMenu();
    }
}