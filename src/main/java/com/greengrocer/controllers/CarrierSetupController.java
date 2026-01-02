package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.User;
import com.greengrocer.utils.PasswordStrengthUtil;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ThemeManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for carrier credential setup screen.
 * <p>
 * This screen is presented to a carrier when they log in for the first time
 * with temporary credentials. It forces them to set a new username and a strong
 * password.
 * </p>
 * 
 * @author Elif Zeynep Talay
 */
public class CarrierSetupController implements Initializable {

    @FXML
    private VBox mainContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private MFXTextField usernameField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXPasswordField confirmPasswordField;
    @FXML
    private MFXButton saveButton;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox passwordStrengthContainer;

    private final UserDAO userDAO = new UserDAO();
    private User currentUser;

    /**
     * Initializes the controller class.
     * <p>
     * Verifies that a user is logged in, applies the theme, pre-fills the username,
     * sets up responsive layout bindings, and initializes the password strength
     * indicator.
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
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }

        // Apply theme
        javafx.application.Platform.runLater(() -> {
            if (mainContainer != null && mainContainer.getScene() != null) {
                ThemeManager.getInstance().applyTheme(mainContainer.getScene());
            }
        });

        // Pre-fill username if available
        if (currentUser.getUsername() != null) {
            usernameField.setText(currentUser.getUsername());
        }

        // Responsive bindings
        if (mainContainer != null) {
            mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    bindResponsiveProperties(newScene);
                }
            });
        }

        // Setup password strength indicator
        if (passwordField != null && passwordStrengthContainer != null) {
            passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.isEmpty()) {
                    PasswordStrengthUtil.updatePasswordStrengthIndicator(passwordStrengthContainer, newValue);
                    passwordStrengthContainer.setVisible(true);
                } else {
                    passwordStrengthContainer.setVisible(false);
                    passwordStrengthContainer.getChildren().clear();
                }
            });
        }
    }

    /**
     * Binds layout properties to the scene size for responsiveness.
     *
     * @param scene The scene to bind properties to.
     * 
     * @author Elif Zeynep Talay
     */
    private void bindResponsiveProperties(Scene scene) {
        mainContainer.maxWidthProperty().bind(
                javafx.beans.binding.Bindings.max(450, scene.widthProperty().multiply(0.6)));

        titleLabel.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-font-size: ",
                scene.widthProperty().divide(40).asString(), "px;"));

        subtitleLabel.styleProperty().bind(javafx.beans.binding.Bindings.concat(
                "-fx-text-fill: #757575; -fx-font-size: ",
                scene.widthProperty().divide(70).asString(), "px;"));
    }

    /**
     * Handles the save action for updating credentials.
     * <p>
     * Validates inputs (non-empty, minimum length, password match, password
     * strength,
     * username uniqueness). If valid, updates the user's credentials in the
     * database
     * and navigates to the main carrier interface.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void handleSave() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required!");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters!");
            return;
        }

        // Password validation using PasswordStrengthUtil
        PasswordStrengthUtil.PasswordStrength strength = PasswordStrengthUtil.evaluatePassword(password);
        if (strength.getLevel() == PasswordStrengthUtil.StrengthLevel.WEAK) {
            showError("Password is too weak! " + strength.getMessage());
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            return;
        }

        try {
            // Check if username is unique (excluding current user)
            if (!username.equals(currentUser.getUsername()) &&
                    userDAO.findByUsername(username).isPresent()) {
                showError("This username is already taken!");
                return;
            }

            // Update credentials
            boolean success = userDAO.updateCarrierCredentials(currentUser.getId(), username, password);
            if (success) {
                // Update session
                currentUser.setUsername(username);
                SessionManager.getInstance().setCurrentUser(currentUser);

                // Navigate to carrier interface
                navigateToCarrierInterface();
            } else {
                showError("Registration failed! Please try again.");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the main carrier interface.
     * 
     * @author Elif Zeynep Talay
     */
    private void navigateToCarrierInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/carrier.fxml"));
            Parent root = loader.load();

            Scene scene = saveButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - Carrier Setup");

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {
            showError("Error loading interface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the login screen.
     * 
     * @author Elif Zeynep Talay
     */
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

    /**
     * Displays an error message to the user.
     *
     * @param message The error message to display.
     * 
     * @author Elif Zeynep Talay
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
