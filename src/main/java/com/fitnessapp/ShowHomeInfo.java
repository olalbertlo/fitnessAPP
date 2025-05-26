package com.fitnessapp;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.application.Platform;
import javafx.scene.Node;

public class ShowHomeInfo {
    private CalendarView calendarView;
    private Calendar workoutCalendar;

    public void initializeCalendar(VBox container) {
        // clear the container
        container.getChildren().clear();

        calendarView = new CalendarView();

        workoutCalendar = new Calendar("Workouts");
        workoutCalendar.setStyle(Calendar.Style.STYLE1);

        // Calendar : calendar -> source -> view

        // Create a calendar source and add the calendar
        CalendarSource calendarSource = new CalendarSource("Fitness Schedule");
        calendarSource.getCalendars().add(workoutCalendar);

        // Add the calendar source to the view
        calendarView.getCalendarSources().add(calendarSource);

        // set the view of the calendar
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPageToolBarControls(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowSearchField(false);
        calendarView.setShowSearchResultsTray(false);
        calendarView.setShowToolBar(false);

        // show week view only
        calendarView.showWeekPage();

        // Add some sample entries
        addSampleWorkouts();

        calendarView.setMaxHeight(Double.MAX_VALUE);
        calendarView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(calendarView, Priority.ALWAYS);
        container.getChildren().add(calendarView);

        // Block mouse events on the "WeekDayHeaderView" in com.calendarfx.view after
        // the UI is rendered
        Platform.runLater(() -> {
            Node weekHeader = findNodeByClass(calendarView, "com.calendarfx.view.WeekDayHeaderView");
            if (weekHeader != null) {
                weekHeader.setMouseTransparent(true);
                System.out.println(
                        "Blocked mouse events on: " + weekHeader.getClass() + " " + weekHeader.getStyleClass());
            } else {
                System.out.println("WeekDayHeaderView not found!");
            }
        });
    }

    private void addSampleWorkouts() {
        // Monday - Morning Workout
        Entry<String> mondayWorkout = new Entry<>("Morning Workout");
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        mondayWorkout.setInterval(monday, LocalTime.of(7, 0), monday, LocalTime.of(8, 0));
        workoutCalendar.addEntry(mondayWorkout);

        // Wednesday - Cardio
        Entry<String> wednesdayWorkout = new Entry<>("Cardio");
        LocalDate wednesday = LocalDate.now().with(java.time.DayOfWeek.WEDNESDAY);
        wednesdayWorkout.setInterval(wednesday, LocalTime.of(17, 0), wednesday, LocalTime.of(18, 0));
        workoutCalendar.addEntry(wednesdayWorkout);

        // Friday - HIIT
        Entry<String> fridayWorkout = new Entry<>("HIIT");
        LocalDate friday = LocalDate.now().with(java.time.DayOfWeek.FRIDAY);
        fridayWorkout.setInterval(friday, LocalTime.of(17, 0), friday, LocalTime.of(18, 0));
        workoutCalendar.addEntry(fridayWorkout);

        // Saturday - Yoga
        Entry<String> saturdayWorkout = new Entry<>("Yoga");
        LocalDate saturday = LocalDate.now().with(java.time.DayOfWeek.SATURDAY);
        saturdayWorkout.setInterval(saturday, LocalTime.of(10, 0), saturday, LocalTime.of(11, 0));
        workoutCalendar.addEntry(saturdayWorkout);
    }

    // Helper method to find a node by class name
    private Node findNodeByClass(Node node, String className) {
        if (node.getClass().getName().equals(className)) {
            return node;
        }
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                Node found = findNodeByClass(child, className);
                if (found != null)
                    return found;
            }
        }
        return null;
    }
}