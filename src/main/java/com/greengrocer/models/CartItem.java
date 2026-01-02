package com.greengrocer.models;

/**
 * CartItem model representing an item in the shopping cart.
 * Used for managing cart state before order creation.
 * 
 * @author Suhan Arda Öner
 */
public class CartItem {
    /** The product associated with this cart item. */
    private Product product;
    /** The quantity of the product in the cart. */
    private double amount;
    /** The unit price of the product at the time of adding to cart. */
    private double unitPrice;

    /**
     * Default constructor.
     * 
     * @author Suhan Arda Öner
     */
    public CartItem() {
    }

    /**
     * Full constructor for CartItem.
     *
     * @param product the product to add to cart
     * @param amount  the quantity of the product
     * 
     * @author Suhan Arda Öner
     */
    public CartItem(Product product, double amount) {
        this.product = product;
        this.amount = amount;
        this.unitPrice = product.getDisplayPrice();
    }

    // Getters and Setters

    /**
     * Gets the product.
     * 
     * @return the product
     * 
     * @author Suhan Arda Öner
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Sets the product.
     * 
     * @param product the product to set
     * 
     * @author Suhan Arda Öner
     */
    public void setProduct(Product product) {
        this.product = product;
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
     * 
     * @param amount the amount to set
     * 
     * @author Suhan Arda Öner
     */
    public void setAmount(double amount) {
        this.amount = amount;
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
     * 
     * @param unitPrice the unit price to set
     * 
     * @author Suhan Arda Öner
     */
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Get the product ID.
     * 
     * @return product ID
     * 
     * @author Suhan Arda Öner
     */
    public int getProductId() {
        return product != null ? product.getId() : 0;
    }

    /**
     * Get the product name.
     * 
     * @return product name
     * 
     * @author Suhan Arda Öner
     */
    public String getProductName() {
        return product != null ? product.getName() : "";
    }

    /**
     * Calculate total price for this cart item.
     * 
     * @return total price (amount * unitPrice)
     * 
     * @author Suhan Arda Öner
     */
    public double getTotalPrice() {
        return amount * unitPrice;
    }

    /**
     * Add more amount to this cart item.
     * 
     * @param additionalAmount the amount to add
     * 
     * @author Suhan Arda Öner
     */
    public void addAmount(double additionalAmount) {
        this.amount += additionalAmount;
    }

    /**
     * Update the unit price based on current product display price.
     * Should be called when threshold status might have changed.
     * 
     * @author Suhan Arda Öner
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
     * 
     * @author Suhan Arda Öner
     */
    public boolean isAvailable() {
        return product != null && product.isAmountAvailable(amount);
    }

    /**
     * Convert to OrderItem for order creation.
     * 
     * @return OrderItem representation
     * 
     * @author Suhan Arda Öner
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
