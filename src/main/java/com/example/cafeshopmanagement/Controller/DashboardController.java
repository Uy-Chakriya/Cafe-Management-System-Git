package com.example.cafeshopmanagement.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    public Label number_of_customer_label;
    @FXML
    public Label today_income_label;
    @FXML
    public Label total_income_label;
    @FXML
    public Label sold_product_label;
    @FXML
    public AreaChart<?, ?> income_chart;
    @FXML
    public BarChart<?, ?> customer_chart;

    // The methods to populate the dashboard data would go here
    // For now, they are empty as your original code didn't have the implementation.

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialization logic for the dashboard, e.g., fetching data from DB
        //test
    }
}