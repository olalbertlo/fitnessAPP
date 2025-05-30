package com.fitnessapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection {
    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "fitness_app";
    private static final String USER = "root"; // Change this to your MySQL username
    private static final String PASS = "Albert"; // Change this to your MySQL password

    private static Connection connection = null;

    // Get database connection
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Register JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // First, connect to MySQL server without selecting a database
                Connection tempConnection = DriverManager.getConnection(DB_URL, USER, PASS);

                // Create database if it doesn't exist
                String createDatabase = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
                tempConnection.createStatement().executeUpdate(createDatabase);
                tempConnection.close();

                // Now connect to the database
                connection = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
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

    public static boolean userExists(String userId) {
        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
                            userPassword VARCHAR(255) NOT NULL,
                            display_name VARCHAR(100) NOT NULL,
                            height DECIMAL(5,2),
                            weight DECIMAL(5,2),
                            age INT,
                            gender ENUM('Male', 'Female'),
                            fitness_target VARCHAR(50),
                            exercise_frequency INT,
                            meal_preference TEXT,
                            profile_image MEDIUMBLOB,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            last_login TIMESTAMP NULL
                        )
                    """;

            // Create alarms table
            String createAlarmsTable = """
                        CREATE TABLE IF NOT EXISTS alarms (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            hour INT NOT NULL,
                            minute INT NOT NULL,
                            task VARCHAR(50) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """;

            // Create workouts table
            String createWorkoutsTable = """
                        CREATE TABLE IF NOT EXISTS workouts (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            day_of_week VARCHAR(20) NOT NULL,
                            workout_text TEXT NOT NULL,
                            start_hour INT NOT NULL,
                            start_minute INT NOT NULL,
                            end_hour INT NOT NULL,
                            end_minute INT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """;

            // Create diets table
            String createDietsTable = """
                        CREATE TABLE IF NOT EXISTS diets (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            day_index INT NOT NULL,
                            meal_index INT NOT NULL,
                            meal_text TEXT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """;

            // Execute the create table statements
            connection.createStatement().execute(createUsersTable);
            connection.createStatement().execute(createAlarmsTable);
            connection.createStatement().execute(createWorkoutsTable);
            connection.createStatement().execute(createDietsTable);

            System.out.println("Database tables initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}