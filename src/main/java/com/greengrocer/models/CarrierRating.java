package com.greengrocer.models;

import java.time.LocalDateTime;

/**
 * CarrierRating model representing customer rating for a carrier.
 * Maps to the CarrierRatings table in the database.
 * 
 * @author Suhan Arda Öner
 */
public class CarrierRating {
    /** Unique identifier for the rating. */
    private int id;
    /** ID of the carrier being rated. */
    private int carrierId;
    /** ID of the customer who gave the rating. */
    private int customerId;
    /** ID of the order associated with the rating. */
    private int orderId;
    /** Rating value (1-5). */
    private int rating;
    /** Optional comment provided by the customer. */
    private String comment;
    /** Timestamp when the rating was created. */
    private LocalDateTime createdAt;

    // Additional fields for display
    /** Name of the carrier (for display purposes). */
    private String carrierName;
    /** Name of the customer (for display purposes). */
    private String customerName;

    /**
     * Default constructor.
     * Initializes creation timestamp to current time.
     * 
     * @author Suhan Arda Öner
     */
    public CarrierRating() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Full constructor for CarrierRating.
     *
     * @param id         the unique identifier
     * @param carrierId  the carrier's ID
     * @param customerId the customer's ID
     * @param orderId    the order's ID
     * @param rating     the rating value (1-5)
     * @param comment    the comment text
     * @param createdAt  the creation timestamp
     * 
     * @author Suhan Arda Öner
     */
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

    /**
     * Gets the rating ID.
     * 
     * @return the rating ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the rating ID.
     * 
     * @param id the rating ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the carrier ID.
     * 
     * @return the carrier ID
     * 
     * @author Suhan Arda Öner
     */
    public int getCarrierId() {
        return carrierId;
    }

    /**
     * Sets the carrier ID.
     * 
     * @param carrierId the carrier ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    /**
     * Gets the customer ID.
     * 
     * @return the customer ID
     * 
     * @author Suhan Arda Öner
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer ID.
     * 
     * @param customerId the customer ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the order ID.
     * 
     * @return the order ID
     * 
     * @author Suhan Arda Öner
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the order ID.
     * 
     * @param orderId the order ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the rating value.
     * 
     * @return the rating value
     * 
     * @author Suhan Arda Öner
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the rating value.
     * Clamps the value between 1 and 5.
     * 
     * @param rating the rating value to set
     * 
     * @author Suhan Arda Öner
     */
    public void setRating(int rating) {
        if (rating < 1)
            rating = 1;
        if (rating > 5)
            rating = 5;
        this.rating = rating;
    }

    /**
     * Gets the comment.
     * 
     * @return the comment
     * 
     * @author Suhan Arda Öner
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the comment.
     * 
     * @param comment the comment to set
     * 
     * @author Suhan Arda Öner
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
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
     * Gets the carrier name.
     * 
     * @return the carrier name
     * 
     * @author Suhan Arda Öner
     */
    public String getCarrierName() {
        return carrierName;
    }

    /**
     * Sets the carrier name.
     * 
     * @param carrierName the carrier name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    /**
     * Gets the customer name.
     * 
     * @return the customer name
     * 
     * @author Suhan Arda Öner
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the customer name.
     * 
     * @param customerName the customer name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Get star representation of rating.
     * 
     * @return string with star characters
     * 
     * @author Suhan Arda Öner
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
