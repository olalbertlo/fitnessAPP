package com.fitnessapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
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

public class AddWorkoutPage {

    private ShowHomeInfo showHomeInfo;

    public void setShowHomeInfo(ShowHomeInfo showHomeInfo) {
        System.out.println("ShowHomeInfo set: " + showHomeInfo);
        this.showHomeInfo = showHomeInfo;
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
        // check if the weekDay is selected
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
        // add the workout to the calendar
        LocalTime startTime = LocalTime.of(hour, minute);
        LocalTime endTime = LocalTime.of(hour2, minute2);
        RadioButton selectedDayButton = (RadioButton) weekDay.getSelectedToggle();
        String dayName = selectedDayButton.getText().toUpperCase();
        showHomeInfo.addWorkoutToCalendar("Sample Workout", dayName, startTime, endTime);
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
