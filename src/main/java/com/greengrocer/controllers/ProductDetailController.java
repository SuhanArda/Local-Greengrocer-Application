package com.greengrocer.controllers;

import com.greengrocer.models.Product;
import com.greengrocer.utils.ShoppingCart;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Controller for the product detail view.
 * <p>
 * Displays detailed information about a selected product, including its image,
 * name, price, description, and stock status. Allows the user to add the
 * product
 * to their shopping cart.
 * </p>
 * 
 * @author Elif Zeynep Talay
 */
public class ProductDetailController {

    /**
     * ImageView to display the product's image.
     */
    @FXML
    private ImageView productImage;

    /**
     * Label to display the product's name.
     */
    @FXML
    private Label productName;

    /**
     * Label to display the product's price.
     */
    @FXML
    private Label productPrice;

    /**
     * Label to display the product's description.
     */
    @FXML
    private Label productDescription;

    /**
     * Label to display the product's stock status.
     */
    @FXML
    private Label stockLabel;

    /**
     * Text field for entering the quantity to purchase.
     */
    @FXML
    private MFXTextField quantityField;

    /**
     * Label to display the unit type (kg/piece).
     */
    @FXML
    private Label unitLabel;

    /**
     * Button to toggle the application theme.
     */
    @FXML
    private io.github.palexdev.materialfx.controls.MFXButton themeToggleBtn;

    /**
     * The product being displayed.
     */
    private Product product;

    /**
     * The shopping cart instance.
     */
    private final ShoppingCart cart = ShoppingCart.getInstance();

    /**
     * Sets the product to display and updates the UI.
     *
     * @param product The {@link Product} to display details for.
     * 
     * @author Elif Zeynep Talay
     */
    public void setProduct(Product product) {
        this.product = product;
        updateUI();
    }

    /**
     * Updates the UI elements with the product's information.
     * <p>
     * Sets the product name, unit type, price, description, stock, and image.
     * Handles cases where the description or image is missing.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    private void updateUI() {
        if (product == null)
            return;

        productName.setText(product.getName());

        String unit = product.getUnitType() == Product.UnitType.KG ? "kg" : "piece";
        unitLabel.setText(unit);

        double displayPrice = product.getDisplayPrice();
        productPrice.setText(String.format("₺%.2f / %s", displayPrice, unit));

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            productDescription.setText(product.getDescription());
        } else {
            productDescription.setText("No description available for this product.");
        }

        stockLabel.setText(String.format("Stock: %.1f %s", product.getStock(), unit));

        if (product.getImage() != null && product.getImage().length > 0) {
            try {
                productImage.setImage(new Image(new ByteArrayInputStream(product.getImage())));
            } catch (Exception e) {
                // Ignore
            }
        } else {
            // Set default placeholder if needed, or leave empty
        }
    }

    /**
     * Adds the selected quantity of the product to the shopping cart.
     * <p>
     * Validates the input quantity against available stock and ensures it is a
     * positive number.
     * Displays success or error messages accordingly.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void addToCart() {
        if (product == null)
            return;

        String quantityStr = quantityField.getText();
        try {
            double amount = Double.parseDouble(quantityStr);

            if (amount <= 0) {
                showError("Amount must be greater than 0!");
                return;
            }

            if (amount > product.getStock()) {
                showError("Insufficient stock! Available stock: " + product.getStock());
                return;
            }

            cart.addItem(product, amount);
            showSuccess(String.format("%.2f %s %s added to cart!", amount, unitLabel.getText(), product.getName()));

        } catch (NumberFormatException e) {
            showError("Invalid amount! Please enter a number.");
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
            Scene scene = productName.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showError("Error going back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an error alert with the specified message.
     * 
     * @param message The error message to display.
     * @author Elif Zeynep Talay
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays a success alert with the specified message.
     * 
     * @param message The success message to display.
     * @author Elif Zeynep Talay
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
