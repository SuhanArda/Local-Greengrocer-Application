package com.greengrocer.dao;

import com.greengrocer.models.Product;
import com.greengrocer.models.Product.ProductType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Product operations.
 */
public class ProductDAO {
    private final DatabaseAdapter dbAdapter;

    public ProductDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Find a product by ID.
     * 
     * @param id the product ID
     * @return Optional containing the product if found
     * @throws SQLException if a database error occurs
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
     * Get all active products sorted by name.
     * 
     * @return list of all active products
     * @throws SQLException if a database error occurs
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
     * Get all products with stock > 0, sorted by name.
     * 
     * @return list of in-stock products
     * @throws SQLException if a database error occurs
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
     * Find products by type.
     * 
     * @param type the product type
     * @return list of products of the specified type
     * @throws SQLException if a database error occurs
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
     * Get all vegetables in stock.
     * 
     * @return list of vegetables
     * @throws SQLException if a database error occurs
     */
    public List<Product> getVegetables() throws SQLException {
        return findByType(ProductType.VEGETABLE);
    }

    /**
     * Get all fruits in stock.
     * 
     * @return list of fruits
     * @throws SQLException if a database error occurs
     */
    public List<Product> getFruits() throws SQLException {
        return findByType(ProductType.FRUIT);
    }

    /**
     * Search products by name keyword.
     * 
     * @param keyword the search keyword
     * @return list of matching products
     * @throws SQLException if a database error occurs
     */
    public List<Product> search(String keyword) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM ProductInfo WHERE is_active = TRUE AND stock > 0 AND name LIKE ? ORDER BY name";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    /**
     * Create a new product.
     * 
     * @param product the product to create
     * @return the created product with ID set
     * @throws SQLException if a database error occurs
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
     * Update an existing product.
     * 
     * @param product the product to update
     * @return true if update successful
     * @throws SQLException if a database error occurs
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
     * Update product stock.
     * 
     * @param productId the product ID
     * @param newStock  the new stock value
     * @return true if update successful
     * @throws SQLException if a database error occurs
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
     * Update product threshold.
     * 
     * @param productId    the product ID
     * @param newThreshold the new threshold value
     * @return true if update successful
     * @throws SQLException if a database error occurs
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
     * Reduce product stock by a given amount.
     * 
     * @param productId the product ID
     * @param amount    the amount to reduce
     * @return true if update successful
     * @throws SQLException if a database error occurs
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
     * Restore product stock by a given amount (for order cancellation).
     * 
     * @param productId the product ID
     * @param amount    the amount to restore
     * @return true if update successful
     * @throws SQLException if a database error occurs
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
     * Deactivate a product (soft delete).
     * 
     * @param id the product ID
     * @return true if deactivation successful
     * @throws SQLException if a database error occurs
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
     * Get popular products based on total quantity sold.
     *
     * @param limit the maximum number of products to return
     * @return list of popular products
     * @throws SQLException if a database error occurs
     */
    public List<Product> getPopularProducts(int limit) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, SUM(oi.amount) as total_sold " +
                "FROM ProductInfo p " +
                "JOIN OrderItems oi ON p.id = oi.product_id " +
                "WHERE p.is_active = TRUE " +
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
     * Search products starting with the given prefix.
     *
     * @param prefix the search prefix
     * @param limit  the maximum number of results
     * @return list of matching products
     * @throws SQLException if a database error occurs
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
