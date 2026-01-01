package com.greengrocer.utils;

import com.greengrocer.models.CartItem;
import com.greengrocer.models.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shopping cart manager for the customer.
 * Implements singleton pattern with cart merging logic.
 */
public class ShoppingCart {
    private static ShoppingCart instance;
    private List<CartItem> items;
    private static final double VAT_RATE = 0.18;

    private ShoppingCart() {
        this.items = new ArrayList<>();
    }

    public static synchronized ShoppingCart getInstance() {
        if (instance == null) {
            instance = new ShoppingCart();
        }
        return instance;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Add a product to the cart. Merges if same product exists.
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

    public void removeItem(int productId) {
        items.removeIf(item -> item.getProductId() == productId);
    }

    public void updateItemAmount(int productId, double newAmount) {
        items.stream()
                .filter(item -> item.getProductId() == productId)
                .findFirst()
                .ifPresent(item -> item.setAmount(newAmount));
    }

    public void clear() {
        items.clear();
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }

    public double getVatAmount() {
        return getSubtotal() * VAT_RATE;
    }

    public double getTotal() {
        return getSubtotal() + getVatAmount();
    }

    public double getTotalWithDiscount(double discountPercent) {
        double subtotal = getSubtotal();
        double discount = subtotal * (discountPercent / 100.0);
        return (subtotal - discount) + getVatAmount();
    }

    public int getItemCount() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public static void resetInstance() {
        instance = null;
    }
}
