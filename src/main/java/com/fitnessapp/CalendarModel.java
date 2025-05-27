package com.fitnessapp;

public class CalendarModel {
    private static final com.calendarfx.model.Calendar workoutCalendar = new com.calendarfx.model.Calendar("Workouts");

    public static com.calendarfx.model.Calendar getWorkoutCalendar() {
        return workoutCalendar;
    }
}
