package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * CarrierRating model representing customer rating for a carrier.
 * Maps to the CarrierRatings table in the database.
 */
public class CarrierRating {
    private int id;
    private int carrierId;
    private int customerId;
    private int orderId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    // Additional fields for display
    private String carrierName;
    private String customerName;

    // Default constructor
    public CarrierRating() {
        this.createdAt = LocalDateTime.now();
    }

    // Full constructor
    public CarrierRating(int id, int carrierId, int customerId, int orderId,
            int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.carrierId = carrierId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1)
            rating = 1;
        if (rating > 5)
            rating = 5;
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Get star representation of rating.
     * 
     * @return string with star characters
     */
    public String getStarRating() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    @Override
    public String toString() {
        return "CarrierRating{" +
                "carrierId=" + carrierId +
                ", rating=" + rating +
                ", stars=" + getStarRating() +
                '}';
    }
}
