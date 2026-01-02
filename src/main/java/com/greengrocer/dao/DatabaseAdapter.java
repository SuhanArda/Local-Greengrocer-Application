package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database adapter for managing MySQL database connections.
 * <p>
 * This class implements the <b>Singleton</b> design pattern to ensure a single
 * global access point
 * for database connection management, although it creates a new
 * {@link Connection} for each request
 * to avoid issues with closed result sets in a multi-threaded or sequential
 * access environment.
 * </p>
 * 
 * @author Burak Özevin
 */
public class DatabaseAdapter {
    private static final String URL = "jdbc:mysql://localhost:3306/greengrocer_db";
    private static final String USERNAME = "myuser";
    private static final String PASSWORD = "1234";

    private static DatabaseAdapter instance;
    private Connection connection;

    /**
     * Private constructor to enforce the Singleton pattern.
     * <p>
     * Loads the MySQL JDBC driver ({@code com.mysql.cj.jdbc.Driver}).
     * If the driver is not found, an error message is printed to
     * {@code System.err}.
     * </p>
     * 
     * @author Burak Özevin
     */
    private DatabaseAdapter() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Retrieves the single instance of {@code DatabaseAdapter}.
     * <p>
     * If the instance does not exist, it is created in a thread-safe manner.
     * </p>
     *
     * @return The singleton {@link DatabaseAdapter} instance.
     * 
     * @author Burak Özevin
     */
    public static synchronized DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    /**
     * Establishes and returns a new connection to the database.
     * <p>
     * <b>Note:</b> A new connection is created for each call to prevent issues with
     * {@link java.sql.ResultSet} being closed prematurely when sharing connections.
     * </p>
     *
     * @return A valid {@link Connection} object to the MySQL database.
     * @throws SQLException If a database access error occurs or the url is null.
     * 
     * @author Burak Özevin
     */
    public Connection getConnection() throws SQLException {
        // Create new connection each time to avoid ResultSet closed errors
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Closes the currently stored connection if it exists and is open.
     * <p>
     * This method is primarily for cleaning up the internal connection reference,
     * though {@link #getConnection()} currently returns new connections that should
     * be closed by the caller (e.g., using try-with-resources).
     * </p>
     * 
     * @author Burak Özevin
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Tests the database connection validity.
     * <p>
     * Attempts to establish a connection and checks if it is open.
     * </p>
     *
     * @return {@code true} if a connection can be successfully established;
     *         {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the database is accessible by validating a connection.
     * <p>
     * Uses {@link Connection#isValid(int)} with a 5-second timeout.
     * </p>
     *
     * @return {@code true} if the database is accessible and responds within the
     *         timeout;
     *         {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean isDatabaseAccessible() {
        try {
            Connection conn = getConnection();
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}
