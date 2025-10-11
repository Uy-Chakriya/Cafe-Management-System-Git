package com.example.cafeshopmanagement.Controller;
import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.UserDetail; // Import UserDetail
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
// Removed AreaChart import
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
    // Removed income_chart FXML field (AreaChart)
    @FXML
    public BarChart<String, Integer> customer_chart;
    @FXML
    public Label welcome_header;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public void setWelcomeMessage() {
        String user = UserDetail.getUsername();
        if (user != null && !user.isEmpty()) {
            user = user.substring(0, 1).toUpperCase() + user.substring(1);
        } else {
            user = "Guest";
        }
        welcome_header.setText("Welcome, " + user + "!");
    }

    // refresh Data
    public void refreshData() {
        setWelcomeMessage();
        showNumberOfCustomers();
        showTodayIncome();
        showTotalIncome();
        showSoldProducts();
        // Removed showIncomeChart() call
        showCustomerChart();
    }

    // Number of customer
    public void showNumberOfCustomers() {
        String sql = "SELECT COUNT(DISTINCT customer_id) FROM Receipt";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                number_of_customer_label.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //today income
    public void showTodayIncome() {
        String sql = "SELECT SUM(total) FROM Receipt WHERE date = ?";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    double todayIncome = rs.getDouble(1);
                    today_income_label.setText(String.format("$%.2f", todayIncome));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Total Income
    public void showTotalIncome() {
        String sql = "SELECT SUM(total) FROM Receipt";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                double totalIncome = rs.getDouble(1);
                total_income_label.setText(String.format("$%.2f", totalIncome));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Product sold
    public void showSoldProducts() {
        String sql = "SELECT SUM(quantity) FROM Customer";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                sold_product_label.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Show customer chart
    private void showCustomerChart() {
        customer_chart.getData().clear();
        String sql = "SELECT date, COUNT(DISTINCT customer_id) FROM Receipt GROUP BY date ORDER BY date ASC";
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("Daily Customers");
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString(1), rs.getInt(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        customer_chart.getData().add(series);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        refreshData();
    }
}