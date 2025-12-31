package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * Message model representing communication between users.
 * Maps to the Messages table in the database.
 */
public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String subject;
    private String content;
    private boolean isRead;
    private Integer parentId;
    private LocalDateTime createdAt;

    // Additional fields for display
    private String senderName;
    private String receiverName;

    // Default constructor
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Full constructor
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    /**
     * Check if this message is a reply to another message.
     * 
     * @return true if this is a reply
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
