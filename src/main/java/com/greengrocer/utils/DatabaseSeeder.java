package com.greengrocer.utils;

import com.greengrocer.dao.ProductDAO;
import com.greengrocer.models.Product;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for seeding the database with initial data.
 * <p>
 * Handles the population of default products and their associated images.
 * Ensures that the application starts with a usable set of data.
 * </p>
 * 
 * @author Ramazan Birkan Öztürk
 */
public class DatabaseSeeder {

    private static final Map<String, String> PRODUCT_IMAGE_MAP = new HashMap<>();
    private static final java.util.Set<String> SEEDED_FILENAMES = new java.util.HashSet<>();

    static {
        // Map product names to image filenames in src/main/resources/images
        PRODUCT_IMAGE_MAP.put("Apples", "apple.jpg");
        PRODUCT_IMAGE_MAP.put("Pears", "pear.jpg");
        PRODUCT_IMAGE_MAP.put("Mandarins", "mandarin.jpg");
        PRODUCT_IMAGE_MAP.put("Oranges", "orange.jpg");
        PRODUCT_IMAGE_MAP.put("Potatoes", "potato.jpg");
        PRODUCT_IMAGE_MAP.put("Cucumbers", "cucumber.jpg");
        PRODUCT_IMAGE_MAP.put("Peppers", "pepper.jpg");
        PRODUCT_IMAGE_MAP.put("Eggplant", "eggplant.jpg"); // Changed to Singular to match DB
        PRODUCT_IMAGE_MAP.put("Avocados", "avocado.jpg");
        PRODUCT_IMAGE_MAP.put("Strawberries", "strawberry.jpg");
        PRODUCT_IMAGE_MAP.put("Onions", "onion.jpg");

        // Comprehensive list of fruits and vegetables
        PRODUCT_IMAGE_MAP.put("Apricots", "apricot.jpg");
        PRODUCT_IMAGE_MAP.put("Bananas", "banana.jpg");
        PRODUCT_IMAGE_MAP.put("Broccoli", "broccoli.jpg");
        PRODUCT_IMAGE_MAP.put("Cabbage", "cabbage.jpg");
        PRODUCT_IMAGE_MAP.put("Carrots", "carrot.jpg");
        PRODUCT_IMAGE_MAP.put("Cherries", "cherry.jpg");
        PRODUCT_IMAGE_MAP.put("Garlic", "garlic.jpg");
        PRODUCT_IMAGE_MAP.put("Grapes", "grape.jpg");
        PRODUCT_IMAGE_MAP.put("Leeks", "leek.jpg");
        PRODUCT_IMAGE_MAP.put("Lemons", "lemon.jpg");
        PRODUCT_IMAGE_MAP.put("Lettuce", "lettuce.jpg");
        PRODUCT_IMAGE_MAP.put("Melons", "melon.jpg");
        PRODUCT_IMAGE_MAP.put("Peaches", "peach.jpg");
        PRODUCT_IMAGE_MAP.put("Plums", "plum.jpg");
        PRODUCT_IMAGE_MAP.put("Pomegranates", "pomegranate.jpg");
        PRODUCT_IMAGE_MAP.put("Radishes", "radish.jpg");
        PRODUCT_IMAGE_MAP.put("Spinach", "spinach.jpg");
        PRODUCT_IMAGE_MAP.put("Tomatoes", "tomato.jpg");
        PRODUCT_IMAGE_MAP.put("Watermelons", "watermelon.jpg");
        PRODUCT_IMAGE_MAP.put("Zucchini", "zucchini.jpg");
        // Add more mappings as needed

        // Populate the set of seeded filenames for quick lookup
        SEEDED_FILENAMES.addAll(PRODUCT_IMAGE_MAP.values());

        // Allow migration from old filenames
        SEEDED_FILENAMES.add("onion.png");
        SEEDED_FILENAMES.add("pear.jpg");
        SEEDED_FILENAMES.add("pear.png");
        SEEDED_FILENAMES.add("default.jpg");
    }

    /**
     * Seeds product images into the database.
     * <p>
     * Iterates through all products and assigns images based on the
     * {@code PRODUCT_IMAGE_MAP}.
     * If a specific image is not found, a default image is used.
     * Also creates missing products if they are defined in the map but not in the
     * DB.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public static void seedImages() {
        ProductDAO productDAO = new ProductDAO();

        // Ensure products exist before seeding images
        seedMissingProducts(productDAO);

        try {
            List<Product> products = productDAO.findAll();

            System.out.println("Checking for default images...");

            for (Product product : products) {
                // Determine if we should seed the image:
                // 1. If product has no image at all.
                // 2. OR if the current image is a "seeded" image (meaning we can safely
                // update/overwrite it).

                boolean hasNoImage = product.getImage() == null || product.getImage().length == 0;
                boolean isSeededImage = product.getImageName() != null
                        && SEEDED_FILENAMES.contains(product.getImageName());

                // Debug log
                // System.out.println("Processing product: " + product.getName() + ",
                // hasNoImage: " + hasNoImage + ", isSeeded: " + isSeededImage);

                if (hasNoImage || isSeededImage) {
                    String imageName = PRODUCT_IMAGE_MAP.get(product.getName());
                    if (imageName == null) {
                        // Fallback to default image if no specific mapping exists
                        imageName = "default.jpg";
                    }

                    try (InputStream is = DatabaseSeeder.class.getResourceAsStream("/images/" + imageName)) {
                        if (is != null) {
                            byte[] imageBytes = is.readAllBytes();
                            product.setImage(imageBytes);
                            product.setImageName(imageName);

                            if (productDAO.update(product)) {
                                System.out.println("Seeded/Updated image for product: " + product.getName());
                            } else {
                                System.err.println(
                                        "Failed to update product with seeded image: " + product.getName());
                            }
                        } else {
                            System.out.println(
                                    "Image resource not found for: " + product.getName() + " (" + imageName + ")");
                        }
                    } catch (IOException e) {
                        System.err.println(
                                "Error reading image resource for " + product.getName() + ": " + e.getMessage());
                    }
                } else {
                    System.out.println("Skipping existing custom image for product: " + product.getName() + " (Image: "
                            + product.getImageName() + ")");
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Database error during image seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates missing products based on the image map keys.
     * <p>
     * If a product defined in {@code PRODUCT_IMAGE_MAP} does not exist in the
     * database,
     * it is created with default values (price, stock, etc.) and an inferred type
     * (Fruit/Vegetable).
     * </p>
     *
     * @param productDAO The DAO to use for product creation.
     * 
     * @author Ramazan Birkan Öztürk
     */
    private static void seedMissingProducts(ProductDAO productDAO) {
        try {
            List<Product> existingProducts = productDAO.findAll();
            java.util.Set<String> existingNames = new java.util.HashSet<>();
            for (Product p : existingProducts) {
                existingNames.add(p.getName());
            }

            for (String potentialProduct : PRODUCT_IMAGE_MAP.keySet()) {
                if (!existingNames.contains(potentialProduct)) {
                    System.out.println("Product not found in DB, creating: " + potentialProduct);
                    createNewProduct(productDAO, potentialProduct);
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Error seeding missing products: " + e.getMessage());
        }
    }

    /**
     * Creates a new product with default values.
     *
     * @param productDAO The DAO to use for persistence.
     * @param name       The name of the product to create.
     * @throws java.sql.SQLException If a database error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    private static void createNewProduct(ProductDAO productDAO, String name) throws java.sql.SQLException {
        Product p = new Product();
        p.setName(name);
        p.setUnitType(Product.UnitType.KG);
        p.setPrice(10.0); // Default price
        p.setStock(100.0); // Default stock
        p.setThreshold(10.0);
        p.setActive(true);
        p.setDescription("Fresh " + name);

        // Guess type
        String n = name.toLowerCase();
        if (n.contains("apple") || n.contains("pear") || n.contains("orange") || n.contains("mandarin") ||
                n.contains("strawberry") || n.contains("grape") || n.contains("lemon") || n.contains("melon") ||
                n.contains("peach") || n.contains("plum") || n.contains("berry") || n.contains("banana") ||
                n.contains("cherry") || n.contains("apricot") || n.contains("pomegranate") || n.contains("avocado")) {
            p.setType(Product.ProductType.FRUIT);
        } else {
            p.setType(Product.ProductType.VEGETABLE);
        }

        productDAO.create(p);
        System.out.println("Created missing product: " + name);
    }
}
