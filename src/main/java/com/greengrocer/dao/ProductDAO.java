package com.greengrocer.dao;

import com.greengrocer.models.Product;
import com.greengrocer.models.Product.ProductType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for {@link Product} operations.
 * <p>
 * Handles all database interactions related to products, including inventory
 * management,
 * searching, and retrieval by various criteria.
 * </p>
 */
public class ProductDAO {
    /** The database adapter for connection management. */
    private final DatabaseAdapter dbAdapter;

    /**
     * Default constructor.
     * <p>
     * Initializes the {@link DatabaseAdapter} instance.
     * </p>
     */
    public ProductDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Finds a product by its unique identifier.
     *
     * @param id The unique identifier of the product.
     * @return An {@link Optional} containing the {@link Product} if found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Optional<Product> findById(int id) throws SQLException {
        String sql = "SELECT * FROM ProductInfo WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all active products.
     * <p>
     * Results are sorted alphabetically by product name.
     * Only products marked as {@code is_active = TRUE} are returned.
     * </p>
     *
     * @return A {@link List} of all active {@link Product} objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE is_active = TRUE ORDER BY name";
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Retrieves all active products that are currently in stock.
     * <p>
     * Filters for {@code is_active = TRUE} and {@code stock > 0}.
     * Results are sorted alphabetically by product name.
     * </p>
     *
     * @return A {@link List} of in-stock {@link Product} objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> findAllInStock() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE is_active = TRUE AND stock > 0 ORDER BY name";
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Finds active, in-stock products of a specific type.
     *
     * @param type The {@link ProductType} to filter by (e.g., VEGETABLE, FRUIT).
     * @return A {@link List} of matching {@link Product} objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> findByType(ProductType type) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE type = ? AND is_active = TRUE AND stock > 0 ORDER BY name";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type.getValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Retrieves all available vegetables.
     * <p>
     * Convenience method for {@code findByType(ProductType.VEGETABLE)}.
     * </p>
     *
     * @return A {@link List} of vegetable products.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> getVegetables() throws SQLException {
        return findByType(ProductType.VEGETABLE);
    }

    /**
     * Retrieves all available fruits.
     * <p>
     * Convenience method for {@code findByType(ProductType.FRUIT)}.
     * </p>
     *
     * @return A {@link List} of fruit products.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> getFruits() throws SQLException {
        return findByType(ProductType.FRUIT);
    }

    /**
     * Searches for products matching a keyword.
     * <p>
     * Performs a case-insensitive search on the product name.
     * Handles Turkish locale-specific character matching (e.g., I-ı, İ-i).
     * Only returns active, in-stock products.
     * </p>
     *
     * @param keyword The search term.
     * @return A {@link List} of matching {@link Product} objects, sorted by name.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> search(String keyword) throws SQLException {
        List<Product> products = new ArrayList<>();
        // Fetch all active products
        // We filter in Java to handle Turkish characters correctly (e.g. I-ı, İ-i, O-o,
        // Ö-ö distinctions)
        String sql = "SELECT * FROM ProductInfo WHERE is_active = TRUE AND stock > 0";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            java.util.Locale trLocale = java.util.Locale.forLanguageTag("tr-TR");
            String searchKey = keyword.toLowerCase(trLocale);

            while (rs.next()) {
                Product p = mapResultSetToProduct(rs);
                String pName = p.getName().toLowerCase(trLocale);

                // Using startsWith to mimic original behavior but with correct Locale
                // MySQL default collation might be accent-insensitive (o==ö), so we handle it
                // here
                if (pName.startsWith(searchKey)) {
                    products.add(p);
                }
            }
        }

        // Sort results
        products.sort((p1, p2) -> {
            java.util.Locale trLocale = java.util.Locale.forLanguageTag("tr-TR");
            return p1.getName().toLowerCase(trLocale).compareTo(p2.getName().toLowerCase(trLocale));
        });

        return products;
    }

    /**
     * Creates a new product in the database.
     * <p>
     * Inserts product details including image data.
     * Retrieves and sets the generated product ID.
     * </p>
     *
     * @param product The {@link Product} object to create.
     * @return The updated {@link Product} object with its new database ID.
     * @throws SQLException If a database access error occurs.
     */
    public Product create(Product product) throws SQLException {
        String sql = "INSERT INTO ProductInfo (name, type, unit_type, price, stock, threshold, image, image_name, description, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType().getValue());
            stmt.setString(3, product.getUnitType().getValue());
            stmt.setDouble(4, product.getPrice());
            stmt.setDouble(5, product.getStock());
            stmt.setDouble(6, product.getThreshold());
            stmt.setBytes(7, product.getImage());
            stmt.setString(8, product.getImageName());
            stmt.setString(9, product.getDescription());
            stmt.setBoolean(10, product.isActive());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                product.setId(rs.getInt(1));
            }
        }
        return product;
    }

    /**
     * Updates an existing product's details.
     *
     * @param product The {@link Product} object containing updated information.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE ProductInfo SET name = ?, type = ?, unit_type = ?, price = ?, stock = ?, threshold = ?, image = ?, image_name = ?, description = ?, is_active = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getType().getValue());
            stmt.setString(3, product.getUnitType().getValue());
            stmt.setDouble(4, product.getPrice());
            stmt.setDouble(5, product.getStock());
            stmt.setDouble(6, product.getThreshold());
            stmt.setBytes(7, product.getImage());
            stmt.setString(8, product.getImageName());
            stmt.setString(9, product.getDescription());
            stmt.setBoolean(10, product.isActive());
            stmt.setInt(11, product.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the stock quantity of a product.
     *
     * @param productId The unique identifier of the product.
     * @param newStock  The new stock quantity to set.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean updateStock(int productId, double newStock) throws SQLException {
        String sql = "UPDATE ProductInfo SET stock = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newStock);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates the stock threshold for a product.
     * <p>
     * The threshold determines when the product price might increase (e.g.,
     * doubling price when stock is low).
     * </p>
     *
     * @param productId    The unique identifier of the product.
     * @param newThreshold The new threshold value.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean updateThreshold(int productId, double newThreshold) throws SQLException {
        String sql = "UPDATE ProductInfo SET threshold = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newThreshold);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Reduces the stock of a product by a specified amount.
     * <p>
     * This operation is atomic and ensures stock does not go below zero.
     * </p>
     *
     * @param productId The unique identifier of the product.
     * @param amount    The amount to subtract from the current stock.
     * @return {@code true} if the stock was successfully reduced; {@code false} if
     *         insufficient stock or error.
     * @throws SQLException If a database access error occurs.
     */
    public boolean reduceStock(int productId, double amount) throws SQLException {
        String sql = "UPDATE ProductInfo SET stock = stock - ? WHERE id = ? AND stock >= ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, productId);
            stmt.setDouble(3, amount);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Restores stock for a product (e.g., after an order cancellation).
     *
     * @param productId The unique identifier of the product.
     * @param amount    The amount to add back to the stock.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean restoreStock(int productId, double amount) throws SQLException {
        String sql = "UPDATE ProductInfo SET stock = stock + ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deactivates a product (soft delete).
     * <p>
     * Sets {@code is_active} to {@code FALSE}, making the product unavailable for
     * new orders
     * but preserving historical data.
     * </p>
     *
     * @param id The unique identifier of the product.
     * @return {@code true} if the deactivation was successful; {@code false}
     *         otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public boolean deactivate(int id) throws SQLException {
        String sql = "UPDATE ProductInfo SET is_active = FALSE WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves the most popular products based on total quantity sold.
     * <p>
     * Aggregates sales data from {@code OrderItems} for active products.
     * </p>
     *
     * @param limit The maximum number of products to return.
     * @return A {@link List} of the top-selling {@link Product} objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> getPopularProducts(int limit) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, SUM(oi.amount) as total_sold " +
                "FROM ProductInfo p " +
                "JOIN OrderItems oi ON p.id = oi.product_id " +
                "WHERE p.is_active = TRUE AND p.stock > p.threshold " +
                "GROUP BY p.id " +
                "ORDER BY total_sold DESC " +
                "LIMIT ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Searches for products whose names start with the given prefix.
     * <p>
     * Useful for autocomplete or quick search features.
     * Only returns active, in-stock products.
     * </p>
     *
     * @param prefix The prefix string to search for.
     * @param limit  The maximum number of results to return.
     * @return A {@link List} of matching {@link Product} objects.
     * @throws SQLException If a database access error occurs.
     */
    public List<Product> searchStartsWith(String prefix, int limit) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE is_active = TRUE AND stock > 0 AND name LIKE ? ORDER BY name LIMIT ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Map a ResultSet row to a Product object.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setType(ProductType.fromString(rs.getString("type")));
        // Handle potentially missing unit_type for backward compatibility or if column
        // not yet populated
        try {
            product.setUnitType(Product.UnitType.fromString(rs.getString("unit_type")));
        } catch (SQLException e) {
            // Column might not exist yet or be null, default to KG handled in fromString or
            // constructor
            product.setUnitType(Product.UnitType.KG);
        }
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getDouble("stock"));
        product.setThreshold(rs.getDouble("threshold"));
        product.setImage(rs.getBytes("image"));
        product.setImageName(rs.getString("image_name"));
        product.setDescription(rs.getString("description"));
        product.setActive(rs.getBoolean("is_active"));
        return product;
    }
}
