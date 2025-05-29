package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;

public class Login {

    @FXML
    private TextField accountField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMe;
    @FXML
    private Hyperlink createUser;

    @FXML
    public void onEnter(ActionEvent event) {
        loginAction(event);
    }

    public void loginAction(ActionEvent event) {
        // get username and password
        String password = passwordField.getText();
        String account = accountField.getText();
        if (account.equals("admin") && password.equals("admin")) {
            // switch to home page
            try {
                // load the home page
                Parent root = FXMLLoader.load(getClass().getResource("/com/fitnessapp/Home.fxml"));
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // pop up a window to show "account or password incorrect"
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setContentText("Account or password incorrect");
            alert.showAndWait();
        }
    }

    public void switchToCreateUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/CreateUser.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
