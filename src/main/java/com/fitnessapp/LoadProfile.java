package com.fitnessapp;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.ByteArrayInputStream;
import com.fitnessapp.database.DatabaseConnection;

public class LoadProfile {
    private int currentUserId;
    private ImageView userImage;
    private Label userDisplayName;
    private Label userStatus;
    private Label userBMI;
    private Label userTarget;

    public LoadProfile(ImageView userImage, Label userDisplayName, Label userStatus, Label userBMI, Label userTarget) {
        this.userImage = userImage;
        this.userDisplayName = userDisplayName;
        this.userStatus = userStatus;
        this.userBMI = userBMI;
        this.userTarget = userTarget;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadProfilePage();
    }

    public void loadProfilePage() {
        if (currentUserId <= 0) {
            userStatus.setText("Error: No user ID provided");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT display_name, height, weight, age, gender, profile_image, fitness_target FROM users WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Load display name
                String displayName = rs.getString("display_name");
                userDisplayName.setText(displayName);

                // Load status information
                double height = rs.getDouble("height");
                double weight = rs.getDouble("weight");
                int age = rs.getInt("age");
                String gender = rs.getString("gender");

                // Create status text
                String statusText = String.format("Height: %.1f cm | Weight: %.1f kg | Age: %d | Gender: %s",
                        height, weight, age, gender);
                userStatus.setText(statusText);

                // Calculate and display BMI
                if (height > 0 && weight > 0) {
                    double heightInMeters = height / 100.0;
                    double bmi = weight / (heightInMeters * heightInMeters);
                    String bmiCategory = getBMICategory(bmi);
                    userBMI.setText(String.format("BMI: %.1f (%s)", bmi, bmiCategory));
                } else {
                    userBMI.setText("Error loading height or weight");
                }

                // Load profile image
                byte[] imageData = rs.getBytes("profile_image");
                if (imageData != null) {
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    userImage.setImage(image);
                    userImage.setPreserveRatio(true);
                    userImage.setFitHeight(149); // Set height limit
                }
                // Load fitness target
                String fitnessTarget = rs.getString("fitness_target");
                userTarget.setText(fitnessTarget);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            userStatus.setText("Error loading profile data");
        }
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25) {
            return "Normal weight";
        } else if (bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
}
