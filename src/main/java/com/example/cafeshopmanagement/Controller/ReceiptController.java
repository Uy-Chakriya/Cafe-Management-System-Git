package com.example.cafeshopmanagement.Controller;

import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.CustomerModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReceiptController implements Initializable {
    public Label receipt_customer_id;
    public TableView<CustomerModel> receipt_tableview;
    public TableColumn<CustomerModel, String> receipt_product_name;
    public TableColumn<CustomerModel, String> receipt_product_type;
    public TableColumn<CustomerModel, String> receipt_quantity;
    public TableColumn<CustomerModel, String> receipt_price;
    public Label receipt_total;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public void setReceiptData(int customerID, double total) {
        receipt_customer_id.setText(String.valueOf(customerID));
        receipt_total.setText(String.valueOf(total));
        showReceiptTable(customerID);
    }

    private void showReceiptTable(int customerID) {
        ObservableList<CustomerModel> receiptList = FXCollections.observableArrayList();
        String sql = "SELECT c.product_name, p.type, c.quantity, c.price FROM Customer c JOIN Product p ON c.product_id = p.product_id WHERE c.customer_id = ?";
        connection = Database.connectionDB();

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, customerID);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerModel customer = new CustomerModel(
                        0, // id is not needed for the receipt view
                        null, // customer_id is not needed in the table view
                        null, // product_id not needed in table view
                        resultSet.getString("product_name"),
                        resultSet.getString("quantity"),
                        resultSet.getString("price"),
                        null, // date not needed
                        null  // em_username not needed
                );
                // The `CustomerModel` class does not have a field for `product_type`, so this will not display.
                // To fix this, you would need to add `private String product_type;` to `CustomerModel`
                // and a corresponding getter/setter.
                receiptList.add(customer);
            }

            receipt_product_name.setCellValueFactory(new PropertyValueFactory<>("product_name"));
            // The following line will not work with your current CustomerModel class.
            // You will need to modify the CustomerModel to include `product_type`.
            receipt_product_type.setCellValueFactory(new PropertyValueFactory<>("product_type"));
            receipt_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            receipt_price.setCellValueFactory(new PropertyValueFactory<>("price"));
            receipt_tableview.setItems(receiptList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}