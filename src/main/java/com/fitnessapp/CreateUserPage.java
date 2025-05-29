package com.fitnessapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.fitnessapp.database.DatabaseConnection;

public class CreateUserPage {

    @FXML
    private TextField userId;
    @FXML
    private TextField userPassword;
    @FXML
    private TextField confirmPassword;
    @FXML
    private TextField userName;
    @FXML
    private TextField Height;
    @FXML
    private TextField Weight;
    @FXML
    private TextField age;
    @FXML
    private ToggleGroup Gender;
    @FXML
    private ChoiceBox<String> target;
    @FXML
    private TextField frequency;
    @FXML
    private TextArea mealPreference;

    @FXML
    public void initialize() {
        // Initialize the target ChoiceBox with fitness goals
        target.getItems().addAll(
                "增加肌肉量",
                "減少體脂肪",
                "改善體態",
                "提升運動表現",
                "增強肌力",
                "改善心肺功能",
                "單純保持健康");
        // Set default value
        target.setValue("增加肌肉量");
    }

    public void createUser(ActionEvent event) {
        if (userId.getText().isEmpty() || userPassword.getText().isEmpty() || confirmPassword.getText().isEmpty()
                || userName.getText().isEmpty()) {
            showAlert("錯誤", "請輸入基本資訊", "用戶名稱、密碼和確認密碼為必填項目。");
            return;
        }

        if (!userPassword.getText().equals(confirmPassword.getText())) {
            showAlert("錯誤", "密碼不匹配", "請確保密碼和確認密碼相同。");
            return;
        }

        try {
            String sql = """
                    INSERT INTO users (username, password, display_name, height, weight, age,
                    gender, fitness_target, exercise_frequency, meal_preference)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // Get selected gender
            RadioButton selectedGender = (RadioButton) Gender.getSelectedToggle();
            String genderValue = selectedGender != null ? selectedGender.getText() : null;

            // Convert Height and weight to decimal
            Double HeightValue = !Height.getText().isEmpty() ? Double.parseDouble(Height.getText()) : null;
            Double WeightValue = !Weight.getText().isEmpty() ? Double.parseDouble(Weight.getText()) : null;
            Integer ageValue = !age.getText().isEmpty() ? Integer.parseInt(age.getText()) : null;
            Integer frequencyValue = !frequency.getText().isEmpty() ? Integer.parseInt(frequency.getText()) : null;

            pstmt.setString(1, userId.getText());
            pstmt.setString(2, userPassword.getText()); // Note: In production, you should hash the password
            pstmt.setString(3, userName.getText());
            pstmt.setObject(4, HeightValue);
            pstmt.setObject(5, WeightValue);
            pstmt.setObject(6, ageValue);
            pstmt.setString(7, genderValue);
            pstmt.setString(8, target.getValue());
            pstmt.setObject(9, frequencyValue);
            pstmt.setString(10, mealPreference.getText());

            pstmt.executeUpdate();

            showAlert("成功", "用戶創建成功", "您的帳戶已成功創建！");
            switchToLogin(event);

        } catch (SQLException e) {
            showAlert("錯誤", "數據庫錯誤", "創建用戶時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void switchToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/Login.fxml"));
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