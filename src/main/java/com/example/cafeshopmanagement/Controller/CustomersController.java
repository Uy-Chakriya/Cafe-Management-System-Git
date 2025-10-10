package com.example.cafeshopmanagement.Controller;

import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.Receipt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CustomersController implements Initializable {
    @FXML
    public TableView<Receipt> customer_tableview;
    @FXML
    public TableColumn<Receipt, String> customer_id_col;
    @FXML
    public TableColumn<Receipt, Double> customer_total_col;
    @FXML
    public TableColumn<Receipt, String> customer_date_col;
    @FXML
    public TableColumn<Receipt, String> customer_cashier_col;


    // Get reciept
    public ObservableList<Receipt> getReceiptData() {
        ObservableList<Receipt> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Receipt";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Receipt receipt = new Receipt(
                        rs.getInt("id"),
                        rs.getString("customer_id"),
                        rs.getDouble("total"),
                        rs.getString("date"),
                        rs.getString("em_username")
                );
                listData.add(receipt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listData;
    }

    // show Customer
    public void showCustomerData() {
        ObservableList<Receipt> receiptList = getReceiptData();
        customer_id_col.setCellValueFactory(new PropertyValueFactory<>("customer_id"));
        customer_total_col.setCellValueFactory(new PropertyValueFactory<>("total"));
        customer_date_col.setCellValueFactory(new PropertyValueFactory<>("date"));
        customer_cashier_col.setCellValueFactory(new PropertyValueFactory<>("em_username"));
        customer_tableview.setItems(receiptList);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        showCustomerData();
    }
}