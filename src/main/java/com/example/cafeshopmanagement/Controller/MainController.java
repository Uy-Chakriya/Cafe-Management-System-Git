package com.example.cafeshopmanagement.Controller;

import com.example.cafeshopmanagement.App;
import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.CustomerModel;
import com.example.cafeshopmanagement.Model.ProductData;
import com.example.cafeshopmanagement.Model.Receipt;
import com.example.cafeshopmanagement.Model.UserDetail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public Label username;
    public Button dashboard_button;
    public Button inventory_button;
    public Button menu_button;
    public Button customers_button;
    public Button log_out_button;
    public AnchorPane inventory_form;
    public TableView<ProductData> inventory_tableview;
    public TableColumn<ProductData, String> inventory_product_id;
    public TableColumn<ProductData, String> inventory_product_name;
    public TableColumn<ProductData, String> inventory_product_type;
    public TableColumn<ProductData, String> inventory_product_stock;
    public TableColumn<ProductData, String> inventory_product_price;
    public TableColumn<ProductData, String> inventory_product_status;
    public TableColumn<ProductData, String> inventory_product_date;
    public TextField product_id_textfield;
    public TextField product_name_textfield;
    public ComboBox<String> type_combobox;
    public TextField stock_textfield;
    public TextField price_textfield;
    public ImageView display_selected_image;
    public Button choose_image_button;
    public Button add_button;
    public Button update_button;
    public Button delete_button;
    public Button clear_button;
    public ComboBox<String> status_combobox;
    public AnchorPane main_form;
    public ScrollPane menu_scroll_pane;
    public GridPane menu_grid_pane;
    public AnchorPane menu_section;
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
    public AnchorPane dashboard_section;
    public AnchorPane customers_section;
    public TableView<Receipt> customer_tableview;
    public TableColumn<Receipt, String> customer_id_col;
    public TableColumn<Receipt, Double> customer_total_col;
    public TableColumn<Receipt, String> customer_date_col;
    public TableColumn<Receipt, String> customer_cashier_col;

    private Alert alert;

    private Image image;
    private String[] list = {
            "Meal",
            "Drinks",
    };
    ObservableList<String> typeList = FXCollections.observableArrayList(list);

    private String[] status = {
            "Available",
            "Unavailable"
    };


    //==>> connect to data base

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private final ObservableList<ProductData> cardListData = FXCollections.observableArrayList();

    LoginController loginController = new LoginController();

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
                cardProductController.setData(cardListDatum);

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

    private int customerID;

    private ObservableList<CustomerModel> menuListData;


    // ===>>  calculate all the price [== Menu ==]

    public void menuShowData(){
        menuListData = menuDisplayOrder();
        menu_product_name.setCellValueFactory(new PropertyValueFactory<>("product_name"));
        menu_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        menu_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        menu_table_view.setItems(menuListData);
    }

    private String totalPrice = "";
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
    private double change;
    private double amount;
    private double tPrice;
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
            }
            else{
                change = (amount - tPrice);
                menu_change.setText("$" + change);
            }
        }
    }

    public void menuReceiptBtn() {
        if (tPrice > 0) {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("FXML/Receipt.fxml"));
            try {
                Parent root = fxmlLoader.load();
                ReceiptController receiptController = fxmlLoader.getController();
                receiptController.setReceiptData(customerID, tPrice);

                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setTitle("Receipt");
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Cannot generate receipt without an order.");
            alert.showAndWait();
        }
    }

    public void menuPayBtn() {
        if (tPrice ==0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please choose your order first!");
            alert.showAndWait();
        } else {
            String insertPay = "INSERT INTO Receipt (customer_id, total, date, em_username) VALUES(?,?,?,?)";
            connection = Database.connectionDB();
            try{
                if (amount ==0) {
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

                    menuReceiptBtn();

                    menuShowData();
                    menuRestart();
                }else {
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
                customerModel = new CustomerModel(
                        resultSet.getInt("id"),
                        resultSet.getString("customer_id"),
                        resultSet.getString("product_id"),
                        resultSet.getString("product_name"),
                        resultSet.getString("quantity"),
                        resultSet.getString("price"),
                        resultSet.getString("date"),
                        resultSet.getString("em_username")
                ) ;
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
            }

            else if (customerID == checkID) {
                customerID += 1;
            }

            UserDetail.setCustomerID(customerID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void inventoryAddBtn() {
        if (product_id_textfield.getText().isEmpty()
                || product_name_textfield.getText().isEmpty()
                || stock_textfield.getText().isEmpty()
                || price_textfield.getText().isEmpty()
                || type_combobox.getSelectionModel().getSelectedItem() == null
                || status_combobox.getSelectionModel().getSelectedItem() == null
                || UserDetail.getPath() == null
        ) {

            loginController.fillAllFieldError();
        } else {
            String checkProductID = "SELECT product_id from Product WHERE product_id = ?";
            connection = Database.connectionDB();
            try {
                preparedStatement = connection.prepareStatement(checkProductID);
                preparedStatement.setString(1, product_id_textfield.getText());
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText(product_id_textfield.getText() + "already exist");
                } else {

                    String insertData = "INSERT INTO Product (product_id, product_name, type, stock, price, status, image, date) VALUES(?,?,?,?,?,?,?,?)";
                    Date date = new Date();
                    java.sql.Date _date = new java.sql.Date(date.getTime());
                    String path = UserDetail.getPath();
                    path = path.replace("\\", "\\\\");

                    preparedStatement = connection.prepareStatement(insertData);
                    preparedStatement.setString(1, product_id_textfield.getText());
                    preparedStatement.setString(2, product_name_textfield.getText());
                    preparedStatement.setString(3, type_combobox.getSelectionModel().getSelectedItem());
                    preparedStatement.setString(4, stock_textfield.getText());
                    preparedStatement.setString(5, price_textfield.getText());
                    preparedStatement.setString(6, status_combobox.getSelectionModel().getSelectedItem());
                    preparedStatement.setString(7, path);
                    preparedStatement.setString(8, String.valueOf(_date));
                    preparedStatement.executeUpdate();

                    inventoryShowData();
                    getSuccessAlert("Successfully Added!");
                    inventoryClearBtn();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void inventoryDeleteBtn() {
        if (product_id_textfield.getText().isEmpty()
                || product_name_textfield.getText().isEmpty()
                || stock_textfield.getText().isEmpty()
                || price_textfield.getText().isEmpty()
                || type_combobox.getSelectionModel().getSelectedItem() == null
                || status_combobox.getSelectionModel().getSelectedItem() == null
                || UserDetail.getPath() == null
                || UserDetail.getId() == 0
        ) {

            loginController.fillAllFieldError();
        } else {
            String deleteData = "DELETE FROM Product WHERE id = ?";
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Alert");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to Delete product with ID: " + product_id_textfield.getText() + "?");
            connection = Database.connectionDB();
            Optional<ButtonType> optional = alert.showAndWait();

            if (optional.get().equals(ButtonType.OK)) {
                try {
                    preparedStatement = connection.prepareStatement(deleteData);
                    preparedStatement.setString(1, String.valueOf(UserDetail.getId()));
                    preparedStatement.executeUpdate();

                    getSuccessAlert("Successfully Deleted!");

                    inventoryShowData();
                    inventoryClearBtn();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


            } else {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Cancelled");
                alert.showAndWait();
            }

        }
    }

    public void inventoryUpdateBtn() {
        if (product_id_textfield.getText().isEmpty()
                || product_name_textfield.getText().isEmpty()
                || stock_textfield.getText().isEmpty()
                || price_textfield.getText().isEmpty()
                || type_combobox.getSelectionModel().getSelectedItem() == null
                || status_combobox.getSelectionModel().getSelectedItem() == null
                || UserDetail.getPath() == null
                || UserDetail.getId() == 0
        ) {

            loginController.fillAllFieldError();
        } else {
            String path = UserDetail.getPath();
//            path = path.replace("\\", "\\\\");
            String updataData = "UPDATE Product SET product_id = ?, product_name=?, type=?, stock=?, price =?, status=?, image=?, date=? WHERE id= ?";
            connection = Database.connectionDB();
            Date date = new Date();
            java.sql.Date _date = new java.sql.Date(date.getTime());
            try {
                preparedStatement = connection.prepareStatement(updataData);
                preparedStatement.setString(1, product_id_textfield.getText());
                preparedStatement.setString(2, product_name_textfield.getText());
                preparedStatement.setString(3, type_combobox.getSelectionModel().getSelectedItem());
                preparedStatement.setString(4, stock_textfield.getText());
                preparedStatement.setString(5, price_textfield.getText());
                preparedStatement.setString(6, status_combobox.getSelectionModel().getSelectedItem());
                preparedStatement.setString(7, path);
                preparedStatement.setString(8, String.valueOf(_date));
                preparedStatement.setString(9, String.valueOf(UserDetail.getId()));
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alert");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to Update product with ID: " + product_id_textfield.getText());

                Optional<ButtonType> optional = alert.showAndWait();
                if (optional.get().equals(ButtonType.OK)) {
                    preparedStatement.executeUpdate();
                    inventoryShowData();
                    getSuccessAlert("Successfully Updated!");
                    inventoryClearBtn();
                } else {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Cancelled");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //import images
    public void inventoryImportBtn() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Open Image File", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(main_form.getScene().getWindow());
        if (file != null) {
            UserDetail.setPath(file.getAbsolutePath());
            image = new Image(file.toURI().toString());
            display_selected_image.setImage(image);
        }
    }

    ObservableList<String> observableStatus = FXCollections.observableArrayList(status);

    public ObservableList<ProductData> inventoryDataList() {
        ObservableList<ProductData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Product";
        connection = Database.connectionDB();
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
    };

    private ObservableList<ProductData> inventoryListData;

    public void inventoryShowData() {
        inventoryListData = inventoryDataList();
        inventory_product_id.setCellValueFactory(new PropertyValueFactory<>("productId"));
        inventory_product_name.setCellValueFactory(new PropertyValueFactory<>("productName"));
        inventory_product_type.setCellValueFactory(new PropertyValueFactory<>("type"));
        inventory_product_stock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        inventory_product_price.setCellValueFactory(new PropertyValueFactory<>("price"));
        inventory_product_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        inventory_product_date.setCellValueFactory(new PropertyValueFactory<>("date"));

        inventory_tableview.setItems(inventoryListData);

    }

    public void logout() {

        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log out");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> optional = alert.showAndWait();
        if (optional.get().equals(ButtonType.OK)) {

            log_out_button.getScene().getWindow().hide();
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("Login.fxml"));
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(fxmlLoader.load());
                stage.setTitle("Cafe Shop Management");
                stage.setMinHeight(430);
                stage.setMinWidth(610);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    public void getUsername() {
        String user = UserDetail.getUsername();
        user = user.substring(0, 1).toUpperCase() + user.substring(1);
        username.setText(user);
    }

    public void getSuccessAlert(String message) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void inventoryClearBtn() {
        product_id_textfield.setText("");
        product_name_textfield.setText("");
        type_combobox.getSelectionModel().clearSelection();
        stock_textfield.setText("");
        price_textfield.setText("");
        status_combobox.getSelectionModel().clearSelection();
        UserDetail.setPath("");
        display_selected_image.setImage(null);
        UserDetail.setId(0);
    }

    public void inventorySelectedData() {
        ProductData productData = inventory_tableview.getSelectionModel().getSelectedItem();
        int getIndex = inventory_tableview.getSelectionModel().getSelectedIndex();

        if ((getIndex - 1) < -1) {
            return;
        }
        product_id_textfield.setText(productData.getProductId());
        product_name_textfield.setText(productData.getProductName());
        type_combobox.setValue(productData.getType());
        stock_textfield.setText(String.valueOf(productData.getStock()));
        price_textfield.setText(String.valueOf(productData.getPrice()));
        status_combobox.setValue(productData.getStatus());

        UserDetail.setPath("File:" + productData.getImage());
        UserDetail.setDate(productData.getDate());
        UserDetail.setId(productData.getId());
        display_selected_image.setImage(new Image(UserDetail.getPath()));

    }

    public void switchForm(ActionEvent event) {

        if(event.getSource() == dashboard_button) {
            dashboard_section.setVisible(true);
            inventory_form.setVisible(false);
            menu_section.setVisible(false);
            customers_section.setVisible(false);
        } else if (event.getSource() == inventory_button) {
            dashboard_section.setVisible(false);
            inventory_form.setVisible(true);
            menu_section.setVisible(false);
            customers_section.setVisible(false);
            inventoryShowData();
        } else if (event.getSource() == menu_button) {
            dashboard_section.setVisible(false);
            inventory_form.setVisible(false);
            menu_section.setVisible(true);
            customers_section.setVisible(false);
            menuDisplayCard();
            menuDisplayOrder();
            menuDisplayTotal();
            menuShowData();
        } else if (event.getSource() == customers_button) {
            dashboard_section.setVisible(false);
            inventory_form.setVisible(false);
            menu_section.setVisible(false);
            customers_section.setVisible(true);
            showCustomerData();
        }
    }

    public ObservableList<Receipt> getReceiptData() {
        ObservableList<Receipt> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Receipt";
        connection = Database.connectionDB();
        try {
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            Receipt receipt;
            while (resultSet.next()) {
                receipt = new Receipt(
                        resultSet.getInt("id"),
                        resultSet.getString("customer_id"),
                        resultSet.getDouble("total"),
                        resultSet.getString("date"),
                        resultSet.getString("em_username")
                );
                listData.add(receipt);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return listData;
    }

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
        getUsername();
        log_out_button.setOnAction(event -> logout());
        type_combobox.setItems(typeList);
        status_combobox.setItems(observableStatus);
        inventoryShowData();
        choose_image_button.setOnAction(event -> inventoryImportBtn());
        add_button.setOnAction(event -> inventoryAddBtn());
        update_button.setOnAction(event -> inventoryUpdateBtn());
        delete_button.setOnAction(event -> inventoryDeleteBtn());
        clear_button.setOnAction(event -> inventoryClearBtn());
        menuDisplayCard();
        menuDisplayOrder();
        menuDisplayTotal();
        menuShowData();
        showCustomerData();
        menu_receipt_btn.setOnAction(event -> menuReceiptBtn());
    }


}