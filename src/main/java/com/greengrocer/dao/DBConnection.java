package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/greengrocer_db";
    private static final String USERNAME = "myuser";
    private static final String PASSWORD = "1234";

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        // Create new connection each time to avoid ResultSet closed errors
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isDatabaseAccessible() {
        try {
            Connection conn = getConnection();
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
