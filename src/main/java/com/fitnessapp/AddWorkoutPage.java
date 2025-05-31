package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import java.util.List;
import javafx.scene.control.ComboBox;
import java.time.LocalTime;
import javafx.scene.control.Alert;
import java.util.stream.Collectors;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import com.calendarfx.model.Entry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fitnessapp.database.DatabaseConnection;

public class AddWorkoutPage {

    private ShowHomeInfo showHomeInfo;
    private int currentUserId;

    public void setShowHomeInfo(ShowHomeInfo showHomeInfo) {
        this.showHomeInfo = showHomeInfo;
    }

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    private AnchorPane workoutPage;
    @FXML
    private ToggleGroup weekDay;
    @FXML
    private ComboBox<Integer> fromHour;
    @FXML
    private ComboBox<Integer> toHour;
    @FXML
    private ComboBox<Integer> fromMinute;
    @FXML
    private ComboBox<Integer> toMinute;

    private int hour, minute, hour2, minute2;

    @FXML
    public void initialize() {
        for (int i = 0; i < 24; i++) {
            fromHour.getItems().add(i);
            toHour.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            fromMinute.getItems().add(i);
            toMinute.getItems().add(i);
        }
        fromHour.setEditable(true);
        fromMinute.setEditable(true);
        toHour.setEditable(true);
        toMinute.setEditable(true);
    }

    public void addWorkout(ActionEvent event) {
        // get all selected checkboxes
        List<CheckBox> selectedCheckBoxes = workoutPage.getChildren().stream()
                .filter(node -> node instanceof CheckBox).map(node -> (CheckBox) node).filter(CheckBox::isSelected)
                .collect(Collectors.toList());
        hour = parseComboBoxValue(fromHour);
        minute = parseComboBoxValue(fromMinute);
        hour2 = parseComboBoxValue(toHour);
        minute2 = parseComboBoxValue(toMinute);

        // check if the user has selected at least 1 and at most 10 exercises and a day
        if (selectedCheckBoxes.size() < 1 || selectedCheckBoxes.size() > 10
                || weekDay.getSelectedToggle() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Select exercises carefully");
            if ((selectedCheckBoxes.size() < 1 || selectedCheckBoxes.size() > 10))
                alert.setContentText("Please select at least 1 and at most 10 exercises");
            else if (weekDay.getSelectedToggle() == null)
                alert.setContentText("Please select a day");
            alert.showAndWait();
            return;
        }

        // check if the time is valid
        if (hour > hour2 || (hour == hour2 && minute > minute2) || hour < 0 || hour > 23 || minute < 0
                || minute > 59 || hour2 < 0 || hour2 > 23 || minute2 < 0 || minute2 > 59) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid time");
            alert.setContentText("Please select a valid time");
            alert.showAndWait();
            return;
        }

        // Format workout text
        StringBuilder workoutText = new StringBuilder();
        for (CheckBox checkBox : selectedCheckBoxes) {
            workoutText.append(checkBox.getText()).append("\n");
        }

        RadioButton selectedDayButton = (RadioButton) weekDay.getSelectedToggle();
        String dayName = selectedDayButton.getText().toUpperCase();

        try {
            // Save to database
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO workouts (user_id, day_of_week, workout_text, start_hour, start_minute, end_hour, end_minute) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, dayName);
            pstmt.setString(3, workoutText.toString());
            pstmt.setInt(4, hour);
            pstmt.setInt(5, minute);
            pstmt.setInt(6, hour2);
            pstmt.setInt(7, minute2);
            pstmt.executeUpdate();

            // Get the generated workout ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int workoutId = rs.getInt(1);

                // add the workout to the calendar
                LocalTime startTime = LocalTime.of(hour, minute);
                LocalTime endTime = LocalTime.of(hour2, minute2);

        // Create calendar entry
                Entry<String> workoutEntry = new Entry<>("Workout");
        java.time.DayOfWeek dayOfWeek = java.time.DayOfWeek.valueOf(dayName);
        java.time.DayOfWeek today = LocalDate.now().getDayOfWeek();
        LocalDate date;
        if (dayOfWeek.getValue() < today.getValue()) {
            date = LocalDate.now().with(TemporalAdjusters.previousOrSame(dayOfWeek));
        } else {
            date = LocalDate.now().with(TemporalAdjusters.nextOrSame(dayOfWeek));
        }
        workoutEntry.setInterval(date, startTime, date, endTime);
        showHomeInfo.addWorkoutToCalendar(workoutEntry);

        // create button for the workout
        Button workoutButton = new Button();
        // Format button text
        StringBuilder buttonText = new StringBuilder();
                buttonText.append(workoutText);
        buttonText.append(String.format("%02d:%02d - %02d:%02d", hour, minute, hour2, minute2));
        workoutButton.setText(buttonText.toString());

        // Style the button
        workoutButton.setMaxWidth(Double.MAX_VALUE);
        workoutButton.setStyle("-fx-background-color: #230850; -fx-text-fill: white; -fx-font-size: 12px;");

                // Convert day name to index
        int dayIndex = switch (dayName) {
            case "MONDAY" -> 0;
            case "TUESDAY" -> 1;
            case "WEDNESDAY" -> 2;
            case "THURSDAY" -> 3;
            case "FRIDAY" -> 4;
            case "SATURDAY" -> 5;
            case "SUNDAY" -> 6;
            default -> -1;
        };

        // Add click handler to delete the button and calendar entry
        workoutButton.setOnAction(clickEvent -> {
                    try {
                        // Remove from database
                        String deleteSql = "DELETE FROM workouts WHERE id = ? AND user_id = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                        deleteStmt.setInt(1, workoutId);
                        deleteStmt.setInt(2, currentUserId);
                        deleteStmt.executeUpdate();

            // Remove from calendar
            CalendarModel.getWorkoutCalendar().removeEntry(workoutEntry);

            // Remove from grid
            if (dayIndex != -1 && SceneController.weekGrid[dayIndex] != null) {
                SceneController.weekGrid[dayIndex].getChildren().remove(workoutButton);
            }
            // Remove from stored list
            SceneController.removeWorkoutButton(workoutButton);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
        });

        if (dayIndex != -1) {
            // Store the button information with calendar entry
            SceneController.storeWorkoutButton(dayName, buttonText.toString(), workoutEntry, workoutButton);

            // Add the button to the grid
                    SceneController.weekGrid[dayIndex].add(workoutButton, 0,
                            SceneController.weekGrid[dayIndex].getRowCount());
            javafx.scene.layout.RowConstraints rowConstraints = new javafx.scene.layout.RowConstraints();
            rowConstraints.setPrefHeight(60);
            SceneController.weekGrid[dayIndex].getRowConstraints().add(rowConstraints);
        }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("Failed to save workout to database");
            alert.showAndWait();
            return;
        }

        closePopupWindow();
    }

    public void closePopupWindow() {
        Stage stage = (Stage) workoutPage.getScene().getWindow();
        stage.close();
    }

    private int parseComboBoxValue(ComboBox<Integer> comboBox) {
        Object value = comboBox.getValue();
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                // Handle invalid input (e.g., show an error or set a default)
                return -1; // or throw an exception
            }
        } else {
            return -1; // or throw an exception
        }
    }
}
