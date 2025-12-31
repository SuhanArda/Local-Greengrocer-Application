package com.greengrocer.dao;

import com.greengrocer.models.User;
import com.greengrocer.models.User.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.greengrocer.utils.Argon2Hasher;

/**
 * Data Access Object for User operations.
 */
public class UserDAO {
    private final DBConnection dbAdapter;

    private final Argon2Hasher hasher = new Argon2Hasher();

    public UserDAO() {
        this.dbAdapter = DBConnection.getInstance();
    }

    /**
     * Authenticate a user by username and password.
     * 
     * @param username the username
     * @param password the password
     * @return Optional containing the user if authentication successful
     * @throws SQLException if a database access error occurs
     */
    public Optional<User> authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE username = ? AND is_active = TRUE";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (hasher.verify(password.toCharArray(), user.getPassword())) {
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Find a user by ID.
     * 
     * @param id the user ID
     * @return Optional containing the user if found
     * @throws SQLException if a database access error occurs
     */
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Find a user by username.
     * 
     * @param username the username
     * @return Optional containing the user if found
     * @throws SQLException if a database access error occurs
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE username = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Find all users by role.
     * 
     * @param role the user role
     * @return list of users with the specified role
     * @throws SQLException if a database access error occurs
     */
    public List<User> findByRole(UserRole role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM UserInfo WHERE role = ? AND is_active = TRUE ORDER BY full_name";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.getValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Get all carriers.
     * 
     * @return list of carrier users
     * @throws SQLException if a database access error occurs
     */
    public List<User> getAllCarriers() throws SQLException {
        return findByRole(UserRole.CARRIER);
    }

    /**
     * Get all customers.
     * 
     * @return list of customer users
     * @throws SQLException if a database access error occurs
     */
    public List<User> getAllCustomers() throws SQLException {
        return findByRole(UserRole.CUSTOMER);
    }

    /**
     * Create a new user.
     * 
     * @param user the user to create
     * @return the created user with ID set
     * @throws SQLException if a database access error occurs
     */
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO UserInfo (username, password, role, full_name, address, phone, email, loyalty_points, total_orders, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Hash password before saving
            String hashedPassword = hasher.hash(user.getPassword().toCharArray());
            user.setPassword(hashedPassword);

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole().getValue());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getEmail());

            if (user instanceof com.greengrocer.models.Customer) {
                stmt.setInt(8, ((com.greengrocer.models.Customer) user).getLoyaltyPoints());
                stmt.setInt(9, ((com.greengrocer.models.Customer) user).getTotalOrders());
            } else {
                stmt.setInt(8, 0);
                stmt.setInt(9, 0);
            }

            stmt.setBoolean(10, user.isActive());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                user.setId(rs.getInt(1));
            }
        }
        return user;
    }

    /**
     * Update an existing user.
     * 
     * @param user the user to update
     * @return true if update successful
     * @throws SQLException if a database access error occurs
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE UserInfo SET username = ?, password = ?, role = ?, full_name = ?, address = ?, phone = ?, email = ?, loyalty_points = ?, total_orders = ?, is_active = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Check if password needs hashing (if it's not already hashed)
            if (!user.getPassword().startsWith("$argon2id$")) {
                String hashedPassword = hasher.hash(user.getPassword().toCharArray());
                user.setPassword(hashedPassword);
            }

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole().getValue());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getPhone());
            stmt.setString(7, user.getEmail());

            if (user instanceof com.greengrocer.models.Customer) {
                stmt.setInt(8, ((com.greengrocer.models.Customer) user).getLoyaltyPoints());
                stmt.setInt(9, ((com.greengrocer.models.Customer) user).getTotalOrders());
            } else {
                stmt.setInt(8, 0);
                stmt.setInt(9, 0);
            }

            stmt.setBoolean(10, user.isActive());
            stmt.setInt(11, user.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Update a user's password.
     * 
     * @param userId      the user ID
     * @param newPassword the new password (plain text)
     * @return true if update successful
     * @throws SQLException if a database access error occurs
     */
    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE UserInfo SET password = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = hasher.hash(newPassword.toCharArray());
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deactivate a user (soft delete).
     * 
     * @param id the user ID
     * @return true if deactivation successful
     * @throws SQLException if a database access error occurs
     */
    public boolean deactivate(int id) throws SQLException {
        String sql = "UPDATE UserInfo SET is_active = FALSE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Increment the total orders count for a user.
     * 
     * @param userId the user ID
     * @return true if update successful
     * @throws SQLException if a database access error occurs
     */
    public boolean incrementTotalOrders(int userId) throws SQLException {
        String sql = "UPDATE UserInfo SET total_orders = total_orders + 1 WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Get the owner user.
     * 
     * @return Optional containing the owner
     * @throws SQLException if a database access error occurs
     */
    public Optional<User> getOwner() throws SQLException {
        List<User> owners = findByRole(UserRole.OWNER);
        return owners.isEmpty() ? Optional.empty() : Optional.of(owners.get(0));
    }

    /**
     * Check if a phone number already exists, excluding a specific user ID.
     * 
     * @param phone         the phone number to check
     * @param excludeUserId the user ID to exclude from the check
     * @return true if exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isPhoneExists(String phone, int excludeUserId) throws SQLException {
        String sql = "SELECT 1 FROM UserInfo WHERE phone = ? AND id != ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Check if an email already exists, excluding a specific user ID.
     * 
     * @param email         the email to check
     * @param excludeUserId the user ID to exclude from the check
     * @return true if exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean isEmailExists(String email, int excludeUserId) throws SQLException {
        String sql = "SELECT 1 FROM UserInfo WHERE email = ? AND id != ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /**
     * Map a ResultSet row to a User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        UserRole role = UserRole.fromString(rs.getString("role"));
        User user;

        switch (role) {
            case CUSTOMER:
                com.greengrocer.models.Customer customer = new com.greengrocer.models.Customer();
                customer.setLoyaltyPoints(rs.getInt("loyalty_points"));
                customer.setTotalOrders(rs.getInt("total_orders"));
                user = customer;
                break;
            case CARRIER:
                user = new com.greengrocer.models.Carrier();
                break;
            case OWNER:
                user = new com.greengrocer.models.Owner();
                break;
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }

        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(role);
        user.setFullName(rs.getString("full_name"));
        user.setAddress(rs.getString("address"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }
}
