package com.fitnessapp;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = getClass().getResource("/com/fitnessapp/Profile.fxml");
        System.out.println("FXML URL = " + fxml);   // null = 找不到

        Parent root = FXMLLoader.load(fxml);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Fitness APP");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
