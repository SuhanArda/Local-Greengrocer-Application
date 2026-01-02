package com.greengrocer.dao;

import java.sql.*;
import java.util.Optional;

/**
 * Data Access Object for managing system-wide settings.
 * <p>
 * Handles the retrieval and modification of global application configurations
 * stored in the {@code SystemSettings} table.
 * </p>
 * 
 * @author Burak Özevin
 */
public class SystemSettingDAO {
    /** The database adapter for connection management. */
    private final DatabaseAdapter dbAdapter;

    /**
     * Default constructor.
     * <p>
     * Initializes the {@link DatabaseAdapter} and ensures the settings table exists
     * with default values populated.
     * </p>
     * 
     * @author Burak Özevin
     */
    public SystemSettingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
        ensureTableExists();
    }

    /**
     * Ensures the {@code SystemSettings} table exists in the database.
     * <p>
     * If the table does not exist, it creates it and populates it with default
     * configuration values (e.g., global minimum order amount).
     * </p>
     * 
     * @author Burak Özevin
     */
    private void ensureTableExists() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS SystemSettings (
                        setting_key VARCHAR(50) PRIMARY KEY,
                        setting_value VARCHAR(255) NOT NULL,
                        description VARCHAR(255)
                    )
                """;
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Insert default values if not exist
            insertDefault("GLOBAL_MIN_ORDER_AMOUNT", "50.0", "Minimum cart amount required for checkout");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts a default setting if it does not already exist.
     * <p>
     * Uses {@code INSERT IGNORE} to skip insertion if the key is already present.
     * </p>
     *
     * @param key   The unique key for the setting.
     * @param value The default value for the setting.
     * @param desc  A brief description of what the setting controls.
     * 
     * @author Burak Özevin
     */
    private void insertDefault(String key, String value, String desc) {
        String sql = "INSERT IGNORE INTO SystemSettings (setting_key, setting_value, description) VALUES (?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.setString(2, value);
            stmt.setString(3, desc);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the value of a specific setting by its key.
     *
     * @param key The unique key of the setting.
     * @return An {@link Optional} containing the setting value if found;
     *         {@link Optional#empty()} otherwise.
     * 
     * @author Burak Özevin
     */
    public Optional<String> getValue(String key) {
        String sql = "SELECT setting_value FROM SystemSettings WHERE setting_key = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Updates the value of a specific setting.
     * <p>
     * If the setting does not exist, it will be created (via fallback logic).
     * </p>
     *
     * @param key   The unique key of the setting.
     * @param value The new value to set.
     * 
     * @author Burak Özevin
     */
    public void setValue(String key, String value) {
        String sql = "UPDATE SystemSettings SET setting_value = ? WHERE setting_key = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, key);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                // Insert if not exists (should be handled by ensureTableExists/insertDefault
                // but safe fallback)
                insertDefault(key, value, "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the global minimum order amount required for checkout.
     * <p>
     * Defaults to {@code 50.0} if the setting is not found or invalid.
     * </p>
     *
     * @return The minimum order amount as a {@code double}.
     * 
     * @author Burak Özevin
     */
    public double getGlobalMinOrderAmount() {
        return getValue("GLOBAL_MIN_ORDER_AMOUNT")
                .map(Double::parseDouble)
                .orElse(50.0);
    }

    /**
     * Sets the global minimum order amount.
     *
     * @param amount The new minimum order amount.
     * 
     * @author Burak Özevin
     */
    public void setGlobalMinOrderAmount(double amount) {
        setValue("GLOBAL_MIN_ORDER_AMOUNT", String.valueOf(amount));
    }
}
