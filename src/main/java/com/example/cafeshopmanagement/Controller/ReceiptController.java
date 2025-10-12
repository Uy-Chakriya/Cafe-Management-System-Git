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
        receipt_total.setText(String.format("%.2f", total)); // Format total as currency
        showReceiptTable(customerID);
    }
    private void showReceiptTable(int customerID) {
        ObservableList<CustomerModel> receiptList = FXCollections.observableArrayList();
        // Query the permanent ReceiptItem table
        String sql = "SELECT product_name, product_type, quantity, price FROM ReceiptItem WHERE receipt_id = ?";
        connection = Database.connectionDB();
        try {
            preparedStatement = connection.prepareStatement(sql);
            // Use setString for the TEXT column in the database
            preparedStatement.setString(1, String.valueOf(customerID));
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                CustomerModel customer = new CustomerModel(
                        0,
                        null,
                        null,
                        resultSet.getString("product_name"),
                        resultSet.getString("product_type"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("price"),
                        null,
                        null
                );
                receiptList.add(customer);
            }

            receipt_product_name.setCellValueFactory(new PropertyValueFactory<>("product_name"));
            receipt_product_type.setCellValueFactory(new PropertyValueFactory<>("product_type"));
            receipt_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            receipt_price.setCellValueFactory(new PropertyValueFactory<>("price"));
            receipt_tableview.setItems(receiptList);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Closing resources
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}