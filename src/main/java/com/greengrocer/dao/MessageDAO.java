package com.greengrocer.dao;

import com.greengrocer.models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Message operations.
 */
public class MessageDAO {
    private final DBConnection dbAdapter;

    public MessageDAO() {
        this.dbAdapter = DBConnection.getInstance();
    }

    public Optional<Message> findById(int id) throws SQLException {
        String sql = "SELECT m.*, s.full_name AS sender_name, r.full_name AS receiver_name " +
                "FROM Messages m JOIN UserInfo s ON m.sender_id = s.id " +
                "JOIN UserInfo r ON m.receiver_id = r.id WHERE m.id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return Optional.of(mapResultSetToMessage(rs));
        }
        return Optional.empty();
    }

    public List<Message> findReceivedMessages(int userId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, s.full_name AS sender_name, r.full_name AS receiver_name " +
                "FROM Messages m JOIN UserInfo s ON m.sender_id = s.id " +
                "JOIN UserInfo r ON m.receiver_id = r.id WHERE m.receiver_id = ? ORDER BY m.created_at DESC";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }

    public int getUnreadCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        }
        return 0;
    }

    public Message create(Message message) throws SQLException {
        String sql = "INSERT INTO Messages (sender_id, receiver_id, subject, content, is_read, parent_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            stmt.setString(3, message.getSubject());
            stmt.setString(4, message.getContent());
            stmt.setBoolean(5, message.isRead());
            stmt.setObject(6, message.getParentId());
            stmt.setTimestamp(7, Timestamp.valueOf(message.getCreatedAt()));
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                message.setId(rs.getInt(1));
        }
        return message;
    }

    public boolean markAsRead(int messageId) throws SQLException {
        String sql = "UPDATE Messages SET is_read = TRUE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        message.setSubject(rs.getString("subject"));
        message.setContent(rs.getString("content"));
        message.setRead(rs.getBoolean("is_read"));
        message.setParentId(rs.getObject("parent_id") != null ? rs.getInt("parent_id") : null);
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            message.setCreatedAt(createdAt.toLocalDateTime());
        message.setSenderName(rs.getString("sender_name"));
        message.setReceiverName(rs.getString("receiver_name"));
        return message;
    }
}
