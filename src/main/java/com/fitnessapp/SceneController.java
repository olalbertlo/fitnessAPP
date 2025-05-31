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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TextArea;
import com.calendarfx.model.Entry;
import com.fitnessapp.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.image.ImageView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import java.sql.ResultSet;
import java.lang.StringBuilder;

public class SceneController {

    // use VBox to store the calendar
    @FXML
    private VBox calendarContainer;

    public VBox getCalendarContainer() {
        return calendarContainer;
    }

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

    // Static storage for diet buttons
    private static class DietButton {
        int day;
        int meal;
        String text;
        Button button;

        DietButton(int day, int meal, String text, Button button) {
            this.day = day;
            this.meal = meal;
            this.text = text;
            this.button = button;
        }
    }

    private static List<DietButton> storedDietButtons = new ArrayList<>();

    @FXML
    private AnchorPane mealPane;
    @FXML
    private AnchorPane weekPane;
    @FXML
    private TextArea eat;
    @FXML
    private GridPane weekDay;

    private AddDiet addDiet;

    private int currentUserId;

    @FXML
    private ImageView userImage;
    @FXML
    private Label userDisplayName;
    @FXML
    private Label userStatus;
    @FXML
    private Label userBMI;
    @FXML
    private Label userTarget;

    private LoadProfile loadProfile;

    @FXML
    private GridPane dailyTasks;

    @FXML
    public void initialize() {
        // Store this instance
        instance = this;

        // Initialize AddDiet after FXML components are loaded
        addDiet = new AddDiet(mealPane, weekPane, eat, weekDay);

        // Initialize profile if we're on the profile page
        if (userImage != null && userDisplayName != null) {
            loadProfile = new LoadProfile(userImage, userDisplayName, userStatus, userBMI, userTarget);
        }

        // Restore diet buttons if we're on the Diet page
        if (weekDay != null) {
            restoreDietButtons();
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
                scenePath = "/com/fitnessapp/Login.fxml";
                break;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(scenePath));
            root = loader.load();

            // Pass the user ID to the new controller
            SceneController newController = loader.getController();
            if (newController != null) {
                newController.setCurrentUserId(currentUserId);
            }

            // If loading the home scene, initialize the calendar
            if (scenePath.equals("/com/fitnessapp/Home.fxml")) {
                if (newController != null && newController.calendarContainer != null) {
                    newController.showHomeInfo = new ShowHomeInfo();
                    newController.showHomeInfo.initializeCalendar(newController.calendarContainer);
                    newController.loadHomePageData();
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

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        if (addDiet != null) {
            addDiet.setCurrentUserId(userId);
        }
        if (loadProfile != null) {
            loadProfile.setCurrentUserId(userId);
        }
    }

    public void showAlarm(ActionEvent event) {
        if (currentUserId <= 0) {
            System.err.println("No valid user ID found: " + currentUserId);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/Alarm.fxml"));
            Parent root = loader.load();

            ShowAlarmPage showAlarmPage = loader.getController();
            showAlarmPage.setCurrentUserId(currentUserId);
            System.out.println("Passing user ID to alarm page: " + currentUserId); // Debug print

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Alarm");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAddWorkoutPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/AddWorkout.fxml"));
            Parent root = loader.load();
            AddWorkoutPage addWorkoutPage = loader.getController();
            addWorkoutPage.setCurrentUserId(currentUserId);
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

    // Clear all workout buttons
    public static void clearWorkoutButtons() {
        storedWorkoutButtons.clear();
        // Clear all workout grids
        for (GridPane grid : weekGrid) {
            if (grid != null) {
                grid.getChildren().removeIf(node -> node instanceof Button);
            }
        }
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

    // store a diet button
    public static void storeDietButton(int day, int meal, String text, Button button) {
        storedDietButtons.add(new DietButton(day, meal, text, button));
    }

    // remove a diet button
    public static void removeDietButton(Button button) {
        storedDietButtons.removeIf(db -> db.button == button);
    }

    // restore all diet buttons
    private void restoreDietButtons() {
        if (weekDay != null) {
            // Clear existing buttons from the grid
            weekDay.getChildren().removeIf(node -> node instanceof Button);

            for (DietButton db : storedDietButtons) {
                Button button = new Button(db.text);
                button.setMaxWidth(weekDay.getColumnConstraints().get(db.day).getPrefWidth());
                button.setMaxHeight(weekDay.getRowConstraints().get(db.meal + 1).getPrefHeight() - 5);

                if (db.day % 2 == 0) {
                    button.setStyle(
                            "-fx-alignment: center; -fx-background-color:rgb(26, 155, 187); -fx-text-fill: white;");
                } else {
                    button.setStyle(
                            "-fx-alignment: center; -fx-background-color:rgb(21, 165, 153); -fx-text-fill: white;");
                }

                final int dayIndex = db.day;
                final int mealIndex = db.meal;

                // Set up removal action with proper user ID
                button.setOnAction(e -> {
                    Button clickedButton = (Button) e.getSource();
                    weekDay.getChildren().remove(clickedButton);
                    removeDietButton(clickedButton);
                    // Call deleteDietFromDatabase through addDiet instance
                    if (addDiet != null) {
                        addDiet.deleteDietFromDatabase(dayIndex, mealIndex);
                    }
                });

                weekDay.add(button, db.day, db.meal + 1);
                GridPane.setFillWidth(button, true);
                GridPane.setFillHeight(button, true);
                GridPane.setHalignment(button, javafx.geometry.HPos.CENTER);
                GridPane.setValignment(button, javafx.geometry.VPos.CENTER);

                // Update the stored button reference
                db.button = button;
            }
        }
    }

    // add diet to the pane
    public void addDiet(ActionEvent event) {
        if (addDiet != null) {
            addDiet.initialize();
            addDiet.setCurrentUserId(currentUserId);
            addDiet.createMealButtons();
        }
    }

    public void logOut(ActionEvent event) {
        // Clear the token
        Connection conn = DatabaseConnection.getConnection();
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM remember_tokens WHERE user_id = ?");
            pstmt.setInt(1, currentUserId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Clear all session data
        currentUserId = 0;
        storedWorkoutButtons.clear();
        storedDietButtons.clear();
        weekGrid = new javafx.scene.layout.GridPane[7];

        // Switch to login page
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fitnessapp/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadHomePageData() {
        if (calendarContainer != null) {
            LoadTheDataBase dataLoader = new LoadTheDataBase(currentUserId);
            dataLoader.setShowHomeInfo(showHomeInfo);
            if (dailyTasks != null) {
                dataLoader.setDailyTasks(dailyTasks);
            }
            dataLoader.loadAllData();
        }
    }

    // Keep this for backward compatibility, but make it use the new method
    public void initializeHomePageData() {
        if (showHomeInfo == null) {
            showHomeInfo = new ShowHomeInfo();
            showHomeInfo.initializeCalendar(calendarContainer);
        }
        loadHomePageData();
    }

    // add a done workout to the database
    public void addDoneWorkoutToDatabase() {
        String today = java.time.LocalDate.now().getDayOfWeek().toString().toUpperCase();
        Connection conn = DatabaseConnection.getConnection();

        try {
            // get the workout ID for today's workout
            String getWorkoutSql = "SELECT id, workout_text FROM workouts WHERE user_id = ? AND day_of_week = ?";
            PreparedStatement getWorkoutStmt = conn.prepareStatement(getWorkoutSql);
            getWorkoutStmt.setInt(1, currentUserId);
            getWorkoutStmt.setString(2, today);
            ResultSet rs = getWorkoutStmt.executeQuery();

            if (rs.next()) {
                int workoutId = rs.getInt("id");
                String[] allWorkouts = rs.getString("workout_text").split("[\\n]+");
                StringBuilder remainingWorkouts = new StringBuilder();
                StringBuilder completedWorkouts = new StringBuilder();

                // Get all checkboxes from dailyTasks
                List<CheckBox> checkBoxes = dailyTasks.getChildren().stream()
                        .filter(node -> node instanceof CheckBox)
                        .map(node -> (CheckBox) node)
                        .collect(java.util.stream.Collectors.toList());

                // Separate completed and remaining workouts
                for (String workout : allWorkouts) {
                    final String workoutTrim = workout.trim();
                    if (!workoutTrim.isEmpty()) {
                        boolean isCompleted = checkBoxes.stream()
                                .filter(cb -> cb.getText().equals(workoutTrim))
                                .findFirst()
                                .map(CheckBox::isSelected)
                                .orElse(false);

                        if (isCompleted) {
                            completedWorkouts.append(workoutTrim).append("\n");
                        } else {
                            remainingWorkouts.append(workoutTrim).append("\n");
                        }
                    }
                }

                // Only proceed if there are completed workouts
                if (completedWorkouts.length() > 0) {
                    // Insert completed workouts into done_workouts table
                    String insertDoneSql = "INSERT INTO done_workouts (user_id, workout_id, workout_text) VALUES (?, ?, ?)";
                    PreparedStatement insertDoneStmt = conn.prepareStatement(insertDoneSql);
                    insertDoneStmt.setInt(1, currentUserId);
                    insertDoneStmt.setInt(2, workoutId);
                    insertDoneStmt.setString(3, completedWorkouts.toString());
                    insertDoneStmt.executeUpdate();

                    // Update the original workout with remaining exercises
                    if (remainingWorkouts.length() > 0) {
                        String updateWorkoutSql = "UPDATE workouts SET workout_text = ? WHERE id = ? AND user_id = ?";
                        PreparedStatement updateWorkoutStmt = conn.prepareStatement(updateWorkoutSql);
                        updateWorkoutStmt.setString(1, remainingWorkouts.toString());
                        updateWorkoutStmt.setInt(2, workoutId);
                        updateWorkoutStmt.setInt(3, currentUserId);
                        updateWorkoutStmt.executeUpdate();
                    } else {
                        // If all workouts are completed, delete the workout
                        String deleteWorkoutSql = "DELETE FROM workouts WHERE id = ? AND user_id = ?";
                        PreparedStatement deleteWorkoutStmt = conn.prepareStatement(deleteWorkoutSql);
                        deleteWorkoutStmt.setInt(1, workoutId);
                        deleteWorkoutStmt.setInt(2, currentUserId);
                        deleteWorkoutStmt.executeUpdate();
                    }

                    // Refresh the display
                    loadHomePageData();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Database Error");
            alert.setContentText("Failed to update workout status");
            alert.showAndWait();
        }
    }
}
