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
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalTime;

public class ShowAlarmPage {

    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        setupIntegerSlider(sliderForHour);
        setupIntegerSlider(sliderForMinute);
    }

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

    public void addAlarm(ActionEvent event) {
        int hour = (int) sliderForHour.getValue(), minute = (int) sliderForMinute.getValue();

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            LocalTime currentTime = LocalTime.now();
            if (currentTime.getHour() == hour && currentTime.getMinute() == minute) {
                try {
                    displayTray();
                } catch (AWTException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10, TimeUnit.SECONDS);

    }

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
