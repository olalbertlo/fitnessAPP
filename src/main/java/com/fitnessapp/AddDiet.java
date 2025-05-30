package com.fitnessapp;

import javafx.scene.layout.AnchorPane;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.Node;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.fitnessapp.database.DatabaseConnection;
import javafx.scene.control.Alert;

public class AddDiet {
    private List<Button>[] mealButtons = new ArrayList[7];
    private int currentUserId;

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    public void initialize() {
        for (int i = 0; i < 7; i++) {
            if (mealButtons[i] == null)
                mealButtons[i] = new ArrayList<Button>();
            for (int j = 0; j < 3; j++) {
                final int dayIndex = i; // Create final variable for i
                final int mealIndex = j; // Create final variable for j
                Button button = new Button();
                button.setOnAction(e -> {
                    Button clickedButton = (Button) e.getSource();
                    weekDay.getChildren().remove(clickedButton);
                    SceneController.removeDietButton(clickedButton);
                    deleteDietFromDatabase(dayIndex, mealIndex);
                });
                // Set button properties for proper centering and sizing
                button.setMaxWidth(weekDay.getColumnConstraints().get(i).getPrefWidth());
                button.setMaxHeight(weekDay.getRowConstraints().get(j + 1).getPrefHeight() - 5);
                if (i % 2 == 0)
                    button.setStyle(
                            "-fx-alignment: center; -fx-background-color:rgb(26, 155, 187); -fx-text-fill: white;");
                else
                    button.setStyle(
                            "-fx-alignment: center; -fx-background-color:rgb(21, 165, 153); -fx-text-fill: white;");
                mealButtons[i].add(button);
            }
        }
    }

    @FXML
    private AnchorPane mealPane;
    @FXML
    private AnchorPane weekPane;
    @FXML
    private TextArea eat;
    @FXML
    private GridPane weekDay;

    public AddDiet(AnchorPane mealPane, AnchorPane weekPane, TextArea eat, GridPane weekDay) {
        this.mealPane = mealPane;
        this.weekPane = weekPane;
        this.eat = eat;
        this.weekDay = weekDay;
    }

    public boolean check(AnchorPane anchorPane) {
        AtomicBoolean isAnySelected = new AtomicBoolean(false);
        anchorPane.getChildren().forEach(child -> {
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isSelected()) {
                    isAnySelected.set(true);
                }
            }
        });
        return isAnySelected.get();
    }

    private int getDayIndex(String day) {
        return switch (day) {
            case "Mon" -> 0;
            case "Tue" -> 1;
            case "Wed" -> 2;
            case "Thu" -> 3;
            case "Fri" -> 4;
            case "Sat" -> 5;
            case "Sun" -> 6;
            default -> -1;
        };
    }

    private int getMealIndex(String meal) {
        return switch (meal) {
            case "Breakfast" -> 0;
            case "Lunch" -> 1;
            case "Dinner" -> 2;
            default -> -1;
        };
    }

    public void createMealButtons() {
        String eatWhat = eat.getText();
        weekPane.getChildren().forEach(child -> {
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isSelected()) {
                    String day = checkBox.getText();
                    int index = getDayIndex(day);
                    if (index != -1) {
                        mealPane.getChildren().forEach(child2 -> {
                            if (child2 instanceof CheckBox) {
                                CheckBox checkBox2 = (CheckBox) child2;
                                if (checkBox2.isSelected()) {
                                    String meal = checkBox2.getText();
                                    int mealIndex = getMealIndex(meal);
                                    if (mealIndex != -1) {
                                        mealButtons[index].get(mealIndex).setText(eatWhat);
                                        addDietToPane(index, mealIndex, mealButtons[index].get(mealIndex));
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Database Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void deleteDietFromDatabase(int day, int meal) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String deleteSql = "DELETE FROM diets WHERE user_id = ? AND day_index = ? AND meal_index = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, currentUserId);
            deleteStmt.setInt(2, day);
            deleteStmt.setInt(3, meal);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to delete diet from database");
        }
    }

    public void addDietToPane(int day, int meal, Button button) {
        if (button != null && !button.getText().isEmpty()) {
            try {
                // First find and remove any existing button and database entry at this position
                Button existingButton = null;
                for (Node node : weekDay.getChildren()) {
                    if (node instanceof Button) {
                        Integer columnIndex = GridPane.getColumnIndex(node);
                        Integer rowIndex = GridPane.getRowIndex(node);
                        if (columnIndex != null && rowIndex != null &&
                                columnIndex == day && rowIndex == (meal + 1)) {
                            existingButton = (Button) node;
                            break;
                        }
                    }
                }

                // Remove the existing button if found
                if (existingButton != null) {
                    // Remove from database
                    deleteDietFromDatabase(day, meal);

                    // Remove from UI
                    weekDay.getChildren().remove(existingButton);
                    SceneController.removeDietButton(existingButton);
                }

                // Add new entry to database
                Connection conn = DatabaseConnection.getConnection();
                String sql = "INSERT INTO diets (user_id, day_index, meal_index, meal_text) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, currentUserId);
                pstmt.setInt(2, day);
                pstmt.setInt(3, meal);
                pstmt.setString(4, button.getText());
                pstmt.executeUpdate();

                // Get the generated diet ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int dietId = rs.getInt(1);
                    // Store the diet ID in the button's properties
                    button.setUserData(dietId);
                }

                // Add the new button to UI
                weekDay.add(button, day, meal + 1);
                GridPane.setFillWidth(button, true);
                GridPane.setFillHeight(button, true);
                GridPane.setHalignment(button, javafx.geometry.HPos.CENTER);
                GridPane.setValignment(button, javafx.geometry.VPos.CENTER);
                SceneController.storeDietButton(day, meal, button.getText(), button);

                // Update button's click handler to also remove from database
                button.setOnAction(e -> {
                    // Delete from database using day and meal indices
                    deleteDietFromDatabase(day, meal);

                    // Remove from UI
                    weekDay.getChildren().remove(button);
                    SceneController.removeDietButton(button);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Failed to save diet to database");
            }
        }
    }
}
