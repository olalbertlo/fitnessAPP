package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fitnessapp.database.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ShowAlarmPage {

    public static int gridRow = 0, gridColumn = 0;

    private static class Alarm {
        int id;
        int hour, minute;
        String task;
        Button button;
        ScheduledExecutorService scheduler;

        public Alarm(int id, int hour, int minute, String task, Button button, ScheduledExecutorService scheduler) {
            this.id = id;
            this.hour = hour;
            this.minute = minute;
            this.task = task;
            this.button = button;
            this.scheduler = scheduler;
        }
    }

    private List<Alarm> alarms = new ArrayList<>();
    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        // Reload alarms when user ID is set
        loadAlarms();
    }

    @FXML
    public void initialize() {
        setupIntegerSlider(sliderForHour);
        setupIntegerSlider(sliderForMinute);
    }

    private void loadAlarms() {
        // Clear existing alarms
        alarms.forEach(alarm -> {
            if (alarm.scheduler != null) {
                alarm.scheduler.shutdown();
            }
        });
        alarms.clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM alarms WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            // get alarms details by user id
            while (rs.next()) {
                int id = rs.getInt("id");
                int hour = rs.getInt("hour");
                int minute = rs.getInt("minute");
                String task = rs.getString("task");

                // Create scheduler for each alarm
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
                    thread.setDaemon(true);
                    return thread;
                });

                // Create button for the alarm
                String labelText = String.format("AT %02d:%02d %s", hour, minute, task);
                Button button = new Button(labelText);

                // Click to remove alarm
                button.setOnAction(event -> {
                    Alarm alarmToRemove = alarms.stream()
                            .filter(a -> a.id == id)
                            .findFirst()
                            .orElse(null);

                    if (alarmToRemove != null) {
                        deleteAlarm(alarmToRemove);
                    }
                });

                // Create and add alarm
                Alarm alarm = new Alarm(id, hour, minute, task, button, scheduler);
                alarms.add(alarm);

                // Schedule the alarm
                scheduleAlarm(alarm);
            }
            refreshGrid();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // delete alarm from database and remove from list
    private void deleteAlarm(Alarm alarm) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "DELETE FROM alarms WHERE id = ? AND user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, alarm.id);
            pstmt.setInt(2, currentUserId);
            pstmt.executeUpdate();

            alarm.scheduler.shutdown();
            alarms.remove(alarm);
            refreshGrid();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // save alarm to database
    private void saveAlarm(int hour, int minute, String task) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO alarms (user_id, hour, minute, task) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, currentUserId);
            pstmt.setInt(2, hour);
            pstmt.setInt(3, minute);
            pstmt.setString(4, task);
            pstmt.executeUpdate();

            // Get the generated ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);

                // Create scheduler
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
                    thread.setDaemon(true);
                    return thread;
                });

                // Create button
                String labelText = String.format("AT %02d:%02d %s", hour, minute, task);
                Button button = new Button(labelText);

                // Add click handler
                button.setOnAction(event -> {
                    Alarm alarmToRemove = alarms.stream()
                            .filter(a -> a.id == id)
                            .findFirst()
                            .orElse(null);

                    if (alarmToRemove != null) {
                        deleteAlarm(alarmToRemove);
                    }
                });

                // Create and add alarm
                Alarm alarm = new Alarm(id, hour, minute, task, button, scheduler);
                alarms.add(alarm);
                scheduleAlarm(alarm);
                refreshGrid();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // schedule alarm, timeup then remove
    private void scheduleAlarm(Alarm alarm) {
        alarm.scheduler.scheduleAtFixedRate(() -> {
            LocalTime currentTime = LocalTime.now();
            if (currentTime.getHour() == alarm.hour && currentTime.getMinute() == alarm.minute) {
                try {
                    displayTray();
                } catch (AWTException e) {
                    e.printStackTrace();
                }

                javafx.application.Platform.runLater(() -> {
                    deleteAlarm(alarm);
                });
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    // set the slider moves in integer
    private void setupIntegerSlider(Slider slider) {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.intValue());
        });
    }

    public void showAlarmPagePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/Alarm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Alarm");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Slider sliderForHour;
    @FXML
    private Slider sliderForMinute;
    @FXML
    private ToggleGroup chooseWhatToDo;
    @FXML
    private RadioButton toEat;
    @FXML
    private RadioButton toExercise;
    @FXML
    private GridPane yetTriggerAlarm;

    public void addAlarm(ActionEvent event) {
        int hour = (int) sliderForHour.getValue();
        int minute = (int) sliderForMinute.getValue();

        String task;
        if (toEat.isSelected())
            task = "To Eat";
        else if (toExercise.isSelected())
            task = "To Exercise";
        else
            task = "Do Something";

        saveAlarm(hour, minute, task);
    }

    public void refreshGrid() {
        yetTriggerAlarm.getChildren().clear();
        gridRow = 0;
        gridColumn = 0;
        for (Alarm alarm : alarms) {
            yetTriggerAlarm.add(alarm.button, gridColumn, gridRow);
            if (gridColumn == 1) {
                gridColumn = 0;
                gridRow++;
            } else if (gridColumn == 0 && gridRow <= 3) {
                gridColumn++;
            }
        }
    }

    // display windows notification
    public void displayTray() throws AWTException {
        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("icon.png"), "Tray Demo");
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Tray Demo");
        tray.add(trayIcon);
        if (chooseWhatToDo.getSelectedToggle() == null) {
            trayIcon.displayMessage("Alarm", "Time for do something", TrayIcon.MessageType.INFO);
        } else if (chooseWhatToDo.getSelectedToggle().equals(toEat)) {
            trayIcon.displayMessage("Alarm", "Time for eat", TrayIcon.MessageType.INFO);
        } else if (chooseWhatToDo.getSelectedToggle().equals(toExercise)) {
            trayIcon.displayMessage("Alarm", "Time for exercise", TrayIcon.MessageType.INFO);
        }
    }
}
