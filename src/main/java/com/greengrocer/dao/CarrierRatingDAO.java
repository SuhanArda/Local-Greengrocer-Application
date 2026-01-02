package com.greengrocer.dao;

import com.greengrocer.models.CarrierRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CarrierRating operations.
 */
public class CarrierRatingDAO {
    /**
     * The {@link DatabaseAdapter} instance used for managing database connections.
     * <p>
     * This adapter ensures that a valid connection is available for executing SQL
     * queries.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    private final DatabaseAdapter dbAdapter;

    /**
     * Default constructor for {@code CarrierRatingDAO}.
     * <p>
     * Initializes the DAO by obtaining the singleton instance of
     * {@link DatabaseAdapter}.
     * This constructor should be called to create an instance of the DAO for
     * performing database operations.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public CarrierRatingDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Retrieves all rating records associated with a specific carrier.
     * <p>
     * This method executes a {@code SELECT} query joining the <b>CarrierRatings</b>
     * table
     * with the <b>UserInfo</b> table to fetch details for both the carrier and the
     * customer.
     * </p>
     * <ul>
     * <li>The results are ordered by {@code created_at} in descending order (newest
     * first).</li>
     * <li>Includes the carrier's full name and the customer's full name in the
     * result objects.</li>
     * </ul>
     *
     * @param carrierId The unique identifier (ID) of the carrier whose ratings are
     *                  being requested.
     * @return A {@link List} of {@link CarrierRating} objects representing the
     *         ratings found.
     *         Returns an empty list if no ratings are found or if a
     *         {@link SQLException} occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<CarrierRating> findByCarrier(int carrierId) {
        List<CarrierRating> ratings = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS carrier_name, cu.full_name AS customer_name " +
                "FROM CarrierRatings r JOIN UserInfo c ON r.carrier_id = c.id " +
                "JOIN UserInfo cu ON r.customer_id = cu.id WHERE r.carrier_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                ratings.add(mapResultSetToRating(rs));
        } catch (SQLException e) {
            System.err.println("Find ratings error: " + e.getMessage());
        }
        return ratings;
    }

    /**
     * Calculates the average rating score for a specific carrier.
     * <p>
     * This method performs an aggregate {@code AVG} query on the
     * <b>CarrierRatings</b> table.
     * </p>
     *
     * @param carrierId The unique identifier (ID) of the carrier.
     * @return The average rating as a {@code double}.
     *         Returns {@code 0.0} if the carrier has no ratings or if a database
     *         error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public double getAverageRating(int carrierId) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM CarrierRatings WHERE carrier_id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getDouble("avg_rating");
        } catch (SQLException e) {
            System.err.println("Get average rating error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Checks if a customer has already submitted a rating for a specific order.
     * <p>
     * This prevents duplicate ratings for the same transaction. It counts the
     * number of rows
     * matching the given {@code orderId} and {@code customerId}.
     * </p>
     *
     * @param orderId    The unique identifier of the order.
     * @param customerId The unique identifier of the customer.
     * @return {@code true} if a rating already exists for this order by this
     *         customer;
     *         {@code false} otherwise.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean hasRated(int orderId, int customerId) {
        String sql = "SELECT COUNT(*) FROM CarrierRatings WHERE order_id = ? AND customer_id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Has rated error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Persists a new {@link CarrierRating} to the database.
     * <p>
     * This method inserts a new record into the <b>CarrierRatings</b> table with
     * the provided details.
     * It also retrieves the auto-generated primary key (ID) and updates the passed
     * {@code rating} object.
     * </p>
     *
     * @param rating The {@link CarrierRating} object containing the rating details
     *               (score, comment, IDs, etc.).
     * @return The same {@link CarrierRating} object, updated with the newly
     *         generated database ID.
     *         If the insertion fails, the ID may remain unset (0).
     * 
     * @author Ramazan Birkan Öztürk
     */
    public CarrierRating create(CarrierRating rating) {
        String sql = "INSERT INTO CarrierRatings (carrier_id, customer_id, order_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, rating.getCarrierId());
            stmt.setInt(2, rating.getCustomerId());
            stmt.setInt(3, rating.getOrderId());
            stmt.setInt(4, rating.getRating());
            stmt.setString(5, rating.getComment());
            stmt.setTimestamp(6, Timestamp.valueOf(rating.getCreatedAt()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                rating.setId(rs.getInt(1));
        } catch (SQLException e) {
            System.err.println("Create rating error: " + e.getMessage());
        }
        return rating;
    }

    private CarrierRating mapResultSetToRating(ResultSet rs) throws SQLException {
        CarrierRating rating = new CarrierRating();
        rating.setId(rs.getInt("id"));
        rating.setCarrierId(rs.getInt("carrier_id"));
        rating.setCustomerId(rs.getInt("customer_id"));
        rating.setOrderId(rs.getInt("order_id"));
        rating.setRating(rs.getInt("rating"));
        rating.setComment(rs.getString("comment"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            rating.setCreatedAt(createdAt.toLocalDateTime());
        rating.setCarrierName(rs.getString("carrier_name"));
        rating.setCustomerName(rs.getString("customer_name"));
        return rating;
    }
}
