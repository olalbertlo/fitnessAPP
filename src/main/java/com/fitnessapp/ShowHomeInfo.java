package com.fitnessapp;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.time.LocalTime;

public class ShowHomeInfo {
    private CalendarView calendarView;
    private Calendar workoutCalendar;

    public void initializeCalendar(VBox container) {
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

        // Add the calendar view to the container
        container.getChildren().add(calendarView);
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
}