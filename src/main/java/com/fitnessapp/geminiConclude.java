package com.fitnessapp;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import com.fitnessapp.database.DatabaseConnection;

public class geminiConclude {
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Label adviceFromGemini;
    private int currentUserId;
    private String apiKey;

    public geminiConclude(Label adviceLabel, int userId) {
        this.adviceFromGemini = adviceLabel;
        this.currentUserId = userId;
        loadApiKey();
    }

    private void loadApiKey() {
        try {
            Properties props = new Properties();
            FileInputStream input = new FileInputStream("src/main/resources/application.properties");
            props.load(input);
            this.apiKey = props.getProperty("gemini.api.key");
            input.close();

            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IOException("API key not found in properties file");
            }
        } catch (Exception e) {
            e.printStackTrace();
            adviceFromGemini.setText("Error: Unable to load API key. Please check your configuration.");
        }
    }

    public void generateAdvice() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            adviceFromGemini.setText("Error: API key not configured");
            return;
        }

        try {
            String userData = getUserData();
            if (userData.equals("User data not found")) {
                adviceFromGemini.setText("Error: Unable to load user profile data");
                return;
            }

            String workoutHistory = getWorkoutHistory();

            // Create the prompt for Gemini
            String prompt = String.format(
                    """
                            As a fitness expert, analyze this user's profile and workout history to provide personalized advice, simply but precisely (reply in traditional chinese):

                            User Profile:
                            %s

                            Recent Workout History (Last 30 days):
                            %s

                            Please provide:
                            1. Suggestions for improvement based on their fitness target based on the user profile and workout history
                            2. Specific exercise recommendations based on their workout history
                            Keep the response concise and actionable.
                            """,
                    userData, workoutHistory);

            // Create request body
            JSONObject requestBody = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("text", prompt)))));

            // Create HTTP client with longer timeout
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            // Create request
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();

            // Execute request
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "No response body";

                if (!response.isSuccessful()) {
                    throw new IOException(
                            "API request failed with code " + response.code() + "\nResponse: " + responseBody);
                }

                JSONObject jsonResponse = new JSONObject(responseBody);

                // Extract the text from the response
                String advice;
                try {
                    advice = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                } catch (Exception e) {
                    throw new IOException(
                            "Failed to parse API response: " + responseBody + "\nError: " + e.getMessage());
                }

                if (advice == null || advice.trim().isEmpty()) {
                    throw new IOException("Received empty response from API");
                }

                adviceFromGemini.setText(advice);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Error: " + e.getMessage();
            if (errorMessage.length() > 200) {
                errorMessage = errorMessage.substring(0, 200) + "...";
            }
            System.err.println("Full error: " + e.getMessage());
            adviceFromGemini.setText("Unable to generate advice. " + errorMessage);
        }
    }

    private String getUserData() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = """
                SELECT height, weight, age, gender, fitness_target, exercise_frequency
                FROM users
                WHERE id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Double height = rs.getDouble("height");
                Double weight = rs.getDouble("weight");
                Integer age = rs.getInt("age");
                String gender = rs.getString("gender");
                String target = rs.getString("fitness_target");
                Integer frequency = rs.getInt("exercise_frequency");

                // Check for null values
                if (height == null || weight == null || age == null ||
                        gender == null || target == null || frequency == null) {
                    return "User data incomplete";
                }

                return String.format("""
                        Height: %.2f cm
                        Weight: %.2f kg
                        Age: %d
                        Gender: %s
                        Fitness Target: %s
                        Exercise Frequency: %d times per week
                        """,
                        height, weight, age, gender, target, frequency);
            }
            return "User data not found";
        }
    }

    private String getWorkoutHistory() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = """
                SELECT workout_text, completed_at
                FROM done_workouts
                WHERE user_id = ?
                AND completed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                ORDER BY completed_at DESC
                """;

        List<String> workouts = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
            while (rs.next()) {
                LocalDate date = rs.getTimestamp("completed_at").toLocalDateTime().toLocalDate();
                String workout = rs.getString("workout_text").trim();
                if (!workout.isEmpty()) {
                    workouts.add(String.format("%s: %s", date.format(formatter), workout));
                }
            }
        }

        if (workouts.isEmpty()) {
            return "No workout history available for the last 30 days";
        }

        return String.join("\n", workouts);
    }
}
