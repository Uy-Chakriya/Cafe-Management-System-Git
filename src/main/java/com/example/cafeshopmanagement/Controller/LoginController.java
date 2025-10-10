package com.example.cafeshopmanagement.Controller;
import com.example.cafeshopmanagement.App;
import com.example.cafeshopmanagement.Database.Database;
import com.example.cafeshopmanagement.Model.UserDetail;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    public AnchorPane login_section;
    public TextField login_username;
    public PasswordField login_password;
    public Button login_button;
    public Hyperlink login_forget_password;
    public AnchorPane register_account_section;
    public TextField register_account_username;
    public PasswordField register_account_password;
    public Button create_account_button;
    public ComboBox<String> register_account_question;
    public TextField register_account_answer;
    public AnchorPane side_form;
    public Button side_create_account_button;
    public Button side_already_have_an_account;
    public TextField user_username;
    public Button user_proceed;
    public ComboBox<String> user_question;
    public Button back_to_login;
    public TextField user_answer;
    public Button back_to;
    public Button change_password;
    public PasswordField confirm_password;
    public PasswordField new_password;
    public AnchorPane forget_password_section;
    public AnchorPane forget_password_proceed_section;

    private final String[] questionList = {
            "What is your favorite Color?",
    };

    ObservableList<String> observableList = FXCollections.observableArrayList(questionList);

    private Alert alert;

    public void proceedAction() {
        if (user_username.getText().isEmpty() || user_question.getSelectionModel().getSelectedItem() == null || user_answer.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
        } else {
            String checkUsernameAndQuestion = "SELECT username, question, answer FROM Employee WHERE username = ? AND question = ? AND answer = ?";
            try (Connection conn = Database.connectionDB();
                 PreparedStatement pst = conn.prepareStatement(checkUsernameAndQuestion)) {
                pst.setString(1, user_username.getText());
                pst.setString(2, user_question.getSelectionModel().getSelectedItem());
                pst.setString(3, user_answer.getText());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        side_already_have_an_account.setVisible(false);
                        side_create_account_button.setVisible(false);
                        forget_password_section.setVisible(true);
                        forget_password_proceed_section.setVisible(false);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error Message", "Incorrect Username, Question or Answer");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void loginAction() {
        if (login_username.getText().isEmpty() || login_password.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
        } else if (login_password.getText().length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Password must be more than 8 characters.");
        } else {
            String confirmIfTrue = "SELECT username, password FROM Employee WHERE username = ? AND password = ?";
            try (Connection conn = Database.connectionDB();
                 PreparedStatement pst = conn.prepareStatement(confirmIfTrue)) {
                pst.setString(1, login_username.getText());
                pst.setString(2, login_password.getText());
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        UserDetail.setUsername(login_username.getText());
                        showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Login!");

                        Stage stage = (Stage) login_button.getScene().getWindow();
                        stage.close();

                        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("FXML/Main.fxml"));
                        Stage mainStage = new Stage();
                        Scene scene = new Scene(fxmlLoader.load());
                        mainStage.setScene(scene);
                        mainStage.setTitle("Cafe Shop Management");
                        mainStage.setMinHeight(800);
                        mainStage.setMinWidth(1280);
                        mainStage.show();

                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error Message", "Incorrect Username/Password");
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Registe
    public void registrationButton() throws SQLException {
        if (register_account_username.getText().isEmpty() || register_account_password.getText().isEmpty()
                || register_account_question.getSelectionModel().getSelectedItem() == null || register_account_answer.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
        } else {
            String checkUsername = "SELECT username FROM Employee WHERE username = ?";
            String regData = "INSERT INTO Employee (username, password, question, answer, date) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = Database.connectionDB()) {
                try (PreparedStatement checkPst = conn.prepareStatement(checkUsername)) {
                    checkPst.setString(1, register_account_username.getText());
                    try (ResultSet rs = checkPst.executeQuery()) {
                        if (rs.next()) {
                            showAlert(Alert.AlertType.ERROR, "Error Message", register_account_username.getText() + " is already registered\nPlease Log in or click forgot password");
                            transitionLeft();
                            return;
                        }
                    }
                }
                if (register_account_password.getText().length() < 8) {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "Password must be more than 8 characters.");
                    return;
                }

                try (PreparedStatement regPst = conn.prepareStatement(regData)) {
                    regPst.setString(1, register_account_username.getText());
                    regPst.setString(2, register_account_password.getText());
                    regPst.setString(3, register_account_question.getSelectionModel().getSelectedItem());
                    regPst.setString(4, register_account_answer.getText());
                    regPst.setDate(5, new java.sql.Date(new Date().getTime()));
                    regPst.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Operation was successful!\nPlease Log in");

                    register_account_username.setText("");
                    register_account_answer.setText("");
                    register_account_password.setText("");
                    register_account_question.getSelectionModel().clearSelection();
                    transitionLeft();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    TranslateTransition translateTransition = new TranslateTransition();

    public void switchForm(ActionEvent event) {
        if (event.getSource() == side_create_account_button) {
            transitionRight();
        } else if (event.getSource() == side_already_have_an_account) {
            transitionLeft();
        }
    }

    public void transitionLeft() {
        translateTransition.setNode(side_form);
        translateTransition.setToX(0);
        translateTransition.setDuration(Duration.millis(1000));
        translateTransition.play();
        translateTransition.setOnFinished(e -> {
            side_already_have_an_account.setVisible(false);
            side_create_account_button.setVisible(true);
            forget_password_section.setVisible(false);
            forget_password_proceed_section.setVisible(false);
        });
    }

    public void transitionRight() {
        translateTransition.setNode(side_form);
        translateTransition.setToX(300);
        translateTransition.setDuration(Duration.millis(1000));
        translateTransition.play();
        translateTransition.setOnFinished(e -> {
            side_already_have_an_account.setVisible(true);
            side_create_account_button.setVisible(false);
            forget_password_section.setVisible(false);
            forget_password_proceed_section.setVisible(false);
        });
    }

    public void forgetPasswordAction() {
        side_already_have_an_account.setVisible(false);
        side_create_account_button.setVisible(false);
        forget_password_section.setVisible(false);
        forget_password_proceed_section.setVisible(true);
        side_create_account_button.setVisible(true);
    }

    public void backToLogin() {
        side_already_have_an_account.setVisible(false);
        side_create_account_button.setVisible(true);
        forget_password_section.setVisible(false);
        forget_password_proceed_section.setVisible(false);
    }

    public void changePasswordAction() {
        if (new_password.getText().isEmpty() || confirm_password.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields");
            return;
        } else if (!new_password.getText().equals(confirm_password.getText())) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "New password and confirm password are not the same.");
            return;
        } else if (new_password.getText().length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Password must be more than 8 characters.");
            return;
        }

        String changePasswordSql = "UPDATE Employee SET password = ? WHERE username = ?";
        try (Connection conn = Database.connectionDB();
             PreparedStatement pst = conn.prepareStatement(changePasswordSql)) {
            pst.setString(1, new_password.getText());
            pst.setString(2, user_username.getText());
            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Information Message", "Password Successfully Changed");
                new_password.setText("");
                confirm_password.setText("");
                user_username.setText("");
                user_answer.setText("");
                user_question.getSelectionModel().clearSelection();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Failed to change password. User might not exist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        register_account_question.setItems(observableList);
        user_question.setItems(observableList);
        create_account_button.setOnAction(event -> {
            try {
                registrationButton();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        login_button.setOnAction(event -> loginAction());
        login_forget_password.setOnAction(event -> forgetPasswordAction());
        back_to_login.setOnAction(event -> backToLogin());
        back_to.setOnAction(event -> backToLogin());
        user_proceed.setOnAction(event -> proceedAction());
        change_password.setOnAction(event -> changePasswordAction());
    }
}