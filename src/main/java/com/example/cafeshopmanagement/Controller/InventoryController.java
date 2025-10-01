package com.example.cafeshopmanagement.Controller;
import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.ProductData;
import com.example.cafeshopmanagement.Model.UserDetail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

public class InventoryController implements Initializable {
    @FXML
    public AnchorPane inventory_form;
    @FXML
    public TableView<ProductData> inventory_tableview;
    @FXML
    public TableColumn<ProductData, String> inventory_product_id;
    @FXML
    public TableColumn<ProductData, String> inventory_product_name;
    @FXML
    public TableColumn<ProductData, String> inventory_product_type;
    @FXML
    public TableColumn<ProductData, String> inventory_product_stock;
    @FXML
    public TableColumn<ProductData, String> inventory_product_price;
    @FXML
    public TableColumn<ProductData, String> inventory_product_status;
    @FXML
    public TableColumn<ProductData, String> inventory_product_date;
    @FXML
    public TextField product_id_textfield;
    @FXML
    public TextField product_name_textfield;
    @FXML
    public ComboBox<String> type_combobox;
    @FXML
    public TextField stock_textfield;
    @FXML
    public TextField price_textfield;
    @FXML
    public ImageView display_selected_image;
    @FXML
    public Button choose_image_button;
    @FXML
    public Button add_button;
    @FXML
    public Button update_button;
    @FXML
    public Button delete_button;
    @FXML
    public Button clear_button;
    @FXML
    public ComboBox<String> status_combobox;
    @FXML
    public AnchorPane main_form;

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

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ObservableList<ProductData> inventoryListData;
    private LoginController loginController = new LoginController();

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
    }

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> observableStatus = FXCollections.observableArrayList(status);
        type_combobox.setItems(typeList);
        status_combobox.setItems(observableStatus);
        inventoryShowData();
        //test
    }
}