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
import java.text.SimpleDateFormat;
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
    private String[] typeListItems = {"Meal", "Drinks"};
    private String[] statusListItems = {"Available", "Unavailable"};
    private ObservableList<ProductData> inventoryListData;
    private String selectedImagePath;

    public ObservableList<ProductData> inventoryDataList() {
        ObservableList<ProductData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM Product";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                ProductData productData = new ProductData(
                        rs.getInt("id"),
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("type"),
                        rs.getInt("stock"),
                        rs.getDouble("price"),
                        rs.getString("status"),
                        rs.getString("image"),
                        rs.getString("date")
                );
                listData.add(productData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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

    // Add
    public void inventoryAddBtn() {
        if (product_id_textfield.getText().isEmpty() || product_name_textfield.getText().isEmpty() ||
                stock_textfield.getText().isEmpty() || price_textfield.getText().isEmpty() ||
                type_combobox.getSelectionModel().getSelectedItem() == null ||
                status_combobox.getSelectionModel().getSelectedItem() == null ||
                selectedImagePath == null || selectedImagePath.isEmpty() // Check against the instance variable
        ) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields and select an image.");
        } else {
            String checkProductID = "SELECT product_id from Product WHERE product_id = ?";
            String insertData = "INSERT INTO Product (product_id, product_name, type, stock, price, status, image, date) VALUES(?,?,?,?,?,?,?,?)";
            try (Connection conn = Database.connectionDB()) {
                PreparedStatement checkStmt = conn.prepareStatement(checkProductID);
                checkStmt.setString(1, product_id_textfield.getText());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    showAlert(Alert.AlertType.ERROR, "Error Message", product_id_textfield.getText() + " already exists.");
                } else {
                    PreparedStatement insertStmt = conn.prepareStatement(insertData);
                    insertStmt.setString(1, product_id_textfield.getText());
                    insertStmt.setString(2, product_name_textfield.getText());
                    insertStmt.setString(3, type_combobox.getSelectionModel().getSelectedItem());
                    insertStmt.setInt(4, Integer.parseInt(stock_textfield.getText()));
                    insertStmt.setDouble(5, Double.parseDouble(price_textfield.getText()));
                    insertStmt.setString(6, status_combobox.getSelectionModel().getSelectedItem());
                    insertStmt.setString(7, selectedImagePath); // Use the instance variable
                    insertStmt.setString(8, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    insertStmt.executeUpdate();

                    inventoryShowData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully Added!");
                    inventoryClearBtn();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Delete
    public void inventoryDeleteBtn() {
        if (UserDetail.getId() == 0) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please select an item to delete.");
            return;
        }
        String deleteData = "DELETE FROM Product WHERE id = ?";
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to Delete product with ID: " + product_id_textfield.getText() + "?");
        Optional<ButtonType> optional = alert.showAndWait();

        if (optional.isPresent() && optional.get().equals(ButtonType.OK)) {
            try (Connection conn = Database.connectionDB();
                 PreparedStatement pst = conn.prepareStatement(deleteData)) {
                pst.setInt(1, UserDetail.getId());
                pst.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully Deleted!");
                inventoryShowData();
                inventoryClearBtn();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Error Message", "Cancelled.");
        }
    }

    // Update
    public void inventoryUpdateBtn() {
        if (product_id_textfield.getText().isEmpty() || product_name_textfield.getText().isEmpty() ||
                stock_textfield.getText().isEmpty() || price_textfield.getText().isEmpty() ||
                type_combobox.getSelectionModel().getSelectedItem() == null ||
                status_combobox.getSelectionModel().getSelectedItem() == null ||
                selectedImagePath == null || UserDetail.getId() == 0
        ) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please select an item to update and fill all fields.");
        } else {
            String updateData = "UPDATE Product SET product_id = ?, product_name=?, type=?, stock=?, price =?, status=?, image=?, date=? WHERE id= ?";
            try (Connection conn = Database.connectionDB();
                 PreparedStatement pst = conn.prepareStatement(updateData)) {
                pst.setString(1, product_id_textfield.getText());
                pst.setString(2, product_name_textfield.getText());
                pst.setString(3, type_combobox.getSelectionModel().getSelectedItem());
                pst.setInt(4, Integer.parseInt(stock_textfield.getText()));
                pst.setDouble(5, Double.parseDouble(price_textfield.getText()));
                pst.setString(6, status_combobox.getSelectionModel().getSelectedItem());
                pst.setString(7, selectedImagePath); // Use the instance variable
                pst.setString(8, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                pst.setInt(9, UserDetail.getId());

                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Alert");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to Update product with ID: " + product_id_textfield.getText());
                Optional<ButtonType> optional = alert.showAndWait();
                if (optional.isPresent() && optional.get().equals(ButtonType.OK)) {
                    pst.executeUpdate();
                    inventoryShowData();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully Updated!");
                    inventoryClearBtn();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "Cancelled.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Import Button
    public void inventoryImportBtn() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Open Image File", "*.png", "*.jpg"));

        // Corrected line: Get the window from the button itself, which is in the scene.
        File file = fileChooser.showOpenDialog(choose_image_button.getScene().getWindow());

        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            Image image = new Image(file.toURI().toString());
            display_selected_image.setImage(image);
        }
    }


    // verify success
    public void getSuccessAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Success", message);
    }

    // Clear
    public void inventoryClearBtn() {
        product_id_textfield.setText("");
        product_name_textfield.setText("");
        type_combobox.getSelectionModel().clearSelection();
        stock_textfield.setText("");
        price_textfield.setText("");
        status_combobox.getSelectionModel().clearSelection();
        selectedImagePath = null; // Clear the instance variable
        display_selected_image.setImage(null);
        UserDetail.setPath("");
        UserDetail.setId(0);
    }


    // Seleted Data in the inventory
    public void inventorySelectedData() {
        ProductData productData = inventory_tableview.getSelectionModel().getSelectedItem();
        int getIndex = inventory_tableview.getSelectionModel().getSelectedIndex();
        if (getIndex <= -1) {
            return;
        }
        product_id_textfield.setText(productData.getProductId());
        product_name_textfield.setText(productData.getProductName());
        type_combobox.setValue(productData.getType());
        stock_textfield.setText(String.valueOf(productData.getStock()));
        price_textfield.setText(String.valueOf(productData.getPrice()));
        status_combobox.setValue(productData.getStatus());
        selectedImagePath = productData.getImage(); // Set the instance variable
        UserDetail.setDate(productData.getDate());
        UserDetail.setId(productData.getId());
        display_selected_image.setImage(new Image("file:" + productData.getImage().replace("\\", "/")));
    }


    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        type_combobox.setItems(FXCollections.observableArrayList(typeListItems));
        status_combobox.setItems(FXCollections.observableArrayList(statusListItems));
        inventoryShowData();
        choose_image_button.setOnAction(event -> inventoryImportBtn());
        add_button.setOnAction(event -> inventoryAddBtn());
        update_button.setOnAction(event -> inventoryUpdateBtn());
        delete_button.setOnAction(event -> inventoryDeleteBtn());
        clear_button.setOnAction(event -> inventoryClearBtn());
        inventory_tableview.setOnMouseClicked(event -> inventorySelectedData());
    }
}