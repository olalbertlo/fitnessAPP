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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import com.fitnessapp.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.security.SecureRandom;
import java.util.Base64;
import javafx.application.Platform;

public class Login {

    @FXML
    private TextField accountField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberMe;
    @FXML
    private Hyperlink createUser;

    private Connection conn = DatabaseConnection.getConnection();
    private static final long REMEMBER_ME_DURATION_DAYS = 30;

    @FXML
    public void initialize() {
        // Use Platform.runLater to ensure the login page is loaded first
        Platform.runLater(() -> {
            checkRememberMeToken();
        });
    }

    private void checkRememberMeToken() {
        // Check for existing remember-me token
        String sql = """
                SELECT u.id, u.username
                FROM remember_tokens t
                JOIN users u ON t.user_id = u.id
                WHERE t.expiry_date > NOW()
                ORDER BY t.expiry_date DESC
                LIMIT 1
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Get user info
                int userId = rs.getInt("id");

                // Auto-login
                try {
                    FXMLLoader loader = new FXMLLoader(Login.class.getResource("/com/fitnessapp/Home.fxml"));
                    Parent root = loader.load();

                    SceneController controller = loader.getController();
                    controller.setCurrentUserId(userId);
                    controller.initializeHomePageData();
                    // Load the login page first so that we can get the stage of accountField to
                    // switch
                    // to home page
                    Stage stage = (Stage) accountField.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void saveRememberMeToken(int userId) {
        if (!rememberMe.isSelected()) {
            return;
        }

        // First, clear existing tokens for this user, ensure the token is unique
        try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM remember_tokens WHERE user_id = ?")) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Then save the new token
        String token = generateToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(REMEMBER_ME_DURATION_DAYS);

        String sql = "INSERT INTO remember_tokens (user_id, token, expiry_date) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setTimestamp(3, Timestamp.valueOf(expiryDate));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearRememberMeToken() {
        String username = accountField.getText();
        if (!username.isEmpty()) {
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE t FROM remember_tokens t JOIN users u ON t.user_id = u.id WHERE u.username = ?")) {
                pstmt.setString(1, username);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onEnter(ActionEvent event) {
        login(event);
    }

    public void login(ActionEvent event) {
        String password = passwordField.getText();
        String account = accountField.getText();

        String sql = "SELECT id FROM users WHERE username = ? AND userPassword = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id");

                if (rememberMe.isSelected()) {
                    saveRememberMeToken(userId);
                }

                // Load the home page
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/Home.fxml"));
                Parent root = loader.load();

                SceneController controller = loader.getController();
                controller.setCurrentUserId(userId);
                controller.initializeHomePageData();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } else {
                // Show error message for invalid credentials
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Login Error");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("The username or password you entered is incorrect.");
                alert.showAndWait();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
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
