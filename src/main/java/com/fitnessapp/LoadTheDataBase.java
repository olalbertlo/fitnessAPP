package com.fitnessapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fitnessapp.database.DatabaseConnection;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import com.calendarfx.model.Entry;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class LoadTheDataBase {
    private Connection conn;
    private int currentUserId;
    private ShowHomeInfo showHomeInfo;
    private GridPane dailyTasks;

    public LoadTheDataBase(int userId) {
        this.conn = DatabaseConnection.getConnection();
        this.currentUserId = userId;
    }

    public void setShowHomeInfo(ShowHomeInfo showHomeInfo) {
        this.showHomeInfo = showHomeInfo;
    }

    public void setDailyTasks(GridPane dailyTasks) {
        this.dailyTasks = dailyTasks;
    }

    public void loadAllData() {
        loadDiets();
        // Clear existing workout data before loading
        CalendarModel.getWorkoutCalendar().clear();
        SceneController.clearWorkoutButtons();
        loadWorkouts();
        if (dailyTasks != null) {
            loadTodayWorkoutTasks();
        }
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

                // Add to grid if needed
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

                if (dayIndex != -1 && SceneController.weekGrid[dayIndex] != null) {
                    SceneController.weekGrid[dayIndex].add(button, 0, SceneController.weekGrid[dayIndex].getRowCount());
                    javafx.scene.layout.RowConstraints rowConstraints = new javafx.scene.layout.RowConstraints();
                    rowConstraints.setVgrow(javafx.scene.layout.Priority.ALWAYS);
                    rowConstraints.setMinHeight(30); // Set minimum height
                    rowConstraints.setFillHeight(true); // Allow the row to fill available height
                    SceneController.weekGrid[dayIndex].getRowConstraints().add(rowConstraints);

                    // Make the button fill its cell
                    GridPane.setFillHeight(button, true);
                    GridPane.setVgrow(button, javafx.scene.layout.Priority.ALWAYS);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading workouts: " + e.getMessage());
        }
    }

    private void loadTodayWorkoutTasks() {
        // Clear existing items
        dailyTasks.getChildren().clear();
        dailyTasks.getRowConstraints().clear();

        // Get today's day of week in the correct format (uppercase)
        String today = LocalDate.now().getDayOfWeek().toString().toUpperCase();

        try {
            String sql = "SELECT workout_text, day_of_week FROM workouts WHERE user_id = ? AND day_of_week = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, today);
            ResultSet rs = pstmt.executeQuery();

            int rowIndex = 0;
            while (rs.next()) {
                String workoutText = rs.getString("workout_text");
                String[] movements = workoutText.split("[\\n]+");

                for (String movement : movements) {
                    movement = movement.trim();
                    if (!movement.isEmpty()) {
                        // Add row constraint for this row
                        javafx.scene.layout.RowConstraints rowConstraints = new javafx.scene.layout.RowConstraints();
                        rowConstraints.setPrefHeight(40);
                        rowConstraints.setMinHeight(40);
                        dailyTasks.getRowConstraints().add(rowConstraints);

                        CheckBox checkBox = new CheckBox(movement);
                        checkBox.setStyle("-fx-text-fill: black;");
                        dailyTasks.add(checkBox, 0, rowIndex++);
                        System.out.println("Added checkbox for: " + movement); // Debug print
                    }
                }
            }

            // If no workouts
            if (rowIndex == 0) {
                // Add row constraint for the message
                javafx.scene.layout.RowConstraints rowConstraints = new javafx.scene.layout.RowConstraints();
                rowConstraints.setPrefHeight(40);
                rowConstraints.setMinHeight(40);
                dailyTasks.getRowConstraints().add(rowConstraints);

                CheckBox noWorkouts = new CheckBox("No workouts today");
                noWorkouts.setStyle("-fx-text-fill: black;");
                noWorkouts.setDisable(true);
                dailyTasks.add(noWorkouts, 0, 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage()); // Debug print
        }
    }
}
