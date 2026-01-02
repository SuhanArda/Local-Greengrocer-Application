package com.greengrocer.controllers;

import com.greengrocer.dao.CarrierRatingDAO;
import com.greengrocer.dao.OrderDAO;
import com.greengrocer.models.Order;
import com.greengrocer.models.Order.OrderStatus;
import com.greengrocer.models.OrderItem;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ThemeManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the carrier interface.
 * <p>
 * Manages the carrier's dashboard, including viewing available orders,
 * managing active deliveries, and viewing completed delivery history.
 * Handles UI interactions for accepting and completing orders.
 * </p>
 * 
 * @author Elif Zeynep Talay
 */
public class CarrierController implements Initializable {

    @FXML
    private Label pageTitle;
    @FXML
    private Label ratingStars;
    @FXML
    private Label ratingText;
    @FXML
    private VBox ordersPane;
    @FXML
    private MFXButton themeToggleBtn;
    @FXML
    private MFXButton menuButton;
    @FXML
    private MFXButton closeMenuButton;
    @FXML
    private Region backdrop;
    @FXML
    private VBox sidebar;
    @FXML
    private BorderPane mainContainer;

    private final OrderDAO orderDAO = new OrderDAO();
    private final CarrierRatingDAO carrierRatingDAO = new CarrierRatingDAO();
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private String currentView = "available";

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the hamburger menu, loads the default view (available orders),
     * updates the carrier's rating display, and initializes the theme.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     * 
     * @author Elif Zeynep Talay
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupHamburgerMenu();
        showAvailable();
        updateRating();

        // Initialize theme
        updateThemeIcon();
        javafx.application.Platform.runLater(() -> {
            if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
                themeManager.applyTheme(themeToggleBtn.getScene());
            }
        });
    }

    // Theme toggle methods
    /**
     * Toggles the application theme between light and dark modes.
     * <p>
     * Updates the scene's stylesheets and the toggle button icon.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void handleThemeToggle() {
        if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
            themeManager.toggleTheme(themeToggleBtn.getScene());
            updateThemeIcon();
        }
    }

    /**
     * Updates the theme toggle button's icon based on the current theme.
     * 
     * @author Elif Zeynep Talay
     */
    private void updateThemeIcon() {
        if (themeToggleBtn != null) {
            String iconLiteral = themeManager.isDarkTheme() ? "fas-sun" : "fas-moon";
            String color = themeManager.isDarkTheme() ? "#ffeb3b" : "white";

            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon(iconLiteral);
            icon.setIconSize(20);
            icon.setIconColor(javafx.scene.paint.Color.web(color));

            themeToggleBtn.setGraphic(icon);
            themeToggleBtn.setText("");
        }
    }

    // Hamburger menu methods
    /**
     * Configures the hamburger menu sidebar.
     * <p>
     * Sets initial visibility and adds event handlers for opening/closing the menu
     * via buttons or clicking the backdrop.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void setupHamburgerMenu() {
        sidebar.setTranslateX(320); // Hidden to the right
        backdrop.setVisible(false);
        backdrop.setOpacity(0);

        menuButton.setOnAction(e -> toggleMenu(true));
        closeMenuButton.setOnAction(e -> toggleMenu(false));
        backdrop.setOnMouseClicked(e -> toggleMenu(false));
    }

    /**
     * Toggles the visibility of the sidebar menu with animation.
     *
     * @param show {@code true} to show the menu, {@code false} to hide it.
     * 
     * @author Elif Zeynep Talay
     */
    private void toggleMenu(boolean show) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebar);
        FadeTransition fade = new FadeTransition(Duration.millis(300), backdrop);

        if (show) {
            backdrop.setVisible(true);
            slide.setToX(0);
            fade.setFromValue(0);
            fade.setToValue(1);
        } else {
            slide.setToX(320); // Slide back to right
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> backdrop.setVisible(false));
        }

        slide.play();
        fade.play();
    }

    // Close menu after navigation
    /**
     * Closes the hamburger menu.
     * 
     * @author Elif Zeynep Talay
     */
    private void closeMenu() {
        toggleMenu(false);
    }

    /**
     * Displays the list of available orders that can be accepted by the carrier.
     * <p>
     * Fetches pending orders from the database and updates the UI.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void showAvailable() {
        closeMenu();
        currentView = "available";
        pageTitle.setText("📋 Available Deliveries");
        try {
            List<Order> orders = orderDAO.findPendingOrders();
            displayOrders(orders, true);
        } catch (SQLException e) {
            showError("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the list of currently active deliveries for the logged-in carrier.
     * <p>
     * Fetches orders assigned to the current carrier with status
     * {@link OrderStatus#SELECTED}.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void showCurrent() {
        closeMenu();
        currentView = "current";
        pageTitle.setText("🚛 My Active Deliveries");
        try {
            int carrierId = SessionManager.getInstance().getCurrentUserId();
            List<Order> orders = orderDAO.findByCarrier(carrierId);
            orders.removeIf(o -> o.getStatus() != OrderStatus.SELECTED);
            displayOrders(orders, false);
        } catch (SQLException e) {
            showError("Error loading deliveries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the history of completed deliveries for the logged-in carrier.
     * <p>
     * Fetches orders assigned to the current carrier with status
     * {@link OrderStatus#DELIVERED}.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void showCompleted() {
        closeMenu();
        currentView = "completed";
        pageTitle.setText("✅ Completed Deliveries");
        try {
            int carrierId = SessionManager.getInstance().getCurrentUserId();
            List<Order> orders = orderDAO.findByCarrier(carrierId);
            orders.removeIf(o -> o.getStatus() != OrderStatus.DELIVERED);
            displayOrders(orders, false);
        } catch (SQLException e) {
            showError("Error loading completed deliveries: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renders a list of orders into the orders pane.
     *
     * @param orders           The list of {@link Order} objects to display.
     * @param showSelectButton If {@code true}, shows the "Accept Order" button for
     *                         pending orders.
     * 
     * @author Elif Zeynep Talay
     */
    private void displayOrders(List<Order> orders, boolean showSelectButton) {
        ordersPane.getChildren().clear();

        if (orders.isEmpty()) {
            Label noOrders = new Label("No orders found");
            noOrders.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
            ordersPane.getChildren().add(noOrders);
            return;
        }

        for (Order order : orders) {
            VBox card = createOrderCard(order, showSelectButton);
            ordersPane.getChildren().add(card);
        }
    }

    /**
     * Creates a UI card component representing a single order.
     *
     * @param order            The {@link Order} to display.
     * @param showSelectButton If {@code true}, includes an "Accept Order" button
     *                         for pending orders.
     * @return A {@link VBox} containing the order details and action buttons.
     * 
     * @author Elif Zeynep Talay
     */
    private VBox createOrderCard(Order order, boolean showSelectButton) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("product-card");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label orderId = new Label("Order #" + order.getId());
        orderId.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label status = new Label(getStatusText(order.getStatus()));
        status.getStyleClass().addAll("badge", "badge-" + order.getStatus().getValue());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(orderId, spacer, status);

        // Customer info
        Label customer = new Label("👤 " + order.getCustomerName());
        Label address = new Label("📍 " + order.getCustomerAddress());

        // Delivery time
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        Label deliveryTime = new Label("🕐 Delivery: " + order.getRequestedDeliveryTime().format(fmt));

        // Total
        Label total = new Label(String.format("💰 Total: ₺%.2f", order.getTotalCost()));
        total.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        // Products
        StringBuilder products = new StringBuilder("📦 Products: ");
        for (OrderItem item : order.getItems()) {
            products.append(item.getProductName()).append(" (").append(item.getAmount()).append("kg), ");
        }
        Label productList = new Label(products.toString());
        productList.setWrapText(true);

        card.getChildren().addAll(header, customer, address, deliveryTime, total, productList);

        // Action buttons
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (showSelectButton && order.getStatus() == OrderStatus.PENDING) {
            MFXButton selectBtn = new MFXButton("✓ Accept Order");
            selectBtn.getStyleClass().add("primary-button");
            selectBtn.setOnAction(e -> selectOrder(order));
            actions.getChildren().add(selectBtn);
        }

        if (order.getStatus() == OrderStatus.SELECTED) {
            MFXButton completeBtn = new MFXButton("✓ Mark Delivered");
            completeBtn.getStyleClass().add("primary-button");
            completeBtn.setOnAction(e -> completeOrder(order));
            actions.getChildren().add(completeBtn);
        }

        if (!actions.getChildren().isEmpty()) {
            card.getChildren().add(actions);
        }

        return card;
    }

    /**
     * Converts the order status to a human-readable string.
     *
     * @param status The {@link OrderStatus} to convert.
     * @return A string representation of the status.
     * 
     * @author Elif Zeynep Talay
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
     * Assigns the current carrier to the specified order.
     * <p>
     * Attempts to update the order status to {@link OrderStatus#SELECTED} and set
     * the carrier ID.
     * Shows a success or error message based on the result.
     * </p>
     *
     * @param order The {@link Order} to accept.
     * 
     * @author Elif Zeynep Talay
     */
    private void selectOrder(Order order) {
        int carrierId = SessionManager.getInstance().getCurrentUserId();
        try {
            boolean success = orderDAO.assignCarrier(order.getId(), carrierId);
            if (success) {
                showSuccess("Order accepted!");
                showAvailable();
            } else {
                showError("This order has already been accepted by another carrier!");
            }
        } catch (SQLException e) {
            showError("Error accepting order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Marks the specified order as delivered.
     * <p>
     * Updates the order status to {@link OrderStatus#DELIVERED} and sets the
     * delivery timestamp.
     * </p>
     *
     * @param order The {@link Order} to complete.
     * 
     * @author Elif Zeynep Talay
     */
    private void completeOrder(Order order) {
        try {
            boolean success = orderDAO.markDelivered(order.getId(), LocalDateTime.now());
            if (success) {
                showSuccess("Delivery completed!");
                showCurrent();
            } else {
                showError("Delivery could not be completed!");
            }
        } catch (SQLException e) {
            showError("Error completing delivery: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the current view (available, current, or completed orders).
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void refreshOrders() {
        switch (currentView) {
            case "available" -> showAvailable();
            case "current" -> showCurrent();
            case "completed" -> showCompleted();
        }
    }

    /**
     * Navigates to the carrier profile page.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/carrier_profile.fxml"));
            Parent root = loader.load();
            Scene scene = ordersPane.getScene();
            scene.setRoot(root);
            ((Stage) scene.getWindow()).setTitle("GreenGrocer - Carrier Profile");
        } catch (IOException e) {
            showError("Error loading profile page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs out the current user and returns to the login screen.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        navigateToLogin();
    }

    /**
     * Navigates to the login screen.
     * <p>
     * Loads the {@code login.fxml} view and sets it as the current scene.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = ordersPane.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert with the specified message.
     *
     * @param msg The error message to display.
     * 
     * @author Elif Zeynep Talay
     */
    private void showError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.initOwner(ordersPane.getScene().getWindow());
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Displays a success alert with the specified message.
     *
     * @param msg The success message to display.
     * 
     * @author Elif Zeynep Talay
     */
    private void showSuccess(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.initOwner(ordersPane.getScene().getWindow());
        alert.setTitle("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Updates the carrier's rating display.
     * <p>
     * Fetches the average rating from the database and updates the star display and
     * text.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void updateRating() {
        int carrierId = SessionManager.getInstance().getCurrentUserId();
        double avgRating = carrierRatingDAO.getAverageRating(carrierId);

        // Ensure rating is within 0-5
        if (avgRating < 0)
            avgRating = 0;
        if (avgRating > 5)
            avgRating = 5;

        StringBuilder stars = new StringBuilder();
        int fullStars = (int) avgRating;
        boolean halfStar = (avgRating - fullStars) >= 0.5;

        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars.append("★");
            } else if (i == fullStars && halfStar) {
                stars.append("★"); // Using full star for simplicity as font might not have half star, or could use
                                   // ½ symbol
            } else {
                stars.append("☆");
            }
        }

        if (ratingStars != null) {
            ratingStars.setText(stars.toString());
        }

        if (ratingText != null) {
            ratingText.setText(String.format("%.1f out of 5", avgRating));
        }
    }
}
