package com.fitnessapp;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SceneController {

    // use VBox to store the calendar
    @FXML
    private VBox calendarContainer;

    private ShowHomeInfo showHomeInfo;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    public void initialize() {
        showHomeInfo = new ShowHomeInfo();
    }

    public void switchSceneButton(ActionEvent event) {
        Node source = (Node) event.getSource();
        String buttonId = source.getId(), scenePath = "";
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
}