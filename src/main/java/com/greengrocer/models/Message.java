package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * Message model representing communication between users.
 * Maps to the Messages table in the database.
 * 
 * @author Suhan Arda Öner
 */
public class Message {
    /** Unique identifier for the message. */
    private int id;
    /** ID of the sender user. */
    private int senderId;
    /** ID of the receiver user. */
    private int receiverId;
    /** Subject of the message. */
    private String subject;
    /** Content body of the message. */
    private String content;
    /** Flag indicating if the message has been read by the receiver. */
    private boolean isRead;
    /** Optional ID of the parent message if this is a reply. */
    private Integer parentId;
    /** Timestamp when the message was created. */
    private LocalDateTime createdAt;

    // Additional fields for display
    /** Name of the sender (for display purposes). */
    private String senderName;
    /** Name of the receiver (for display purposes). */
    private String receiverName;

    /**
     * Default constructor.
     * Initializes creation time to now and isRead to false.
     * 
     * @author Suhan Arda Öner
     */
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    /**
     * Full constructor for Message.
     *
     * @param id         the unique identifier
     * @param senderId   the sender's user ID
     * @param receiverId the receiver's user ID
     * @param subject    the message subject
     * @param content    the message content
     * @param isRead     the read status
     * @param parentId   the parent message ID (can be null)
     * @param createdAt  the creation timestamp
     * 
     * @author Suhan Arda Öner
     */
    public Message(int id, int senderId, int receiverId, String subject,
            String content, boolean isRead, Integer parentId, LocalDateTime createdAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.content = content;
        this.isRead = isRead;
        this.parentId = parentId;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    /**
     * Gets the message ID.
     * 
     * @return the message ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the message ID.
     * 
     * @param id the message ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the sender ID.
     * 
     * @return the sender ID
     * 
     * @author Suhan Arda Öner
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * Sets the sender ID.
     * 
     * @param senderId the sender ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    /**
     * Gets the receiver ID.
     * 
     * @return the receiver ID
     * 
     * @author Suhan Arda Öner
     */
    public int getReceiverId() {
        return receiverId;
    }

    /**
     * Sets the receiver ID.
     * 
     * @param receiverId the receiver ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    /**
     * Gets the subject.
     * 
     * @return the subject
     * 
     * @author Suhan Arda Öner
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject.
     * 
     * @param subject the subject to set
     * 
     * @author Suhan Arda Öner
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the content.
     * 
     * @return the content
     * 
     * @author Suhan Arda Öner
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content.
     * 
     * @param content the content to set
     * 
     * @author Suhan Arda Öner
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Checks if the message is read.
     * 
     * @return true if read, false otherwise
     * 
     * @author Suhan Arda Öner
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Sets the read status.
     * 
     * @param read the read status to set
     * 
     * @author Suhan Arda Öner
     */
    public void setRead(boolean read) {
        isRead = read;
    }

    /**
     * Gets the parent message ID.
     * 
     * @return the parent message ID
     * 
     * @author Suhan Arda Öner
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * Sets the parent message ID.
     * 
     * @param parentId the parent message ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
     * 
     * @author Suhan Arda Öner
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * 
     * @param createdAt the creation timestamp to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the sender name.
     * 
     * @return the sender name
     * 
     * @author Suhan Arda Öner
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the sender name.
     * 
     * @param senderName the sender name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Gets the receiver name.
     * 
     * @return the receiver name
     * 
     * @author Suhan Arda Öner
     */
    public String getReceiverName() {
        return receiverName;
    }

    /**
     * Sets the receiver name.
     * 
     * @param receiverName the receiver name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    /**
     * Check if this message is a reply to another message.
     * 
     * @return true if this is a reply
     * 
     * @author Suhan Arda Öner
     */
    public boolean isReply() {
        return parentId != null;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
