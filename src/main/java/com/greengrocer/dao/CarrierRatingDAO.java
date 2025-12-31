package com.greengrocer.dao;

import com.greengrocer.models.CarrierRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CarrierRating operations.
 */
public class CarrierRatingDAO {
    private final DBConnection dbAdapter;

    public CarrierRatingDAO() {
        this.dbAdapter = DBConnection.getInstance();
    }

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
