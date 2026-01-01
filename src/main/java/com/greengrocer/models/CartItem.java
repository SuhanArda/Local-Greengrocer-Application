package com.greengrocer.models;

/**
 * CartItem model representing an item in the shopping cart.
 * Used for managing cart state before order creation.
 */
public class CartItem {
    private Product product;
    private double amount;
    private double unitPrice;

    // Default constructor
    public CartItem() {
    }

    // Full constructor
    public CartItem(Product product, double amount) {
        this.product = product;
        this.amount = amount;
        this.unitPrice = product.getDisplayPrice();
    }

    // Getters and Setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Get the product ID.
     * 
     * @return product ID
     */
    public int getProductId() {
        return product != null ? product.getId() : 0;
    }

    /**
     * Get the product name.
     * 
     * @return product name
     */
    public String getProductName() {
        return product != null ? product.getName() : "";
    }

    /**
     * Calculate total price for this cart item.
     * 
     * @return total price (amount * unitPrice)
     */
    public double getTotalPrice() {
        return amount * unitPrice;
    }

    /**
     * Add more amount to this cart item.
     * 
     * @param additionalAmount the amount to add
     */
    public void addAmount(double additionalAmount) {
        this.amount += additionalAmount;
    }

    /**
     * Update the unit price based on current product display price.
     * Should be called when threshold status might have changed.
     */
    public void updatePrice() {
        if (product != null) {
            this.unitPrice = product.getDisplayPrice();
        }
    }

    /**
     * Check if the current amount is available in stock.
     * 
     * @return true if amount is available
     */
    public boolean isAvailable() {
        return product != null && product.isAmountAvailable(amount);
    }

    /**
     * Convert to OrderItem for order creation.
     * 
     * @return OrderItem representation
     */
    public OrderItem toOrderItem() {
        return new OrderItem(
                product.getId(),
                product.getName(),
                amount,
                unitPrice);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "product=" + (product != null ? product.getName() : "null") +
                ", amount=" + amount +
                ", unitPrice=" + unitPrice +
                ", total=" + getTotalPrice() +
                '}';
    }
}
