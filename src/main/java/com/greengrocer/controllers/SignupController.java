package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.User;
import com.greengrocer.utils.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the signup/profile completion screen.
 * <p>
 * Handles the completion of user profiles after initial registration.
 * Manages input validation for address, phone, and email fields, ensures data
 * uniqueness,
 * and navigates the user to the appropriate dashboard upon successful
 * completion.
 * </p>
 * 
 * @author Burak Özevin
 */
public class SignupController implements Initializable {

    @FXML
    private javafx.scene.layout.VBox mainContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private javafx.scene.layout.VBox addressBox;
    @FXML
    private Label addressLabel;
    @FXML
    private javafx.scene.control.TextArea addressField;
    @FXML
    private javafx.scene.layout.VBox phoneBox;
    @FXML
    private Label phoneLabel;
    @FXML
    private MFXTextField phoneField;
    @FXML
    private javafx.scene.layout.VBox emailBox;
    @FXML
    private Label emailLabel;
    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXButton saveButton;
    @FXML
    private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    /**
     * Initializes the controller class.
     * <p>
     * Checks if a user is logged in, pre-fills existing user information,
     * sets up character limits for text fields, and binds responsive layout
     * properties.
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
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        // Pre-fill fields
        if (currentUser.getAddress() != null)
            addressField.setText(currentUser.getAddress());
        if (currentUser.getPhone() != null)
            phoneField.setText(currentUser.getPhone());
        if (currentUser.getEmail() != null)
            emailField.setText(currentUser.getEmail());

        // Character limit
        addressField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() > 255)
                addressField.setText(old);
        });

        // Responsive Bindings
        if (mainContainer != null && mainContainer.sceneProperty() != null) {
            // Wait for scene to be available
            mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    bindResponsiveProperties(newScene);
                }
            });
        }
    }

    /**
     * Binds responsive layout properties to the scene size.
     * <p>
     * Adjusts the container width and font sizes dynamically based on the window
     * width.
     * </p>
     *
     * @param scene The {@link Scene} to bind properties to.
     * @author Burak Özevin
     */
    private void bindResponsiveProperties(Scene scene) {
        // Bind container max width to 60% of window width (min 450px)
        mainContainer.maxWidthProperty().bind(
                javafx.beans.binding.Bindings.max(450, scene.widthProperty().multiply(0.6)));

        // Bind font sizes
        titleLabel.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: ",
                scene.widthProperty().divide(40).asString(), "px;"));

        subtitleLabel.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-text-fill: #757575; -fx-font-size: ",
                scene.widthProperty().divide(70).asString(), "px;"));

        String fieldLabelStyle = "-fx-font-weight: bold; -fx-text-fill: #424242; -fx-font-size: ";
        javafx.beans.binding.StringExpression labelSize = javafx.beans.binding.Bindings.concat(
                fieldLabelStyle, scene.widthProperty().divide(80).asString(), "px;");
        addressLabel.styleProperty().bind(labelSize);
        phoneLabel.styleProperty().bind(labelSize);
        emailLabel.styleProperty().bind(labelSize);
    }

    /**
     * Handles the save button click event.
     * <p>
     * Validates all input fields (address, phone, email).
     * Checks for uniqueness of phone and email in the database.
     * Updates the user's profile and navigates to the main interface upon success.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void handleSave() {
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showError("All fields are required!");
            return;
        }

        // Validate phone number (10 digits starting with 5)
        // Remove spaces for validation
        String cleanPhone = phone.replaceAll("\\s+", "");
        if (!cleanPhone.matches("^5[0-9]{9}$")) {
            showError("Invalid phone number! (Example: 5XX XXX XX XX)");
            return;
        }

        // Validate Email
        if (email.length() > 100) {
            showError("Email address is too long! (Max 100 characters)");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showError("Invalid email address!");
            return;
        }

        try {
            // Check Uniqueness
            if (userDAO.isPhoneExists(cleanPhone, currentUser.getId())) {
                showError("This phone number is already used by another user!");
                return;
            }
            if (userDAO.isEmailExists(email, currentUser.getId())) {
                showError("This email address is already used by another user!");
                return;
            }

            currentUser.setAddress(address);
            currentUser.setPhone(cleanPhone); // Save cleaned phone number
            currentUser.setEmail(email);

            if (userDAO.update(currentUser)) {
                SessionManager.getInstance().setCurrentUser(currentUser); // Update session
                navigateToUserInterface(currentUser);
            } else {
                showError("Save failed! Please try again.");
            }
        } catch (SQLException e) {
            showError("Database error during save: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates the user to the appropriate interface based on their role.
     *
     * @param user The {@link User} to navigate.
     * @author Burak Özevin
     */
    private void navigateToUserInterface(User user) {
        String fxmlPath;
        String title = "Group06 GreenGrocer";

        switch (user.getRole()) {
            case CUSTOMER:
                fxmlPath = "/views/customer.fxml";
                title += " - Customer";
                break;
            case CARRIER:
                fxmlPath = "/views/carrier.fxml";
                title += " - Carrier";
                break;
            case OWNER:
                fxmlPath = "/views/owner.fxml";
                title += " - Owner";
                break;
            default:
                showError("Unknown user role!");
                return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = saveButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle(title);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {
            showError("Error loading interface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = saveButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
