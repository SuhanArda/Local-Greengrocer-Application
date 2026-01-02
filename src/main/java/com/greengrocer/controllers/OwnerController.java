package com.greengrocer.controllers;

import com.greengrocer.dao.*;
import com.greengrocer.models.*;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ThemeManager;
import io.github.palexdev.materialfx.controls.*;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.*;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Comparator;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the owner interface.
 * <p>
 * Manages the overall dashboard for the store owner, including product
 * management,
 * carrier management, order oversight, coupon creation, and sales reports.
 * Provides navigation between these different modules.
 * </p>
 * 
 * @author Burak Özevin, Elif Zeynep Talay
 */
public class OwnerController implements Initializable {

    /**
     * Label displaying the title of the current page.
     */
    @FXML
    private Label pageTitle;

    /**
     * Button for adding new items (products, carriers, coupons).
     */
    @FXML
    private MFXButton addButton;

    /**
     * Main content area where different views are loaded.
     */
    @FXML
    private VBox contentPane;

    // Hamburger menu and sidebar

    /**
     * Button to toggle between light and dark themes.
     */
    @FXML
    private MFXButton themeToggleBtn;

    /**
     * Button to open the sidebar menu.
     */
    @FXML
    private MFXButton menuButton;

    /**
     * Button to close the sidebar menu.
     */
    @FXML
    private MFXButton closeMenuButton;

    /**
     * Backdrop overlay shown when the sidebar is open.
     */
    @FXML
    private Region backdrop;

    /**
     * Sidebar container holding navigation menu items.
     */
    @FXML
    private VBox sidebar;

    // Search

    /**
     * Text field for entering search queries.
     */
    @FXML
    private TextField searchField;

    /**
     * Container for the search bar.
     */
    @FXML
    private HBox searchContainer;

    /**
     * Main container for the owner interface layout.
     */
    @FXML
    private BorderPane mainContainer;

    /**
     * Button to trigger the search action.
     */
    @FXML
    private MFXButton searchButton;

    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final CouponDAO couponDAO = new CouponDAO();
    private final CarrierRatingDAO ratingDAO = new CarrierRatingDAO();
    private final SystemSettingDAO systemSettingDAO = new SystemSettingDAO();
    private final ThemeManager themeManager = ThemeManager.getInstance();

    private String currentView = "products";

    // Cache for search filtering
    private List<Product> allProducts;
    private List<User> allCarriers;
    private List<Coupon> allCoupons;

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the hamburger menu, applies the current theme, initializes the search
     * functionality,
     * and loads the default view (Products).
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Setup hamburger menu
        setupHamburgerMenu();

        // Setup theme
        themeManager.applyTheme(themeToggleBtn.getScene());
        updateThemeIcon();

        // Setup search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        showProducts();
    }

    // ==================== THEME MANAGEMENT ====================

    @FXML
    private void handleThemeToggle() {
        themeManager.toggleTheme(themeToggleBtn.getScene());
        updateThemeIcon();
    }

    private void updateThemeIcon() {
        FontIcon icon = (FontIcon) themeToggleBtn.getGraphic();
        if (icon != null) {
            String iconLiteral = themeManager.isDarkTheme() ? "fas-sun" : "fas-moon";
            String color = themeManager.isDarkTheme() ? "#ffeb3b" : "white";
            icon.setIconLiteral(iconLiteral);
            icon.setIconColor(javafx.scene.paint.Color.web(color));
        }
    }

    /**
     * Applies the current theme to a dialog.
     * <p>
     * Ensures that pop-up dialogs match the application's current theme
     * (Light/Dark).
     * </p>
     *
     * @param dialog The dialog to style.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void applyThemeToDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        if (themeManager.isDarkTheme()) {
            dialogPane.getStylesheets().add(getClass().getResource("/styles/styles-dark.css").toExternalForm());
            dialogPane.getStyleClass().add("dark-theme");
        }
    }

    // ==================== HAMBURGER MENU ====================

    /**
     * Sets up the hamburger menu animation and interaction.
     * <p>
     * Configures the sidebar to slide in/out and the backdrop to fade in/out.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
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
     * Toggles the visibility of the sidebar menu.
     * 
     * @param show True to show the menu, false to hide it.
     * @author Burak Özevin, Elif Zeynep Talay
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
            slide.setToX(320);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> backdrop.setVisible(false));
        }

        slide.play();
        fade.play();
    }

    /**
     * Closes the sidebar menu.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void closeMenu() {
        toggleMenu(false);
    }

    // ==================== SEARCH ====================

    /**
     * Handles search input changes.
     * <p>
     * Delegates filtering to the appropriate method based on the current view
     * (Products, Carriers, or Coupons).
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        switch (currentView) {
            case "products" -> filterProducts(query);
            case "carriers" -> filterCarriers(query);
            case "coupons" -> filterCoupons(query);
        }
    }

    /**
     * Filters the displayed products based on a search query.
     * 
     * @param query The search string to filter by.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void filterProducts(String query) {
        if (allProducts == null)
            return;
        if (query.isEmpty()) {
            createPagination(allProducts, this::displayProductPage);
        } else {
            List<Product> filtered = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().startsWith(query))
                    .collect(Collectors.toList());
            createPagination(filtered, this::displayProductPage);
        }
    }

    /**
     * Filters the displayed carriers based on a search query.
     * 
     * @param query The search string to filter by.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void filterCarriers(String query) {
        if (allCarriers == null)
            return;
        if (query.isEmpty()) {
            createPagination(allCarriers, this::displayCarrierPage);
        } else {
            List<User> filtered = allCarriers.stream()
                    .filter(c -> c.getFullName().toLowerCase().startsWith(query) ||
                            (c.getPhone() != null && c.getPhone().startsWith(query)))
                    .collect(Collectors.toList());
            createPagination(filtered, this::displayCarrierPage);
        }
    }

    /**
     * Filters the displayed coupons based on a search query.
     * 
     * @param query The search string to filter by.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void filterCoupons(String query) {
        if (allCoupons == null)
            return;
        if (query.isEmpty()) {
            createPagination(allCoupons, this::displayCouponPage);
        } else {
            List<Coupon> filtered = allCoupons.stream()
                    .filter(c -> c.getCode().toLowerCase().startsWith(query))
                    .collect(Collectors.toList());
            createPagination(filtered, this::displayCouponPage);
        }
    }

    // ==================== PROFILE ====================

    /**
     * Navigates to the profile settings page.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Parent root = loader.load();
            Scene scene = contentPane.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - My Profile");
        } catch (IOException e) {
            showError("Error loading profile page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== NAVIGATION ====================

    // ==================== NAVIGATION ====================

    /**
     * Displays the product management view.
     * <p>
     * Loads all products from the database and displays them with pagination.
     * Enables the "Add Product" button and search functionality.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showProducts() {
        closeMenu();
        currentView = "products";
        pageTitle.setText("📦 Product Management");
        addButton.setText("➕ New Product");
        addButton.setVisible(true);
        searchContainer.setVisible(true);
        searchField.setPromptText("Search product...");
        searchField.clear();

        try {
            allProducts = productDAO.findAll();
            createPagination(allProducts, this::displayProductPage);
        } catch (SQLException e) {
            showError("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a pagination control for a list of items.
     *
     * @param items        The list of items to paginate.
     * @param pageConsumer A consumer that populates a VBox with the items for a
     *                     specific page.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void createPagination(List<?> items, java.util.function.BiConsumer<VBox, List<?>> pageConsumer) {
        contentPane.getChildren().clear();

        if (items == null || items.isEmpty()) {
            Label placeholder = new Label("No items found.");
            placeholder.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 14px;");
            contentPane.getChildren().add(placeholder);
            return;
        }

        int itemsPerPage = 10;
        int pageCount = (int) Math.ceil((double) items.size() / itemsPerPage);

        Pagination pagination = new Pagination(pageCount, 0);
        pagination.setPageFactory(pageIndex -> {
            VBox pageBox = new VBox(10);
            pageBox.setPadding(new Insets(10));

            int fromIndex = pageIndex * itemsPerPage;
            int toIndex = Math.min(fromIndex + itemsPerPage, items.size());

            if (fromIndex < toIndex) {
                List<?> pageItems = items.subList(fromIndex, toIndex);
                pageConsumer.accept(pageBox, pageItems);
            }

            return pageBox;
        });

        // Ensure pagination fills the space
        VBox.setVgrow(pagination, Priority.ALWAYS);
        contentPane.getChildren().add(pagination);
    }

    @SuppressWarnings("unchecked")
    private void displayProductPage(VBox container, List<?> items) {
        List<Product> products = (List<Product>) items;
        for (Product product : products) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.getStyleClass().add("product-card");

            // Image Column
            ImageView imageView = new ImageView();
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setPreserveRatio(true);

            if (product.getImage() != null && product.getImage().length > 0) {
                try {
                    imageView.setImage(new Image(new java.io.ByteArrayInputStream(product.getImage())));
                } catch (Exception e) {
                    // ignore
                }
            }
            // If no image or error, maybe set a placeholder or leave empty

            row.getChildren().add(imageView);

            Label name = new Label(product.getName());
            name.setPrefWidth(150);
            name.setStyle("-fx-font-weight: bold;");

            Label type = new Label(product.getType() == Product.ProductType.VEGETABLE ? "Vegetable" : "Fruit");
            type.setPrefWidth(80);

            Label price = new Label(String.format("₺%.2f", product.getPrice()));
            price.setPrefWidth(80);

            Label stock = new Label(String.format("%.1f %s", product.getStock(),
                    product.getUnitType() == Product.UnitType.KG ? "kg" : "piece"));
            stock.setPrefWidth(80);

            Label threshold = new Label("Threshold: " + product.getThreshold());
            threshold.setPrefWidth(80);

            Label status = new Label(product.isBelowThreshold() ? "⚠️ Low" : "✓ Normal");
            status.setPrefWidth(80);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            MFXButton editBtn = new MFXButton("✏️");
            editBtn.setOnAction(e -> editProduct(product));

            MFXButton deleteBtn = new MFXButton("🗑️");
            deleteBtn.getStyleClass().add("danger-button");
            deleteBtn.setOnAction(e -> deleteProduct(product));

            row.getChildren().addAll(name, type, price, stock, threshold, status, spacer, editBtn, deleteBtn);
            container.getChildren().add(row);
        }
    }

    /**
     * Displays the carrier management view.
     * <p>
     * Loads all carriers from the database and displays them with pagination.
     * Enables the "Add Carrier" button and search functionality.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showCarriers() {
        closeMenu();
        currentView = "carriers";
        pageTitle.setText("🚚 Carrier Management");
        addButton.setText("➕ New Carrier");
        addButton.setVisible(true);
        searchContainer.setVisible(true);
        searchField.setPromptText("Search carrier...");
        searchField.clear();

        try {
            allCarriers = userDAO.getAllCarriers();
            createPagination(allCarriers, this::displayCarrierPage);
        } catch (SQLException e) {
            showError("Error loading carriers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void displayCarrierPage(VBox container, List<?> items) {
        List<User> carriers = (List<User>) items;
        for (User carrier : carriers) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.getStyleClass().add("product-card");

            Label name = new Label(carrier.getFullName());
            name.setPrefWidth(150);
            name.setStyle("-fx-font-weight: bold;");

            Label phone = new Label(carrier.getPhone());
            phone.setPrefWidth(120);

            double avgRating = ratingDAO.getAverageRating(carrier.getId());
            Label rating = new Label(String.format("⭐ %.1f", avgRating));
            rating.setPrefWidth(80);

            Label status = new Label(carrier.isActive() ? "Active" : "Inactive");
            status.setPrefWidth(80);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            MFXButton fireBtn = new MFXButton("❌ Fire");
            fireBtn.getStyleClass().add("danger-button");
            fireBtn.setOnAction(e -> {
                e.consume(); // Prevent row click
                fireCarrier(carrier);
            });

            row.getChildren().addAll(name, phone, rating, status, spacer, fireBtn);

            // Make row clickable
            row.setCursor(javafx.scene.Cursor.HAND);
            row.setOnMouseClicked(e -> showCarrierReviews(carrier));
            container.getChildren().add(row);
        }
    }

    private void showCarrierReviews(User carrier) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reviews for " + carrier.getFullName());
        dialog.initOwner(contentPane.getScene().getWindow());
        applyThemeToDialog(dialog);

        ButtonType closeType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeType);

        VBox reviewsBox = new VBox(15);
        reviewsBox.setPadding(new Insets(15));
        reviewsBox.setPrefWidth(400);

        List<CarrierRating> reviews = ratingDAO.findByCarrier(carrier.getId());

        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet.");
            noReviews.setStyle("-fx-text-fill: -text-secondary; -fx-font-style: italic;");
            reviewsBox.getChildren().add(noReviews);
        } else {
            for (CarrierRating r : reviews) {
                VBox card = new VBox(5);
                card.getStyleClass().add("product-card"); // Reuse card style for consistent look
                card.setPadding(new Insets(10));

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);

                Label stars = new Label(r.getStarRating());
                stars.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px;"); // Gold color

                Label date = new Label(r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                date.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 12px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                header.getChildren().addAll(stars, spacer, date);

                Label customer = new Label("By: " + (r.getCustomerName() != null ? r.getCustomerName() : "Unknown"));
                customer.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -text-primary;");

                Label comment = new Label(r.getComment());
                comment.setWrapText(true);
                comment.setStyle("-fx-text-fill: -text-primary;");

                card.getChildren().addAll(header, customer, comment);
                reviewsBox.getChildren().add(card);
            }
        }

        ScrollPane scroll = new ScrollPane(reviewsBox);
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(500);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    /**
     * Displays the orders view.
     * <p>
     * Loads all orders and displays them in a list.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showOrders() {
        closeMenu();
        currentView = "orders";
        pageTitle.setText("📋 All Orders");
        addButton.setVisible(false);
        searchContainer.setVisible(false);

        try {
            List<Order> orders = orderDAO.findAll();
            displayOrders(orders);
        } catch (SQLException e) {
            showError("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renders the list of orders in the content pane.
     * 
     * @param orders The list of {@link Order} objects to display.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void displayOrders(List<Order> orders) {
        contentPane.getChildren().clear();

        for (Order order : orders) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.getStyleClass().add("product-card");

            Label id = new Label("#" + order.getId());
            id.setPrefWidth(50);

            Label customer = new Label(order.getCustomerName());
            customer.setPrefWidth(120);

            Label total = new Label(String.format("₺%.2f", order.getTotalCost()));
            total.setPrefWidth(80);

            Label status = new Label(order.getStatus().getValue());
            status.getStyleClass().addAll("badge", "badge-" + order.getStatus().getValue());

            Label carrier = new Label(order.getCarrierName() != null ? order.getCarrierName() : "-");
            carrier.setPrefWidth(100);

            row.getChildren().addAll(id, customer, total, status, carrier);
            contentPane.getChildren().add(row);
        }
    }

    // ... displayCoupons, displayReports etc need check ..

    /**
     * Displays the messaging interface.
     * <p>
     * Loads the `owner_messages.fxml` view.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showMessages() {
        closeMenu();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/owner_messages.fxml"));
            Parent root = loader.load();
            contentPane.getScene().setRoot(root);
        } catch (IOException e) {
            showError("Error loading messages page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays the coupon management view.
     * <p>
     * Deletes expired coupons, loads all valid coupons, and displays them with
     * pagination.
     * Enables the "Add Coupon" button and search functionality.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showCoupons() {
        closeMenu();
        currentView = "coupons";
        pageTitle.setText("🎫 Coupon Management");
        addButton.setText("➕ New Coupon");
        addButton.setVisible(true);
        searchContainer.setVisible(true);
        searchField.setPromptText("Search coupon...");
        searchField.clear();

        try {
            couponDAO.deleteExpired();
            allCoupons = couponDAO.findAll();
            createPagination(allCoupons, this::displayCouponPage);
        } catch (SQLException e) {
            showError("Error loading coupons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void displayCouponPage(VBox container, List<?> items) {
        if ("coupons".equals(currentView) && container.getChildren().isEmpty()) {
        }

        List<Coupon> coupons = (List<Coupon>) items;
        for (Coupon coupon : coupons) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10));
            row.getStyleClass().add("product-card");

            Label code = new Label(coupon.getCode());
            code.setPrefWidth(100);
            code.setStyle("-fx-font-weight: bold;");

            String discountText = coupon.getDiscountType() == Coupon.DiscountType.PERCENT
                    ? "%" + (int) coupon.getDiscountPercent()
                    : "₺" + (int) coupon.getDiscountPercent();

            Label discount = new Label(discountText);
            discount.setPrefWidth(80);

            Label minValue = new Label("Min: ₺" + (int) coupon.getMinCartValue());
            minValue.setPrefWidth(100);

            Label uses = new Label(coupon.getCurrentUses() + "/" + coupon.getMaxUses());
            uses.setPrefWidth(70);

            Label valid = new Label(coupon.isActive() ? (coupon.isValid() ? "✅ Valid" : "⚠️ Used") : "❌ Inactive");
            valid.setPrefWidth(90);

            Label deadline = new Label(
                    coupon.getValidUntil() != null ? "Expires: " + coupon.getValidUntil().toLocalDate() : "Unlimited");
            deadline.setPrefWidth(100);
            deadline.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            MFXButton removeBtn = new MFXButton("🗑️ Delete");
            removeBtn.getStyleClass().add("danger-button");
            removeBtn.setOnAction(e -> removeCoupon(coupon));

            row.getChildren().addAll(code, discount, minValue, uses, valid, deadline, spacer, removeBtn);
            container.getChildren().add(row);
        }
    }

    /**
     * Deletes a coupon after user confirmation.
     *
     * @param coupon The {@link Coupon} to delete.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void removeCoupon(Coupon coupon) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                coupon.getCode() + " coupon be deleted?\nThis action cannot be undone!");
        confirm.initOwner(contentPane.getScene().getWindow());
        confirm.setTitle("Delete Coupon");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    couponDAO.delete(coupon.getId());
                    showCoupons();
                } catch (SQLException e) {
                    showError("Error deleting coupon: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Displays the sales reports view.
     * <p>
     * Calculates and displays key metrics (Total Revenue, Total Orders, Products
     * Sold, Avg Order Value).
     * Renders charts for daily revenue, order status distribution, and top-selling
     * products.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void showReports() {
        closeMenu();
        currentView = "reports";
        pageTitle.setText("📊 Sales Reports");
        addButton.setVisible(false);
        searchContainer.setVisible(false);

        contentPane.getChildren().clear();

        try {
            List<Order> allOrders = orderDAO.findAll();
            List<Order> completedOrders = allOrders.stream()
                    .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
                    .collect(Collectors.toList());

            // --- METRICS ---
            double totalRevenue = completedOrders.stream().mapToDouble(Order::getTotalCost).sum();
            int totalOrders = completedOrders.size();
            int totalItemsSold = completedOrders.stream()
                    .flatMap(o -> o.getItems().stream())
                    .mapToInt(item -> (int) item.getAmount()) // Assuming amount is roughly items/kg
                    .sum();
            double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

            HBox metricsBox = new HBox(20);
            metricsBox.setAlignment(Pos.CENTER);
            metricsBox.setPadding(new Insets(0, 0, 20, 0));

            metricsBox.getChildren().addAll(
                    createMetricCard("Total Revenue", String.format("₺%.2f", totalRevenue), "fas-lira-sign",
                            "-success-color"),
                    createMetricCard("Total Orders", String.valueOf(totalOrders), "fas-shopping-bag",
                            "-primary-color"),
                    createMetricCard("Products Sold", String.valueOf(totalItemsSold) + " units", "fas-carrot",
                            "-secondary-color"),
                    createMetricCard("Avg Order", String.format("₺%.2f", avgOrderValue), "fas-chart-line",
                            "-text-secondary"));

            // --- CHARTS ---

            // 1. Revenue over Time (Line Chart)
            CategoryAxis xAxis = new CategoryAxis();
            xAxis.setLabel("Date");
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Revenue (₺)");
            LineChart<String, Number> revenueChart = new LineChart<>(xAxis, yAxis);
            revenueChart.setTitle("Daily Revenue");

            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Revenue");

            Map<String, Double> dailyRevenue = new TreeMap<>(); // TreeMap for sorted dates
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM");

            for (Order o : completedOrders) {
                String date = o.getOrderTime().format(dateFmt);
                dailyRevenue.put(date, dailyRevenue.getOrDefault(date, 0.0) + o.getTotalCost());
            }

            dailyRevenue.forEach((date, revenue) -> revenueSeries.getData().add(new XYChart.Data<>(date, revenue)));
            revenueChart.getData().add(revenueSeries);
            revenueChart.setPrefHeight(300);

            // Let's do Order Status Distribution instead as it's easier with Order objects
            PieChart statusChart = new PieChart();
            statusChart.setTitle("Order Status Distribution");
            Map<String, Integer> statusCounts = new HashMap<>();
            for (Order o : allOrders) {
                statusCounts.put(o.getStatus().getValue(), statusCounts.getOrDefault(o.getStatus().getValue(), 0) + 1);
            }
            statusCounts.forEach((status, count) -> statusChart.getData().add(new PieChart.Data(status, count)));

            // 3. Top Selling Products (Bar Chart)
            CategoryAxis pXAxis = new CategoryAxis();
            pXAxis.setLabel("Product");
            NumberAxis pYAxis = new NumberAxis();
            pYAxis.setLabel("Quantity");
            BarChart<String, Number> productChart = new BarChart<>(pXAxis, pYAxis);
            productChart.setTitle("Top Selling Products");

            XYChart.Series<String, Number> productSeries = new XYChart.Series<>();
            productSeries.setName("Sales Quantity");

            Map<String, Double> productSales = new HashMap<>();
            for (Order o : completedOrders) {
                for (OrderItem item : o.getItems()) {
                    productSales.put(item.getProductName(),
                            productSales.getOrDefault(item.getProductName(), 0.0) + item.getAmount());
                }
            }

            productSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .forEach(
                            entry -> productSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

            productChart.getData().add(productSeries);
            productChart.setPrefHeight(300);

            // Layout
            VBox chartsBox = new VBox(20);
            chartsBox.getChildren().addAll(metricsBox, revenueChart, new HBox(20, statusChart, productChart));

            // Ensure charts resize well
            statusChart.setPrefWidth(400);
            productChart.setPrefWidth(400);
            ((HBox) chartsBox.getChildren().get(2)).setAlignment(Pos.CENTER);

            ScrollPane scrollPane = new ScrollPane(chartsBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent;");

            contentPane.getChildren().add(scrollPane);

        } catch (SQLException e) {
            showError("Error loading report data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a card displaying a key metric.
     * 
     * @param title       The title of the metric.
     * @param value       The value to display.
     * @param iconLiteral The icon literal for the FontIcon.
     * @param colorVar    The CSS variable or color string for the icon.
     * @return A {@link VBox} containing the metric card.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private VBox createMetricCard(String title, String value, String iconLiteral, String colorVar) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("metric-card");
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(24);
        String color = "#43a047"; // default green
        if (colorVar.contains("secondary"))
            color = "#ffca28";
        if (colorVar.contains("text-secondary"))
            color = "#546e7a";
        if (colorVar.contains("primary"))
            color = "#43a047";

        icon.setIconColor(javafx.scene.paint.Color.web(color));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: #757575; -fx-font-size: 14px;");

        Label valueLbl = new Label(value);
        valueLbl.setStyle("-fx-text-fill: #263238; -fx-font-size: 20px; -fx-font-weight: bold;");

        card.getChildren().addAll(icon, valueLbl, titleLbl);
        return card;
    }

    /**
     * Handles the "Add" button click event.
     * <p>
     * Opens the appropriate dialog (Add Product, Add Carrier, or Add Coupon)
     * based on the current view.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void handleAdd() {
        switch (currentView) {
            case "products" -> showAddProductDialog();
            case "carriers" -> showAddCarrierDialog();
            case "coupons" -> showAddCouponDialog();
        }
    }

    /**
     * Shows a dialog to add a new product.
     * <p>
     * Handles input validation and product creation.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void showAddProductDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product details:");
        dialog.initOwner(contentPane.getScene().getWindow());
        applyThemeToDialog(dialog);

        VBox content = new VBox(25);
        content.setPadding(new Insets(20));

        // Product Name
        Label nameLabel = new Label("Product Name");
        MFXTextField nameField = new MFXTextField();
        nameField.setPrefWidth(250);

        // Type
        Label typeLabel = new Label("Type");
        MFXComboBox<String> typeCombo = new MFXComboBox<>();
        typeCombo.getItems().addAll("Vegetable", "Fruit");
        typeCombo.setPrefWidth(250);

        // Unit
        Label unitLabel = new Label("Unit");
        MFXComboBox<String> unitCombo = new MFXComboBox<>();
        unitCombo.getItems().addAll("kg", "piece");
        unitCombo.setPrefWidth(250);
        unitCombo.selectItem("kg"); // Default

        // Price
        Label priceLabel = new Label("Unit Price (₺)");
        MFXTextField priceField = new MFXTextField();
        priceField.setPrefWidth(250);

        // Stock
        Label stockLabel = new Label("Stock (kg/piece)");
        MFXTextField stockField = new MFXTextField();
        stockField.setPrefWidth(250);

        // Threshold
        Label thresholdLabel = new Label("Threshold Value (kg/piece)");
        MFXTextField thresholdField = new MFXTextField();
        thresholdField.setPrefWidth(250);

        // Image selection
        MFXButton selectImageBtn = new MFXButton("📷 Select Image");
        Label imageLabel = new Label("No image selected");
        final byte[][] selectedImageBytes = { null };
        final String[] selectedImageName = { null };

        selectImageBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            java.io.File selectedFile = fileChooser.showOpenDialog(contentPane.getScene().getWindow());

            if (selectedFile != null) {
                try {
                    selectedImageBytes[0] = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    selectedImageName[0] = selectedFile.getName();
                    imageLabel.setText(selectedFile.getName());
                } catch (IOException ex) {
                    showError("Error loading image: " + ex.getMessage());
                }
            }
        });

        // Layout Containers
        VBox nameBox = new VBox(5, nameLabel, nameField);

        VBox typeBox = new VBox(5, typeLabel, typeCombo);
        VBox unitBox = new VBox(5, unitLabel, unitCombo);
        HBox row2 = new HBox(20, typeBox, unitBox);
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        HBox.setHgrow(unitBox, Priority.ALWAYS);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        unitCombo.setMaxWidth(Double.MAX_VALUE);

        VBox priceBox = new VBox(5, priceLabel, priceField);
        VBox stockBox = new VBox(5, stockLabel, stockField);
        HBox row3 = new HBox(20, priceBox, stockBox);
        HBox.setHgrow(priceBox, Priority.ALWAYS);
        HBox.setHgrow(stockBox, Priority.ALWAYS);
        priceField.setMaxWidth(Double.MAX_VALUE);
        stockField.setMaxWidth(Double.MAX_VALUE);

        VBox thresholdBox = new VBox(5, thresholdLabel, thresholdField);
        HBox imageBox = new HBox(10, selectImageBtn, imageLabel);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        VBox imageContainer = new VBox(5, new Label(" "), imageBox); // Spacer label for alignment

        HBox row4 = new HBox(20, thresholdBox, imageContainer);
        HBox.setHgrow(thresholdBox, Priority.ALWAYS);
        HBox.setHgrow(imageContainer, Priority.ALWAYS);
        thresholdField.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(nameBox, row2, row3, row4);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation Filter
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String typeStr = typeCombo.getValue();
            String unitStr = unitCombo.getValue();
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String thresholdStr = thresholdField.getText().trim();

            if (name.isEmpty()) {
                showError("Product name is required!");
                event.consume();
                return;
            }
            if (typeStr == null) {
                showError("Please select a product type!");
                event.consume();
                return;
            }
            if (unitStr == null) {
                showError("Please select a unit type!");
                event.consume();
                return;
            }
            if (priceStr.isEmpty()) {
                showError("Price is required!");
                event.consume();
                return;
            }
            if (stockStr.isEmpty()) {
                showError("Stock is required!");
                event.consume();
                return;
            }
            if (thresholdStr.isEmpty()) {
                showError("Threshold value is required!");
                event.consume();
                return;
            }

            try {
                Double.parseDouble(priceStr);
                Double.parseDouble(stockStr);
                Double.parseDouble(thresholdStr);
            } catch (NumberFormatException e) {
                showError("Please enter valid numeric values for price, stock and threshold!");
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    String typeStr = typeCombo.getValue();
                    String unitStr = unitCombo.getValue();
                    double price = Double.parseDouble(priceField.getText());
                    double stock = Double.parseDouble(stockField.getText());
                    double threshold = Double.parseDouble(thresholdField.getText());

                    Product p = new Product();
                    p.setName(name);
                    p.setType(typeStr.equals("Vegetable") ? Product.ProductType.VEGETABLE : Product.ProductType.FRUIT);
                    p.setUnitType(unitStr.equals("kg") ? Product.UnitType.KG : Product.UnitType.PIECE);
                    p.setPrice(price);
                    p.setStock(stock);
                    p.setThreshold(threshold);
                    p.setImage(selectedImageBytes[0]);
                    p.setImageName(selectedImageName[0]);
                    p.setActive(true);
                    return p;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            if (product.getPrice() <= 0) {
                showError("Price must be greater than 0!");
                return;
            }
            if (product.getStock() < 0) {
                showError("Stock cannot be negative!");
                return;
            }
            if (product.getThreshold() <= 0) {
                showError("Threshold value must be greater than 0!");
                return;
            }

            try {
                productDAO.create(product);
                showProducts();
            } catch (SQLException e) {
                showError("Error adding product: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Shows a dialog to add a new carrier.
     * <p>
     * Handles input validation and carrier creation.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void showAddCarrierDialog() {
        Dialog<Carrier> dialog = new Dialog<>();
        dialog.setTitle("Add New Carrier");
        dialog.setHeaderText("Enter carrier information:");
        dialog.initOwner(contentPane.getScene().getWindow());
        applyThemeToDialog(dialog);

        // Form fields
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Name
        Label nameLabel = new Label("Full Name:");
        nameLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXTextField fullNameField = new MFXTextField();
        fullNameField.setPrefWidth(250);
        fullNameField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        // Username
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXTextField usernameField = new MFXTextField();
        usernameField.setPrefWidth(250);
        usernameField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        // Password
        Label passLabel = new Label("Password:");
        passLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXPasswordField passwordField = new MFXPasswordField();
        passwordField.setPrefWidth(250);
        passwordField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        grid.add(nameLabel, 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == javafx.scene.control.ButtonType.OK) {
                String fullName = fullNameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    showError("All fields are required!");
                    return null;
                }

                Carrier carrier = new Carrier();
                carrier.setFullName(fullName);
                carrier.setUsername(username);
                carrier.setPassword(password); // Will be stored unhashed
                carrier.setRole(User.UserRole.CARRIER);
                carrier.setActive(true);
                return carrier;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(carrier -> {
            try {
                userDAO.createCarrierWithTempPassword(carrier);
                showCarriers();
            } catch (SQLException e) {
                showError("Error adding carrier: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Shows a dialog to add a new coupon.
     * <p>
     * Handles input validation and coupon creation.
     * </p>
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void showAddCouponDialog() {
        Dialog<Coupon> dialog = new Dialog<>();
        dialog.setTitle("Add New Coupon");
        dialog.setHeaderText("Enter coupon information:");
        dialog.initOwner(contentPane.getScene().getWindow());
        applyThemeToDialog(dialog);

        // Form fields
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Coupon Code
        Label codeLabel = new Label("Coupon Code");
        codeLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXTextField codeField = new MFXTextField();
        codeField.setPrefWidth(300);
        codeField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        // Discount Type Selection
        Label typeLabel = new Label("Discount Type:");
        typeLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton percentRb = new RadioButton("Percentage (%)");
        percentRb.setToggleGroup(typeGroup);
        percentRb.setSelected(true);
        RadioButton fixedRb = new RadioButton("Fixed Amount (₺)");
        fixedRb.setToggleGroup(typeGroup);

        if (themeManager.isDarkTheme()) {
            percentRb.setStyle("-fx-text-fill: white;");
            fixedRb.setStyle("-fx-text-fill: white;");
        } else {
            percentRb.setStyle("-fx-text-fill: black;");
            fixedRb.setStyle("-fx-text-fill: black;");
        }

        HBox typeBox = new HBox(15, percentRb, fixedRb);

        // Discount Value
        Label discountLabel = new Label("Discount Value");
        discountLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXTextField discountField = new MFXTextField();
        discountField.setPrefWidth(300);
        discountField.setText("10");
        discountField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        // Min Cart Value
        Label minCartLabel = new Label("Minimum Cart Amount (₺)");
        minCartLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXTextField minCartField = new MFXTextField();
        minCartField.setPrefWidth(300);
        minCartField.setText("50");
        minCartField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        // Max Uses
        Label maxUsesLabel = new Label("Maximum Usage Count");
        maxUsesLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        MFXTextField maxUsesField = new MFXTextField();
        maxUsesField.setPrefWidth(300);
        maxUsesField.setText("100");
        maxUsesField.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");

        // Deadline
        Label dateLabel = new Label("Expiration Date (Optional):");
        dateLabel.setStyle(themeManager.isDarkTheme() ? "-fx-text-fill: white;" : "-fx-text-fill: black;");
        DatePicker datePicker = new DatePicker();
        datePicker.setPrefWidth(300);

        content.getChildren().addAll(
                codeLabel, codeField,
                typeLabel, typeBox,
                discountLabel, discountField,
                minCartLabel, minCartField,
                maxUsesLabel, maxUsesField,
                dateLabel, datePicker);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String code = codeField.getText().trim().toUpperCase();
                    double discount = Double.parseDouble(discountField.getText().trim());
                    double minCart = Double.parseDouble(minCartField.getText().trim());
                    int maxUses = Integer.parseInt(maxUsesField.getText().trim());
                    boolean isPercent = percentRb.isSelected();

                    if (code.isEmpty()) {
                        showError("Coupon code cannot be empty!");
                        return null;
                    }

                    // Validation Limits
                    if (isPercent) {
                        if (discount <= 0 || discount > 50) {
                            showError("Percentage discount must be between 0-50!");
                            return null;
                        }
                    } else {
                        if (discount <= 0 || discount > 250) {
                            showError("Fixed discount must be between 0-250 TL!");
                            return null;
                        }
                    }

                    if (minCart < 0) {
                        showError("Minimum cart amount cannot be negative!");
                        return null;
                    }
                    if (maxUses <= 0) {
                        showError("Maximum usage count must be greater than 0!");
                        return null;
                    }

                    // Deadline check
                    LocalDateTime validUntil = null;
                    if (datePicker.getValue() != null) {
                        if (datePicker.getValue().isBefore(LocalDate.now())) {
                            showError("You cannot select a past date!");
                            return null;
                        }
                        validUntil = datePicker.getValue().atTime(LocalTime.MAX);
                    }

                    Coupon coupon = new Coupon();
                    coupon.setCode(code);
                    coupon.setDiscountPercent(discount);
                    coupon.setMinCartValue(minCart);
                    coupon.setMaxUses(maxUses);
                    coupon.setActive(true);
                    coupon.setDiscountType(isPercent ? Coupon.DiscountType.PERCENT : Coupon.DiscountType.FIXED);
                    coupon.setValidUntil(validUntil);

                    return coupon;
                } catch (NumberFormatException e) {
                    showError("Please enter valid numeric values!");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(coupon -> {
            try {
                couponDAO.create(coupon);
                showCoupons();
            } catch (SQLException e) {
                showError("Error adding coupon: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Shows a dialog to edit an existing product.
     * 
     * @param product The {@link Product} to edit.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void editProduct(Product product) {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Update " + product.getName() + " details:");
        dialog.initOwner(contentPane.getScene().getWindow());
        applyThemeToDialog(dialog);

        VBox content = new VBox(25);
        content.setPadding(new Insets(20));

        // Product Name
        Label nameLabel = new Label("Product Name");
        MFXTextField nameField = new MFXTextField(product.getName());
        nameField.setPrefWidth(400); // Full width for name
        VBox nameBox = new VBox(5, nameLabel, nameField);

        // Type and Unit
        Label typeLabel = new Label("Type");
        MFXComboBox<String> typeCombo = new MFXComboBox<>();
        typeCombo.getItems().addAll("Vegetable", "Fruit");
        typeCombo.setPrefWidth(250);
        typeCombo.selectItem(product.getType() == Product.ProductType.VEGETABLE ? "Vegetable" : "Fruit");
        VBox typeBox = new VBox(5, typeLabel, typeCombo);

        Label unitLabel = new Label("Unit");
        MFXComboBox<String> unitCombo = new MFXComboBox<>();
        unitCombo.getItems().addAll("kg", "piece");
        unitCombo.setPrefWidth(250);
        unitCombo.selectItem(product.getUnitType() == Product.UnitType.KG ? "kg" : "piece");
        VBox unitBox = new VBox(5, unitLabel, unitCombo);

        HBox row2 = new HBox(20, typeBox, unitBox);
        HBox.setHgrow(typeBox, Priority.ALWAYS);
        HBox.setHgrow(unitBox, Priority.ALWAYS);
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        unitCombo.setMaxWidth(Double.MAX_VALUE);

        // Price and Stock
        Label priceLabel = new Label("Unit Price (₺)");
        MFXTextField priceField = new MFXTextField(String.valueOf(product.getPrice()));
        priceField.setPrefWidth(250);
        VBox priceBox = new VBox(5, priceLabel, priceField);

        Label stockLabel = new Label("Stock (kg/piece)");
        MFXTextField stockField = new MFXTextField(String.valueOf(product.getStock()));
        stockField.setPrefWidth(250);
        VBox stockBox = new VBox(5, stockLabel, stockField);

        HBox row3 = new HBox(20, priceBox, stockBox);
        HBox.setHgrow(priceBox, Priority.ALWAYS);
        HBox.setHgrow(stockBox, Priority.ALWAYS);
        priceField.setMaxWidth(Double.MAX_VALUE);
        stockField.setMaxWidth(Double.MAX_VALUE);

        // Threshold and Image
        Label thresholdLabel = new Label("Threshold Value (kg/piece)");
        MFXTextField thresholdField = new MFXTextField(String.valueOf(product.getThreshold()));
        thresholdField.setPrefWidth(250);
        VBox thresholdBox = new VBox(5, thresholdLabel, thresholdField);

        MFXButton selectImageBtn = new MFXButton("📷 Change Image");
        Label imageLabel = new Label(product.getImageName() != null ? product.getImageName() : "No image");
        final byte[][] selectedImageBytes = { product.getImage() };
        final String[] selectedImageName = { product.getImageName() };

        selectImageBtn.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            java.io.File selectedFile = fileChooser.showOpenDialog(contentPane.getScene().getWindow());

            if (selectedFile != null) {
                try {
                    selectedImageBytes[0] = java.nio.file.Files.readAllBytes(selectedFile.toPath());
                    selectedImageName[0] = selectedFile.getName();
                    imageLabel.setText(selectedFile.getName());
                } catch (IOException ex) {
                    showError("Error loading image: " + ex.getMessage());
                }
            }
        });

        HBox imageBox = new HBox(10, selectImageBtn, imageLabel);
        imageBox.setAlignment(Pos.CENTER_LEFT);
        VBox imageContainer = new VBox(5, new Label(" "), imageBox); // Spacer for alignment

        HBox row4 = new HBox(20, thresholdBox, imageContainer);
        HBox.setHgrow(thresholdBox, Priority.ALWAYS);
        HBox.setHgrow(imageContainer, Priority.ALWAYS);
        thresholdField.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(nameBox, row2, row3, row4);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    String name = nameField.getText();
                    String typeStr = typeCombo.getValue();
                    String unitStr = unitCombo.getValue();
                    double price = Double.parseDouble(priceField.getText());
                    double stock = Double.parseDouble(stockField.getText());
                    double threshold = Double.parseDouble(thresholdField.getText());

                    if (name.isEmpty() || typeStr == null || unitStr == null)
                        return null;

                    product.setName(name);
                    product.setType(
                            typeStr.equals("Vegetable") ? Product.ProductType.VEGETABLE : Product.ProductType.FRUIT);
                    product.setUnitType(unitStr.equals("kg") ? Product.UnitType.KG : Product.UnitType.PIECE);
                    product.setPrice(price);
                    product.setStock(stock);
                    product.setThreshold(threshold);
                    product.setImage(selectedImageBytes[0]);
                    product.setImageName(selectedImageName[0]);
                    return product;
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedProduct -> {
            if (updatedProduct.getPrice() <= 0) {
                showError("Price must be greater than 0!");
                return;
            }
            if (updatedProduct.getStock() < 0) {
                showError("Stock cannot be negative!");
                return;
            }
            if (updatedProduct.getThreshold() <= 0) {
                showError("Threshold value must be greater than 0!");
                return;
            }

            try {
                if (productDAO.update(updatedProduct)) {
                    showProducts();
                } else {
                    showError("Error updating product!");
                }
            } catch (SQLException e) {
                showError("Error updating product: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Deletes (deactivates) a product after confirmation.
     * 
     * @param product The {@link Product} to delete.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void deleteProduct(Product product) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this product?");
        confirm.initOwner(contentPane.getScene().getWindow());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    productDAO.deactivate(product.getId());
                    showProducts();
                } catch (SQLException e) {
                    showError("Error deleting product: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Fires (deactivates) a carrier after confirmation and checking for active
     * orders.
     * 
     * @param carrier The {@link User} (carrier) to fire.
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void fireCarrier(User carrier) {
        try {
            List<Order> carrierOrders = orderDAO.findByCarrier(carrier.getId());
            boolean hasActiveOrders = carrierOrders.stream()
                    .anyMatch(o -> o.getStatus() != Order.OrderStatus.DELIVERED &&
                            o.getStatus() != Order.OrderStatus.CANCELLED);

            if (hasActiveOrders) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.initOwner(contentPane.getScene().getWindow());
                alert.setTitle("Cannot Fire Carrier");
                alert.setHeaderText("Active Orders Exist");
                alert.setContentText(
                        "This carrier has active orders and cannot be removed until they are completed or reassigned.");
                alert.showAndWait();
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Should " + carrier.getFullName() + " be fired?");
            confirm.initOwner(contentPane.getScene().getWindow());
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        userDAO.deactivate(carrier.getId());
                        showCarriers();
                    } catch (SQLException e) {
                        showError("Error during termination process: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            showError("Error checking carrier orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs out the current user and redirects to the login screen.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        navigateToLogin();
    }

    /**
     * Navigates to the login screen.
     * 
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = contentPane.getScene();
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
     * @author Burak Özevin, Elif Zeynep Talay
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(contentPane.getScene().getWindow());
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
