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

        workoutCalendar = CalendarModel.getWorkoutCalendar();
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

        // Make calendar read-only
        calendarView.setEntryDetailsPopOverContentCallback(param -> null);
        calendarView.setContextMenuCallback(param -> null);
        calendarView.setEntryFactory(param -> null);
        calendarView.setEntryEditPolicy(param -> false);
        calendarView.setEntryDetailsCallback(param -> null);

        // show week view only
        calendarView.showWeekPage();

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
            }
        });
    }

    public void addWorkoutToCalendar(String workoutName, String dayName, LocalTime startTime, LocalTime endTime) {
        java.time.DayOfWeek dayOfWeek = java.time.DayOfWeek.valueOf(dayName);
        LocalDate date = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek));
        Entry<String> workout = new Entry<>(workoutName);
        workout.setInterval(date, startTime, date, endTime);
        workoutCalendar.addEntry(workout);
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