package com.greengrocer.utils;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Singleton class for managing the customer's shopping cart.
 * <p>
 * Handles adding/removing items, updating quantities, and calculating totals
 * including VAT and discounts.
 * </p>
 * 
 * @author Burak Özevin
 */
public class ShoppingCart {
    private static ShoppingCart instance;
    private List<CartItem> items;
    private static final double VAT_RATE = 0.18;

    private ShoppingCart() {
        this.items = new ArrayList<>();
    }

    /**
     * Retrieves the singleton instance of the ShoppingCart.
     *
     * @return The single {@link ShoppingCart} instance.
     * 
     * @author Burak Özevin
     */
    public static synchronized ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
        }
        return instance;
    }

    /**
     * Retrieves a copy of the items currently in the cart.
     *
     * @return A {@link List} of {@link CartItem} objects.
     * 
     * @author Burak Özevin
     */
    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Add a product to the cart. Merges if same product exists.
     */
    /**
     * Adds a product to the cart.
     * <p>
     * If the product already exists in the cart, its quantity is increased by the
     * specified amount.
     * Otherwise, a new {@link CartItem} is created.
     * </p>
     *
     * @param product The {@link Product} to add.
     * @param amount  The quantity to add (e.g., kg or pieces).
     * 
     * @author Burak Özevin
     */
    public void addItem(Product product, double amount) {
        Optional<CartItem> existing = items.stream()
                .filter(item -> item.getProductId() == product.getId())
                .findFirst();

        if (existing.isPresent()) {
            // Merge: add to existing amount
            existing.get().addAmount(amount);
            existing.get().updatePrice();
        } else {
            items.add(new CartItem(product, amount));
        }
    }

    /**
     * Removes an item from the cart by its product ID.
     *
     * @param productId The ID of the product to remove.
     * 
     * @author Burak Özevin
     */
    public void removeItem(int productId) {
        items.removeIf(item -> item.getProductId() == productId);
    }

    /**
     * Updates the quantity of a specific item in the cart.
     *
     * @param productId The ID of the product to update.
     * @param newAmount The new quantity.
     * 
     * @author Burak Özevin
     */
    public void updateItemAmount(int productId, double newAmount) {
        items.stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst()
                .ifPresent(item -> item.setAmount(newAmount));
    }

    /**
     * Clears all items from the cart.
     * 
     * @author Burak Özevin
     */
    public void clear() {
        items.clear();
    }

    /**
     * Calculates the subtotal of all items in the cart (before VAT).
     *
     * @return The subtotal amount.
     * 
     * @author Burak Özevin
     */
    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    /**
     * Calculates the total VAT amount for the cart.
     *
     * @return The VAT amount (18% of subtotal).
     * 
     * @author Burak Özevin
     */
    public double getVatAmount() {
        return getSubtotal() * VAT_RATE;
    }

    /**
     * Calculates the grand total (Subtotal + VAT).
     *
     * @return The total amount to be paid.
     * 
     * @author Burak Özevin
     */
    public double getTotal() {
        return getSubtotal() + getVatAmount();
    }

    /**
     * Calculates the total amount after applying a discount percentage.
     * <p>
     * The discount is applied to the subtotal, and VAT is added to the discounted
     * subtotal.
     * </p>
     *
     * @param discountPercent The discount percentage (0-100).
     * @return The discounted total amount.
     * 
     * @author Burak Özevin
     */
    public double getTotalWithDiscount(double discountPercent) {
        double subtotal = getSubtotal();
        double discount = subtotal * (discountPercent / 100.0);
        return (subtotal - discount) + getVatAmount();
    }

    /**
     * Returns the number of unique items in the cart.
     *
     * @return The item count.
     * 
     * @author Burak Özevin
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Checks if the cart is empty.
     *
     * @return {@code true} if the cart contains no items; {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Resets the singleton instance.
     * <p>
     * Useful for testing or completely resetting the application state.
     * </p>
     * 
     * @author Burak Özevin
     */
    public static void resetInstance() {
        instance = null;
    }
}
