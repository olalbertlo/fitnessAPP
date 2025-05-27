package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.*;
import java.nio.file.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
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
        int hour, minute;
        String task;
        Button button;
        ScheduledExecutorService scheduler;

        public Alarm(int hour, int minute, String task, Button button, ScheduledExecutorService scheduler) {
            this.hour = hour;
            this.minute = minute;
            this.task = task;
            this.button = button;
            this.scheduler = scheduler;
        }
    }

    private List<Alarm> alarms = new ArrayList<>();
    private int currentUserId;

    private static final String ALARMS_FILE = "alarms.json";

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    @FXML
    public void initialize() {
        setupIntegerSlider(sliderForHour);
        setupIntegerSlider(sliderForMinute);
        loadAlarms();
    }

    private void loadAlarms() {
        File file = new File(ALARMS_FILE);
        if (!file.exists()) {
            return;
        }

        try {
            JSONParser parser = new JSONParser();
            JSONArray alarmsArray = (JSONArray) parser.parse(new FileReader(ALARMS_FILE));

            for (Object obj : alarmsArray) {
                JSONObject alarmJson = (JSONObject) obj;
                int hour = ((Long) alarmJson.get("hour")).intValue();
                int minute = ((Long) alarmJson.get("minute")).intValue();
                String task = (String) alarmJson.get("task");

                // Create scheduler for each alarm
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
                    thread.setDaemon(true);
                    return thread;
                });

                // Create button for the alarm
                String labelText = String.format("AT %02d:%02d %s", hour, minute, task);
                Button button = new Button(labelText);

                // Add click handler to remove alarm
                button.setOnAction(event -> {
                    Alarm alarmToRemove = alarms.stream()
                            .filter(a -> a.button == button)
                            .findFirst()
                            .orElse(null);

                    if (alarmToRemove != null) {
                        alarmToRemove.scheduler.shutdown(); // Stop the scheduler
                        alarms.remove(alarmToRemove);
                        saveAlarms();
                        refreshGrid();
                    }
                });

                // Create and add alarm
                Alarm alarm = new Alarm(hour, minute, task, button, scheduler);
                alarms.add(alarm);

                // Schedule the alarm
                scheduleAlarm(alarm);
            }
            refreshGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAlarms() {
        JSONArray alarmsArray = new JSONArray();

        for (Alarm alarm : alarms) {
            JSONObject alarmJson = new JSONObject();
            alarmJson.put("hour", alarm.hour);
            alarmJson.put("minute", alarm.minute);
            alarmJson.put("task", alarm.task);
            alarmsArray.add(alarmJson);
        }

        try (FileWriter file = new FileWriter(ALARMS_FILE)) {
            file.write(alarmsArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // schedule alarm , timeup then remove
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
                    alarms.remove(alarm);
                    saveAlarms();
                    refreshGrid();
                });
                alarm.scheduler.shutdown();
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

        // Create scheduler for the new alarm
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        });

        // Create button for the alarm
        String labelText = String.format("AT %02d:%02d %s", hour, minute, task);
        Button button = new Button(labelText);

        // Add click handler to remove alarm
        button.setOnAction(e -> {
            Alarm alarmToRemove = alarms.stream()
                    .filter(a -> a.button == button)
                    .findFirst()
                    .orElse(null);

            if (alarmToRemove != null) {
                alarmToRemove.scheduler.shutdown(); // Stop the scheduler
                alarms.remove(alarmToRemove);
                saveAlarms();
                refreshGrid();
            }
        });

        // Create and add alarm
        Alarm alarm = new Alarm(hour, minute, task, button, scheduler);
        alarms.add(alarm);
        saveAlarms();
        refreshGrid();

        // Schedule the alarm
        scheduleAlarm(alarm);
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
