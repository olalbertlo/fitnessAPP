package com.fitnessapp.models;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import com.fitnessapp.database.DatabaseConnection;

public class User {
    private int id;
    private String username;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // Constructor
    public User(int id, String username, Timestamp createdAt, Timestamp lastLogin) {
        this.id = id;
        this.username = username;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    // Static methods for user operations
    public static User register(String username, String password, String email) throws SQLException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // TODO: Hash the password before storing
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return login(username, password);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    public static User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, email, created_at, last_login FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            // TODO: Hash the password before comparing
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Update last login
                    updateLastLogin(rs.getInt("id"));

                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("last_login"));
                }
            }
        }
        return null;
    }

    private static void updateLastLogin(int userId) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    public static boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}