package com.greengrocer.utils;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.dao.ProductDAO;
import com.greengrocer.models.Order;
import com.greengrocer.models.Order.OrderStatus;
import com.greengrocer.models.Product;
import com.greengrocer.models.User;

/**
 * Intelligent chatbot service with intent recognition and natural language
 * understanding.
 * <p>
 * Provides automated responses to common customer queries regarding products,
 * orders,
 * account details, and general information.
 * </p>
 * <p>
 * Features:
 * <ul>
 * <li>Keyword-based intent recognition with weighted scoring.</li>
 * <li>Fuzzy matching for product names (typo tolerance).</li>
 * <li>Context-aware responses (e.g., checking user's specific orders).</li>
 * <li>Product recommendations and stock checking.</li>
 * </ul>
 * 
 * @author Burak Özevin
 */
public class ChatbotService {
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private List<Product> allProductsCache;

    // Intent keywords with weights for better matching
    private static final Map<String, Integer> PRODUCT_INTENT_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> ORDER_INTENT_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> ACCOUNT_INTENT_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> CONTACT_INTENT_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> GREETING_INTENT_KEYWORDS = new HashMap<>();
    private static final Map<String, Integer> HELP_INTENT_KEYWORDS = new HashMap<>();

    static {
        // Product intent keywords (weighted)
        PRODUCT_INTENT_KEYWORDS.put("product", 3);
        PRODUCT_INTENT_KEYWORDS.put("products", 3);
        PRODUCT_INTENT_KEYWORDS.put("item", 2);
        PRODUCT_INTENT_KEYWORDS.put("items", 2);
        PRODUCT_INTENT_KEYWORDS.put("price", 4);
        PRODUCT_INTENT_KEYWORDS.put("prices", 4);
        PRODUCT_INTENT_KEYWORDS.put("cost", 3);
        PRODUCT_INTENT_KEYWORDS.put("how much", 4);
        PRODUCT_INTENT_KEYWORDS.put("do you have", 5);
        PRODUCT_INTENT_KEYWORDS.put("available", 3);
        PRODUCT_INTENT_KEYWORDS.put("stock", 3);
        PRODUCT_INTENT_KEYWORDS.put("search", 2);
        PRODUCT_INTENT_KEYWORDS.put("find", 2);
        PRODUCT_INTENT_KEYWORDS.put("looking for", 3);
        PRODUCT_INTENT_KEYWORDS.put("need", 2);
        PRODUCT_INTENT_KEYWORDS.put("want", 2);
        PRODUCT_INTENT_KEYWORDS.put("buy", 3);
        PRODUCT_INTENT_KEYWORDS.put("purchase", 2);
        PRODUCT_INTENT_KEYWORDS.put("tell me about", 4);
        PRODUCT_INTENT_KEYWORDS.put("information about", 3);
        PRODUCT_INTENT_KEYWORDS.put("what is", 2);
        PRODUCT_INTENT_KEYWORDS.put("show me", 3);
        PRODUCT_INTENT_KEYWORDS.put("list", 2);
        PRODUCT_INTENT_KEYWORDS.put("catalog", 2);
        PRODUCT_INTENT_KEYWORDS.put("menu", 2);
        PRODUCT_INTENT_KEYWORDS.put("offer", 2);
        PRODUCT_INTENT_KEYWORDS.put("selling", 2);
        PRODUCT_INTENT_KEYWORDS.put("sell", 2);

        // Order intent keywords
        ORDER_INTENT_KEYWORDS.put("order", 4);
        ORDER_INTENT_KEYWORDS.put("orders", 4);
        ORDER_INTENT_KEYWORDS.put("my order", 5);
        ORDER_INTENT_KEYWORDS.put("my orders", 5);
        ORDER_INTENT_KEYWORDS.put("where", 4);
        ORDER_INTENT_KEYWORDS.put("where is", 5);
        ORDER_INTENT_KEYWORDS.put("status", 4);
        ORDER_INTENT_KEYWORDS.put("delivery", 4);
        ORDER_INTENT_KEYWORDS.put("delivered", 3);
        ORDER_INTENT_KEYWORDS.put("track", 4);
        ORDER_INTENT_KEYWORDS.put("tracking", 4);
        ORDER_INTENT_KEYWORDS.put("shipped", 3);
        ORDER_INTENT_KEYWORDS.put("arrived", 3);
        ORDER_INTENT_KEYWORDS.put("when will", 4);
        ORDER_INTENT_KEYWORDS.put("when is", 4);
        ORDER_INTENT_KEYWORDS.put("last order", 5);
        ORDER_INTENT_KEYWORDS.put("recent order", 4);
        ORDER_INTENT_KEYWORDS.put("pending", 3);
        ORDER_INTENT_KEYWORDS.put("cancel", 2);
        ORDER_INTENT_KEYWORDS.put("cancelled", 2);
        ORDER_INTENT_KEYWORDS.put("package", 2);
        ORDER_INTENT_KEYWORDS.put("parcel", 2);
        ORDER_INTENT_KEYWORDS.put("shipment", 2);

        // Account intent keywords
        ACCOUNT_INTENT_KEYWORDS.put("point", 3);
        ACCOUNT_INTENT_KEYWORDS.put("points", 3);
        ACCOUNT_INTENT_KEYWORDS.put("loyalty", 4);
        ACCOUNT_INTENT_KEYWORDS.put("my account", 4);
        ACCOUNT_INTENT_KEYWORDS.put("account", 3);
        ACCOUNT_INTENT_KEYWORDS.put("order count", 4);
        ACCOUNT_INTENT_KEYWORDS.put("total orders", 4);
        ACCOUNT_INTENT_KEYWORDS.put("how many orders", 5);
        ACCOUNT_INTENT_KEYWORDS.put("my profile", 3);
        ACCOUNT_INTENT_KEYWORDS.put("profile", 2);
        ACCOUNT_INTENT_KEYWORDS.put("balance", 2);
        ACCOUNT_INTENT_KEYWORDS.put("reward", 2);
        ACCOUNT_INTENT_KEYWORDS.put("rewards", 2);
        ACCOUNT_INTENT_KEYWORDS.put("coupon", 2);
        ACCOUNT_INTENT_KEYWORDS.put("coupons", 2);

        // Contact intent keywords
        CONTACT_INTENT_KEYWORDS.put("contact", 4);
        CONTACT_INTENT_KEYWORDS.put("phone", 3);
        CONTACT_INTENT_KEYWORDS.put("telephone", 2);
        CONTACT_INTENT_KEYWORDS.put("address", 3);
        CONTACT_INTENT_KEYWORDS.put("location", 3);
        CONTACT_INTENT_KEYWORDS.put("where are you", 4);
        CONTACT_INTENT_KEYWORDS.put("working hours", 4);
        CONTACT_INTENT_KEYWORDS.put("working hour", 3);
        CONTACT_INTENT_KEYWORDS.put("hours", 3);
        CONTACT_INTENT_KEYWORDS.put("open", 3);
        CONTACT_INTENT_KEYWORDS.put("closed", 3);
        CONTACT_INTENT_KEYWORDS.put("when", 2);
        CONTACT_INTENT_KEYWORDS.put("time", 2);
        CONTACT_INTENT_KEYWORDS.put("email", 2);
        CONTACT_INTENT_KEYWORDS.put("call", 2);
        CONTACT_INTENT_KEYWORDS.put("reach", 2);

        // Greeting intent keywords
        GREETING_INTENT_KEYWORDS.put("hello", 3);
        GREETING_INTENT_KEYWORDS.put("hi", 3);
        GREETING_INTENT_KEYWORDS.put("hey", 2);
        GREETING_INTENT_KEYWORDS.put("greetings", 2);
        GREETING_INTENT_KEYWORDS.put("good morning", 3);
        GREETING_INTENT_KEYWORDS.put("good afternoon", 3);
        GREETING_INTENT_KEYWORDS.put("good evening", 3);
        GREETING_INTENT_KEYWORDS.put("good night", 2);
        GREETING_INTENT_KEYWORDS.put("morning", 2);
        GREETING_INTENT_KEYWORDS.put("afternoon", 2);

        // Help intent keywords
        HELP_INTENT_KEYWORDS.put("help", 5);
        HELP_INTENT_KEYWORDS.put("what can you do", 4);
        HELP_INTENT_KEYWORDS.put("commands", 3);
        HELP_INTENT_KEYWORDS.put("command", 3);
        HELP_INTENT_KEYWORDS.put("what do you do", 4);
        HELP_INTENT_KEYWORDS.put("capabilities", 3);
        HELP_INTENT_KEYWORDS.put("what can", 3);
        HELP_INTENT_KEYWORDS.put("how can", 3);
        HELP_INTENT_KEYWORDS.put("assist", 2);
        HELP_INTENT_KEYWORDS.put("support", 2);
    }

    /**
     * Default constructor.
     * <p>
     * Initializes DAOs and pre-loads the product cache for faster search
     * performance.
     * </p>
     * 
     * @author Burak Özevin
     */
    public ChatbotService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
        refreshProductCache();
    }

    /**
     * Refreshes the internal cache of available products.
     * <p>
     * Should be called periodically or before processing product-related queries
     * to ensure stock levels and prices are up-to-date.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void refreshProductCache() {
        try {
            allProductsCache = productDAO.findAllInStock();
        } catch (SQLException e) {
            allProductsCache = new ArrayList<>();
        }
    }

    /**
     * Processes a user message and returns an appropriate response.
     * <p>
     * Uses a multi-stage process:
     * </p>
     * <ol>
     * <li>Checks for specific product mentions (fuzzy match).</li>
     * <li>Calculates intent scores for various categories (Product, Order, Account,
     * etc.).</li>
     * <li>Selects the highest scoring intent.</li>
     * <li>Generates a response based on the intent and user context.</li>
     * </ol>
     *
     * @param userMessage The raw message text from the user.
     * @param user        The currently logged-in {@link User} (for context-aware
     *                    answers).
     * @return A string containing the chatbot's response.
     * 
     * @author Burak Özevin
     */
    public String processMessage(String userMessage, User user) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Hello! How can I help you today?";
        }

        String lowerMessage = userMessage.toLowerCase(Locale.ENGLISH).trim();

        // Check for product names first (even if not explicitly asking about products)
        Product mentionedProduct = findProductInMessage(lowerMessage);
        if (mentionedProduct != null && !isExplicitProductQuery(lowerMessage)) {
            // Product mentioned but not explicitly asking - provide helpful info
            return "I noticed you mentioned " + mentionedProduct.getName() + ". " +
                    formatProductInfo(mentionedProduct) +
                    "\n\nWould you like to know more about this product or anything else?";
        }

        // Intent recognition with scoring
        IntentScore productScore = calculateIntentScore(lowerMessage, PRODUCT_INTENT_KEYWORDS);
        IntentScore orderScore = calculateIntentScore(lowerMessage, ORDER_INTENT_KEYWORDS);
        IntentScore accountScore = calculateIntentScore(lowerMessage, ACCOUNT_INTENT_KEYWORDS);
        IntentScore contactScore = calculateIntentScore(lowerMessage, CONTACT_INTENT_KEYWORDS);
        IntentScore greetingScore = calculateIntentScore(lowerMessage, GREETING_INTENT_KEYWORDS);
        IntentScore helpScore = calculateIntentScore(lowerMessage, HELP_INTENT_KEYWORDS);

        // Find the highest scoring intent
        IntentScore maxScore = findMaxScore(productScore, orderScore, accountScore, contactScore, greetingScore,
                helpScore);

        // Handle based on intent
        if (greetingScore.score >= 2) {
            return "Hello! Welcome to GreenGrocer! How can I assist you today?";
        }

        if (helpScore.score >= 3) {
            return "I can help you with:\n" +
                    "• Product search (e.g., 'do you have tomatoes?', 'apple price?', 'tell me about potatoes')\n" +
                    "• Order status (e.g., 'where is my order?', 'my last order?', 'track my order')\n" +
                    "• Account information (e.g., 'my points?', 'total orders?', 'how many orders do I have?')\n" +
                    "• General information (e.g., 'working hours?', 'contact?', 'when are you open?')\n\n" +
                    "Just ask me naturally - I understand typos and can help you find what you're looking for!";
        }

        if (productScore.score >= 2) {
            return handleProductQuery(lowerMessage);
        }

        if (orderScore.score >= 2) {
            return handleOrderQuery(lowerMessage, user);
        }

        if (accountScore.score >= 2) {
            return handleAccountQuery(lowerMessage, user);
        }

        if (contactScore.score >= 2) {
            return "GreenGrocer working hours:\n" +
                    "• Monday - Friday: 09:00 - 20:00\n" +
                    "• Saturday: 10:00 - 18:00\n" +
                    "• Sunday: Closed\n\n" +
                    "Contact:\n" +
                    "• Phone: 555-0003\n" +
                    "• Email: owner@test.com\n" +
                    "• Address: Istanbul, Turkey\n\n" +
                    "You can also contact us through the Messages section!";
        }

        // Thank you / Goodbye
        if (containsAny(lowerMessage, "thank", "thanks", "thank you", "bye", "goodbye", "see you", "farewell",
                "appreciate", "grateful")) {
            return "You're welcome! Feel free to ask if you have any other questions. Have a great day! 😊";
        }

        // If product was mentioned but no clear intent, assume product query
        if (mentionedProduct != null) {
            return handleProductQuery(lowerMessage);
        }

        // Default response with suggestions
        return "I'm sorry, I couldn't help you with that. Could you please rephrase your question?\n\n" +
                "I can help you with:\n" +
                "• Product search and pricing\n" +
                "• Order status and tracking\n" +
                "• Account information\n" +
                "• Working hours and contact\n\n" +
                "Type 'help' to see all available commands, or just ask me naturally!";
    }

    /**
     * Calculates an intent score based on keyword matching with weights.
     *
     * @param message  The lower-case user message.
     * @param keywords A map of keywords to their relevance weights.
     * @return An {@link IntentScore} object containing the total score and match
     *         count.
     * 
     * @author Burak Özevin
     */
    private IntentScore calculateIntentScore(String message, Map<String, Integer> keywords) {
        int score = 0;
        int matches = 0;

        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            if (message.contains(entry.getKey())) {
                score += entry.getValue();
                matches++;
            }
        }

        return new IntentScore(score, matches);
    }

    /**
     * Finds the intent score with the highest value.
     * 
     * @param scores Variable arguments of IntentScore objects
     * @return The IntentScore with the highest score
     * @author Burak Özevin
     */
    private IntentScore findMaxScore(IntentScore... scores) {
        IntentScore max = scores[0];
        for (IntentScore score : scores) {
            if (score.score > max.score) {
                max = score;
            }
        }
        return max;
    }

    /**
     * Helper class to hold intent scoring results.
     * 
     * @author Burak Özevin
     */
    private static class IntentScore {
        final int score;
        final int matches;

        IntentScore(int score, int matches) {
            this.score = score;
            this.matches = matches;
        }
    }

    /**
     * Handles product-related queries.
     * 
     * @param message The user's message
     * @return The response string
     * @author Burak Özevin
     */
    private String handleProductQuery(String message) {
        try {
            refreshProductCache();

            // Try to find product using fuzzy matching
            Product foundProduct = findProductInMessage(message);

            if (foundProduct != null) {
                return formatProductInfo(foundProduct);
            }

            // Try exact search
            String productName = extractProductName(message);
            if (productName != null && !productName.isEmpty()) {
                List<Product> products = productDAO.search(productName);

                if (products.isEmpty()) {
                    // Try fuzzy search
                    Product fuzzyMatch = fuzzySearchProduct(productName);
                    if (fuzzyMatch != null) {
                        return "Did you mean '" + fuzzyMatch.getName() + "'?\n\n" + formatProductInfo(fuzzyMatch);
                    }

                    return "Sorry, I couldn't find a product named '" + productName + "'. " +
                            "Please try a different product name or ask me to search for similar products.";
                }

                return formatProductsList(products);
            } else {
                // Check if asking for all products or categories
                if (containsAny(message, "all", "everything", "list", "show", "what", "what do you", "what products",
                        "catalog", "menu")) {
                    return handleListProducts();
                }

                return "Which product would you like information about? " +
                        "For example: 'do you have tomatoes?', 'apple price?', or just mention the product name!";
            }
        } catch (SQLException e) {
            return "Sorry, I'm unable to access product information right now. Please try again later.";
        }
    }

    /**
     * Generates a list of available products.
     * 
     * @return The formatted list of products
     * @author Burak Özevin
     */
    private String handleListProducts() {
        try {
            refreshProductCache();
            if (allProductsCache.isEmpty()) {
                return "Sorry, we don't have any products in stock right now.";
            }

            StringBuilder response = new StringBuilder();
            response.append("We have ").append(allProductsCache.size()).append(" products available:\n\n");

            // Group by type
            List<Product> vegetables = new ArrayList<>();
            List<Product> fruits = new ArrayList<>();

            for (Product p : allProductsCache) {
                if (p.getType() == Product.ProductType.VEGETABLE) {
                    vegetables.add(p);
                } else {
                    fruits.add(p);
                }
            }

            if (!vegetables.isEmpty()) {
                response.append("🥕 Vegetables:\n");
                for (int i = 0; i < Math.min(10, vegetables.size()); i++) {
                    response.append("• ").append(vegetables.get(i).getName())
                            .append(" - ₺").append(String.format("%.2f", vegetables.get(i).getPrice()))
                            .append(" / ")
                            .append(vegetables.get(i).getUnitType() == Product.UnitType.KG ? "kg" : "piece")
                            .append("\n");
                }
                if (vegetables.size() > 10) {
                    response.append("... and ").append(vegetables.size() - 10).append(" more\n");
                }
                response.append("\n");
            }

            if (!fruits.isEmpty()) {
                response.append("🍎 Fruits:\n");
                for (int i = 0; i < Math.min(10, fruits.size()); i++) {
                    response.append("• ").append(fruits.get(i).getName())
                            .append(" - ₺").append(String.format("%.2f", fruits.get(i).getPrice()))
                            .append(" / ").append(fruits.get(i).getUnitType() == Product.UnitType.KG ? "kg" : "piece")
                            .append("\n");
                }
                if (fruits.size() > 10) {
                    response.append("... and ").append(fruits.size() - 10).append(" more\n");
                }
            }

            response.append("\nAsk me about any specific product for more details!");
            return response.toString();
        } catch (Exception e) {
            return "Sorry, I'm unable to list products right now. Please try again later.";
        }
    }

    /**
     * Formats detailed information about a single product.
     * 
     * @param p The product to format
     * @return The formatted string
     * @author Burak Özevin
     */
    private String formatProductInfo(Product p) {
        StringBuilder response = new StringBuilder();
        response.append("✅ ").append(p.getName()).append(" is available!\n\n");
        response.append("Price: ₺").append(String.format("%.2f", p.getPrice())).append(" / ");
        response.append(p.getUnitType() == Product.UnitType.KG ? "kg" : "piece").append("\n");
        response.append("Stock: ").append(String.format("%.1f", p.getStock())).append(" ");
        response.append(p.getUnitType() == Product.UnitType.KG ? "kg" : "piece");
        if (p.isBelowThreshold()) {
            response.append("\n⚠️ Low stock - Price 2x!");
        }
        if (p.getDescription() != null && !p.getDescription().isEmpty()) {
            response.append("\n\nDescription: ").append(p.getDescription());
        }
        return response.toString();
    }

    /**
     * Formats a list of products into a summary string.
     * 
     * @param products The list of products
     * @return The formatted string
     * @author Burak Özevin
     */
    private String formatProductsList(List<Product> products) {
        if (products.size() == 1) {
            return formatProductInfo(products.get(0));
        }

        StringBuilder response = new StringBuilder();
        response.append("Found ").append(products.size()).append(" product(s):\n\n");
        for (Product p : products) {
            if (p.getStock() > 0) {
                response.append("• ").append(p.getName())
                        .append(" - ₺").append(String.format("%.2f", p.getPrice()))
                        .append(" / ").append(p.getUnitType() == Product.UnitType.KG ? "kg" : "piece")
                        .append(" (Stock: ").append(String.format("%.1f", p.getStock())).append(")\n");
            }
        }
        return response.toString();
    }

    /**
     * Handles order-related queries.
     * 
     * @param message The user's message
     * @param user    The current user
     * @return The response string
     * @author Burak Özevin
     */
    private String handleOrderQuery(String message, User user) {
        try {
            List<Order> orders = orderDAO.findByCustomer(user.getId());

            if (orders.isEmpty()) {
                return "You don't have any orders yet. You can add products to your cart to place your first order! " +
                        "Would you like me to help you find some products?";
            }

            // Get latest order
            Order latestOrder = orders.get(0);

            if (containsAny(message, "last", "latest", "recent", "my last order", "most recent", "previous")) {
                return "Your last order:\n\n" + formatOrderInfo(latestOrder);
            }

            if (containsAny(message, "where", "status", "track", "location", "when will", "when is", "how is",
                    "what is the status")) {
                return formatOrderStatus(latestOrder);
            }

            // Check for specific order number
            if (message.matches(".*order\\s*#?\\s*\\d+.*") || message.matches(".*#\\s*\\d+.*")) {
                String orderNum = extractOrderNumber(message);
                if (orderNum != null) {
                    try {
                        int orderId = Integer.parseInt(orderNum);
                        for (Order order : orders) {
                            if (order.getId() == orderId) {
                                return formatOrderInfo(order) + "\n\n" + formatOrderStatus(order);
                            }
                        }
                        return "Order #" + orderId + " not found. Please check your order number.";
                    } catch (NumberFormatException e) {
                        // Continue with general order info
                    }
                }
            }

            // General order info
            StringBuilder response = new StringBuilder();
            response.append("You have ").append(orders.size()).append(" total order(s).\n\n");
            response.append("Your last order:\n\n");
            response.append(formatOrderInfo(latestOrder));

            if (orders.size() > 1) {
                response.append("\n\nWould you like information about a specific order? " +
                        "Just mention the order number (e.g., 'order #123').");
            }

            return response.toString();
        } catch (SQLException e) {
            return "Sorry, I'm unable to access order information right now. Please try again later.";
        }
    }

    /**
     * Extracts an order number from the message using regex.
     * 
     * @param message The message text
     * @return The extracted order number or null
     * @author Burak Özevin
     */
    private String extractOrderNumber(String message) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:order\\s*#?\\s*|#\\s*)(\\d+)",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Handles account-related queries.
     * 
     * @param message The user's message
     * @param user    The current user
     * @return The response string
     * @author Burak Özevin
     */
    private String handleAccountQuery(String message, User user) {
        try {
            if (user instanceof com.greengrocer.models.Customer) {
                com.greengrocer.models.Customer customer = (com.greengrocer.models.Customer) user;

                if (containsAny(message, "point", "points", "loyalty", "reward", "rewards")) {
                    int points = customer.getLoyaltyPoints();
                    return "Your loyalty points: " + points + " points\n\n" +
                            (points > 0 ? "You can convert your points into coupons and get discounts! " +
                                    "Check the 'My Coupons' section to see available offers."
                                    : "Start ordering to earn loyalty points! Each order earns you points that can be converted to discounts.");
                }

                if (containsAny(message, "order count", "total orders", "how many orders", "number of orders",
                        "orders do i have")) {
                    List<Order> orders = orderDAO.findByCustomer(user.getId());
                    int totalOrders = orders.size();
                    return "Total number of orders: " + totalOrders + "\n\n" +
                            "Your loyalty points: " + customer.getLoyaltyPoints() + " points\n\n" +
                            (totalOrders == 0 ? "Start shopping to place your first order!"
                                    : "Keep ordering to earn more loyalty points!");
                }

                if (containsAny(message, "coupon", "coupons", "discount", "discounts", "voucher", "vouchers")) {
                    return "You can view your available coupons in the 'My Coupons' section. " +
                            "Loyalty points can be converted to coupons for discounts on your orders!";
                }
            }

            return "You can access your account information from the profile page. " +
                    "Is there anything specific you'd like to know about your account?";
        } catch (SQLException e) {
            return "Sorry, I'm unable to access account information right now. Please try again later.";
        }
    }

    /**
     * Formats order details into a readable string.
     * 
     * @param order The order to format
     * @return The formatted string
     * @author Burak Özevin
     */
    private String formatOrderInfo(Order order) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        StringBuilder info = new StringBuilder();

        info.append("Order #").append(order.getId()).append("\n");
        info.append("Status: ").append(getStatusText(order.getStatus())).append("\n");
        info.append("Date: ").append(order.getOrderTime().format(fmt)).append("\n");
        info.append("Total: ₺").append(String.format("%.2f", order.getTotalCost())).append("\n");

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            info.append("Products: ");
            for (int i = 0; i < Math.min(3, order.getItems().size()); i++) {
                info.append(order.getItems().get(i).getProductName());
                if (i < Math.min(2, order.getItems().size() - 1)) {
                    info.append(", ");
                }
            }
            if (order.getItems().size() > 3) {
                info.append(" and ").append(order.getItems().size() - 3).append(" more product(s)");
            }
        }

        return info.toString();
    }

    /**
     * Formats order status information.
     * 
     * @param order The order to check
     * @return The status message
     * @author Burak Özevin
     */
    private String formatOrderStatus(Order order) {
        String status = getStatusText(order.getStatus());
        StringBuilder response = new StringBuilder();

        response.append("Order #").append(order.getId()).append(" status: ").append(status).append("\n\n");

        switch (order.getStatus()) {
            case PENDING:
                response.append("Your order is pending approval. It will be processed shortly. " +
                        "You'll receive a notification once it's confirmed!");
                break;
            case SELECTED:
                response.append("Your order is on the way! It will be delivered by a carrier. ");
                if (order.getCarrierName() != null) {
                    response.append("\nCarrier: ").append(order.getCarrierName());
                }
                response.append("\n\nYou can track your order in the 'Orders' section.");
                break;
            case DELIVERED:
                response.append("Your order has been delivered! We hope you're satisfied. " +
                        "Don't forget to rate your carrier if you haven't already!");
                break;
            case CANCELLED:
                response.append("This order has been cancelled. " +
                        "If you have any questions, please contact us through the Messages section.");
                break;
        }

        return response.toString();
    }

    /**
     * Converts OrderStatus enum to display text.
     * 
     * @param status The order status
     * @return The display text
     * @author Burak Özevin
     */
    private String getStatusText(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Pending";
            case SELECTED -> "On the way";
            case DELIVERED -> "Delivered";
            case CANCELLED -> "Cancelled";
        };
    }

    /**
     * Finds a product mentioned in the message using fuzzy matching.
     * <p>
     * This handles typos and variations in product names (e.g., "aple" -> "Apple").
     * </p>
     *
     * @param message The user message to search within.
     * @return The matched {@link Product}, or {@code null} if no match found.
     * @author Burak Özevin
     */
    private Product findProductInMessage(String message) {
        if (allProductsCache == null || allProductsCache.isEmpty()) {
            refreshProductCache();
        }

        if (allProductsCache.isEmpty()) {
            return null;
        }

        // First, try exact substring match
        for (Product product : allProductsCache) {
            String productName = product.getName().toLowerCase(Locale.ENGLISH);
            if (message.contains(productName)) {
                return product;
            }
        }

        // If no exact match, try fuzzy matching
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (word.length() < 3)
                continue; // Skip very short words

            Product bestMatch = fuzzySearchProduct(word);
            if (bestMatch != null) {
                return bestMatch;
            }
        }

        return null;
    }

    /**
     * Fuzzy search for a product using Levenshtein distance.
     * <p>
     * Tolerates typos and similar spellings by calculating the edit distance
     * between the search term and product names.
     * </p>
     *
     * @param searchTerm The term to search for.
     * @return The best matching {@link Product}, or {@code null} if no close match
     *         found.
     * @author Burak Özevin
     */
    private Product fuzzySearchProduct(String searchTerm) {
        if (allProductsCache == null || allProductsCache.isEmpty()) {
            refreshProductCache();
        }

        if (allProductsCache.isEmpty() || searchTerm == null || searchTerm.length() < 2) {
            return null;
        }

        String lowerSearch = searchTerm.toLowerCase(Locale.ENGLISH);
        Product bestMatch = null;
        int bestDistance = Integer.MAX_VALUE;
        int threshold = Math.max(2, searchTerm.length() / 3); // Adaptive threshold

        for (Product product : allProductsCache) {
            String productName = product.getName().toLowerCase(Locale.ENGLISH);

            // Check if product name contains search term or vice versa
            if (productName.contains(lowerSearch) || lowerSearch.contains(productName)) {
                return product; // Exact substring match
            }

            // Calculate Levenshtein distance
            int distance = levenshteinDistance(lowerSearch, productName);

            // Also check word-by-word matching
            String[] productWords = productName.split("\\s+");
            for (String word : productWords) {
                int wordDistance = levenshteinDistance(lowerSearch, word);
                if (wordDistance < distance) {
                    distance = wordDistance;
                }
            }

            if (distance < bestDistance && distance <= threshold) {
                bestDistance = distance;
                bestMatch = product;
            }
        }

        return bestMatch;
    }

    /**
     * Calculates the Levenshtein distance between two strings.
     * <p>
     * The Levenshtein distance is the minimum number of single-character edits
     * (insertions, deletions, or substitutions) required to change one word into
     * the other.
     * </p>
     *
     * @param s1 The first string.
     * @param s2 The second string.
     * @return The edit distance as an integer.
     * @author Burak Özevin
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == 0)
            return len2;
        if (len2 == 0)
            return len1;

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[len1][len2];
    }

    /**
     * Checks if the message is an explicit product query.
     * <p>
     * Distinguishes between casual mentions ("I like apples") and specific
     * questions
     * ("How much are apples?", "Do you have apples?").
     * </p>
     *
     * @param message The user message.
     * @return {@code true} if the message contains product query keywords.
     * @author Burak Özevin
     */
    private boolean isExplicitProductQuery(String message) {
        return containsAny(message, "price", "cost", "how much", "do you have", "available",
                "stock", "tell me about", "information about", "what is", "search");
    }

    /**
     * Extracts a potential product name from the message by removing common words.
     * 
     * @param message The message text
     * @return The extracted name or null
     * @author Burak Özevin
     */
    private String extractProductName(String message) {
        // Remove common query words
        String cleaned = message
                .replaceAll(
                        "(?i)(do you have|is|are|price|prices|available|stock|how much|what is|tell me about|information about|search for|find|looking for|need|want|buy|purchase|cost|the|a|an|show me|list|what products|what do you|everything|all)\\s+",
                        "")
                .replaceAll("[?.,!]", "")
                .trim();

        if (cleaned.length() > 2) {
            return cleaned;
        }

        return null;
    }

    /**
     * Checks if the text contains any of the given keywords.
     * 
     * @param text     The text to check
     * @param keywords The keywords to look for
     * @return true if any keyword is found
     * @author Burak Özevin
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
