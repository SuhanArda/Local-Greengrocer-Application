package com.greengrocer.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model representing a customer order.
 * Maps to the OrderInfo table in the database.
 */
public class Order {
    private int id;
    private int customerId;
    private Integer carrierId;
    private LocalDateTime orderTime;
    private LocalDateTime requestedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private OrderStatus status;
    private double subtotal;
    private double vatAmount;
    private double discountAmount;
    private double totalCost;
    private String couponCode;
    private byte[] invoice;
    private String notes;
    private List<OrderItem> items;

    // Additional fields for display purposes
    private String customerName;
    private String customerAddress;
    private String carrierName;

    /**
     * Enum representing order statuses.
     */
    public enum OrderStatus {
        PENDING("pending"),
        SELECTED("selected"),
        DELIVERED("delivered"),
        CANCELLED("cancelled");

        private final String value;

        OrderStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static OrderStatus fromString(String text) {
            for (OrderStatus status : OrderStatus.values()) {
                if (status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown order status: " + text);
        }
    }

    // Default constructor
    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
    }

    // Full constructor
    public Order(int id, int customerId, Integer carrierId, LocalDateTime orderTime,
            LocalDateTime requestedDeliveryTime, LocalDateTime actualDeliveryTime,
            OrderStatus status, double subtotal, double vatAmount,
            double discountAmount, double totalCost, String couponCode,
            byte[] invoice, String notes) {
        this.id = id;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.orderTime = orderTime;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.actualDeliveryTime = actualDeliveryTime;
        this.status = status;
        this.subtotal = subtotal;
        this.vatAmount = vatAmount;
        this.discountAmount = discountAmount;
        this.totalCost = totalCost;
        this.couponCode = couponCode;
        this.invoice = invoice;
        this.notes = notes;
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Integer getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Integer carrierId) {
        this.carrierId = carrierId;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getRequestedDeliveryTime() {
        return requestedDeliveryTime;
    }

    public void setRequestedDeliveryTime(LocalDateTime requestedDeliveryTime) {
        this.requestedDeliveryTime = requestedDeliveryTime;
    }

    public LocalDateTime getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public byte[] getInvoice() {
        return invoice;
    }

    public void setInvoice(byte[] invoice) {
        this.invoice = invoice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    /**
     * Add an item to the order.
     * 
     * @param item the order item to add
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    /**
     * Check if order can be cancelled.
     * Orders can only be cancelled within 1 hour of placement
     * and only if status is PENDING.
     * 
     * @return true if order can be cancelled
     */
    public boolean canBeCancelled() {
        if (status != OrderStatus.PENDING) {
            return false;
        }
        LocalDateTime cancellationDeadline = orderTime.plusHours(1);
        return LocalDateTime.now().isBefore(cancellationDeadline);
    }

    /**
     * Get time remaining for cancellation in minutes.
     * 
     * @return minutes remaining, or 0 if cannot be cancelled
     */
    public long getCancellationTimeRemaining() {
        if (!canBeCancelled()) {
            return 0;
        }
        LocalDateTime cancellationDeadline = orderTime.plusHours(1);
        return java.time.Duration.between(LocalDateTime.now(), cancellationDeadline).toMinutes();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", status=" + status +
                ", totalCost=" + totalCost +
                '}';
    }
}
