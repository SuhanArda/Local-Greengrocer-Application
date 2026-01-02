package com.greengrocer.dao;

import com.greengrocer.models.Coupon;
import com.greengrocer.models.CustomerCouponUsage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Coupon operations.
 */
public class CouponDAO {
    /**
     * The {@link DatabaseAdapter} used to obtain database connections.
     */
    private final DatabaseAdapter dbAdapter;

    /**
     * Default constructor for {@code CouponDAO}.
     * <p>
     * Initializes the DAO and ensures that the necessary database schema (columns)
     * exists.
     * Specifically, it checks for and adds the {@code discount_type} column if it's
     * missing.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public CouponDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
        ensureSchema();
    }

    private void ensureSchema() {
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement()) {
            // Add discount_type column if it doesn't exist
            try {
                stmt.execute("ALTER TABLE Coupons ADD COLUMN discount_type VARCHAR(20) DEFAULT 'PERCENT'");
            } catch (SQLException e) {
                // Column likely exists, ignore
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds a valid, active coupon by its code.
     *
     * @param code the coupon code
     * @return an Optional containing the Coupon if found and active, empty
     *         otherwise
     * @throws SQLException if a database error occurs
     * 
     * @author Ramazan Birkan Öztürk
     */
    public Optional<Coupon> findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM Coupons WHERE code = ? AND is_active = TRUE";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return Optional.of(mapResultSetToCoupon(rs));
        }
        return Optional.empty();
    }

    /**
     * Retrieves all coupons from the database.
     * <p>
     * The list is sorted by the {@code valid_until} date in descending order,
     * showing the
     * coupons that expire furthest in the future (or most recently) first.
     * </p>
     *
     * @return A {@link List} of all {@link Coupon} objects in the system.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<Coupon> findAll() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM Coupons ORDER BY valid_until DESC";
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                coupons.add(mapResultSetToCoupon(rs));
        }
        return coupons;
    }

    /**
     * Creates a new coupon record in the database.
     * <p>
     * Inserts all coupon details including code, discount value, limits, and
     * validity period.
     * The generated ID is retrieved and set on the passed {@code coupon} object.
     * </p>
     *
     * @param coupon The {@link Coupon} object to be persisted.
     * @return The updated {@link Coupon} object with its new database ID.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public Coupon create(Coupon coupon) throws SQLException {
        String sql = "INSERT INTO Coupons (code, discount_percent, min_cart_value, max_uses, current_uses, user_id, valid_from, valid_until, is_active, discount_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, coupon.getCode());
            stmt.setDouble(2, coupon.getDiscountPercent());
            stmt.setDouble(3, coupon.getMinCartValue());
            stmt.setInt(4, coupon.getMaxUses());
            stmt.setInt(5, coupon.getCurrentUses());
            stmt.setObject(6, coupon.getUserId());
            stmt.setTimestamp(7, Timestamp.valueOf(coupon.getValidFrom()));
            stmt.setTimestamp(8, coupon.getValidUntil() != null ? Timestamp.valueOf(coupon.getValidUntil()) : null);
            stmt.setBoolean(9, coupon.isActive());
            stmt.setString(10, coupon.getDiscountType().name());
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                coupon.setId(rs.getInt(1));
        }
        return coupon;
    }

    /**
     * Increments the global usage count of a coupon.
     * <p>
     * This should be called when a coupon is successfully applied and an order is
     * finalized.
     * </p>
     *
     * @param couponId The unique identifier of the coupon.
     * @return {@code true} if the update was successful (one row affected);
     *         {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean incrementUses(int couponId) throws SQLException {
        String sql = "UPDATE Coupons SET current_uses = current_uses + 1 WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deactivates a coupon, preventing future use.
     * <p>
     * This method sets the {@code is_active} flag to {@code FALSE} for the
     * specified coupon.
     * </p>
     *
     * @param couponId The unique identifier of the coupon to deactivate.
     * @return {@code true} if the coupon was successfully deactivated;
     *         {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean deactivate(int couponId) throws SQLException {
        String sql = "UPDATE Coupons SET is_active = FALSE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Permanently deletes a coupon and all its associated usage records.
     * <p>
     * <b>Warning:</b> This operation is irreversible. It performs a transactional
     * delete:
     * <ol>
     * <li>Deletes all records from {@code CustomerCouponUsage} associated with this
     * coupon.</li>
     * <li>Deletes the coupon record itself from the {@code Coupons} table.</li>
     * </ol>
     * If any step fails, the entire transaction is rolled back.
     *
     * @param couponId The unique identifier of the coupon to delete.
     * @return {@code true} if the coupon was successfully deleted; {@code false}
     *         otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean delete(int couponId) throws SQLException {
        String deleteUsageSql = "DELETE FROM CustomerCouponUsage WHERE coupon_id = ?";
        String deleteCouponSql = "DELETE FROM Coupons WHERE id = ?";

        try (Connection conn = dbAdapter.getConnection()) {
            // Disable auto-commit for transaction
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Delete usages first
                try (PreparedStatement stmt = conn.prepareStatement(deleteUsageSql)) {
                    stmt.setInt(1, couponId);
                    stmt.executeUpdate();
                }

                // Delete coupon
                boolean deleted;
                try (PreparedStatement stmt = conn.prepareStatement(deleteCouponSql)) {
                    stmt.setInt(1, couponId);
                    deleted = stmt.executeUpdate() > 0;
                }

                conn.commit();
                return deleted;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    /**
     * Identifies and deletes all expired coupons from the database.
     * <p>
     * This method finds coupons where {@code valid_until} is in the past.
     * For each expired coupon, it performs a safe deletion (removing usages first)
     * within a transaction.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public void deleteExpired() {
        // We need to find expired IDs first, then delete usages, then delete coupons
        // Or just use a subquery delete if supported, but let's be safe with
        // transaction
        String findExpiredSql = "SELECT id FROM Coupons WHERE valid_until < NOW()";
        String deleteUsageSql = "DELETE FROM CustomerCouponUsage WHERE coupon_id = ?";
        String deleteCouponSql = "DELETE FROM Coupons WHERE id = ?";

        try (Connection conn = dbAdapter.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                // Find expired coupons
                try (Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(findExpiredSql)) {
                    while (rs.next()) {
                        int id = rs.getInt("id");

                        // Delete usage
                        try (PreparedStatement delUsage = conn.prepareStatement(deleteUsageSql)) {
                            delUsage.setInt(1, id);
                            delUsage.executeUpdate();
                        }

                        // Delete coupon
                        try (PreparedStatement delCoupon = conn.prepareStatement(deleteCouponSql)) {
                            delCoupon.setInt(1, id);
                            delCoupon.executeUpdate();
                        }
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds all valid coupons assigned to a specific customer that still have
     * remaining uses.
     * <p>
     * The query filters for:
     * <ul>
     * <li>Coupons assigned to the given {@code customerId}.</li>
     * <li>{@code uses_remaining} greater than 0.</li>
     * <li>Coupon is marked as active.</li>
     * <li>Coupon is not expired (valid_until is null or in the future).</li>
     * </ul>
     * Results are ordered by discount percentage in descending order (best
     * discounts first).
     *
     * @param customerId The unique identifier of the customer.
     * @return A {@link List} of {@link CustomerCouponUsage} objects, each
     *         containing the associated {@link Coupon}.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<CustomerCouponUsage> findCouponsForCustomer(int customerId) throws SQLException {
        List<CustomerCouponUsage> result = new ArrayList<>();
        String sql = """
                SELECT ccu.*, c.*
                FROM CustomerCouponUsage ccu
                JOIN Coupons c ON ccu.coupon_id = c.id
                WHERE ccu.customer_id = ?
                  AND ccu.uses_remaining > 0
                  AND c.is_active = TRUE
                  AND (c.valid_until IS NULL OR c.valid_until > NOW())
                ORDER BY c.discount_percent DESC
                """;
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CustomerCouponUsage usage = mapResultSetToCustomerCouponUsage(rs);
                usage.setCoupon(mapResultSetToCoupon(rs));
                result.add(usage);
            }
        }
        return result;
    }

    /**
     * Assigns a coupon to a customer with a specified number of uses.
     * <p>
     * If the assignment already exists (duplicate key), it updates the existing
     * record
     * by adding the new uses to the existing {@code uses_remaining}.
     * </p>
     *
     * @param couponId   The unique identifier of the coupon.
     * @param customerId The unique identifier of the customer.
     * @param uses       The number of uses to grant.
     * @return A {@link CustomerCouponUsage} object representing the assignment.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public CustomerCouponUsage assignCouponToCustomer(int couponId, int customerId, int uses) throws SQLException {
        String sql = "INSERT INTO CustomerCouponUsage (customer_id, coupon_id, uses_remaining) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE uses_remaining = uses_remaining + VALUES(uses_remaining)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, couponId);
            stmt.setInt(3, uses);
            stmt.executeUpdate();

            CustomerCouponUsage usage = new CustomerCouponUsage();
            usage.setCustomerId(customerId);
            usage.setCouponId(couponId);
            usage.setUsesRemaining(uses);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                usage.setId(rs.getInt(1));

            return usage;
        }
    }

    /**
     * Decrements the remaining uses for a specific customer's coupon.
     * <p>
     * This is atomic and ensures the count does not drop below zero.
     * </p>
     *
     * @param customerId The unique identifier of the customer.
     * @param couponId   The unique identifier of the coupon.
     * @return {@code true} if the decrement was successful (i.e., the user had uses
     *         remaining);
     *         {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean decrementCustomerCouponUses(int customerId, int couponId) throws SQLException {
        String sql = "UPDATE CustomerCouponUsage SET uses_remaining = uses_remaining - 1 " +
                "WHERE customer_id = ? AND coupon_id = ? AND uses_remaining > 0";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, couponId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves a specific coupon usage record for a customer.
     *
     * @param customerId The unique identifier of the customer.
     * @param couponId   The unique identifier of the coupon.
     * @return An {@link Optional} containing the {@link CustomerCouponUsage} if
     *         found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public Optional<CustomerCouponUsage> getCustomerCouponUsage(int customerId, int couponId) throws SQLException {
        String sql = "SELECT ccu.*, c.* FROM CustomerCouponUsage ccu " +
                "JOIN Coupons c ON ccu.coupon_id = c.id " +
                "WHERE ccu.customer_id = ? AND ccu.coupon_id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, couponId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CustomerCouponUsage usage = mapResultSetToCustomerCouponUsage(rs);
                usage.setCoupon(mapResultSetToCoupon(rs));
                return Optional.of(usage);
            }
        }
        return Optional.empty();
    }

    private Coupon mapResultSetToCoupon(ResultSet rs) throws SQLException {
        Coupon coupon = new Coupon();
        coupon.setId(rs.getInt("id"));
        coupon.setCode(rs.getString("code"));
        coupon.setDiscountPercent(rs.getDouble("discount_percent"));
        coupon.setMinCartValue(rs.getDouble("min_cart_value"));
        coupon.setMaxUses(rs.getInt("max_uses"));
        coupon.setCurrentUses(rs.getInt("current_uses"));
        coupon.setUserId(rs.getObject("user_id") != null ? rs.getInt("user_id") : null);
        Timestamp validFrom = rs.getTimestamp("valid_from");
        if (validFrom != null)
            coupon.setValidFrom(validFrom.toLocalDateTime());
        Timestamp validUntil = rs.getTimestamp("valid_until");
        if (validUntil != null)
            coupon.setValidUntil(validUntil.toLocalDateTime());
        coupon.setActive(rs.getBoolean("is_active"));
        String typeStr = rs.getString("discount_type");
        if (typeStr != null) {
            try {
                coupon.setDiscountType(Coupon.DiscountType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                coupon.setDiscountType(Coupon.DiscountType.PERCENT);
            }
        } else {
            coupon.setDiscountType(Coupon.DiscountType.PERCENT);
        }
        return coupon;
    }

    private CustomerCouponUsage mapResultSetToCustomerCouponUsage(ResultSet rs) throws SQLException {
        CustomerCouponUsage usage = new CustomerCouponUsage();
        usage.setId(rs.getInt("ccu.id"));
        usage.setCustomerId(rs.getInt("customer_id"));
        usage.setCouponId(rs.getInt("coupon_id"));
        usage.setUsesRemaining(rs.getInt("uses_remaining"));
        Timestamp assignedAt = rs.getTimestamp("assigned_at");
        if (assignedAt != null)
            usage.setAssignedAt(assignedAt.toLocalDateTime());
        return usage;
    }
}
