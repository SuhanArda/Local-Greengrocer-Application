package com.greengrocer.dao;

import com.greengrocer.models.Coupon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Coupon operations.
 */
public class CouponDAO {
    private final DatabaseAdapter dbAdapter;

    public CouponDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

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

    public Coupon create(Coupon coupon) throws SQLException {
        String sql = "INSERT INTO Coupons (code, discount_percent, min_cart_value, max_uses, current_uses, user_id, valid_from, valid_until, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                coupon.setId(rs.getInt(1));
        }
        return coupon;
    }

    public boolean incrementUses(int couponId) throws SQLException {
        String sql = "UPDATE Coupons SET current_uses = current_uses + 1 WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deactivate(int couponId) throws SQLException {
        String sql = "UPDATE Coupons SET is_active = FALSE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, couponId);
            return stmt.executeUpdate() > 0;
        }
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
        return coupon;
    }
}
