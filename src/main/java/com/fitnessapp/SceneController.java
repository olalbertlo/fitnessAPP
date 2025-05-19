package com.fitnessapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import java.io.IOException;

public class SceneController {
    private Stage stage;
    private Scene scene;
    private Parent root;

    public void switchSceneButton(ActionEvent event) {
        Node source = (Node) event.getSource();
        String buttonId = source.getId(), scenePath = "";
        switch (buttonId) {
            case "homeButton":
                scenePath = "Home.fxml";
                break;
            case "workoutButton":
                scenePath = "Workout.fxml";
                break;
            case "dietButton":
                scenePath = "Diet.fxml";
                break;
            case "profileButton":
                scenePath = "Profile.fxml";
                break;
            default:
                scenePath = "Home.fxml";
                break;
        }
        try {
            root = FXMLLoader.load(getClass().getResource(scenePath));
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
