package com.greengrocer.dao;

import com.greengrocer.models.Order;
import com.greengrocer.models.Order.OrderStatus;
import com.greengrocer.models.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Order operations.
 */
public class OrderDAO {
    private final DBConnection dbAdapter;

    public OrderDAO() {
        this.dbAdapter = DBConnection.getInstance();
    }

    /**
     * Find an order by ID.
     * 
     * @param id the order ID
     * @return Optional containing the order if found
     * @throws SQLException if a database error occurs
     */
    public Optional<Order> findById(int id) throws SQLException {
        String sql = "SELECT o.*, " +
                "c.full_name AS customer_name, c.address AS customer_address, " +
                "cr.full_name AS carrier_name " +
                "FROM OrderInfo o " +
                "LEFT JOIN UserInfo c ON o.customer_id = c.id " +
                "LEFT JOIN UserInfo cr ON o.carrier_id = cr.id " +
                "WHERE o.id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(id));
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    /**
     * Find all orders for a customer.
     * 
     * @param customerId the customer ID
     * @return list of customer's orders
     * @throws SQLException if a database error occurs
     */
    public List<Order> findByCustomer(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, " +
                "c.full_name AS customer_name, c.address AS customer_address, " +
                "cr.full_name AS carrier_name " +
                "FROM OrderInfo o " +
                "LEFT JOIN UserInfo c ON o.customer_id = c.id " +
                "LEFT JOIN UserInfo cr ON o.carrier_id = cr.id " +
                "WHERE o.customer_id = ? ORDER BY o.order_time DESC";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    /**
     * Find all orders for a carrier.
     * 
     * @param carrierId the carrier ID
     * @return list of carrier's orders
     * @throws SQLException if a database error occurs
     */
    public List<Order> findByCarrier(int carrierId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, " +
                "c.full_name AS customer_name, c.address AS customer_address, " +
                "cr.full_name AS carrier_name " +
                "FROM OrderInfo o " +
                "LEFT JOIN UserInfo c ON o.customer_id = c.id " +
                "LEFT JOIN UserInfo cr ON o.carrier_id = cr.id " +
                "WHERE o.carrier_id = ? ORDER BY o.order_time DESC";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    /**
     * Find all pending orders (available for carriers).
     * 
     * @return list of pending orders
     * @throws SQLException if a database error occurs
     */
    public List<Order> findPendingOrders() throws SQLException {
        return findByStatus(OrderStatus.PENDING);
    }

    /**
     * Find orders by status.
     * 
     * @param status the order status
     * @return list of orders with the specified status
     * @throws SQLException if a database error occurs
     */
    public List<Order> findByStatus(OrderStatus status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, " +
                "c.full_name AS customer_name, c.address AS customer_address, " +
                "cr.full_name AS carrier_name " +
                "FROM OrderInfo o " +
                "LEFT JOIN UserInfo c ON o.customer_id = c.id " +
                "LEFT JOIN UserInfo cr ON o.carrier_id = cr.id " +
                "WHERE o.status = ? ORDER BY o.requested_delivery_time ASC";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.getValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    /**
     * Find all orders (for owner view).
     * 
     * @return list of all orders
     * @throws SQLException if a database error occurs
     */
    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, " +
                "c.full_name AS customer_name, c.address AS customer_address, " +
                "cr.full_name AS carrier_name " +
                "FROM OrderInfo o " +
                "LEFT JOIN UserInfo c ON o.customer_id = c.id " +
                "LEFT JOIN UserInfo cr ON o.carrier_id = cr.id " +
                "ORDER BY o.order_time DESC";
        try (Connection conn = dbAdapter.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findOrderItems(order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    /**
     * Create a new order with its items.
     * 
     * @param order the order to create
     * @return the created order with ID set
     * @throws SQLException if a database error occurs
     */
    public Order create(Order order) throws SQLException {
        String sql = "INSERT INTO OrderInfo (customer_id, carrier_id, order_time, requested_delivery_time, status, subtotal, vat_amount, discount_amount, total_cost, coupon_code, invoice, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, order.getCustomerId());
            stmt.setObject(2, order.getCarrierId());
            stmt.setTimestamp(3, Timestamp.valueOf(order.getOrderTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(order.getRequestedDeliveryTime()));
            stmt.setString(5, order.getStatus().getValue());
            stmt.setDouble(6, order.getSubtotal());
            stmt.setDouble(7, order.getVatAmount());
            stmt.setDouble(8, order.getDiscountAmount());
            stmt.setDouble(9, order.getTotalCost());
            stmt.setString(10, order.getCouponCode());
            stmt.setBytes(11, order.getInvoice());
            stmt.setString(12, order.getNotes());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                order.setId(rs.getInt(1));
                // Save order items
                for (OrderItem item : order.getItems()) {
                    item.setOrderId(order.getId());
                    createOrderItem(item);
                }
            }
        }
        return order;
    }

    /**
     * Create an order item.
     */
    private void createOrderItem(OrderItem item) throws SQLException {
        String sql = "INSERT INTO OrderItems (order_id, product_id, product_name, amount, unit_price, total_price) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getProductId());
            stmt.setString(3, item.getProductName());
            stmt.setDouble(4, item.getAmount());
            stmt.setDouble(5, item.getUnitPrice());
            stmt.setDouble(6, item.getTotalPrice());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                item.setId(rs.getInt(1));
            }
        }
    }

    /**
     * Find order items for an order.
     */
    private List<OrderItem> findOrderItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM OrderItems WHERE order_id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setProductId(rs.getInt("product_id"));
                item.setProductName(rs.getString("product_name"));
                item.setAmount(rs.getDouble("amount"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setTotalPrice(rs.getDouble("total_price"));
                items.add(item);
            }
        }
        return items;
    }

    /**
     * Update order status.
     * 
     * @param orderId the order ID
     * @param status  the new status
     * @return true if update successful
     * @throws SQLException if a database error occurs
     */
    public boolean updateStatus(int orderId, OrderStatus status) throws SQLException {
        String sql = "UPDATE OrderInfo SET status = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.getValue());
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Assign a carrier to an order.
     * 
     * @param orderId   the order ID
     * @param carrierId the carrier ID
     * @return true if update successful
     * @throws SQLException if a database error occurs
     */
    public boolean assignCarrier(int orderId, int carrierId) throws SQLException {
        String sql = "UPDATE OrderInfo SET carrier_id = ?, status = 'selected' WHERE id = ? AND status = 'pending'";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carrierId);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Mark an order as delivered.
     * 
     * @param orderId      the order ID
     * @param deliveryTime the actual delivery time
     * @return true if update successful
     * @throws SQLException if a database error occurs
     */
    public boolean markDelivered(int orderId, LocalDateTime deliveryTime) throws SQLException {
        String sql = "UPDATE OrderInfo SET status = 'delivered', actual_delivery_time = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(deliveryTime));
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Cancel an order.
     * 
     * @param orderId the order ID
     * @return true if cancellation successful
     * @throws SQLException if a database error occurs
     */
    public boolean cancel(int orderId) throws SQLException {
        return updateStatus(orderId, OrderStatus.CANCELLED);
    }

    /**
     * Update order invoice.
     * 
     * @param orderId the order ID
     * @param invoice the invoice content (BLOB)
     * @return true if update successful
     * @throws SQLException if a database error occurs
     */
    public boolean updateInvoice(int orderId, byte[] invoice) throws SQLException {
        String sql = "UPDATE OrderInfo SET invoice = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBytes(1, invoice);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Map a ResultSet row to an Order object.
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setCarrierId(rs.getObject("carrier_id") != null ? rs.getInt("carrier_id") : null);

        Timestamp orderTime = rs.getTimestamp("order_time");
        if (orderTime != null)
            order.setOrderTime(orderTime.toLocalDateTime());

        Timestamp requestedTime = rs.getTimestamp("requested_delivery_time");
        if (requestedTime != null)
            order.setRequestedDeliveryTime(requestedTime.toLocalDateTime());

        Timestamp actualTime = rs.getTimestamp("actual_delivery_time");
        if (actualTime != null)
            order.setActualDeliveryTime(actualTime.toLocalDateTime());

        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setSubtotal(rs.getDouble("subtotal"));
        order.setVatAmount(rs.getDouble("vat_amount"));
        order.setDiscountAmount(rs.getDouble("discount_amount"));
        order.setTotalCost(rs.getDouble("total_cost"));
        order.setCouponCode(rs.getString("coupon_code"));
        order.setInvoice(rs.getBytes("invoice"));
        order.setNotes(rs.getString("notes"));

        // Additional display fields
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerAddress(rs.getString("customer_address"));
        order.setCarrierName(rs.getString("carrier_name"));

        return order;
    }
}
