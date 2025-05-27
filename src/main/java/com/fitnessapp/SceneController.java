package com.fitnessapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import java.io.IOException;
import javafx.stage.Modality;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import com.calendarfx.model.Entry;

public class SceneController {

    // use VBox to store the calendar
    @FXML
    private VBox calendarContainer;

    @FXML
    private javafx.scene.layout.GridPane Monday;
    @FXML
    private javafx.scene.layout.GridPane Tuesday;
    @FXML
    private javafx.scene.layout.GridPane Wednesday;
    @FXML
    private javafx.scene.layout.GridPane Thursday;
    @FXML
    private javafx.scene.layout.GridPane Friday;
    @FXML
    private javafx.scene.layout.GridPane Saturday;
    @FXML
    private javafx.scene.layout.GridPane Sunday;

    // Static reference to the current controller instance
    private static SceneController instance;

    // Grid panes for workout days - make them static and public
    public static javafx.scene.layout.GridPane[] weekGrid = new javafx.scene.layout.GridPane[7];

    // Flag to track if grids have been initialized
    private static boolean gridsInitialized = false;

    private static ShowHomeInfo showHomeInfo;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private String scenePath;

    // Static storage for workout buttons
    private static class WorkoutButton {
        String day;
        String text;
        Entry<String> calendarEntry; // Store the calendar entry
        Button button; // Store the actual button

        WorkoutButton(String day, String text, Entry<String> calendarEntry, Button button) {
            this.day = day;
            this.text = text;
            this.calendarEntry = calendarEntry;
            this.button = button;
        }
    }

    private static List<WorkoutButton> storedWorkoutButtons = new ArrayList<>();

    @FXML
    public void initialize() {
        // Store this instance
        instance = this;

        if (calendarContainer != null) {
            showHomeInfo = new ShowHomeInfo();
            showHomeInfo.initializeCalendar(calendarContainer);
        }

        // Check if all grids are available
        boolean allGridsAvailable = instance != null &&
                instance.Monday != null &&
                instance.Tuesday != null &&
                instance.Wednesday != null &&
                instance.Thursday != null &&
                instance.Friday != null &&
                instance.Saturday != null &&
                instance.Sunday != null;

        if (allGridsAvailable) {
            // Update the weekGrid array with the instance fields
            weekGrid[0] = instance.Monday;
            weekGrid[1] = instance.Tuesday;
            weekGrid[2] = instance.Wednesday;
            weekGrid[3] = instance.Thursday;
            weekGrid[4] = instance.Friday;
            weekGrid[5] = instance.Saturday;
            weekGrid[6] = instance.Sunday;

            // Restore any stored workout buttons in initialize
            // Cause initialize is called every time the scene is loaded
            restoreWorkoutButtons();

            // Only initialize grid properties if not done before
            if (!gridsInitialized) {
                initializeWorkoutGrids();
                gridsInitialized = true;
            }
        }
    }

    private void initializeWorkoutGrids() {
        for (javafx.scene.layout.GridPane grid : weekGrid) {
            if (grid != null) {
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new javafx.geometry.Insets(10));
            }
        }
    }

    public void switchSceneButton(ActionEvent event) {
        Node source = (Node) event.getSource();
        String buttonId = source.getId();
        switch (buttonId) {
            case "homeButton":
                scenePath = "/com/fitnessapp/Home.fxml";
                break;
            case "workoutButton":
                scenePath = "/com/fitnessapp/Workout.fxml";
                break;
            case "dietButton":
                scenePath = "/com/fitnessapp/Diet.fxml";
                break;
            case "profileButton":
                scenePath = "/com/fitnessapp/Profile.fxml";
                break;
            default:
                scenePath = "/com/fitnessapp/Home.fxml";
                break;
        }
        try {
            // add the scenepath to the root
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            root = loader.load();

            // If loading the home scene, initialize the calendar
            if (scenePath.equals("/com/fitnessapp/Home.fxml")) {
                SceneController controller = loader.getController();
                if (controller != null && controller.calendarContainer != null) {
                    controller.showHomeInfo.initializeCalendar(controller.calendarContainer);
                }
            }

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAlarm(ActionEvent event) {
        ShowAlarmPage showAlarmPage = new ShowAlarmPage();
        showAlarmPage.showAlarmPagePopup();
    }

    public void showAddWorkoutPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/AddWorkout.fxml"));
            Parent root = loader.load();
            AddWorkoutPage addWorkoutPage = loader.getController();
            addWorkoutPage.setShowHomeInfo(showHomeInfo);
            Stage stage = new Stage();
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Workout");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // store a workout button
    public static void storeWorkoutButton(String day, String text, Entry<String> calendarEntry, Button button) {
        storedWorkoutButtons.add(new WorkoutButton(day, text, calendarEntry, button));
    }

    // remove a workout button
    public static void removeWorkoutButton(Button button) {
        storedWorkoutButtons.removeIf(wb -> wb.button == button);
    }

    // restore all workout buttons
    private void restoreWorkoutButtons() {
        for (WorkoutButton wb : storedWorkoutButtons) {
            Button button = wb.button;
            button.setMaxWidth(Double.MAX_VALUE);
            button.setStyle("-fx-background-color: #230850; -fx-text-fill: white; -fx-font-size: 12px;");

            int dayIndex = switch (wb.day) {
                case "MONDAY" -> 0;
                case "TUESDAY" -> 1;
                case "WEDNESDAY" -> 2;
                case "THURSDAY" -> 3;
                case "FRIDAY" -> 4;
                case "SATURDAY" -> 5;
                case "SUNDAY" -> 6;
                default -> -1;
            };

            if (dayIndex != -1 && weekGrid[dayIndex] != null) {
                weekGrid[dayIndex].add(button, 0, weekGrid[dayIndex].getRowCount());
                javafx.scene.layout.RowConstraints rowConstraints = new javafx.scene.layout.RowConstraints();
                rowConstraints.setPrefHeight(60);
                weekGrid[dayIndex].getRowConstraints().add(rowConstraints);
            }
        }
    }
}
