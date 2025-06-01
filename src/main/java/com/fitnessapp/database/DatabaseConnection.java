package com.fitnessapp.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseConnection {
    private static Connection conn = null;

    // Cloud database configuration
    private static final String DB_HOST = "mysql-1fd2a996-fitness-database0604.k.aivencloud.com";
    private static final String DB_PORT = "26464";
    private static final String DB_NAME = "defaultdb";
    private static final String USER = "avnadmin";
    private static final String PASS = "AVNS_DxD3WWod-46MP_3mubZ";

    public static Connection getConnection() {
        if (conn == null) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Construct the connection URL with SSL requirements
                String url = String.format("jdbc:mysql://%s:%s/%s?sslMode=REQUIRED&user=%s&password=%s",
                        DB_HOST, DB_PORT, DB_NAME, USER, PASS);

                // Create the connection
                conn = DriverManager.getConnection(url);
                System.out.println("Database connected successfully");

                // Initialize tables after successful connection
                initializeTables();

            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("Connection to database failed!");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return conn;
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection!");
                e.printStackTrace();
            }
        }
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

            // Create remember_tokens table
            String createRememberTokensTable = """
                        CREATE TABLE IF NOT EXISTS remember_tokens (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            token VARCHAR(255) NOT NULL,
                            expiry_date TIMESTAMP NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                        )
                    """;

            // Create done_workouts table
            String createDoneWorkoutsTable = """
                        CREATE TABLE IF NOT EXISTS done_workouts (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id INT NOT NULL,
                            workout_id INT,
                            workout_text TEXT NOT NULL,
                            completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id) REFERENCES users(id)
                        )
                    """;

            // Execute the create table statements
            conn.createStatement().execute(createUsersTable);
            conn.createStatement().execute(createAlarmsTable);
            conn.createStatement().execute(createWorkoutsTable);
            conn.createStatement().execute(createDietsTable);
            conn.createStatement().execute(createRememberTokensTable);
            conn.createStatement().execute(createDoneWorkoutsTable);
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }
}