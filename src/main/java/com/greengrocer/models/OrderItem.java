package com.greengrocer.models;

/**
 * OrderItem model representing a single item in an order.
 * Maps to the OrderItems table in the database.
 * 
 * @author Suhan Arda Öner
 */
public class OrderItem {
    /** Unique identifier for the order item. */
    private int id;
    /** ID of the order this item belongs to. */
    private int orderId;
    /** ID of the product. */
    private int productId;
    /** Name of the product (stored for historical accuracy). */
    private String productName;
    /** Quantity of the product. */
    private double amount;
    /** Unit price of the product at the time of order. */
    private double unitPrice;
    /** Total price for this item (amount * unitPrice). */
    private double totalPrice;

    /**
     * Default constructor.
     * 
     * @author Suhan Arda Öner
     */
    public OrderItem() {
    }

    /**
     * Full constructor for OrderItem.
     *
     * @param id          the unique identifier
     * @param orderId     the order ID
     * @param productId   the product ID
     * @param productName the product name
     * @param amount      the quantity
     * @param unitPrice   the unit price
     * @param totalPrice  the total price
     * 
     * @author Suhan Arda Öner
     */
    public OrderItem(int id, int orderId, int productId, String productName,
            double amount, double unitPrice, double totalPrice) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    /**
     * Constructor for creating new order items (without ID and orderId).
     * Automatically calculates total price.
     *
     * @param productId   the product ID
     * @param productName the product name
     * @param amount      the quantity
     * @param unitPrice   the unit price
     * 
     * @author Suhan Arda Öner
     */
    public OrderItem(int productId, String productName, double amount, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.totalPrice = amount * unitPrice;
    }

    // Getters and Setters

    /**
     * Gets the order item ID.
     * 
     * @return the order item ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the order item ID.
     * 
     * @param id the order item ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
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
     * Gets the product ID.
     * 
     * @return the product ID
     * 
     * @author Suhan Arda Öner
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Sets the product ID.
     * 
     * @param productId the product ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Gets the product name.
     * 
     * @return the product name
     * 
     * @author Suhan Arda Öner
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the product name.
     * 
     * @param productName the product name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * Gets the amount.
     * 
     * @return the amount
     * 
     * @author Suhan Arda Öner
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the amount.
     * Automatically updates total price.
     * 
     * @param amount the amount to set
     * 
     * @author Suhan Arda Öner
     */
    public void setAmount(double amount) {
        this.amount = amount;
        this.totalPrice = this.amount * this.unitPrice;
    }

    /**
     * Gets the unit price.
     * 
     * @return the unit price
     * 
     * @author Suhan Arda Öner
     */
    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the unit price.
     * Automatically updates total price.
     * 
     * @param unitPrice the unit price to set
     * 
     * @author Suhan Arda Öner
     */
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.amount * this.unitPrice;
    }

    /**
     * Gets the total price.
     * 
     * @return the total price
     * 
     * @author Suhan Arda Öner
     */
    public double getTotalPrice() {
        return totalPrice;
    }

    /**
     * Sets the total price.
     * 
     * @param totalPrice the total price to set
     * 
     * @author Suhan Arda Öner
     */
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Recalculate total price based on amount and unit price.
     * 
     * @author Suhan Arda Öner
     */
    public void recalculateTotal() {
        this.totalPrice = this.amount * this.unitPrice;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productName='" + productName + '\'' +
                ", amount=" + amount +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
