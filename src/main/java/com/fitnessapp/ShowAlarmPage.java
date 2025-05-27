package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
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

    @FXML
    public void initialize() {
        setupIntegerSlider(sliderForHour);
        setupIntegerSlider(sliderForMinute);
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

        // each click create a new scheduler on a new thread
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        });

        int hour = (int) sliderForHour.getValue(), minute = (int) sliderForMinute.getValue();

        String task;
        if (toEat.isSelected())
            task = "To Eat";
        else if (toExercise.isSelected())
            task = "To Exercise";
        else
            task = "Do Something";

        // add the alarm to the grid
        String labelText = String.format("AT %02d:%02d %s", hour, minute, task);
        Button button = new Button(labelText);

        Alarm alarm = new Alarm(hour, minute, task, button, scheduler);
        alarms.add(alarm);
        refreshGrid();

        // use scheduler to detect if the alarm need to be triggered
        scheduler.scheduleAtFixedRate(() -> {
            LocalTime currentTime = LocalTime.now();
            if (currentTime.getHour() == hour && currentTime.getMinute() == minute) {
                try {
                    displayTray();
                } catch (AWTException e) {
                    e.printStackTrace();
                }

                // remove the alarmbutton if the alarm is triggered
                javafx.application.Platform.runLater(() -> {
                    alarms.remove(alarm);
                    refreshGrid();
                });
                scheduler.shutdown();
            }
        }, 0, 10, TimeUnit.SECONDS);
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
