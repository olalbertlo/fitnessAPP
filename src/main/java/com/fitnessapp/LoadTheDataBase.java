package com.fitnessapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fitnessapp.database.DatabaseConnection;
import javafx.scene.control.Button;
import com.calendarfx.model.Entry;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class LoadTheDataBase {
    private Connection conn;
    private int currentUserId;
    private ShowHomeInfo showHomeInfo;

    public LoadTheDataBase(int userId) {
        this.conn = DatabaseConnection.getConnection();
        this.currentUserId = userId;
    }

    public void setShowHomeInfo(ShowHomeInfo showHomeInfo) {
        this.showHomeInfo = showHomeInfo;
    }

    public void loadAllData() {
        loadDiets();
        loadWorkouts();
    }

    private void loadDiets() {
        try {
            String sql = "SELECT day_index, meal_index, meal_text FROM diets WHERE user_id = ? ORDER BY day_index, meal_index";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int dayIndex = rs.getInt("day_index");
                int mealIndex = rs.getInt("meal_index");
                String mealText = rs.getString("meal_text");

                Button button = new Button(mealText);
                // Store the button in SceneController's static storage
                SceneController.storeDietButton(dayIndex, mealIndex, mealText, button);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading diets: " + e.getMessage());
        }
    }

    private void loadWorkouts() {
        try {
            String sql = "SELECT id, day_of_week, workout_text, start_hour, start_minute, end_hour, end_minute FROM workouts WHERE user_id = ? ORDER BY day_of_week";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int workoutId = rs.getInt("id");
                String dayOfWeek = rs.getString("day_of_week");
                String workoutText = rs.getString("workout_text");
                int startHour = rs.getInt("start_hour");
                int startMinute = rs.getInt("start_minute");
                int endHour = rs.getInt("end_hour");
                int endMinute = rs.getInt("end_minute");

                // Create calendar entry, though this code is duplicated
                java.time.DayOfWeek day = java.time.DayOfWeek.valueOf(dayOfWeek);
                java.time.DayOfWeek today = LocalDate.now().getDayOfWeek();
                LocalDate date;
                if (day.getValue() < today.getValue()) {
                    date = LocalDate.now().with(TemporalAdjusters.previousOrSame(day));
                } else {
                    date = LocalDate.now().with(TemporalAdjusters.nextOrSame(day));
                }
                Entry<String> entry = new Entry<>(workoutText);
                entry.setInterval(date, LocalTime.of(startHour, startMinute), date, LocalTime.of(endHour, endMinute));

                if (showHomeInfo != null) {
                    showHomeInfo.addWorkoutToCalendar(entry);
                }

                // Create button with workout info
                Button button = new Button(workoutText + "\n" +
                        String.format("%02d:%02d-%02d:%02d", startHour, startMinute, endHour, endMinute));

                // Style the button (also duplicated)
                button.setMaxWidth(Double.MAX_VALUE);
                button.setStyle("-fx-background-color: #230850; -fx-text-fill: white; -fx-font-size: 12px;");

                // Duplicated code from addWorkoutPage.java
                button.setOnAction(event -> {
                    try {
                        // Remove from database
                        String deleteSql = "DELETE FROM workouts WHERE id = ? AND user_id = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                        deleteStmt.setInt(1, workoutId);
                        deleteStmt.setInt(2, currentUserId);
                        deleteStmt.executeUpdate();

                        // Remove from calendar
                        CalendarModel.getWorkoutCalendar().removeEntry(entry);

                        // Convert day name to index
                        int dayIndex = switch (dayOfWeek) {
                            case "MONDAY" -> 0;
                            case "TUESDAY" -> 1;
                            case "WEDNESDAY" -> 2;
                            case "THURSDAY" -> 3;
                            case "FRIDAY" -> 4;
                            case "SATURDAY" -> 5;
                            case "SUNDAY" -> 6;
                            default -> -1;
                        };

                        // Remove from grid
                        if (dayIndex != -1 && SceneController.weekGrid[dayIndex] != null) {
                            SceneController.weekGrid[dayIndex].getChildren().remove(button);
                        }
                        // Remove from stored list
                        SceneController.removeWorkoutButton(button);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                // Store in SceneController's static storage
                SceneController.storeWorkoutButton(dayOfWeek, workoutText, entry, button);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading workouts: " + e.getMessage());
        }
    }
}
