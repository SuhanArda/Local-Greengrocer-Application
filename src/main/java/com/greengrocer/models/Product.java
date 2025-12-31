package com.greengrocer.models;

/**
 * Product model representing a vegetable or fruit item.
 * Maps to the ProductInfo table in the database.
 */
public class Product {
    private int id;
    private String name;
    private ProductType type;
    private UnitType unitType;
    private double price;
    private double stock;
    private double threshold;
    private byte[] image;
    private String imageName;
    private String description;
    private boolean isActive;

    /**
     * Enum representing product types.
     */
    public enum ProductType {
        VEGETABLE("vegetable"),
        FRUIT("fruit");

        private final String value;

        ProductType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ProductType fromString(String text) {
            for (ProductType type : ProductType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown product type: " + text);
        }
    }

    /**
     * Enum representing unit types.
     */
    public enum UnitType {
        KG("kg"),
        PIECE("piece");

        private final String value;

        UnitType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static UnitType fromString(String text) {
            for (UnitType type : UnitType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            // Default to KG if unknown or null
            return KG;
        }
    }

    // Default constructor
    public Product() {
        this.unitType = UnitType.KG; // Default
    }

    // Full constructor
    public Product(int id, String name, ProductType type, UnitType unitType, double price,
            double stock, double threshold, byte[] image,
            String imageName, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.unitType = unitType;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.image = image;
        this.imageName = imageName;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Get the display price considering threshold.
     * If stock <= threshold, price is doubled.
     * 
     * @return the display price
     */
    public double getDisplayPrice() {
        if (stock <= threshold) {
            return price * 2;
        }
        return price;
    }

    /**
     * Check if the product is below threshold (price doubled).
     * 
     * @return true if stock is at or below threshold
     */
    public boolean isBelowThreshold() {
        return stock <= threshold;
    }

    /**
     * Check if the product is in stock.
     * 
     * @return true if stock is greater than 0
     */
    public boolean isInStock() {
        return stock > 0;
    }

    /**
     * Check if requested amount is available.
     * 
     * @param amount the requested amount
     * @return true if amount is available
     */
    public boolean isAmountAvailable(double amount) {
        return stock >= amount;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }
}
