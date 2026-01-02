package com.greengrocer.controllers;

import com.greengrocer.dao.*;
import com.greengrocer.models.*;
import com.greengrocer.models.Order.OrderStatus;
import com.greengrocer.utils.ChatbotService;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ShoppingCart;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the customer interface.
 * <p>
 * Manages the main customer dashboard, including product browsing, searching,
 * shopping cart management, order history, and profile settings.
 * Handles the display of products in a grid layout with pagination and
 * carousel.
 * </p>
 * 
 * @author Burak Özevin
 */
public class CustomerController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private MFXButton cartBadge;
    @FXML
    private MFXButton menuButton;
    @FXML
    private Region backdrop;
    @FXML
    private VBox sidebar;
    @FXML
    private MFXButton closeMenuButton;
    @FXML
    private VBox mainContentPane;
    @FXML
    private VBox carouselSection;
    @FXML
    private FlowPane carouselContainer;
    @FXML
    private TitledPane vegetablesPane;
    @FXML
    private TitledPane fruitsPane;
    @FXML
    private FlowPane vegetablesFlowPane;
    @FXML
    private FlowPane fruitsFlowPane;

    @FXML
    private MFXButton themeToggleBtn;

    @FXML
    private BorderPane mainContainer;

    @FXML
    private MFXButton searchButton;

    @FXML
    private MFXButton chatbotButton;

    @FXML
    private Label loyaltyPointsLabel;

    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final CarrierRatingDAO ratingDAO = new CarrierRatingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CouponDAO couponDAO = new CouponDAO();
    private final ShoppingCart cart = ShoppingCart.getInstance();
    private final com.greengrocer.utils.ThemeManager themeManager = com.greengrocer.utils.ThemeManager.getInstance();
    private final ChatbotService chatbotService = new ChatbotService();

    private ContextMenu searchSuggestions;
    private List<Product> popularProducts;
    private javafx.stage.Stage chatbotStage;

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the UI components including the cart badge, loyalty points,
     * search autocomplete, popular products carousel, hamburger menu, and theme.
     * Loads the initial list of products.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     * 
     * @author Burak Özevin
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // User user = SessionManager.getInstance().getCurrentUser(); // Removed welcome
        // label usage
        updateCartBadge();
        updateLoyaltyPoints();
        setupSearchAutocomplete();
        setupPopularCarousel();
        setupHamburgerMenu();
        loadProducts();

        // Initialize theme
        updateThemeIcon();
        // Apply theme when scene is ready
        javafx.application.Platform.runLater(() -> {
            if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
                themeManager.applyTheme(themeToggleBtn.getScene());
            }
        });
    }

    /**
     * Updates the loyalty points label for the current customer.
     * <p>
     * Fetches the current user from the session and displays their points if they
     * are a customer.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void updateLoyaltyPoints() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user instanceof Customer && loyaltyPointsLabel != null) {
            Customer customer = (Customer) user;
            int points = customer.getLoyaltyPoints();
            loyaltyPointsLabel.setText(points + " pts");
        }
    }

    /**
     * Toggles the application theme between light and dark modes.
     * 
     * @author Burak Özevin
     */
    @FXML
    private void handleThemeToggle() {
        if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
            themeManager.toggleTheme(themeToggleBtn.getScene());
            updateThemeIcon();
        }
    }

    private void updateThemeIcon() {
        if (themeToggleBtn != null) {
            String iconLiteral = themeManager.isDarkTheme() ? "fas-sun" : "fas-moon";
            String color = themeManager.isDarkTheme() ? "#ffeb3b" : "white"; // Yellow sun, white moon

            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon(iconLiteral);
            icon.setIconSize(20);
            icon.setIconColor(javafx.scene.paint.Color.web(color));

            themeToggleBtn.setGraphic(icon);
            themeToggleBtn.setText("");
        }
    }

    /**
     * Configures the hamburger menu sidebar.
     * <p>
     * Sets initial visibility and adds event handlers for opening/closing the menu.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void setupHamburgerMenu() {
        // Initial state
        sidebar.setTranslateX(320); // Hidden to the right
        backdrop.setVisible(false);
        backdrop.setOpacity(0);

        // Event Handlers
        menuButton.setOnAction(e -> toggleMenu(true));
        closeMenuButton.setOnAction(e -> toggleMenu(false));
        backdrop.setOnMouseClicked(e -> toggleMenu(false));
    }

    /**
     * Toggles the visibility of the sidebar menu with animation.
     *
     * @param show {@code true} to show the menu, {@code false} to hide it.
     * 
     * @author Burak Özevin
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
            slide.setToX(320); // Slide to the right to hide
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> backdrop.setVisible(false));
        }

        slide.play();
        fade.play();
    }

    /**
     * Sets up the search field with autocomplete/filtering functionality.
     * <p>
     * Listens for text changes in the search field and triggers a product search.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void setupSearchAutocomplete() {
        searchSuggestions = new ContextMenu();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                searchSuggestions.hide();
                // Show all products when search is cleared
                loadProducts();
                return;
            }

            // Filter products as user types
            filterProductsBySearch(newValue.trim());
        });
    }

    /**
     * Filters displayed products based on a search keyword.
     * <p>
     * Runs in a background thread to avoid freezing the UI. Updates the vegetable
     * and fruit panes with the search results.
     * </p>
     *
     * @param keyword The search term.
     * 
     * @author Burak Özevin
     */
    private void filterProductsBySearch(String keyword) {
        // Run DB operation in background to avoid freezing UI
        new Thread(() -> {
            try {
                List<Product> results = productDAO.search(keyword);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    List<Product> vegs = results.stream()
                            .filter(p -> p.getType() == Product.ProductType.VEGETABLE && p.getStock() > 0)
                            .collect(java.util.stream.Collectors.toList());
                    List<Product> fruits = results.stream()
                            .filter(p -> p.getType() == Product.ProductType.FRUIT && p.getStock() > 0)
                            .collect(java.util.stream.Collectors.toList());

                    setupPagination(vegetablesPane, vegs);
                    setupPagination(fruitsPane, fruits);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Sets up pagination for a list of products within a TitledPane.
     *
     * @param titledPane The pane to display the products in.
     * @param products   The list of products to paginate.
     * 
     * @author Burak Özevin
     */
    private void setupPagination(TitledPane titledPane, List<Product> products) {
        if (products.isEmpty()) {
            VBox emptyBox = new VBox(new Label("No products found."));
            emptyBox.setPadding(new Insets(15));
            titledPane.setContent(emptyBox);
            return;
        }

        int itemsPerPage = 6;
        int pageCount = (int) Math.ceil((double) products.size() / itemsPerPage);

        // Main container
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.TOP_CENTER);

        // Content Area (FlowPane)
        FlowPane pageGrid = new FlowPane();
        pageGrid.setHgap(20);
        pageGrid.setVgap(20);
        pageGrid.setAlignment(Pos.CENTER); // Centered as requested
        pageGrid.setPrefWrapLength(1000); // Allow roughly 4-5 items width before wrapping, but FlowPane is flexible

        // Pagination Control (Navigation Only)
        Pagination pagination = new Pagination(pageCount, 0);
        pagination.setMaxHeight(45); // Limit height to just show indicators
        pagination.getStyleClass().add("compact-pagination");

        // Hide the default content area of the pagination control since we manage it
        // externally
        pagination.setPageFactory(pageIndex -> new Region());

        // Listener to update content
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updatePageContent(pageGrid, products, newVal.intValue(), itemsPerPage);
        });

        // Initial load
        updatePageContent(pageGrid, products, 0, itemsPerPage);

        container.getChildren().addAll(pageGrid, pagination);
        titledPane.setContent(container);
    }

    /**
     * Updates the content of a pagination page.
     *
     * @param grid         The FlowPane to populate with product cards.
     * @param products     The full list of products.
     * @param pageIndex    The current page index (0-based).
     * @param itemsPerPage The number of items per page.
     * 
     * @author Burak Özevin
     */
    private void updatePageContent(FlowPane grid, List<Product> products, int pageIndex, int itemsPerPage) {
        grid.getChildren().clear();
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, products.size());

        if (fromIndex < products.size()) {
            List<Product> pageItems = products.subList(fromIndex, toIndex);
            for (Product p : pageItems) {
                grid.getChildren().add(createProductCard(p));
            }
        }
    }

    /**
     * Initializes the carousel with popular products.
     * <p>
     * Fetches the top selling products from the database and populates the
     * carousel.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void setupPopularCarousel() {
        try {
            popularProducts = productDAO.getPopularProducts(12);
            updateCarousel();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the carousel UI with the loaded popular products.
     * 
     * @author Burak Özevin
     */
    private void updateCarousel() {
        if (popularProducts == null || popularProducts.isEmpty()) {
            carouselSection.setVisible(false);
            return;
        }
        carouselSection.setVisible(true);
        carouselContainer.getChildren().clear();

        // Show up to 6 popular products in a FlowPane (adaptive layout)
        int maxItems = Math.min(6, popularProducts.size());
        for (int i = 0; i < maxItems; i++) {
            Product p = popularProducts.get(i);
            carouselContainer.getChildren().add(createCarouselItem(p));
        }
    }

    /**
     * Creates a single item card for the carousel.
     *
     * @param product The product to display.
     * @return A VBox containing the product image, name, price, and add-to-cart
     *         button.
     * 
     * @author Burak Özevin
     */
    private VBox createCarouselItem(Product product) {
        VBox card = new VBox(10);
        card.setMinWidth(160);
        card.setPrefWidth(180);
        card.setMaxWidth(200);
        card.setMinHeight(200);
        card.setPrefHeight(220);
        card.setMaxHeight(240);
        card.setPadding(new Insets(12));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("carousel-card");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);

        if (product.getImage() != null && product.getImage().length > 0) {
            try {
                imageView.setImage(new Image(new ByteArrayInputStream(product.getImage())));
            } catch (Exception e) {
                /* ignore */ }
        } else {
            // Fallback
        }

        Label name = new Label(product.getName());
        name.getStyleClass().add("carousel-product-name");

        Label price = new Label(String.format("₺%.2f", product.getPrice()));
        price.getStyleClass().add("carousel-product-price");

        MFXButton addBtn = new MFXButton("Add to Cart");
        addBtn.getStyleClass().add("carousel-button");
        addBtn.setPrefWidth(110);
        addBtn.setOnAction(e -> {
            e.consume();
            addToCart(product);
        });

        card.getChildren().addAll(imageView, name, price, addBtn);

        // Make card clickable
        card.setOnMouseClicked(e -> openProductDetail(product));
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        return card;
    }

    /**
     * Displays the main products view (carousel + product lists).
     * <p>
     * Clears the main content pane and re-adds the carousel and product sections.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void showProducts() {
        // pageTitle.setText("🛒 Products"); // Page title removed in new design
        mainContentPane.getChildren().clear();
        mainContentPane.getChildren().add(carouselSection); // Add carousel back

        VBox productsSection = new VBox(15);
        Label allProductsLabel = new Label("All Products");
        allProductsLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        productsSection.getChildren().add(allProductsLabel);
        productsSection.getChildren().addAll(vegetablesPane, fruitsPane);

        mainContentPane.getChildren().add(productsSection);
        loadProducts();
    }

    /**
     * Loads products from the database and updates the UI.
     * <p>
     * Fetches vegetables and fruits in a background thread and updates the
     * pagination.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void loadProducts() {
        new Thread(() -> {
            try {
                // Fetch data in background
                List<Product> vegetables = productDAO.getVegetables();
                List<Product> fruits = productDAO.getFruits();

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    setupPagination(vegetablesPane, vegetables);
                    setupPagination(fruitsPane, fruits);
                });
            } catch (SQLException e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Error loading products: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /**
     * Handles the search button click action.
     * <p>
     * Filters the currently displayed products based on the search field text.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
            return;
        }

        try {
            List<Product> results = productDAO.search(keyword);
            vegetablesFlowPane.getChildren().clear();
            fruitsFlowPane.getChildren().clear();

            for (Product p : results) {
                if (p.getStock() <= 0)
                    continue;

                if (p.getType() == Product.ProductType.VEGETABLE) {
                    vegetablesFlowPane.getChildren().add(createProductCard(p));
                } else {
                    fruitsFlowPane.getChildren().add(createProductCard(p));
                }
            }
        } catch (SQLException e) {
            showError("Error during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a UI card component representing a single product.
     * <p>
     * Displays the product image (or emoji fallback), name, price, stock, and an
     * "Add to Cart" button.
     * Also shows a warning if the stock is low.
     * </p>
     *
     * @param product The {@link Product} to display.
     * @return A {@link VBox} containing the product details.
     * 
     * @author Burak Özevin
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setMinWidth(200);
        card.setMaxWidth(200);
        card.setPrefHeight(320); // Fixed height
        card.setMinHeight(320);
        card.setMaxHeight(320);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_CENTER); // Align top to keep image position consistent
        card.getStyleClass().addAll("product-card", "animated-card");

        // Fixed-size container for image/emoji
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(120, 120);
        imageContainer.setMinSize(120, 120);
        imageContainer.setMaxSize(120, 120);

        if (product.getImage() != null && product.getImage().length > 0) {
            try {
                ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(product.getImage())));
                imageView.setFitWidth(120);
                imageView.setFitHeight(120);
                imageView.setPreserveRatio(true);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                // Fallback to emoji
                Label emoji = new Label(product.getType() == Product.ProductType.VEGETABLE ? "🥬" : "🍎");
                emoji.setStyle("-fx-font-size: 60px;");
                imageContainer.getChildren().add(emoji);
            }
        } else {
            // Fallback emoji
            Label emoji = new Label(product.getType() == Product.ProductType.VEGETABLE ? "🥬" : "🍎");
            emoji.setStyle("-fx-font-size: 60px;");
            imageContainer.getChildren().add(emoji);
        }

        Label name = new Label(product.getName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setAlignment(Pos.CENTER);
        name.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        name.setMaxHeight(40); // Limit name height (approx 2 lines)
        name.setMinHeight(40);

        double displayPrice = product.getDisplayPrice();
        String unit = product.getUnitType() == Product.UnitType.KG ? "kg" : "piece";
        Label price = new Label(String.format("₺%.2f / %s", displayPrice, unit));
        price.getStyleClass().add(product.isBelowThreshold() ? "product-price-high" : "product-price");

        Label stock = new Label(String.format("Stock: %.1f %s", product.getStock(), unit));
        stock.getStyleClass().add("product-stock");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS); // Push button to bottom

        MFXButton addBtn = new MFXButton("🛒 Add");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> {
            e.consume(); // Prevent card click event
            addToCart(product);
        });

        card.getChildren().addAll(imageContainer, name, price, stock, spacer, addBtn);

        // Make card clickable to view details
        card.setOnMouseClicked(e -> openProductDetail(product));
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        if (product.isBelowThreshold()) {
            Label warning = new Label("⚠️ Low Stock - Price 2x!");
            warning.setStyle("-fx-text-fill: #f44336; -fx-font-size: 10px;");
            // Add warning below image container
            card.getChildren().add(1, warning);
        }

        return card;
    }

    /**
     * Opens a dialog to add the specified product to the shopping cart.
     * <p>
     * Prompts the user for quantity, validates the input against available stock,
     * and adds the item to the cart if valid.
     * </p>
     *
     * @param product The {@link Product} to add.
     * 
     * @author Burak Özevin
     */
    private void addToCart(Product product) {
        String unit = product.getUnitType() == Product.UnitType.KG ? "kg" : "piece";
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.initOwner(mainContentPane.getScene().getWindow());
        dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText(product.getName());
        dialog.setContentText("Amount (" + unit + "):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showError("Amount must be greater than 0!");
                    return;
                }
                if (amount > product.getStock()) {
                    showError("Insufficient stock! Available stock: " + product.getStock() + " " + unit);
                    return;
                }
                cart.addItem(product, amount);
                updateCartBadge();
                showSuccess(String.format("%.2f %s %s added to cart!", amount, unit, product.getName()));
            } catch (NumberFormatException e) {
                showError("Invalid amount! Please enter a number.");
            }
        });
    }

    /**
     * Updates the shopping cart badge with the current number of items.
     * 
     * @author Burak Özevin
     */
    private void updateCartBadge() {
        int count = cart.getItemCount();
        if (count > 0) {
            cartBadge.setText(String.valueOf(count));
            cartBadge.setStyle(
                    "-fx-text-fill: #ffeb3b; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-color: transparent; -fx-cursor: hand;");
        } else {
            cartBadge.setText("");
            cartBadge.setStyle(
                    "-fx-text-fill: white; -fx-font-size: 16px; -fx-background-color: transparent; -fx-cursor: hand;");
        }
    }

    /**
     * Navigates to the shopping cart view.
     */
    @FXML
    private void showCart() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/shopping_cart.fxml"));
            Parent root = loader.load();
            Scene scene = mainContentPane.getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            System.err.println("Error loading cart: " + e.getMessage());
            e.printStackTrace();
            showError("Error loading cart: " + e.getMessage());
        }
    }

    /**
     * Displays the customer's order history.
     * <p>
     * Fetches orders from the database, sorts them by date (newest first),
     * and displays them with pagination.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void showOrders() {
        // pageTitle.setText("📦 My Orders");
        mainContentPane.getChildren().clear();

        try {
            int customerId = SessionManager.getInstance().getCurrentUserId();
            List<Order> orders = orderDAO.findByCustomer(customerId);

            if (orders.isEmpty()) {
                Label noOrders = new Label("You have no orders yet.");
                noOrders.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
                mainContentPane.getChildren().add(noOrders);
                return;
            }

            // Sort by ID descending (newest first)
            orders.sort((o1, o2) -> Integer.compare(o2.getId(), o1.getId()));

            setupOrderPagination(orders);

        } catch (SQLException e) {
            showError("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets up pagination for the order history list.
     *
     * @param orders The list of orders to paginate.
     * 
     * @author Burak Özevin
     */
    private void setupOrderPagination(List<Order> orders) {
        int itemsPerPage = 5;
        int pageCount = (int) Math.ceil((double) orders.size() / itemsPerPage);

        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setAlignment(Pos.TOP_CENTER);

        // Content Area
        VBox ordersListContainer = new VBox(15);
        ordersListContainer.setAlignment(Pos.TOP_CENTER);
        // Ensure the list takes available width but respects padding
        ordersListContainer.setMaxWidth(Double.MAX_VALUE);

        // Pagination Control
        Pagination pagination = new Pagination(pageCount, 0);
        pagination.setMaxHeight(45);
        pagination.getStyleClass().add("compact-pagination");

        // Hide default content
        pagination.setPageFactory(pageIndex -> new Region());

        // Listener to update content
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateOrderPage(ordersListContainer, orders, newVal.intValue(), itemsPerPage);
        });

        // Initial load
        updateOrderPage(ordersListContainer, orders, 0, itemsPerPage);

        container.getChildren().addAll(ordersListContainer, pagination);
        mainContentPane.getChildren().add(container);
    }

    /**
     * Updates the content of the order pagination page.
     *
     * @param container    The VBox to populate with order cards.
     * @param orders       The full list of orders.
     * @param pageIndex    The current page index.
     * @param itemsPerPage The number of items per page.
     * 
     * @author Burak Özevin
     */
    private void updateOrderPage(VBox container, List<Order> orders, int pageIndex, int itemsPerPage) {
        container.getChildren().clear();
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, orders.size());

        if (fromIndex < orders.size()) {
            List<Order> pageItems = orders.subList(fromIndex, toIndex);
            for (Order order : pageItems) {
                container.getChildren().add(createOrderCard(order));
            }
        }
    }

    /**
     * Creates a UI card component representing a single order in the history.
     * <p>
     * Displays order details, status, items, and action buttons (Cancel, Track,
     * Rate, View Invoice).
     * </p>
     *
     * @param order The {@link Order} to display.
     * @return A {@link VBox} containing the order card.
     * 
     * @author Burak Özevin
     */
    private VBox createOrderCard(Order order) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        int customerId = SessionManager.getInstance().getCurrentUserId();

        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.getStyleClass().add("product-card");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label orderId = new Label("Order #" + order.getId());
        orderId.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        Label statusLabel = new Label(getStatusText(order.getStatus()));
        statusLabel.getStyleClass().addAll("badge", "badge-" + order.getStatus().getValue());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(orderId, spacer, statusLabel);

        Label dateLabel = new Label("📅 Order: " + order.getOrderTime().format(fmt));
        Label deliveryLabel = new Label("🚚 Delivery: " + order.getRequestedDeliveryTime().format(fmt));
        Label totalLabel = new Label(String.format("💰 Total: ₺%.2f", order.getTotalCost()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        // Products list
        StringBuilder products = new StringBuilder("📦 ");
        for (OrderItem item : order.getItems()) {
            products.append(item.getProductName()).append(" (").append(item.getAmount()).append("kg), ");
        }
        Label productsLabel = new Label(products.toString());
        productsLabel.setWrapText(true);

        card.getChildren().addAll(header, dateLabel, deliveryLabel, totalLabel, productsLabel);

        // Action buttons
        HBox actions = new HBox(10);

        // Cancel button (only for pending orders within 1 hour)
        if (order.canBeCancelled()) {
            MFXButton cancelBtn = new MFXButton(
                    "❌ Cancel (" + order.getCancellationTimeRemaining() + " min left)");
            cancelBtn.getStyleClass().add("danger-button");
            cancelBtn.setOnAction(e -> cancelOrder(order));
            actions.getChildren().add(cancelBtn);
        }

        // Track Order button (for orders "On the way")
        if (order.getStatus() == OrderStatus.SELECTED) {
            MFXButton trackBtn = new MFXButton("📍 Track Order");
            trackBtn.getStyleClass().add("outlined-button");
            trackBtn.setOnAction(e -> showOrderTracking(order));
            actions.getChildren().add(trackBtn);
        }

        // Rate carrier section (only for delivered orders)
        if (order.getStatus() == OrderStatus.DELIVERED && order.getCarrierId() != null) {
            if (!ratingDAO.hasRated(order.getId(), customerId)) {
                // Inline rating section
                VBox ratingSection = createRatingSection(order);
                card.getChildren().add(ratingSection);
            } else {
                Label rated = new Label("✓ Rated");
                rated.setStyle("-fx-text-fill: #4CAF50;");
                actions.getChildren().add(rated);
            }
        }

        if (!actions.getChildren().isEmpty()) {
            card.getChildren().add(actions);
        }

        // Invoice button
        if (order.getInvoice() != null && !order.getInvoice().isEmpty()) {
            MFXButton invoiceBtn = new MFXButton("📄 View Invoice");
            invoiceBtn.getStyleClass().add("outlined-button");
            invoiceBtn.setOnAction(e -> viewInvoice(order));
            if (actions.getChildren().isEmpty()) {
                card.getChildren().add(invoiceBtn);
            } else {
                actions.getChildren().add(0, invoiceBtn);
            }
        }

        return card;
    }

    /**
     * Displays a dialog showing the tracking status of an order.
     * <p>
     * Shows a stepper indicating the current status (Preparing, On the Way, At
     * Address, Delivered)
     * and a map view if the order is "On the Way".
     * </p>
     *
     * @param order The {@link Order} to track.
     * 
     * @author Burak Özevin
     */
    private void showOrderTracking(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Tracking #" + order.getId());
        dialog.initOwner(mainContentPane.getScene().getWindow());
        applyThemeToDialog(dialog);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setPrefSize(800, 600);

        // Map View (WebView)
        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        javafx.scene.web.WebEngine engine = webView.getEngine();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        String fullAddress = (currentUser != null && currentUser.getAddress() != null)
                ? currentUser.getAddress()
                : "Istanbul";

        // Extract District for display (assuming format "City / District / Details")
        String displayDistrict = "Delivery Address";
        if (fullAddress.contains(" / ")) {
            String[] parts = fullAddress.split(" / ");
            if (parts.length >= 2) {
                displayDistrict = parts[1]; // District
            }
        }

        // Escape for JS
        final String addressForSearch = fullAddress.replace("'", "\\'");
        final String districtForPopup = displayDistrict.replace("'", "\\'");

        // Leaflet map with Nominatim Geocoding
        String mapHtml = "<!DOCTYPE html>" +
                "<html><head>" +
                "<link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\" />" +
                "<script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>" +
                "<style>body { margin: 0; padding: 0; } #map { height: 100vh; width: 100%; }</style>" +
                "</head><body>" +
                "<div id=\"map\"></div>" +
                "<script>" +
                "var map = L.map('map').setView([41.0082, 28.9784], 13);" + // Default Istanbul
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "    attribution: '&copy; OpenStreetMap contributors'" +
                "}).addTo(map);" +

                "var start = [41.0082, 28.9784];" + // Store location (fixed for now)
                "L.marker(start).addTo(map).bindPopup('Store').openPopup();" +

                // Geocode the customer address
                "var address = '" + addressForSearch + "';" +
                "fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(address))" +
                ".then(response => response.json())" +
                ".then(data => {" +
                "    if (data && data.length > 0) {" +
                "        var lat = data[0].lat;" +
                "        var lon = data[0].lon;" +
                "        var end = [lat, lon];" +
                "        L.marker(end).addTo(map).bindPopup('" + districtForPopup + "');" +
                "        var route = L.polyline([start, end], {color: '#2E7D32', weight: 5}).addTo(map);" +
                "        map.fitBounds(route.getBounds(), {padding: [50, 50]});" +
                "    } else {" +
                "        console.log('Address not found, using default');" +
                "        var end = [41.0122, 28.9884];" + // Default if not found
                "        L.marker(end).addTo(map).bindPopup('" + districtForPopup + " (Approx)');" +
                "        var route = L.polyline([start, end], {color: '#2E7D32', weight: 5}).addTo(map);" +
                "        map.fitBounds(route.getBounds(), {padding: [50, 50]});" +
                "    }" +
                "})" +
                ".catch(err => {" +
                "    console.error(err);" +
                "    var end = [41.0122, 28.9884];" +
                "    L.marker(end).addTo(map).bindPopup('" + districtForPopup + "');" +
                "    var route = L.polyline([start, end], {color: '#2E7D32', weight: 5}).addTo(map);" +
                "    map.fitBounds(route.getBounds(), {padding: [50, 50]});" +
                "});" +
                "</script></body></html>";

        engine.loadContent(mapHtml);
        VBox.setVgrow(webView, Priority.ALWAYS);

        // Progress Stepper
        HBox stepper = createTrackingStepper(2); // 2 = "On the Way" (0-indexed: 1)

        content.getChildren().addAll(webView, stepper);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        if (themeManager.isDarkTheme()) {
            dialog.getDialogPane().getStyleClass().add("dark-theme");
        }

        dialog.showAndWait();
    }

    /**
     * Creates a visual stepper component for order tracking.
     *
     * @param currentStepIndex The index of the current active step (0-3).
     * @return An {@link HBox} containing the stepper UI.
     * @author Burak Özevin
     */
    private HBox createTrackingStepper(int currentStepIndex) {
        String[] steps = { "Preparing", "On the Way", "At Address", "Delivered" };
        HBox container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(10, 0, 10, 0));

        for (int i = 0; i < steps.length; i++) {

            // Step Circle
            StackPane circleContainer = new StackPane();
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(15);
            circle.setStroke(null);

            Label number = new Label(String.valueOf(i + 1));
            number.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

            if (i <= currentStepIndex) {
                // Active/Completed: Green
                if (i == currentStepIndex) {
                    // Current Pulse Effect or larger
                    circle.setRadius(20);
                    circle.setFill(javafx.scene.paint.Color.web("#2E7D32")); // Dark Green
                } else {
                    circle.setFill(javafx.scene.paint.Color.web("#4CAF50")); // Green
                }
            } else {
                // Inactive: Gray
                circle.setFill(javafx.scene.paint.Color.web("#E0E0E0"));
                number.setStyle("-fx-text-fill: #757575; -fx-font-weight: bold;");
            }
            circleContainer.getChildren().addAll(circle, number);

            // Label
            Label stepLabel = new Label(steps[i]);
            stepLabel.setStyle("-fx-font-size: 12px; -fx-padding: 5 0 0 0; " +
                    (i <= currentStepIndex ? "-fx-text-fill: #2E7D32; -fx-font-weight: bold;"
                            : "-fx-text-fill: #9E9E9E;"));

            VBox stepBox = new VBox(5);
            stepBox.setAlignment(Pos.CENTER);
            stepBox.getChildren().addAll(circleContainer, stepLabel);

            container.getChildren().add(stepBox);

            // Connecting Line (if not last)
            if (i < steps.length - 1) {
                javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 50, 0);
                line.setStrokeWidth(3);
                if (i < currentStepIndex) {
                    line.setStroke(javafx.scene.paint.Color.web("#4CAF50")); // Green
                } else {
                    line.setStroke(javafx.scene.paint.Color.web("#E0E0E0")); // Gray
                }
                container.getChildren().add(line);
            }
        }

        return container;
    }

    /**
     * Opens the invoice for the specified order.
     * <p>
     * Retrieves the PDF bytes from the order, saves them to a temporary file,
     * and attempts to open the file using the system's default PDF viewer.
     * </p>
     *
     * @param order The {@link Order} to view the invoice for.
     * 
     * @author Burak Özevin
     */
    private void viewInvoice(Order order) {
        new Thread(() -> {
            try {
                byte[] pdfBytes = order.getInvoiceBytes();
                java.io.File tempFile = java.io.File.createTempFile("invoice_" + order.getId() + "_", ".pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    fos.write(pdfBytes);
                }

                String os = System.getProperty("os.name").toLowerCase();
                boolean opened = false;

                if (java.awt.Desktop.isDesktopSupported()
                        && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                    try {
                        java.awt.Desktop.getDesktop().open(tempFile);
                        opened = true;
                    } catch (Exception ignored) {
                        // Fallback to OS commands
                    }
                }

                if (!opened) {
                    if (os.contains("win")) {
                        new ProcessBuilder("cmd", "/c", "start", tempFile.getAbsolutePath()).start();
                    } else if (os.contains("mac")) {
                        new ProcessBuilder("open", tempFile.getAbsolutePath()).start();
                    } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                        new ProcessBuilder("xdg-open", tempFile.getAbsolutePath()).start();
                    } else {
                        javafx.application.Platform.runLater(() -> showError(
                                "Unable to open PDF viewer. File location: " + tempFile.getAbsolutePath()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform
                        .runLater(() -> showError("Error viewing invoice: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Helper method to get a user-friendly status text for an order status.
     *
     * @param status The {@link OrderStatus} enum value.
     * @return A readable string representation of the status.
     * 
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
     * Cancels an order.
     * <p>
     * Prompts the user for confirmation. If confirmed, restores the stock of the
     * items
     * and updates the order status to cancelled in the database.
     * </p>
     *
     * @param order The {@link Order} to cancel.
     * 
     * @author Burak Özevin
     */
    private void cancelOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to cancel this order?");
        confirm.initOwner(mainContentPane.getScene().getWindow());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Restore stock
                    for (OrderItem item : order.getItems()) {
                        productDAO.restoreStock(item.getProductId(), item.getAmount());
                    }
                    orderDAO.cancel(order.getId());
                    showSuccess("Order cancelled!");
                    showOrders();
                } catch (SQLException e) {
                    showError("Error cancelling order: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Creates a rating section for a delivered order.
     * <p>
     * Allows the user to select a star rating (1-5) and enter an optional comment.
     * </p>
     *
     * @param order The {@link Order} to rate.
     * @return A {@link VBox} containing the rating UI.
     * 
     * @author Burak Özevin
     */
    private VBox createRatingSection(Order order) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.getStyleClass().add("rating-section");

        Label instruction = new Label("How many stars would you give for the delivery?");
        instruction.getStyleClass().add("rating-instruction-label");

        HBox stars = new HBox(5);
        int[] selectedRating = { 0 };

        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            MFXButton starBtn = new MFXButton("☆");
            starBtn.setStyle(
                    "-fx-font-size: 20px; -fx-text-fill: #FFD700; -fx-background-color: transparent; -fx-cursor: hand;");
            starBtn.setOnAction(e -> {
                selectedRating[0] = rating;
                updateStars(stars, rating);
            });
            stars.getChildren().add(starBtn);
        }

        TextArea commentField = new TextArea();
        commentField.setPromptText("Comment (optional)");
        commentField.setPrefRowCount(2);
        commentField.setWrapText(true);

        MFXButton submitBtn = new MFXButton("Submit Rating");
        submitBtn.getStyleClass().add("primary-button");
        submitBtn.setOnAction(e -> {
            if (selectedRating[0] == 0) {
                showError("Please select a star rating!");
                return;
            }

            CarrierRating carrierRating = new CarrierRating();
            carrierRating.setCarrierId(order.getCarrierId());
            carrierRating.setCustomerId(SessionManager.getInstance().getCurrentUserId());
            carrierRating.setOrderId(order.getId());
            carrierRating.setRating(selectedRating[0]);
            carrierRating.setComment(commentField.getText());

            try {
                ratingDAO.create(carrierRating);
                showSuccess("Your rating has been saved!");
                showOrders(); // Refresh to show "Rated" status
            } catch (Exception ex) {
                showError("Error saving rating: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        container.getChildren().addAll(instruction, stars, commentField, submitBtn);
        return container;
    }

    /**
     * Updates the star buttons to reflect the selected rating.
     *
     * @param stars  The HBox containing the star buttons.
     * @param rating The selected rating (1-5).
     * 
     * @author Burak Özevin
     */
    private void updateStars(HBox stars, int rating) {
        for (int i = 0; i < stars.getChildren().size(); i++) {
            MFXButton btn = (MFXButton) stars.getChildren().get(i);
            btn.setText(i < rating ? "★" : "☆");
        }
    }

    /**
     * Displays the ratings page (currently shows delivered orders that can be
     * rated).
     * 
     * @author Burak Özevin
     */
    @FXML
    private void showRatings() {
        // pageTitle.setText("⭐ My Ratings");
        mainContentPane.getChildren().clear();

        try {
            int customerId = SessionManager.getInstance().getCurrentUserId();
            List<Order> deliveredOrders = orderDAO.findByCustomer(customerId);
            deliveredOrders.removeIf(o -> o.getStatus() != OrderStatus.DELIVERED);

            if (deliveredOrders.isEmpty()) {
                Label noRatings = new Label("You have no delivered orders yet.");
                mainContentPane.getChildren().add(noRatings);
                return;
            }

            for (Order order : deliveredOrders) {
                if (order.getCarrierId() == null)
                    continue;

                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.getStyleClass().add("product-card");

                Label orderLabel = new Label("Order #" + order.getId() + " - Carrier: " + order.getCarrierName());
                orderLabel.setStyle("-fx-font-weight: bold;");

                boolean hasRated = ratingDAO.hasRated(order.getId(), customerId);
                Label statusLabel = new Label(hasRated ? "✓ Rated" : "Not rated");

                card.getChildren().addAll(orderLabel, statusLabel);

                if (!hasRated) {
                    MFXButton rateBtn = new MFXButton("⭐ Rate");
                    rateBtn.setOnAction(e -> {
                        // Switch back to orders view to rate
                        showOrders();
                    });
                    card.getChildren().add(rateBtn);
                }

                mainContentPane.getChildren().add(card);
            }
        } catch (SQLException e) {
            showError("Error loading ratings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the messages page.
     * 
     * @author Burak Özevin
     */
    @FXML
    private void showMessages() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/messages.fxml"));
            Parent root = loader.load();
            Scene scene = mainContentPane.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Error loading messages page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog to send a message to the store owner.
     *
     * @param owner The {@link User} object representing the owner.
     * 
     * @author Burak Özevin
     */
    private void sendMessage(User owner) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("New Message");
        dialog.setHeaderText("Send a message to the store owner");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        MFXTextField subjectField = new MFXTextField();
        subjectField.setPromptText("Subject");
        subjectField.setPrefWidth(300);

        TextArea contentField = new TextArea();
        contentField.setPromptText("Your message");
        contentField.setPrefRowCount(5);

        content.getChildren().addAll(new Label("Subject:"), subjectField, new Label("Message:"), contentField);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new String[] { subjectField.getText(), contentField.getText() };
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result[0].isEmpty() || result[1].isEmpty()) {
                showError("Subject and message fields are required!");
                return;
            }

            Message msg = new Message();
            msg.setSenderId(SessionManager.getInstance().getCurrentUserId());
            msg.setReceiverId(owner.getId());
            msg.setSubject(result[0]);
            msg.setContent(result[1]);

            try {
                messageDAO.create(msg);
                showSuccess("Your message has been sent!");
                showMessages();
            } catch (SQLException e) {
                showError("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Displays the customer's available coupons.
     * <p>
     * Fetches coupons from the database and displays them in a card layout.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void showCoupons() {
        mainContentPane.getChildren().clear();
        toggleMenu(false);

        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        Label title = new Label("🎟️ My Coupons");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
        container.getChildren().add(title);

        try {
            int customerId = SessionManager.getInstance().getCurrentUserId();
            List<CustomerCouponUsage> coupons = couponDAO.findCouponsForCustomer(customerId);

            if (coupons.isEmpty()) {
                Label noData = new Label("You don't have any available coupons yet.");
                noData.setStyle("-fx-font-size: 16px; -fx-text-fill: #757575;");
                container.getChildren().add(noData);
            } else {
                for (CustomerCouponUsage usage : coupons) {
                    Coupon coupon = usage.getCoupon();
                    if (coupon == null)
                        continue;

                    VBox card = new VBox(10);
                    card.setPadding(new Insets(20));
                    card.setStyle("-fx-background-color: linear-gradient(to right, #43A047, #66BB6A); " +
                            "-fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 5);");

                    // Coupon header
                    HBox header = new HBox(10);
                    header.setAlignment(Pos.CENTER_LEFT);

                    Label codeLabel = new Label(coupon.getCode());
                    codeLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label discountLabel = new Label(String.format("%%%,.0f DISCOUNT", coupon.getDiscountPercent()));
                    discountLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #FFEB3B; " +
                            "-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 20; -fx-padding: 5 15;");

                    header.getChildren().addAll(codeLabel, spacer, discountLabel);

                    // Coupon details
                    HBox details = new HBox(20);
                    details.setAlignment(Pos.CENTER_LEFT);

                    Label minValue = new Label(String.format("Min. Cart: ₺%.2f", coupon.getMinCartValue()));
                    minValue.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

                    Label usesLabel = new Label(String.format("Uses Remaining: %d", usage.getUsesRemaining()));
                    usesLabel.setStyle("-fx-text-fill: #FFEB3B; -fx-font-weight: bold; -fx-font-size: 13px;");

                    details.getChildren().addAll(minValue, usesLabel);

                    // Validity info
                    if (coupon.getValidUntil() != null) {
                        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter
                                .ofPattern("dd.MM.yyyy");
                        Label validity = new Label("Valid Until: " + coupon.getValidUntil().format(fmt));
                        validity.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");
                        card.getChildren().addAll(header, details, validity);
                    } else {
                        card.getChildren().addAll(header, details);
                    }

                    container.getChildren().add(card);
                }
            }
        } catch (SQLException e) {
            showError("Error loading coupons: " + e.getMessage());
            e.printStackTrace();
        }

        mainContentPane.getChildren().add(container);
    }

    /**
     * Navigates to the customer profile page.
     * 
     * @author Burak Özevin
     */
    @FXML
    private void showProfile() {
        closeChatbot();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/profile.fxml"));
            Parent root = loader.load();
            Scene scene = mainContentPane.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - My Profile");
        } catch (IOException e) {
            showError("Error loading profile page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Logs out the current user and returns to the login screen.
     * 
     * @author Burak Özevin
     */
    @FXML
    private void handleLogout() {
        closeChatbot();
        SessionManager.getInstance().logout();
        ShoppingCart.resetInstance();
        navigateToLogin();
    }

    /**
     * Helper method to navigate to the login screen.
     * 
     * @author Burak Özevin
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = mainContentPane.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the chatbot window if it is open.
     * 
     * @author Burak Özevin
     */
    private void closeChatbot() {
        if (chatbotStage != null && chatbotStage.isShowing()) {
            chatbotStage.close();
        }
    }

    /**
     * Displays an error alert with the specified message.
     *
     * @param msg The error message to display.
     * @author Burak Özevin
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(mainContentPane.getScene().getWindow());
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Displays a success information alert with the specified message.
     *
     * @param msg The success message to display.
     * @author Burak Özevin
     */
    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(mainContentPane.getScene().getWindow());
        alert.setTitle("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Opens the product detail view for the specified product.
     *
     * @param product The {@link Product} to view details for.
     * @author Burak Özevin
     */
    private void openProductDetail(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/product_detail.fxml"));
            Parent root = loader.load();

            ProductDetailController controller = loader.getController();
            controller.setProduct(product);

            Scene scene = mainContentPane.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Error opening product details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Closes the hamburger menu.
     * 
     * @author Burak Özevin
     */
    private void closeMenu() {
        toggleMenu(false);
    }

    /**
     * Opens the chatbot window.
     * <p>
     * If the chatbot window is already open, brings it to the front.
     * Otherwise, creates a new stage and initializes the chatbot UI.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void openChatbot() {
        if (chatbotStage != null && chatbotStage.isShowing()) {
            chatbotStage.toFront();
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        // Create chatbot window
        chatbotStage = new Stage();
        chatbotStage.setTitle("GreenGrocer Chatbot");
        chatbotStage.initModality(javafx.stage.Modality.NONE);
        chatbotStage.initOwner(mainContentPane.getScene().getWindow());
        chatbotStage.setResizable(false);
        chatbotStage.setWidth(400);
        chatbotStage.setHeight(600);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("chatbot-root");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.getStyleClass().add("chatbot-header");

        Label title = new Label("🤖 GreenGrocer Chatbot");
        title.getStyleClass().add("chatbot-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MFXButton closeBtn = new MFXButton();
        closeBtn.setGraphic(new org.kordamp.ikonli.javafx.FontIcon("fas-times"));
        closeBtn.setOnAction(e -> chatbotStage.close());
        closeBtn.getStyleClass().add("chatbot-close-btn");

        header.getChildren().addAll(title, spacer, closeBtn);

        // Messages area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        scrollPane.getStyleClass().add("chatbot-scroll-pane");

        VBox messagesContainer = new VBox(10);
        messagesContainer.setPadding(new Insets(15));
        messagesContainer.getStyleClass().add("chatbot-messages-container");

        scrollPane.setContent(messagesContainer);
        scrollPane.setVvalue(1.0);

        // Add welcome message
        addChatMessage(messagesContainer, "Hello! Welcome to GreenGrocer! How can I assist you today?", false);

        // Input area
        HBox inputArea = new HBox(10);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.getStyleClass().add("chatbot-input-area");

        TextField inputField = new TextField();
        inputField.setPromptText("Type your message...");
        inputField.setPrefWidth(300);
        inputField.getStyleClass().add("chatbot-input-field");
        inputField.setOnAction(e -> sendChatMessage(inputField, messagesContainer, scrollPane, currentUser));

        MFXButton sendBtn = new MFXButton("Send");
        sendBtn.setOnAction(e -> sendChatMessage(inputField, messagesContainer, scrollPane, currentUser));
        sendBtn.getStyleClass().add("primary-button");

        inputArea.getChildren().addAll(inputField, sendBtn);

        root.getChildren().addAll(header, scrollPane, inputArea);

        Scene scene = new Scene(root);
        chatbotStage.setScene(scene);

        // Apply theme
        javafx.application.Platform.runLater(() -> {
            themeManager.applyTheme(scene);
        });

        // Position at bottom right
        javafx.application.Platform.runLater(() -> {
            Stage mainStage = (Stage) mainContentPane.getScene().getWindow();
            chatbotStage.setX(mainStage.getX() + mainStage.getWidth() - chatbotStage.getWidth() - 20);
            chatbotStage.setY(mainStage.getY() + mainStage.getHeight() - chatbotStage.getHeight() - 20);
        });

        chatbotStage.show();

        // Auto-scroll to bottom when new messages are added
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    /**
     * Sends a chat message to the chatbot service.
     * <p>
     * Adds the user's message to the UI, clears the input field, and asynchronously
     * fetches and displays the bot's response.
     * </p>
     *
     * @param inputField        The input field containing the message.
     * @param messagesContainer The container for chat messages.
     * @param scrollPane        The scroll pane for the chat area.
     * @param user              The current user.
     * @author Burak Özevin
     */
    private void sendChatMessage(TextField inputField, VBox messagesContainer, ScrollPane scrollPane, User user) {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        // Add user message
        addChatMessage(messagesContainer, message, true);
        inputField.clear();

        // Get bot response
        String response = chatbotService.processMessage(message, user);

        // Simulate typing delay
        new Thread(() -> {
            try {
                Thread.sleep(500); // Small delay for realism
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            javafx.application.Platform.runLater(() -> {
                addChatMessage(messagesContainer, response, false);
                scrollPane.setVvalue(1.0);
            });
        }).start();
    }

    /**
     * Adds a chat message bubble to the chat UI.
     *
     * @param container The container to add the message to.
     * @param text      The message text.
     * @param isUser    {@code true} if the message is from the user, {@code false}
     *                  if from the bot.
     * @author Burak Özevin
     */
    private void addChatMessage(VBox container, String text, boolean isUser) {
        HBox messageBox = new HBox(10);
        messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10, 15, 10, 15));
        bubble.setMaxWidth(280);

        if (isUser) {
            bubble.getStyleClass().add("chatbot-message-bubble-user");
        } else {
            bubble.getStyleClass().add("chatbot-message-bubble-bot");
        }

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add(isUser ? "chatbot-message-text-user" : "chatbot-message-text-bot");

        Label timeLabel = new Label(
                java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.getStyleClass().add(isUser ? "chatbot-message-time-user" : "chatbot-message-time-bot");
        timeLabel.setAlignment(Pos.BOTTOM_RIGHT);

        bubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(bubble);

        container.getChildren().add(messageBox);
    }

    /**
     * Applies the current theme to a dialog.
     *
     * @param dialog The dialog to style.
     * 
     * @author Burak Özevin
     */
    private void applyThemeToDialog(Dialog<?> dialog) {
        if (themeManager.isDarkTheme()) {
            dialog.getDialogPane().getStyleClass().add("dark-theme");
            java.net.URL darkThemeUrl = getClass().getResource("/styles/styles-dark.css");
            if (darkThemeUrl != null) {
                dialog.getDialogPane().getStylesheets().add(darkThemeUrl.toExternalForm());
            }
        }
    }
}
