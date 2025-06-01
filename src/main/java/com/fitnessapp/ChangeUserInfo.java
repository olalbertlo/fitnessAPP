package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import com.fitnessapp.database.DatabaseConnection;
import javafx.stage.Stage;

public class ChangeUserInfo {
    private int currentUserId;

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
    private ImageView userImage;
    @FXML
    private RadioButton maleRadio;
    @FXML
    private RadioButton femaleRadio;

    private File selectedImageFile = null;

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

        // Ensure neither radio button is selected by default
        if (maleRadio != null && femaleRadio != null) {
            maleRadio.setSelected(false);
            femaleRadio.setSelected(false);
        }

        // Load current user data
        loadUserData();
    }

    private void loadUserData() {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userName.setText(rs.getString("display_name"));
                Height.setText(rs.getDouble("height") > 0 ? String.valueOf(rs.getDouble("height")) : "");
                Weight.setText(rs.getDouble("weight") > 0 ? String.valueOf(rs.getDouble("weight")) : "");
                age.setText(rs.getInt("age") > 0 ? String.valueOf(rs.getInt("age")) : "");
                // Set gender
                String gender = rs.getString("gender");

                if (gender != null) {
                    if (gender.equals("Male")) {
                        maleRadio.setSelected(true);
                        femaleRadio.setSelected(false);
                    } else if (gender.equals("Female")) {
                        femaleRadio.setSelected(true);
                        maleRadio.setSelected(false);
                    }
                } else {
                    // If gender is null, ensure no radio button is selected
                    maleRadio.setSelected(false);
                    femaleRadio.setSelected(false);
                }

                // Set target
                String fitnessTarget = rs.getString("fitness_target");
                if (fitnessTarget != null) {
                    target.setValue(fitnessTarget);
                }

                frequency.setText(
                        rs.getInt("exercise_frequency") > 0 ? String.valueOf(rs.getInt("exercise_frequency")) : "");
                mealPreference.setText(rs.getString("meal_preference"));

                // Load profile image
                byte[] imageData = rs.getBytes("profile_image");
                if (imageData != null && imageData.length > 0) {
                    Image image = new Image(new java.io.ByteArrayInputStream(imageData));
                    userImage.setImage(image);
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database Error", "Failed to load user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateUserInfo() {
        // Debug print for gender selection
        System.out.println("Selected toggle: " + Gender.getSelectedToggle());
        if (Gender.getSelectedToggle() instanceof RadioButton rb) {
            System.out.println("Selected gender text: " + rb.getText());
        }

        Connection conn = DatabaseConnection.getConnection();
        String sql = """
                UPDATE users
                SET display_name = ?, height = ?, weight = ?, age = ?,
                    gender = ?, fitness_target = ?, exercise_frequency = ?,
                    meal_preference = ?, profile_image = COALESCE(?, profile_image)
                WHERE id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Get selected gender
            RadioButton selectedGender = (RadioButton) Gender.getSelectedToggle();
            String genderValue = selectedGender != null ? selectedGender.getText() : null;

            // Convert Height and weight to decimal
            Double HeightValue = !Height.getText().isEmpty() ? Double.parseDouble(Height.getText()) : null;
            Double WeightValue = !Weight.getText().isEmpty() ? Double.parseDouble(Weight.getText()) : null;
            Integer ageValue = !age.getText().isEmpty() ? Integer.parseInt(age.getText()) : null;
            Integer frequencyValue = !frequency.getText().isEmpty() ? Integer.parseInt(frequency.getText()) : null;

            pstmt.setString(1, userName.getText());
            pstmt.setObject(2, HeightValue);
            pstmt.setObject(3, WeightValue);
            pstmt.setObject(4, ageValue);
            pstmt.setObject(5, genderValue);
            pstmt.setString(6, target.getValue());
            pstmt.setObject(7, frequencyValue);
            pstmt.setString(8, mealPreference.getText());

            // Handle image update
            if (selectedImageFile != null) {
                FileInputStream fis = new FileInputStream(selectedImageFile);
                pstmt.setBinaryStream(9, fis, selectedImageFile.length());
            } else {
                pstmt.setNull(9, java.sql.Types.BLOB);
            }

            pstmt.setInt(10, currentUserId);

            pstmt.executeUpdate();
            showAlert("Success", "Profile Updated", "Your profile has been successfully updated!");

            // Close the window
            Stage stage = (Stage) userName.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            showAlert("Error", "Update Failed", "Failed to update profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void selectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters()
                .addAll(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            selectedImageFile = selectedFile;
            userImage.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        // Load user data after setting the ID
        if (target != null) { // Check if FXML is initialized
            loadUserData();
        }
    }

    public void handleBackButton() {
        // Simply close the window
        Stage stage = (Stage) userName.getScene().getWindow();
        stage.close();
    }
}
