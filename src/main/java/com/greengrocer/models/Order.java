package com.greengrocer.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order model representing a customer order.
 * Maps to the OrderInfo table in the database.
 * 
 * @author Suhan Arda Öner
 */
public class Order {
    /** Unique identifier for the order. */
    private int id;
    /** ID of the customer who placed the order. */
    private int customerId;
    /** Optional ID of the carrier assigned to the order. */
    private Integer carrierId;
    /** Timestamp when the order was placed. */
    private LocalDateTime orderTime;
    /** Timestamp when the order is requested to be delivered. */
    private LocalDateTime requestedDeliveryTime;
    /** Timestamp when the order was actually delivered. */
    private LocalDateTime actualDeliveryTime;
    /** Current status of the order. */
    private OrderStatus status;
    /** Subtotal amount (before VAT and discounts). */
    private double subtotal;
    /** VAT amount. */
    private double vatAmount;
    /** Discount amount applied. */
    private double discountAmount;
    /** Total cost of the order. */
    private double totalCost;
    /** Coupon code applied to the order. */
    private String couponCode;
    /** Base64 encoded invoice (stored as CLOB/LONGTEXT). */
    private String invoice;
    /** Notes or special instructions for the order. */
    private String notes;
    /** List of items in the order. */
    private List<OrderItem> items;

    // Additional fields for display purposes
    /** Name of the customer (for display purposes). */
    private String customerName;
    /** Address of the customer (for display purposes). */
    private String customerAddress;
    /** Name of the carrier (for display purposes). */
    private String carrierName;

    /**
     * Enum representing order statuses.
     * 
     * @author Suhan Arda Öner
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

    /**
     * Default constructor.
     * Initializes items list, sets status to PENDING, and orderTime to now.
     * 
     * @author Suhan Arda Öner
     */
    public Order() {
        this.items = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.orderTime = LocalDateTime.now();
    }

    /**
     * Full constructor for Order.
     *
     * @param id                    the unique identifier
     * @param customerId            the customer ID
     * @param carrierId             the carrier ID (can be null)
     * @param orderTime             the order placement time
     * @param requestedDeliveryTime the requested delivery time
     * @param actualDeliveryTime    the actual delivery time
     * @param status                the order status
     * @param subtotal              the subtotal amount
     * @param vatAmount             the VAT amount
     * @param discountAmount        the discount amount
     * @param totalCost             the total cost
     * @param couponCode            the applied coupon code
     * @param invoice               the invoice data (Base64)
     * @param notes                 the order notes
     * 
     * @author Suhan Arda Öner
     */
    public Order(int id, int customerId, Integer carrierId, LocalDateTime orderTime,
            LocalDateTime requestedDeliveryTime, LocalDateTime actualDeliveryTime,
            OrderStatus status, double subtotal, double vatAmount,
            double discountAmount, double totalCost, String couponCode,
            String invoice, String notes) {
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

    /**
     * Gets the order ID.
     * 
     * @return the order ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the order ID.
     * 
     * @param id the order ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the customer ID.
     * 
     * @return the customer ID
     * @author Suhan Arda Öner
     */
    public int getCustomerId() {
        return customerId;
    }

    /**
     * Sets the customer ID.
     * 
     * @param customerId the customer ID to set
     * @author Suhan Arda Öner
     */
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    /**
     * Gets the carrier ID.
     * 
     * @return the carrier ID
     * @author Suhan Arda Öner
     */
    public Integer getCarrierId() {
        return carrierId;
    }

    /**
     * Sets the carrier ID.
     * 
     * @param carrierId the carrier ID to set
     * @author Suhan Arda Öner
     */
    public void setCarrierId(Integer carrierId) {
        this.carrierId = carrierId;
    }

    /**
     * Gets the order time.
     * 
     * @return the order time
     * @author Suhan Arda Öner
     */
    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    /**
     * Sets the order time.
     * 
     * @param orderTime the order time to set
     * @author Suhan Arda Öner
     */
    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    /**
     * Gets the requested delivery time.
     * 
     * @return the requested delivery time
     * @author Suhan Arda Öner
     */
    public LocalDateTime getRequestedDeliveryTime() {
        return requestedDeliveryTime;
    }

    /**
     * Sets the requested delivery time.
     * 
     * @param requestedDeliveryTime the requested delivery time to set
     * @author Suhan Arda Öner
     */
    public void setRequestedDeliveryTime(LocalDateTime requestedDeliveryTime) {
        this.requestedDeliveryTime = requestedDeliveryTime;
    }

    /**
     * Gets the actual delivery time.
     * 
     * @return the actual delivery time
     * @author Suhan Arda Öner
     */
    public LocalDateTime getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    /**
     * Sets the actual delivery time.
     * 
     * @param actualDeliveryTime the actual delivery time to set
     * @author Suhan Arda Öner
     */
    public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) {
        this.actualDeliveryTime = actualDeliveryTime;
    }

    /**
     * Gets the order status.
     * 
     * @return the order status
     * @author Suhan Arda Öner
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Sets the order status.
     * 
     * @param status the order status to set
     * @author Suhan Arda Öner
     */
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    /**
     * Gets the subtotal amount.
     * 
     * @return the subtotal amount
     * @author Suhan Arda Öner
     */
    public double getSubtotal() {
        return subtotal;
    }

    /**
     * Sets the subtotal amount.
     * 
     * @param subtotal the subtotal amount to set
     * @author Suhan Arda Öner
     */
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Gets the VAT amount.
     * 
     * @return the VAT amount
     * @author Suhan Arda Öner
     */
    public double getVatAmount() {
        return vatAmount;
    }

    /**
     * Sets the VAT amount.
     * 
     * @param vatAmount the VAT amount to set
     * @author Suhan Arda Öner
     */
    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    /**
     * Gets the discount amount.
     * 
     * @return the discount amount
     * @author Suhan Arda Öner
     */
    public double getDiscountAmount() {
        return discountAmount;
    }

    /**
     * Sets the discount amount.
     * 
     * @param discountAmount the discount amount to set
     * @author Suhan Arda Öner
     */
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    /**
     * Gets the total cost.
     * 
     * @return the total cost
     * @author Suhan Arda Öner
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Sets the total cost.
     * 
     * @param totalCost the total cost to set
     * @author Suhan Arda Öner
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Gets the coupon code.
     * 
     * @return the coupon code
     * @author Suhan Arda Öner
     */
    public String getCouponCode() {
        return couponCode;
    }

    /**
     * Sets the coupon code.
     * 
     * @param couponCode the coupon code to set
     * @author Suhan Arda Öner
     */
    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    /**
     * Gets the invoice string (Base64).
     * 
     * @return the invoice string
     * @author Suhan Arda Öner
     */
    public String getInvoice() {
        return invoice;
    }

    /**
     * Sets the invoice string (Base64).
     * 
     * @param invoice the invoice string to set
     * @author Suhan Arda Öner
     */
    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    /**
     * Get invoice as byte array (decodes from Base64).
     * 
     * @return invoice as byte array, or null if invoice is null/empty
     * @author Suhan Arda Öner
     */
    public byte[] getInvoiceBytes() {
        if (invoice == null || invoice.isEmpty()) {
            return null;
        }
        try {
            return java.util.Base64.getDecoder().decode(invoice);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Set invoice from byte array (encodes to Base64).
     * 
     * @param invoiceBytes invoice as byte array
     * @author Suhan Arda Öner
     */
    public void setInvoiceBytes(byte[] invoiceBytes) {
        if (invoiceBytes == null || invoiceBytes.length == 0) {
            this.invoice = null;
        } else {
            this.invoice = java.util.Base64.getEncoder().encodeToString(invoiceBytes);
        }
    }

    /**
     * Gets the notes.
     * 
     * @return the notes
     * @author Suhan Arda Öner
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets the notes.
     * 
     * @param notes the notes to set
     * @author Suhan Arda Öner
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets the list of items in the order.
     * 
     * @return the list of items
     * @author Suhan Arda Öner
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Sets the list of items in the order.
     * 
     * @param items the list of items to set
     * @author Suhan Arda Öner
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * Gets the customer name.
     * 
     * @return the customer name
     * @author Suhan Arda Öner
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the customer name.
     * 
     * @param customerName the customer name to set
     * @author Suhan Arda Öner
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Gets the customer address.
     * 
     * @return the customer address
     * @author Suhan Arda Öner
     */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /**
     * Sets the customer address.
     * 
     * @param customerAddress the customer address to set
     * @author Suhan Arda Öner
     */
    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    /**
     * Gets the carrier name.
     * 
     * @return the carrier name
     * @author Suhan Arda Öner
     */
    public String getCarrierName() {
        return carrierName;
    }

    /**
     * Sets the carrier name.
     * 
     * @param carrierName the carrier name to set
     * @author Suhan Arda Öner
     */
    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    /**
     * Add an item to the order.
     * 
     * @param item the order item to add
     * @author Suhan Arda Öner
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
     * @author Suhan Arda Öner
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
     * @author Suhan Arda Öner
     */
    public long getCancellationTimeRemaining() {
        if (!canBeCancelled()) {
            return 0;
        }
        LocalDateTime cancellationDeadline = orderTime.plusHours(1);
        return java.time.Duration.between(LocalDateTime.now(), cancellationDeadline).toMinutes();
    }

    /**
     * Returns a string representation of the order.
     * 
     * @return a string representation of the order
     * @author Suhan Arda Öner
     */
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
