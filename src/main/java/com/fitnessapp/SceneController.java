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

public class SceneController {

    // use VBox to store the calendar
    @FXML
    private VBox calendarContainer;

    private static ShowHomeInfo showHomeInfo;
    private Stage stage;
    private Scene scene;
    private Parent root;
    private String scenePath;

    @FXML
    public void initialize() {
        if (calendarContainer != null) {
            showHomeInfo = new ShowHomeInfo();
            // initialize the calendar whenever the scene is loaded
            showHomeInfo.initializeCalendar(calendarContainer);
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
            System.out.println("ShowHomeInfo: " + showHomeInfo);
            addWorkoutPage.setShowHomeInfo(showHomeInfo);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Workout");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
