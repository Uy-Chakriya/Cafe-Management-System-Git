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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

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
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private final ObservableList<ProductData> cardListData = FXCollections.observableArrayList();
    private int customerID;
    private ObservableList<CustomerModel> menuListData;
    private String totalPrice = "";
    private double change;
    private double amount;
    private double tPrice;

    public ObservableList<ProductData> menuGetData() {
        String sql = "SELECT * FROM Product";
        connection = Database.connectionDB();
        ObservableList<ProductData> listData = FXCollections.observableArrayList();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            ProductData productData;
            while (resultSet.next()) {
                productData = new ProductData(
                        resultSet.getInt("id"),
                        resultSet.getString("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("type"),
                        resultSet.getInt("stock"),
                        resultSet.getDouble("price"),
                        resultSet.getString("status"),
                        resultSet.getString("image"),
                        resultSet.getString("date")
                );
                listData.add(productData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return listData;
    }

    public void menuDisplayCard() {
        cardListData.clear();
        cardListData.addAll(menuGetData());
        int row = 0;
        int column = 0;
        menu_grid_pane.getRowConstraints().clear();
        menu_grid_pane.getColumnConstraints().clear();
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
                column++;
                menu_grid_pane.add(anchorPane, column, row);
                GridPane.setMargin(anchorPane, new Insets(10));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void menuShowData(){
        menuListData = menuDisplayOrder();
        menu_product_name.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        menu_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        menu_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        menu_table_view.setItems(menuListData);
    }

    public void menuGetTotal(){
        String total = "SELECT SUM(price) FROM Customer WHERE em_username = ?";
        connection = Database.connectionDB();
        String user = UserDetail.getUsername();
        try{
            preparedStatement = connection.prepareStatement(total);
            preparedStatement.setString(1, user);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                totalPrice = resultSet.getString("SUM(price)");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void menuDisplayTotal(){
        menuGetTotal();
        menu_total.setText("$" + totalPrice);
    }

    public void menuAmount() {
        menuGetTotal();
        if (menu_amount_textfield.getText().isEmpty() || totalPrice.equals("0")) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Invalid :3");
            alert.showAndWait();
        } else{
            amount = Double.parseDouble(menu_amount_textfield.getText());
            tPrice= Double.parseDouble(totalPrice);
            if (amount < tPrice) {
                menu_amount_textfield.setText("");
            } else {
                change = (amount - tPrice);
                menu_change.setText("$" + change);
            }
        }
    }

    public void menuPayBtn() {
        if (tPrice == 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please choose your order first!");
            alert.showAndWait();
        } else {
            String insertPay = "INSERT INTO Receipt (customer_id, total, date, em_username) VALUES(?,?,?,?)";
            connection = Database.connectionDB();
            try {
                if (amount == 0) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid :2");
                    alert.showAndWait();
                }
                Date date = new Date();
                java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure?");
                Optional<ButtonType> optional = alert.showAndWait();
                if (optional.get().equals(ButtonType.OK)) {
                    preparedStatement = connection.prepareStatement(insertPay);
                    preparedStatement.setString(1, String.valueOf(customerID));
                    preparedStatement.setString(2, String.valueOf(tPrice));
                    preparedStatement.setString(3, String.valueOf(sqlDate));
                    preparedStatement.setString(4, UserDetail.getUsername());
                    preparedStatement.executeUpdate();
                    menuShowData();
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successful.");
                    alert.showAndWait();
                    menuShowData();
                    menuRestart();
                } else {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occur.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void menuRestart(){
        tPrice = 0;
        change = 0;
        amount = 0;
        menu_amount_textfield.setText("$0.0");
        menu_total.setText("");
        menu_change.setText("$0.0");
    }

    public ObservableList<CustomerModel> menuDisplayOrder() {
        getCustomerID();
        ObservableList<CustomerModel> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";
        connection = Database.connectionDB();
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, String.valueOf(customerID));
            resultSet = preparedStatement.executeQuery();
            CustomerModel customerModel;
            while(resultSet.next()) {
                // Corrected constructor call with 9 arguments and correct data types
                customerModel = new CustomerModel(
                        resultSet.getInt("id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("product_type"),
                        resultSet.getInt("quantity"),
                        resultSet.getDouble("price"),
                        resultSet.getString("date"),
                        resultSet.getString("em_username")
                        //test
                );
                listData.add(customerModel);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return listData;
    }

    public void getCustomerID(){
        String sql ="SELECT MAX(customer_id) FROM Customer";
        connection = Database.connectionDB();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                customerID = resultSet.getInt("MAX(customer_id)");
            }
            String checkCustomersID = "SELECT MAX(customer_id) FROM Receipt";
            preparedStatement = connection.prepareStatement(checkCustomersID);
            resultSet = preparedStatement.executeQuery();
            int checkID = 0;
            if(resultSet.next()){
                checkID = resultSet.getInt("MAX(customer_id)");
            }
            if(customerID == 0) {
                customerID+=1;
            } else if (customerID == checkID) {
                customerID += 1;
            }
            UserDetail.setCustomerID(customerID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        menuDisplayCard();
        menuDisplayOrder();
        menuDisplayTotal();
        menuShowData();
    }
}