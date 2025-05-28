package com.fitnessapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/fitness_app";
    private static final String USER = "root"; // Change this to your MySQL username
    private static final String PASS = ""; // Change this to your MySQL password

    private static Connection connection = null;

    // Get database connection
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Open a connection
                connection = DriverManager.getConnection(DB_URL, USER, PASS);
                System.out.println("Database connection established successfully!");

                // Initialize database tables if they don't exist
                initializeTables();
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Close database connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed successfully!");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Initialize database tables
    private static void initializeTables() {
        try {
            // Create users table
            String createUsersTable = """
                        CREATE TABLE IF NOT EXISTS users (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            username VARCHAR(50) UNIQUE NOT NULL,
                            password VARCHAR(255) NOT NULL,
                            email VARCHAR(100) UNIQUE NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            last_login TIMESTAMP NULL
                        )
                    """;

            // Create user_profiles table
            String createProfilesTable = """
                        CREATE TABLE IF NOT EXISTS user_profiles (
                            user_id INT PRIMARY KEY,
                            first_name VARCHAR(50),
                            last_name VARCHAR(50),
                            age INT,
                            gender VARCHAR(10),
                            height FLOAT,
                            weight FLOAT,
                            fitness_goal VARCHAR(100),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                        )
                    """;

            // Create workouts table
            String createWorkoutsTable = """
                        CREATE TABLE IF NOT EXISTS workouts (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT,
                            day_of_week VARCHAR(10) NOT NULL,
                            workout_name VARCHAR(100) NOT NULL,
                            start_time TIME NOT NULL,
                            end_time TIME NOT NULL,
                            exercises TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                        )
                    """;

            // Create alarms table
            String createAlarmsTable = """
                        CREATE TABLE IF NOT EXISTS alarms (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT,
                            hour INT NOT NULL,
                            minute INT NOT NULL,
                            task VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                        )
                    """;

            // Execute the create table statements
            connection.createStatement().execute(createUsersTable);
            connection.createStatement().execute(createProfilesTable);
            connection.createStatement().execute(createWorkoutsTable);
            connection.createStatement().execute(createAlarmsTable);

            System.out.println("Database tables initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}