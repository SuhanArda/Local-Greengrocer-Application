package com.greengrocer.utils;

import com.greengrocer.dao.ProductDAO;
import com.greengrocer.models.Product;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSeeder {

    private static final Map<String, String> PRODUCT_IMAGE_MAP = new HashMap<>();

    static {
        // Map product names to image filenames in src/main/resources/images
        PRODUCT_IMAGE_MAP.put("Elma", "apple.jpg");
        PRODUCT_IMAGE_MAP.put("Armut", "pear.jpg");
        PRODUCT_IMAGE_MAP.put("Muz", "banana.jpg");
        PRODUCT_IMAGE_MAP.put("Portakal", "orange.jpg");
        PRODUCT_IMAGE_MAP.put("Domates", "tomato.jpg");
        PRODUCT_IMAGE_MAP.put("Salatalık", "cucumber.jpg");
        PRODUCT_IMAGE_MAP.put("Biber", "pepper.jpg");
        PRODUCT_IMAGE_MAP.put("Patlıcan", "eggplant.jpg");
        PRODUCT_IMAGE_MAP.put("Avokado", "avocado.jpg");
        PRODUCT_IMAGE_MAP.put("Çilek", "strawberry.jpg");
        // Add more mappings as needed
    }

    public static void seedImages() {
        ProductDAO productDAO = new ProductDAO();
        try {
            List<Product> products = productDAO.findAll();

            System.out.println("Checking for default images...");

            for (Product product : products) {
                // If product has no image, try to seed it
                if (product.getImage() == null || product.getImage().length == 0) {
                    String imageName = PRODUCT_IMAGE_MAP.get(product.getName());
                    if (imageName != null) {
                        try (InputStream is = DatabaseSeeder.class.getResourceAsStream("/images/" + imageName)) {
                            if (is != null) {
                                byte[] imageBytes = is.readAllBytes();
                                product.setImage(imageBytes);
                                product.setImageName(imageName);

                                if (productDAO.update(product)) {
                                    System.out.println("Seeded image for product: " + product.getName());
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
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.err.println("Database error during image seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
