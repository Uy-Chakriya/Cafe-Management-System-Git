package com.example.cafeshopmanagement.Controller;
import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.ProductData;
import com.example.cafeshopmanagement.Model.UserDetail;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;
public class CardProductController implements Initializable {
    public Label card_product_name;
    public Label card_price;
    public ImageView card_imageview;
    public Spinner<Integer> card_spinner;
    public Button card_add_btn;
    private ProductData productData;
    private Image image;
    private int quantity;
    private String productID;
    private Alert alert;
    private String type;
    private double pr;
    private MenuController menuController;


    // In card has add button
    public void setData(ProductData productData, MenuController menuController) {
        this.productData = productData;
        this.menuController = menuController;
        type = productData.getType();
        productID = productData.getProductId();
        card_product_name.setText(productData.getProductName());
        card_price.setText("$" + String.valueOf(productData.getPrice()));
        String path = "File:" + productData.getImage();
        image = new Image(path, 200, 150, false, true);
        card_imageview.setImage(image);
        pr = productData.getPrice();
    }

    // Add button
    public void addBtn() {
        quantity = card_spinner.getValue();
        if (quantity == 0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please add quantity first.");
            return;
        }

        String checkStockSql = "SELECT stock FROM Product WHERE product_id = ?";
        String updateStockSql = "UPDATE Product SET stock = ? WHERE product_id = ?";
        String insertCustomerSql = "INSERT INTO Customer (customer_id, product_id, product_name, product_type, quantity, price, date, em_username) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = Database.connectionDB()) {
            conn.setAutoCommit(false); // Start transaction

            // Check stock
            try (PreparedStatement checkStockPst = conn.prepareStatement(checkStockSql)) {
                checkStockPst.setString(1, productID);
                try (ResultSet rs = checkStockPst.executeQuery()) {
                    if (rs.next()) {
                        int currentStock = rs.getInt("stock");
                        if (currentStock < quantity) {
                            showAlert(Alert.AlertType.ERROR, "Error Message", "Invalid. This product is out of stock.");
                            return;
                        }
                    }
                }
            }

            // Insert into Customer table
            try (PreparedStatement insertPst = conn.prepareStatement(insertCustomerSql)) {
                insertPst.setInt(1, UserDetail.getCustomerID());
                insertPst.setString(2, productID);
                insertPst.setString(3, card_product_name.getText());
                insertPst.setString(4, type);
                insertPst.setInt(5, quantity);
                insertPst.setDouble(6, quantity * pr);
                insertPst.setDate(7, new java.sql.Date(new Date().getTime()));
                insertPst.setString(8, UserDetail.getUsername());
                insertPst.executeUpdate();
            }

            // Update stock
            try (PreparedStatement updatePst = conn.prepareStatement(updateStockSql)) {
                updatePst.setInt(1, productData.getStock() - quantity);
                updatePst.setString(2, productID);
                updatePst.executeUpdate();
            }

            conn.commit(); // Commit transaction
            showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Added!");
            menuController.menuShowData(); // Use the injected controller to refresh the view
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Message", "An error occurred. Transaction rolled back.");
        }
    }

    // Show alert
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        card_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        card_add_btn.setOnAction(event -> addBtn());
    }
}