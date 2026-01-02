package com.greengrocer.controllers;

import com.greengrocer.dao.*;
import com.greengrocer.models.*;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ShoppingCart;
import io.github.palexdev.materialfx.controls.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the shopping cart with modern UI design.
 * <p>
 * Manages the shopping cart view, including displaying cart items, updating
 * quantities,
 * calculating totals (subtotal, VAT, discounts), applying coupons, and
 * processing checkout.
 * Also handles delivery date and time selection.
 * </p>
 * 
 * @author Elif Zeynep Talay
 */
public class ShoppingCartController implements Initializable {

    /**
     * Container for the list of cart items.
     */
    @FXML
    private VBox cartItemsPane;

    /**
     * Label displaying the header title.
     */
    @FXML
    private Label headerTitle;

    /**
     * Label displaying the total count of items in the cart.
     */
    @FXML
    private Label itemCountLabel;

    /**
     * Label displaying the item count summary.
     */
    @FXML
    private Label itemCountSummary;

    /**
     * Label displaying the subtotal amount.
     */
    @FXML
    private Label subtotalLabel;

    /**
     * Label displaying the VAT amount.
     */
    @FXML
    private Label vatLabel;

    /**
     * Label displaying the discount amount.
     */
    @FXML
    private Label discountLabel;

    /**
     * Label displaying the total amount to pay.
     */
    @FXML
    private Label totalLabel;

    /**
     * Label displaying the estimated delivery time.
     */
    @FXML
    private Label deliveryEstimate;

    /**
     * Label acting as a link to remove all items from the cart.
     */
    @FXML
    private Label removeAllLink;

    /**
     * Text field for entering a coupon code.
     */
    @FXML
    private TextField couponField;

    /**
     * Date picker for selecting the delivery date.
     */
    @FXML
    private DatePicker deliveryDatePicker;

    /**
     * Combo box for selecting the delivery time slot.
     */
    @FXML
    private ComboBox<String> deliveryTimeCombo;

    /**
     * The shopping cart instance.
     */
    private final ShoppingCart cart = ShoppingCart.getInstance();

    /**
     * Data Access Object for order-related operations.
     */
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * Data Access Object for product-related operations.
     */
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * Data Access Object for coupon-related operations.
     */
    private final CouponDAO couponDAO = new CouponDAO();

    /**
     * Data Access Object for user-related operations.
     */
    private final UserDAO userDAO = new UserDAO();

    /**
     * Data Access Object for system settings.
     */
    private final SystemSettingDAO systemSettingDAO = new SystemSettingDAO();

    /**
     * The percentage of loyalty discount applicable to the user.
     */
    private double discountPercent = 0;

    /**
     * The coupon currently applied to the cart.
     */
    private Coupon appliedCoupon = null;

    /**
     * Button to toggle the application theme.
     */
    @FXML
    private MFXButton themeToggleBtn;

    /**
     * The theme manager instance.
     */
    private final com.greengrocer.utils.ThemeManager themeManager = com.greengrocer.utils.ThemeManager.getInstance();

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the theme, configures the delivery date picker (restricting past
     * dates),
     * initializes the delivery time combo box, checks for user loyalty discounts,
     * and refreshes the cart view.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize theme
        com.greengrocer.utils.ThemeManager.getInstance().initializeTheme(themeToggleBtn);

        // Setup date picker to only allow future dates up to 30 days ahead
        LocalDate maxDate = LocalDate.now().plusDays(30);
        deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable dates before today and after 30 days from now
                setDisable(empty || date.isBefore(LocalDate.now()) || date.isAfter(maxDate));
            }
        });

        // Set default date to today
        deliveryDatePicker.setValue(LocalDate.now());

        // Setup time combo with dynamic filtering
        setupTimeCombo();

        User user = SessionManager.getInstance().getCurrentUser();
        if (user instanceof Customer) {
            discountPercent = ((Customer) user).getLoyaltyDiscount();
        }

        refreshCart();
    }

    /**
     * Configures the delivery date picker.
     * <p>
     * Sets the minimum date to today and adds a listener to validate the selected
     * date.
     * Updates available time slots when the date changes.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void setupDatePicker() {
        // Set minimum date to today
        deliveryDatePicker.setOnAction(e -> {
            LocalDate selected = deliveryDatePicker.getValue();
            if (selected != null && selected.isBefore(LocalDate.now())) {
                deliveryDatePicker.setValue(LocalDate.now());
                showError("You cannot select a past date!");
            }
            // Refresh time options when date changes
            updateAvailableTimes();
        });
    }

    /**
     * Configures the delivery time combo box.
     * <p>
     * Populates available time slots and adds a listener to update them when the
     * selected date changes.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void setupTimeCombo() {
        // Add all time slots initially
        updateAvailableTimes();

        // When date picker value changes, update available times
        deliveryDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateAvailableTimes();
            updateDeliveryEstimate();
        });
    }

    /**
     * Updates the available delivery time slots based on the selected date.
     * <p>
     * Filters out past time slots if the selected date is today.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void updateAvailableTimes() {
        String previousSelection = deliveryTimeCombo.getValue();
        deliveryTimeCombo.getItems().clear();

        LocalDate selectedDate = deliveryDatePicker.getValue();
        LocalTime now = LocalTime.now();

        // All time slots
        String[] allSlots = { "09:00 - 12:00", "12:00 - 15:00", "15:00 - 18:00", "18:00 - 21:00" };
        int[] slotStartHours = { 9, 12, 15, 18 };

        for (int i = 0; i < allSlots.length; i++) {
            // If selected date is today, only show future time slots
            if (selectedDate != null && selectedDate.equals(LocalDate.now())) {
                // Only add if the slot hasn't passed yet (give 30 min buffer)
                if (now.getHour() < slotStartHours[i] ||
                        (now.getHour() == slotStartHours[i] && now.getMinute() < 30)) {
                    deliveryTimeCombo.getItems().add(allSlots[i]);
                }
            } else {
                // For future dates, all slots are available
                deliveryTimeCombo.getItems().add(allSlots[i]);
            }
        }

        // Try to restore previous selection if still valid
        if (previousSelection != null && deliveryTimeCombo.getItems().contains(previousSelection)) {
            deliveryTimeCombo.setValue(previousSelection);
        } else if (!deliveryTimeCombo.getItems().isEmpty()) {
            // Select first available slot
            deliveryTimeCombo.setValue(deliveryTimeCombo.getItems().get(0));
        }
    }

    /**
     * Refreshes the cart view.
     * <p>
     * Rebuilds the list of cart items, updates the item count, totals, and delivery
     * estimate.
     * Shows an empty state if the cart is empty.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void refreshCart() {
        cartItemsPane.getChildren().clear();

        int itemCount = cart.getItemCount();
        itemCountLabel.setText(itemCount + " Items");
        itemCountSummary.setText(itemCount + " items:");

        if (cart.isEmpty()) {
            VBox emptyState = createEmptyState();
            cartItemsPane.getChildren().add(emptyState);
            removeAllLink.setVisible(false);
        } else {
            removeAllLink.setVisible(true);
            for (CartItem item : cart.getItems()) {
                VBox itemCard = createCartItemCard(item);
                cartItemsPane.getChildren().add(itemCard);
            }
        }

        updateTotals();
        updateDeliveryEstimate();
    }

    /**
     * Creates a visual representation for an empty cart.
     *
     * @return A {@link VBox} containing the empty state UI.
     * 
     * @author Elif Zeynep Talay
     */
    private VBox createEmptyState() {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(80, 20, 80, 20));

        Label emptyIcon = new Label("🛒");
        emptyIcon.setStyle("-fx-font-size: 64px;");

        Label emptyText = new Label("Your cart is empty");
        emptyText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label emptySubtext = new Label("Add products to start shopping");
        emptySubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575;");

        MFXButton shopButton = new MFXButton("Start Shopping");
        shopButton.getStyleClass().add("checkout-button");
        shopButton.setOnAction(e -> goBack());

        emptyBox.getChildren().addAll(emptyIcon, emptyText, emptySubtext, shopButton);
        return emptyBox;
    }

    /**
     * Creates a card view for a single cart item.
     * <p>
     * Displays product image, name, price, stock, and quantity controls.
     * Allows updating quantity or removing the item.
     * </p>
     *
     * @param item The {@link CartItem} to display.
     * @return A {@link VBox} containing the item card.
     * 
     * @author Elif Zeynep Talay
     */
    private VBox createCartItemCard(CartItem item) {
        VBox card = new VBox(0);
        card.getStyleClass().add("cart-item-card");
        card.setPadding(new Insets(20));

        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.CENTER_LEFT);

        // Product Image
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("cart-item-image-container");
        imageContainer.setPrefSize(80, 80);
        imageContainer.setMinSize(80, 80);

        Product product = item.getProduct();
        if (product != null && product.getImage() != null && product.getImage().length > 0) {
            try {
                ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(product.getImage())));
                imageView.setFitWidth(70);
                imageView.setFitHeight(70);
                imageView.setPreserveRatio(true);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                Label emoji = new Label(product.getType() == Product.ProductType.VEGETABLE ? "🥬" : "🍎");
                emoji.setStyle("-fx-font-size: 36px;");
                imageContainer.getChildren().add(emoji);
            }
        } else {
            Label emoji = new Label(
                    product != null && product.getType() == Product.ProductType.VEGETABLE ? "🥬" : "🍎");
            emoji.setStyle("-fx-font-size: 36px;");
            imageContainer.getChildren().add(emoji);
        }

        // Product Info
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(item.getProductName());
        nameLabel.getStyleClass().add("cart-item-name");

        String unit = product != null && product.getUnitType() == Product.UnitType.KG ? "kg" : "piece";
        Label priceLabel = new Label(String.format("₺%.2f / %s", item.getUnitPrice(), unit));
        priceLabel.getStyleClass().add("cart-item-unit-price");

        double stock = product != null ? product.getStock() : 0;
        Label stockLabel = new Label(String.format("Stock: %.1f %s", stock, unit));
        stockLabel.getStyleClass().add("cart-item-stock");

        infoBox.getChildren().addAll(nameLabel, priceLabel, stockLabel);

        // Quantity Controls
        HBox quantityBox = new HBox(0);
        quantityBox.setAlignment(Pos.CENTER);
        quantityBox.getStyleClass().add("quantity-container");

        MFXButton minusBtn = new MFXButton("-");
        minusBtn.getStyleClass().add("quantity-button");
        minusBtn.setPrefSize(36, 36);

        Label quantityLabel = new Label(String.format("%.1f", item.getAmount()));
        quantityLabel.getStyleClass().add("quantity-label");
        quantityLabel.setPrefWidth(60);
        quantityLabel.setAlignment(Pos.CENTER);

        MFXButton plusBtn = new MFXButton("+");
        plusBtn.getStyleClass().add("quantity-button");
        plusBtn.setPrefSize(36, 36);

        // Quantity change handlers with stock validation
        double maxStock = stock;
        double step = product != null && product.getUnitType() == Product.UnitType.KG ? 0.5 : 1.0;

        minusBtn.setOnAction(e -> {
            double newAmount = item.getAmount() - step;
            if (newAmount >= step) {
                cart.updateItemAmount(item.getProductId(), newAmount);
                refreshCart();
            }
        });

        plusBtn.setOnAction(e -> {
            double newAmount = item.getAmount() + step;
            if (newAmount <= maxStock) {
                cart.updateItemAmount(item.getProductId(), newAmount);
                refreshCart();
            } else {
                showError("Insufficient stock!\nMaximum: " + String.format("%.1f %s", maxStock, unit));
            }
        });

        quantityBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);

        // Total Price
        VBox priceBox = new VBox(5);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        priceBox.setPrefWidth(100);

        Label totalPrice = new Label(String.format("₺%.2f", item.getTotalPrice()));
        totalPrice.getStyleClass().add("cart-item-total");

        priceBox.getChildren().add(totalPrice);

        // Remove Button
        MFXButton removeBtn = new MFXButton("✕");
        removeBtn.getStyleClass().add("remove-button");
        removeBtn.setOnAction(e -> {
            cart.removeItem(item.getProductId());
            refreshCart();
        });

        mainContent.getChildren().addAll(imageContainer, infoBox, quantityBox, priceBox, removeBtn);
        card.getChildren().add(mainContent);

        // Add separator
        Separator separator = new Separator();
        separator.getStyleClass().add("cart-item-separator");
        VBox.setMargin(separator, new Insets(15, 0, 0, 0));
        card.getChildren().add(separator);

        return card;
    }

    /**
     * Recalculates and updates the cart totals.
     * <p>
     * Calculates subtotal, applies loyalty and coupon discounts, calculates VAT,
     * and updates the corresponding labels.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void updateTotals() {
        double subtotal = cart.getSubtotal();
        double discount = 0;

        // Apply loyalty discount first (percentage)
        if (discountPercent > 0) {
            discount += subtotal * (discountPercent / 100.0);
        }

        // Apply coupon discount
        if (appliedCoupon != null) {
            double couponDiscount = appliedCoupon.calculateDiscount(subtotal);
            discount += couponDiscount;
        }

        double vat = (subtotal - discount) * 0.18;
        double total = subtotal - discount + vat;

        subtotalLabel.setText(String.format("₺%.2f", subtotal));
        vatLabel.setText(String.format("₺%.2f", vat));
        discountLabel.setText(String.format("-₺%.2f", discount));
        totalLabel.setText(String.format("₺%.2f", total));
    }

    /**
     * Updates the delivery estimate label based on the selected date.
     * 
     * @author Elif Zeynep Talay
     */
    private void updateDeliveryEstimate() {
        LocalDate date = deliveryDatePicker.getValue();
        if (date != null) {
            if (date.equals(LocalDate.now().plusDays(1))) {
                deliveryEstimate.setText("Tomorrow, at your selected time");
            } else {
                deliveryEstimate.setText(
                        date.getDayOfMonth() + " " + getMonthName(date.getMonthValue()) + ", at your selected time");
            }
        }
    }

    /**
     * Returns the name of the month for a given month number.
     * 
     * @param month The month number (1-12).
     * @return The name of the month.
     * @author Elif Zeynep Talay
     */
    private String getMonthName(int month) {
        String[] months = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };
        return months[month - 1];
    }

    /**
     * Applies a coupon code entered by the user.
     * <p>
     * Validates the coupon code, checks if it's applicable to the current cart,
     * and updates the totals if valid.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void applyCoupon() {
        String code = couponField.getText().trim();
        if (code.isEmpty()) {
            showError("Enter a coupon code!");
            return;
        }

        try {
            Optional<Coupon> couponOpt = couponDAO.findByCode(code);
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                if (coupon.canApply(cart.getSubtotal())) {
                    appliedCoupon = coupon;

                    updateTotals();

                    String discountText = coupon.getDiscountType() == Coupon.DiscountType.PERCENT
                            ? "%" + (int) coupon.getDiscountPercent()
                            : "₺" + (int) coupon.getDiscountPercent();
                    showSuccess("Coupon applied: " + discountText + " discount!");
                    couponField.setDisable(true);
                } else {
                    showError("Coupon is invalid or minimum cart amount is not met!");
                }
            } else {
                showError("Coupon not found!");
            }
        } catch (SQLException e) {
            showError("Error checking coupon: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Opens a dialog for the user to select from their available coupons.
     * <p>
     * Fetches coupons assigned to the customer and allows them to choose one to
     * apply.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void selectCoupon() {
        try {
            int customerId = SessionManager.getInstance().getCurrentUserId();
            java.util.List<CustomerCouponUsage> coupons = couponDAO.findCouponsForCustomer(customerId);

            if (coupons.isEmpty()) {
                showError("You have no available coupons!");
                return;
            }

            // Create selection dialog
            Dialog<Coupon> dialog = new Dialog<>();
            dialog.setTitle("Select Coupon");
            dialog.setHeaderText("Choose the coupon you want to use");
            dialog.initOwner(cartItemsPane.getScene().getWindow());

            VBox content = new VBox(10);
            content.setPadding(new Insets(20));
            content.setMinWidth(350);

            ToggleGroup group = new ToggleGroup();
            Coupon[] selectedCoupon = { null };

            for (CustomerCouponUsage usage : coupons) {
                Coupon coupon = usage.getCoupon();
                if (coupon == null)
                    continue;

                RadioButton rb = new RadioButton();
                rb.setToggleGroup(group);
                rb.setUserData(coupon);

                VBox couponBox = new VBox(3);
                couponBox.setPadding(new Insets(10));
                couponBox.getStyleClass().add("coupon-selection-box");

                Label codeLabel = new Label(
                        coupon.getCode() + " - %" + (int) coupon.getDiscountPercent() + " Discount");
                codeLabel.getStyleClass().add("coupon-selection-title");

                Label detailLabel = new Label(String.format("Min. Cart: ₺%.2f | Remaining: %d uses",
                        coupon.getMinCartValue(), usage.getUsesRemaining()));
                detailLabel.getStyleClass().add("coupon-selection-detail");

                couponBox.getChildren().addAll(codeLabel, detailLabel);

                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(rb, couponBox);

                // Click on coupon box to select
                couponBox.setOnMouseClicked(e -> rb.setSelected(true));

                content.getChildren().add(row);
            }

            group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    selectedCoupon[0] = (Coupon) newVal.getUserData();
                }
            });

            // Select first by default
            if (!group.getToggles().isEmpty()) {
                group.getToggles().get(0).setSelected(true);
            }

            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setMaxHeight(300);
            scrollPane.setStyle("-fx-background-color: transparent;");

            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return selectedCoupon[0];
                }
                return null;
            });

            dialog.showAndWait().ifPresent(coupon -> {
                couponField.setText(coupon.getCode());
                applyCoupon();
            });

        } catch (SQLException e) {
            showError("Error loading coupons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Processes the checkout operation.
     * <p>
     * Validates the cart (not empty, meets minimum amount), delivery details,
     * and shows an order summary for confirmation.
     * Creates the order in the database, updates stock, increments usage counts,
     * generates an invoice, and clears the cart upon success.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void checkout() {
        if (cart.isEmpty()) {
            showError("Your cart is empty!");
            return;
        }

        double globalMinOrder = systemSettingDAO.getGlobalMinOrderAmount();
        if (cart.getSubtotal() < globalMinOrder) {
            showError(String.format("Minimum cart amount must be ₺%.2f!\nCurrent: ₺%.2f",
                    globalMinOrder, cart.getSubtotal()));
            return;
        }

        if (deliveryDatePicker.getValue() == null) {
            showError("Select a delivery date!");
            return;
        }

        if (deliveryTimeCombo.getValue() == null) {
            showError("Select a delivery time!");
            return;
        }

        LocalDate deliveryDate = deliveryDatePicker.getValue();
        if (deliveryDate.isAfter(LocalDate.now().plusDays(2))) {
            showError("Delivery date can be at most 48 hours from now!");
            return;
        }

        if (!showOrderSummary()) {
            return;
        }

        User user = SessionManager.getInstance().getCurrentUser();
        Order order = new Order();
        order.setCustomerId(user.getId());
        order.setOrderTime(LocalDateTime.now());

        String timeSlot = deliveryTimeCombo.getValue();
        int hour = Integer.parseInt(timeSlot.split(":")[0]);
        order.setRequestedDeliveryTime(LocalDateTime.of(deliveryDate, LocalTime.of(hour, 0)));

        double subtotal = cart.getSubtotal();
        double discount = subtotal * (discountPercent / 100.0);
        if (appliedCoupon != null) {
            discount += appliedCoupon.calculateDiscount(subtotal);
        }
        double vat = (subtotal - discount) * 0.18;
        double total = subtotal - discount + vat;

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discount);
        order.setVatAmount(vat);
        order.setTotalCost(total);

        if (appliedCoupon != null) {
            order.setCouponCode(appliedCoupon.getCode());
        }

        try {
            for (CartItem cartItem : cart.getItems()) {
                OrderItem orderItem = cartItem.toOrderItem();
                order.addItem(orderItem);
                productDAO.reduceStock(cartItem.getProductId(), cartItem.getAmount());
            }

            orderDAO.create(order);
            userDAO.incrementTotalOrders(user.getId());

            if (appliedCoupon != null) {
                couponDAO.incrementUses(appliedCoupon.getId());
            }

            byte[] invoiceBytes = com.greengrocer.utils.InvoiceGenerator.generateInvoice(order, user);
            if (invoiceBytes != null) {
                orderDAO.updateInvoice(order.getId(), invoiceBytes);
            }

            cart.clear();

            showSuccess("✅ Your order has been created successfully!\n\nOrder No: #" + order.getId() +
                    "\nTotal: ₺" + String.format("%.2f", total) +
                    "\n\nInvoice has been created and saved.");
            goBack();
        } catch (SQLException e) {
            showError("Error creating order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shows a confirmation dialog with the order summary.
     *
     * @return true if the user confirms the order, false otherwise.
     * 
     * @author Elif Zeynep Talay
     */
    private boolean showOrderSummary() {
        double subtotal = cart.getSubtotal();
        double discount = subtotal * (discountPercent / 100.0);
        if (appliedCoupon != null) {
            discount += appliedCoupon.calculateDiscount(subtotal);
        }
        double vat = (subtotal - discount) * 0.18;
        double total = subtotal - discount + vat;

        StringBuilder summary = new StringBuilder();
        summary.append("📦 ORDER SUMMARY\n");
        summary.append("═══════════════════════\n\n");

        for (CartItem item : cart.getItems()) {
            summary.append(String.format("• %s: %.2f x ₺%.2f = ₺%.2f\n",
                    item.getProductName(), item.getAmount(), item.getUnitPrice(), item.getTotalPrice()));
        }

        summary.append("\n═══════════════════════\n");
        summary.append(String.format("Subtotal: ₺%.2f\n", subtotal));
        if (discount > 0) {
            summary.append(String.format("Discount (%.0f%%): -₺%.2f\n", discountPercent, discount));
        }
        summary.append(String.format("VAT (18%%): ₺%.2f\n", vat));
        summary.append(String.format("\n💰 TOTAL: ₺%.2f\n", total));
        summary.append("\n🚚 Delivery: ").append(deliveryDatePicker.getValue())
                .append(" ").append(deliveryTimeCombo.getValue());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(cartItemsPane.getScene().getWindow());
        confirm.setTitle("Order Confirmation");
        confirm.setHeaderText("Do you confirm your order?");
        confirm.setContentText(summary.toString());
        confirm.getDialogPane().setMinWidth(400);

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Clears all items from the shopping cart after user confirmation.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void clearCart() {
        if (!cart.isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear the cart?");
            confirm.initOwner(cartItemsPane.getScene().getWindow());
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    cart.clear();
                    refreshCart();
                }
            });
        }
    }

    /**
     * Navigates back to the main customer view.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/customer.fxml"));
            Parent root = loader.load();
            Scene scene = cartItemsPane.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert with the specified message.
     * 
     * @param msg The error message to display.
     * @author Elif Zeynep Talay
     */
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(cartItemsPane.getScene().getWindow());
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Displays a success alert with the specified message.
     * 
     * @param msg The success message to display.
     * @author Elif Zeynep Talay
     */
    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(cartItemsPane.getScene().getWindow());
        alert.setTitle("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

}
