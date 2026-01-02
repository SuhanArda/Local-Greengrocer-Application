package com.greengrocer.dao;

import com.greengrocer.models.User;
import com.greengrocer.models.User.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.greengrocer.utils.Argon2Hasher;

/**
 * Data Access Object for {@link User} operations.
 * <p>
 * Manages all database interactions related to users, including authentication,
 * registration, profile updates, and role-based retrieval.
 * </p>
 * 
 * @author Ramazan Birkan Öztürk
 */
public class UserDAO {
    /** The database adapter for connection management. */
    private final DatabaseAdapter dbAdapter;

    /** The Argon2 hasher for password security. */
    private final Argon2Hasher hasher = new Argon2Hasher();

    /**
     * Default constructor.
     * <p>
     * Initializes the {@link DatabaseAdapter} instance.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public UserDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Authenticates a user using their username and password.
     * <p>
     * Verifies the provided credentials against the stored hash in the database.
     * Only active users can be authenticated.
     * </p>
     *
     * @param username The username of the user.
     * @param password The plain text password to verify.
     * @return An {@link Optional} containing the {@link User} if authentication
     *         succeeds;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Finds a user by their unique identifier.
     *
     * @param id The unique identifier of the user.
     * @return An {@link Optional} containing the {@link User} if found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Finds a user by their username.
     *
     * @param username The username to search for.
     * @return An {@link Optional} containing the {@link User} if found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves all active users with a specific role.
     * <p>
     * Results are sorted alphabetically by full name.
     * </p>
     *
     * @param role The {@link UserRole} to filter by.
     * @return A {@link List} of {@link User} objects with the specified role.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves all active carriers.
     * <p>
     * Convenience method for {@code findByRole(UserRole.CARRIER)}.
     * </p>
     *
     * @return A {@link List} of carrier users.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<User> getAllCarriers() throws SQLException {
        return findByRole(UserRole.CARRIER);
    }

    /**
     * Retrieves all active customers.
     * <p>
     * Convenience method for {@code findByRole(UserRole.CUSTOMER)}.
     * </p>
     *
     * @return A {@link List} of customer users.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<User> getAllCustomers() throws SQLException {
        return findByRole(UserRole.CUSTOMER);
    }

    /**
     * Creates a new user in the database.
     * <p>
     * This method:
     * </p>
     * <ul>
     * <li>Hashes the password using Argon2id before storage.</li>
     * <li>Inserts user details into {@code UserInfo}.</li>
     * <li>Handles role-specific fields (e.g., loyalty points for customers).</li>
     * <li>Retrieves and sets the generated user ID.</li>
     * </ul>
     *
     * @param user The {@link User} object to create.
     * @return The updated {@link User} object with its new database ID.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Creates a new carrier with a temporary (unhashed) password.
     * <p>
     * <b>Security Note:</b> The password is stored in plain text to allow the
     * carrier
     * to log in for the first time. The system enforces a password change upon
     * first login.
     * </p>
     *
     * @param carrier The {@link User} object representing the carrier.
     * @return The updated {@link User} object with its new database ID.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public User createCarrierWithTempPassword(User carrier) throws SQLException {
        String sql = "INSERT INTO UserInfo (username, password, role, full_name, address, phone, email, loyalty_points, total_orders, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Store password WITHOUT hashing for temp credentials
            stmt.setString(1, carrier.getUsername());
            stmt.setString(2, carrier.getPassword()); // Not hashed!
            stmt.setString(3, carrier.getRole().getValue());
            stmt.setString(4, carrier.getFullName());
            stmt.setString(5, carrier.getAddress());
            stmt.setString(6, carrier.getPhone());
            stmt.setString(7, carrier.getEmail());
            stmt.setInt(8, 0);
            stmt.setInt(9, 0);
            stmt.setBoolean(10, carrier.isActive());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                carrier.setId(rs.getInt(1));
            }
        }
        return carrier;
    }

    /**
     * Updates an existing user's profile information.
     * <p>
     * Automatically hashes the password if it has been changed (detects plain text
     * vs hash).
     * Updates all user fields including role-specific data.
     * </p>
     *
     * @param user The {@link User} object with updated information.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Updates a user's password.
     * <p>
     * Hashes the new password before storing it in the database.
     * </p>
     *
     * @param userId      The unique identifier of the user.
     * @param newPassword The new plain text password.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Updates a carrier's credentials (username and password) upon first login.
     * <p>
     * This finalizes the carrier's account setup by replacing the temporary
     * credentials
     * with permanent, hashed ones.
     * </p>
     *
     * @param userId      The unique identifier of the carrier.
     * @param newUsername The new username chosen by the carrier.
     * @param newPassword The new plain text password chosen by the carrier.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean updateCarrierCredentials(int userId, String newUsername, String newPassword) throws SQLException {
        String sql = "UPDATE UserInfo SET username = ?, password = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = hasher.hash(newPassword.toCharArray());
            stmt.setString(1, newUsername);
            stmt.setString(2, hashedPassword);
            stmt.setInt(3, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a carrier is logging in for the first time.
     * <p>
     * Determines this by checking if the stored password is NOT hashed (i.e., does
     * not
     * start with the Argon2id prefix).
     * </p>
     *
     * @param user The {@link User} object to check.
     * @return {@code true} if the user is a carrier with a temporary password;
     *         {@code false} otherwise.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean isCarrierFirstLogin(User user) {
        if (user == null || user.getRole() != UserRole.CARRIER) {
            return false;
        }
        // Check if password is hashed (Argon2id hashes start with $argon2id$)
        return user.getPassword() != null && !user.getPassword().startsWith("$argon2id$");
    }

    /**
     * Authenticates a carrier using a temporary (unhashed) password.
     * <p>
     * Used specifically for the first-time login flow where the password is stored
     * in plain text.
     * </p>
     *
     * @param username The username of the carrier.
     * @param password The plain text temporary password.
     * @return An {@link Optional} containing the {@link User} if authentication
     *         succeeds;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public Optional<User> authenticateCarrierTemp(String username, String password) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE username = ? AND role = 'carrier' AND is_active = TRUE";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                // Check if password matches directly (unhashed temporary password)
                if (!user.getPassword().startsWith("$argon2id$") &&
                        user.getPassword().equals(password)) {
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Deactivates a user account (soft delete).
     * <p>
     * Sets {@code is_active} to {@code FALSE}, preventing future logins but
     * preserving data.
     * </p>
     *
     * @param id The unique identifier of the user.
     * @return {@code true} if the deactivation was successful; {@code false}
     *         otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Increments the total number of orders for a user.
     * <p>
     * Typically used for customers to track order history statistics.
     * </p>
     *
     * @param userId The unique identifier of the user.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves the system owner.
     * <p>
     * Assumes there is only one owner in the system.
     * </p>
     *
     * @return An {@link Optional} containing the {@link User} (Owner) if found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public Optional<User> getOwner() throws SQLException {
        List<User> owners = findByRole(UserRole.OWNER);
        return owners.isEmpty() ? Optional.empty() : Optional.of(owners.get(0));
    }

    /**
     * Checks if a phone number is already in use by another user.
     *
     * @param phone         The phone number to check.
     * @param excludeUserId The ID of the user to exclude from the check (e.g., the
     *                      user being updated).
     * @return {@code true} if the phone number exists for another user;
     *         {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Checks if an email address is already in use by another user.
     *
     * @param email         The email address to check.
     * @param excludeUserId The ID of the user to exclude from the check.
     * @return {@code true} if the email exists for another user; {@code false}
     *         otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Maps a {@link ResultSet} row to a {@link User} object.
     * <p>
     * Instantiates the correct subclass (Customer, Carrier, or Owner) based on the
     * role.
     * </p>
     *
     * @param rs The {@link ResultSet} positioned at the current row.
     * @return The mapped {@link User} object.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
