package com.greengrocer.models;

/**
 * Product model representing a vegetable or fruit item.
 * Maps to the ProductInfo table in the database.
 * 
 * @author Suhan Arda Öner
 */
public class Product {
    /** Unique identifier for the product. */
    private int id;
    /** Name of the product. */
    private String name;
    /** Type of the product (VEGETABLE or FRUIT). */
    private ProductType type;
    /** Unit type for pricing (KG or PIECE). */
    private UnitType unitType;
    /** Base price per unit. */
    private double price;
    /** Current stock quantity available. */
    private double stock;
    /** Stock threshold below which price increases. */
    private double threshold;
    /** Product image data as byte array. */
    private byte[] image;
    /** Name of the image file. */
    private String imageName;
    /** Description of the product. */
    private String description;
    /** Flag indicating if the product is active/available for sale. */
    private boolean isActive;

    /**
     * Enum representing product types.
     * 
     * @author Suhan Arda Öner
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
     * 
     * @author Suhan Arda Öner
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

    /**
     * Default constructor.
     * Sets default unit type to KG.
     * 
     * @author Suhan Arda Öner
     */
    public Product() {
        this.unitType = UnitType.KG; // Default
    }

    /**
     * Full constructor for Product.
     *
     * @param id          the unique identifier
     * @param name        the product name
     * @param type        the product type
     * @param unitType    the unit type
     * @param price       the base price
     * @param stock       the stock quantity
     * @param threshold   the price increase threshold
     * @param image       the image data
     * @param imageName   the image filename
     * @param description the product description
     * @param isActive    the active status
     * 
     * @author Suhan Arda Öner
     */
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

    /**
     * Gets the product ID.
     * 
     * @return the product ID
     * 
     * @author Suhan Arda Öner
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the product ID.
     * 
     * @param id the product ID to set
     * 
     * @author Suhan Arda Öner
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the product name.
     * 
     * @return the product name
     * 
     * @author Suhan Arda Öner
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the product name.
     * 
     * @param name the product name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the product type.
     * 
     * @return the product type
     * 
     * @author Suhan Arda Öner
     */
    public ProductType getType() {
        return type;
    }

    /**
     * Sets the product type.
     * 
     * @param type the product type to set
     * 
     * @author Suhan Arda Öner
     */
    public void setType(ProductType type) {
        this.type = type;
    }

    /**
     * Gets the unit type.
     * 
     * @return the unit type
     * 
     * @author Suhan Arda Öner
     */
    public UnitType getUnitType() {
        return unitType;
    }

    /**
     * Sets the unit type.
     * 
     * @param unitType the unit type to set
     * 
     * @author Suhan Arda Öner
     */
    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    /**
     * Gets the base price.
     * 
     * @return the base price
     * 
     * @author Suhan Arda Öner
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the base price.
     * 
     * @param price the base price to set
     * 
     * @author Suhan Arda Öner
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Gets the stock quantity.
     * 
     * @return the stock quantity
     * 
     * @author Suhan Arda Öner
     */
    public double getStock() {
        return stock;
    }

    /**
     * Sets the stock quantity.
     * 
     * @param stock the stock quantity to set
     * 
     * @author Suhan Arda Öner
     */
    public void setStock(double stock) {
        this.stock = stock;
    }

    /**
     * Gets the threshold.
     * 
     * @return the threshold
     * 
     * @author Suhan Arda Öner
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold.
     * 
     * @param threshold the threshold to set
     * 
     * @author Suhan Arda Öner
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * Gets the image data.
     * 
     * @return the image data
     * 
     * @author Suhan Arda Öner
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the image data.
     * 
     * @param image the image data to set
     * 
     * @author Suhan Arda Öner
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Gets the image name.
     * 
     * @return the image name
     * 
     * @author Suhan Arda Öner
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Sets the image name.
     * 
     * @param imageName the image name to set
     * 
     * @author Suhan Arda Öner
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     * 
     * @author Suhan Arda Öner
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * 
     * @param description the description to set
     * 
     * @author Suhan Arda Öner
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks if the product is active.
     * 
     * @return true if active, false otherwise
     * 
     * @author Suhan Arda Öner
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active status.
     * 
     * @param active the active status to set
     * 
     * @author Suhan Arda Öner
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Get the display price considering threshold.
     * If stock &lt;= threshold, price is doubled.
     * 
     * @return the display price
     * 
     * @author Suhan Arda Öner
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
     * 
     * @author Suhan Arda Öner
     */
    public boolean isBelowThreshold() {
        return stock <= threshold;
    }

    /**
     * Check if the product is in stock.
     * 
     * @return true if stock is greater than 0
     * 
     * @author Suhan Arda Öner
     */
    public boolean isInStock() {
        return stock > 0;
    }

    /**
     * Check if requested amount is available.
     * 
     * @param amount the requested amount
     * @return true if amount is available
     * 
     * @author Suhan Arda Öner
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
