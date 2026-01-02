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
 * Data Access Object for {@link Order} operations.
 * <p>
 * Manages all database interactions related to orders, including creation,
 * retrieval,
 * status updates, and assignment of carriers.
 * </p>
 * 
 * @author Ramazan Birkan Öztürk
 */
public class OrderDAO {
    /** The database adapter for connection management. */
    private final DatabaseAdapter dbAdapter;

    /**
     * Default constructor.
     * <p>
     * Initializes the {@link DatabaseAdapter} instance.
     * </p>
     * 
     * @author Ramazan Birkan Öztürk
     */
    public OrderDAO() {
        this.dbAdapter = DatabaseAdapter.getInstance();
    }

    /**
     * Finds a specific order by its unique identifier.
     * <p>
     * Retrieves the order details along with:
     * <ul>
     * <li>Customer name and address</li>
     * <li>Carrier name (if assigned)</li>
     * <li>All associated {@link OrderItem}s</li>
     * </ul>
     *
     * @param id The unique identifier of the order.
     * @return An {@link Optional} containing the {@link Order} if found;
     *         {@link Optional#empty()} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves all orders placed by a specific customer.
     * <p>
     * The results are ordered by {@code order_time} in descending order (newest
     * first).
     * </p>
     *
     * @param customerId The unique identifier of the customer.
     * @return A {@link List} of {@link Order} objects belonging to the customer.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves all orders assigned to a specific carrier.
     * <p>
     * The results are ordered by {@code order_time} in descending order.
     * </p>
     *
     * @param carrierId The unique identifier of the carrier.
     * @return A {@link List} of {@link Order} objects assigned to the carrier.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves all orders with status {@code PENDING}.
     * <p>
     * These are orders that have been placed but not yet assigned to or accepted by
     * a carrier.
     * </p>
     *
     * @return A {@link List} of pending {@link Order} objects.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public List<Order> findPendingOrders() throws SQLException {
        return findByStatus(OrderStatus.PENDING);
    }

    /**
     * Retrieves all orders matching a specific status.
     * <p>
     * The results are ordered by {@code requested_delivery_time} in ascending order
     * (earliest first).
     * </p>
     *
     * @param status The {@link OrderStatus} to filter by.
     * @return A {@link List} of {@link Order} objects with the specified status.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Retrieves all orders in the system.
     * <p>
     * Typically used by the Owner to view the complete order history.
     * Results are ordered by {@code order_time} in descending order.
     * </p>
     *
     * @return A {@link List} of all {@link Order} objects.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Creates a new order and its associated order items in the database.
     * <p>
     * This method performs the following:
     * <ol>
     * <li>Inserts the main order record into {@code OrderInfo}.</li>
     * <li>Retrieves the generated order ID.</li>
     * <li>Iterates through the list of {@link OrderItem}s and persists each one
     * into {@code OrderItems}.</li>
     * </ol>
     *
     * @param order The {@link Order} object to be created.
     * @return The updated {@link Order} object with its new database ID.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
            stmt.setString(11, order.getInvoice());
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
     * Helper method to persist a single order item.
     *
     * @param item The {@link OrderItem} to create.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Helper method to find all items associated with a specific order.
     *
     * @param orderId The unique identifier of the order.
     * @return A {@link List} of {@link OrderItem}s.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Updates the status of an order.
     *
     * @param orderId The unique identifier of the order.
     * @param status  The new {@link OrderStatus} to set.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Assigns a carrier to a pending order and updates its status to
     * {@code SELECTED}.
     * <p>
     * This operation ensures that only orders currently in {@code PENDING} status
     * can be assigned.
     * </p>
     *
     * @param orderId   The unique identifier of the order.
     * @param carrierId The unique identifier of the carrier.
     * @return {@code true} if the assignment was successful; {@code false} if the
     *         order was not pending or other error.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Marks an order as {@code DELIVERED} and records the actual delivery time.
     *
     * @param orderId      The unique identifier of the order.
     * @param deliveryTime The timestamp when the delivery was completed.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
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
     * Cancels an order by updating its status to {@code CANCELLED}.
     *
     * @param orderId The unique identifier of the order.
     * @return {@code true} if the cancellation was successful; {@code false}
     *         otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean cancel(int orderId) throws SQLException {
        return updateStatus(orderId, OrderStatus.CANCELLED);
    }

    /**
     * Updates the invoice data for an order.
     * <p>
     * The invoice is stored as a Base64 encoded string in the database.
     * </p>
     *
     * @param orderId      The unique identifier of the order.
     * @param invoiceBytes The raw byte content of the invoice (e.g., PDF data).
     * @return {@code true} if the update was successful; {@code false} otherwise.
     * @throws SQLException If a database access error occurs.
     * 
     * @author Ramazan Birkan Öztürk
     */
    public boolean updateInvoice(int orderId, byte[] invoiceBytes) throws SQLException {
        String sql = "UPDATE OrderInfo SET invoice = ? WHERE id = ?";
        try (Connection conn = dbAdapter.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            String invoiceBase64 = (invoiceBytes != null && invoiceBytes.length > 0)
                    ? java.util.Base64.getEncoder().encodeToString(invoiceBytes)
                    : null;
            stmt.setString(1, invoiceBase64);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Map a ResultSet row to an Order object.
     * 
     * @author Ramazan Birkan Öztürk
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
        order.setInvoice(rs.getString("invoice"));
        order.setNotes(rs.getString("notes"));

        // Additional display fields
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerAddress(rs.getString("customer_address"));
        order.setCarrierName(rs.getString("carrier_name"));

        return order;
    }
}
