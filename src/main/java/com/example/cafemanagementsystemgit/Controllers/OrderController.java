
package com.example.cafemanagementsystemgit.Controllers;

import com.cafe.models.MenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import java.util.ArrayList;
import java.util.List;

public class OrderController {
    @FXML private TableView<MenuItem> orderTableView;
    @FXML private TableColumn<MenuItem, String> itemColumn;
    @FXML private TableColumn<MenuItem, Double> priceColumn;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    @FXML private Label changeLabel;

    private ObservableList<MenuItem> orderItems = FXCollections.observableArrayList();

    public void initialize() {
        // Initialize the table columns
        itemColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());

        orderTableView.setItems(orderItems);
    }

    @FXML
    public void onAddItem(MenuItem item) {
        orderItems.add(item);
        calculateTotals();
    }

    @FXML
    public void onRemoveItem() {
        MenuItem selectedItem = orderTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            orderItems.remove(selectedItem);
            calculateTotals();
        }
    }

    @FXML
    public void onPay(double amountPaid) {
        double total = calculateTotal();
        double change = amountPaid - total;
        changeLabel.setText(String.format("$%.2f", change));
        // You would then save the order to the database here.
    }

    @FXML
    public void onGenerateReceipt() {
        // Logic to generate a receipt, maybe in a new window or as a PDF
        System.out.println("Generating receipt...");
    }

    private void calculateTotals() {
        double subtotal = orderItems.stream().mapToDouble(MenuItem::getPrice).sum();
        double total = subtotal * 1.0; // Example with no tax
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        totalLabel.setText(String.format("$%.2f", total));
    }

    private double calculateTotal() {
        return orderItems.stream().mapToDouble(MenuItem::getPrice).sum();
    }
}
