package com.greengrocer.models;

/**
 * OrderItem model representing a single item in an order.
 * Maps to the OrderItems table in the database.
 */
public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private String productName;
    private double amount;
    private double unitPrice;
    private double totalPrice;

    // Default constructor
    public OrderItem() {
    }

    // Full constructor
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

    // Constructor for creating new order items
    public OrderItem(int productId, String productName, double amount, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.amount = amount;
        this.unitPrice = unitPrice;
        this.totalPrice = amount * unitPrice;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        this.totalPrice = this.amount * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.amount * this.unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    /**
     * Recalculate total price based on amount and unit price.
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
