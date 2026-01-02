package com.greengrocer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.greengrocer.models.Message;

/**
 * Data Access Object for {@link Message} operations.
 * <p>
 * Handles database interactions related to the messaging system, including
 * sending,
 * receiving, and retrieving messages between users.
 * </p>
 * 
 * @author Ramazan Birkan Öztürk
 */
public class MessageDAO {
    /** The database adapter for connection management. */
    private final DatabaseAdapter dbAdapter;

    /**
     * Default constructor.
     * <p>
     * Initializes the {@link DatabaseAdapter} instance.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public MessageDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Finds a specific message by its unique identifier.
     * <p>
     * Retrieves the message details along with the sender's and receiver's full
     * names
     * by joining with the <b>UserInfo</b> table.
     * </p>
     *
     * @param id The unique identifier of the message.
     * @return An {@link Optional} containing the {@link Message} if found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
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

    /**
     * Retrieves all messages received by a specific user (Inbox).
     * <p>
     * The results are ordered by {@code created_at} in descending order (newest
     * first).
     * Includes sender and receiver names.
     * </p>
     *
     * @param userId The unique identifier of the receiving user.
     * @return A {@link List} of {@link Message} objects received by the user.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
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

    /**
     * Retrieves all messages sent by a specific user (Outbox).
     * <p>
     * The results are ordered by {@code created_at} in descending order (newest
     * first).
     * Includes sender and receiver names.
     * </p>
     *
     * @param userId The unique identifier of the sending user.
     * @return A {@link List} of {@link Message} objects sent by the user.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<Message> findSentMessages(int userId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, s.full_name AS sender_name, r.full_name AS receiver_name " +
                "FROM Messages m JOIN UserInfo s ON m.sender_id = s.id " +
                "JOIN UserInfo r ON m.receiver_id = r.id WHERE m.sender_id = ? ORDER BY m.created_at DESC";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }

    /**
     * Counts the number of unread messages for a specific user.
     *
     * @param userId The unique identifier of the user.
     * @return The count of unread messages. Returns 0 if none found or error
     *         occurs.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
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

    /**
     * Creates and persists a new message in the database.
     * <p>
     * Inserts the message details and retrieves the auto-generated ID, updating the
     * passed
     * {@link Message} object.
     * </p>
     *
     * @param message The {@link Message} object to be sent/created.
     * @return The updated {@link Message} object with its new database ID.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
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

    /**
     * Marks a specific message as read.
     * <p>
     * Updates the {@code is_read} flag to {@code TRUE} for the given message ID.
     * </p>
     *
     * @param messageId The unique identifier of the message to mark as read.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
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
