package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.Customer;
import com.greengrocer.models.User;
import com.greengrocer.models.User.UserRole;
import com.greengrocer.utils.PasswordStrengthUtil;
import com.greengrocer.utils.SessionManager;
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
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the login and registration screen.
 * <p>
 * Manages user authentication, registration of new customers, and navigation
 * to different user interfaces based on roles (Customer, Carrier, Owner).
 * Also handles password strength validation and theme toggling.
 * </p>
 * 
 * @author Burak Özevin
 */
public class LoginController implements Initializable {

    // Login fields
    @FXML
    private MFXTextField usernameField;
    @FXML
    private MFXPasswordField passwordField;
    @FXML
    private MFXButton loginButton;
    @FXML
    private Label errorLabel;
    @FXML
    private TabPane tabPane;

    // Registration fields
    @FXML
    private MFXTextField regUsernameField;
    @FXML
    private MFXPasswordField regPasswordField;
    @FXML
    private MFXPasswordField regPasswordConfirmField;
    @FXML
    private MFXTextField regFullNameField;
    @FXML
    private MFXButton registerButton;
    @FXML
    private Label regErrorLabel;
    @FXML
    private Label regSuccessLabel;
    @FXML
    private VBox passwordStrengthContainer;

    // Responsive Text Fields
    @FXML
    private javafx.scene.layout.StackPane leftPanel;
    @FXML
    private Label sloganLabel;
    @FXML
    private Label subtextLabel;

    @FXML
    private MFXButton themeToggleBtn;

    private final UserDAO userDAO = new UserDAO();

    /**
     * Initializes the controller class.
     * <p>
     * Sets up event handlers for keyboard shortcuts (Enter key), binds responsive
     * text styles,
     * and initializes the password strength indicator listener.
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
        // Initialize Theme
        com.greengrocer.utils.ThemeManager.getInstance().initializeTheme(themeToggleBtn);

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                handleLogin();
        });
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER)
                passwordField.requestFocus();
        });

        // Responsive Text Binding
        if (leftPanel != null && sloganLabel != null && subtextLabel != null) {
            sloganLabel.styleProperty().bind(
                    javafx.beans.binding.Bindings.concat("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: ",
                            leftPanel.widthProperty().divide(10).asString(), "px;"));
            subtextLabel.styleProperty().bind(
                    javafx.beans.binding.Bindings.concat("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: ",
                            leftPanel.widthProperty().divide(25).asString(), "px;"));
        }

        // Bind managed property to visible property to reclaim space when hidden
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        regErrorLabel.managedProperty().bind(regErrorLabel.visibleProperty());
        regSuccessLabel.managedProperty().bind(regSuccessLabel.visibleProperty());

        // Setup password strength indicator for registration
        if (regPasswordField != null && passwordStrengthContainer != null) {
            regPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
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
     * Handles the login process.
     * <p>
     * Validates input fields, attempts to authenticate the user via
     * {@link UserDAO},
     * and navigates to the appropriate interface upon success.
     * Also checks for temporary carrier passwords.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required!");
            return;
        }

        try {
            // Try normal authentication first
            Optional<User> userOpt = userDAO.authenticate(username, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                SessionManager.getInstance().setCurrentUser(user);
                navigateToUserInterface(user);
            } else {
                // Try carrier temp password authentication
                Optional<User> carrierOpt = userDAO.authenticateCarrierTemp(username, password);
                if (carrierOpt.isPresent()) {
                    User carrier = carrierOpt.get();
                    SessionManager.getInstance().setCurrentUser(carrier);
                    // Redirect to carrier setup screen
                    navigateToCarrierSetup();
                } else {
                    showError("Invalid username or password!");
                    passwordField.clear();
                }
            }
        } catch (SQLException e) {
            showError("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the carrier setup screen.
     * <p>
     * Used when a carrier logs in with a temporary password and needs to set up
     * their profile.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void navigateToCarrierSetup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/carrier_setup.fxml"));
            Parent root = loader.load();
            Scene scene = loginButton.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - Carrier Setup");

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (IOException e) {
            showError("Error loading carrier setup page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the user registration process.
     * <p>
     * Validates registration fields, checks for username uniqueness, validates
     * password strength,
     * and creates a new {@link Customer} account.
     * </p>
     * 
     * @author Burak Özevin
     */
    @FXML
    private void handleRegister() {
        regErrorLabel.setVisible(false);
        regSuccessLabel.setVisible(false);

        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText().trim();
        String passwordConfirm = regPasswordConfirmField.getText().trim();
        String fullName = regFullNameField.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showRegError("* All fields are required!");
            return;
        }

        if (username.length() < 3) {
            showRegError("Username must be at least 3 characters!");
            return;
        }

        try {
            // Check unique username
            if (userDAO.authenticate(username, password).isPresent() || userDAO.findByUsername(username).isPresent()) {
                showRegError("This username is already in use!");
                return;
            }

            // Password strength check relaxed
            PasswordStrengthUtil.PasswordStrength strength = PasswordStrengthUtil.evaluatePassword(password);

            if (password.length() < 3) {
                showRegError("Password must be at least 3 characters long!");
                return;
            }
            if (!password.equals(passwordConfirm)) {
                showRegError("Passwords do not match!");
                return;
            }

            // Create user
            Customer newUser = new Customer();
            newUser.setUsername(username);
            newUser.setPassword(password);
            newUser.setRole(UserRole.CUSTOMER);
            newUser.setFullName(fullName);
            newUser.setAddress(""); // Default empty
            newUser.setPhone(""); // Default empty
            newUser.setEmail(""); // Default empty
            newUser.setActive(true);
            newUser.setLoyaltyPoints(0);
            newUser.setTotalOrders(0);

            User created = userDAO.create(newUser);
            if (created.getId() > 0) {
                showRegSuccess("Registration successful! You can now login.");
                clearRegFields();
                tabPane.getSelectionModel().select(0); // Switch to login tab
            } else {
                showRegError("Registration failed! Please try again.");
            }
        } catch (SQLException e) {
            showRegError("Database error during registration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all registration input fields.
     * 
     * @author Burak Özevin
     */
    private void clearRegFields() {
        regUsernameField.clear();
        regPasswordField.clear();
        regPasswordConfirmField.clear();
        regFullNameField.clear();
    }

    /**
     * Navigates to the appropriate user interface based on the user's role.
     * <p>
     * If the user's profile is incomplete (missing address, phone, or email),
     * redirects to the profile completion screen.
     * </p>
     *
     * @param user The logged-in {@link User}.
     * 
     * @author Burak Özevin
     */
    private void navigateToUserInterface(User user) {
        // Check if profile is complete (Address, Phone, Email)
        if (user.getAddress() == null || user.getAddress().isEmpty() ||
                user.getPhone() == null || user.getPhone().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty()) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/signup.fxml"));
                Parent root = loader.load();
                Scene scene = loginButton.getScene();
                Stage stage = (Stage) scene.getWindow();
                scene.setRoot(root);
                stage.setTitle("GreenGrocer - Complete Profile");
                return;
            } catch (IOException e) {
                showError("Error loading interface: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        String fxmlPath;
        String title = "GroupXX GreenGrocer";

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

            Scene scene = loginButton.getScene();
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

    /**
     * Switches the view to the registration tab with an animation.
     * 
     * @author Burak Özevin
     */
    @FXML
    private void switchToRegister() {
        tabPane.getSelectionModel().select(1);
        animateTabChange(tabPane.getTabs().get(1).getContent());
    }

    /**
     * Switches the view to the login tab with an animation.
     * 
     * @author Burak Özevin
     */
    @FXML
    private void switchToLogin() {
        tabPane.getSelectionModel().select(0);
        animateTabChange(tabPane.getTabs().get(0).getContent());
    }

    /**
     * Applies a fade-in animation to the specified node.
     *
     * @param node The node to animate.
     * 
     * @author Burak Özevin
     */
    private void animateTabChange(javafx.scene.Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    /**
     * Displays an error message on the login screen.
     *
     * @param message The error message to display.
     * 
     * @author Burak Özevin
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Displays an error message on the registration screen.
     *
     * @param message The error message to display.
     * 
     * @author Burak Özevin
     */
    private void showRegError(String message) {
        regErrorLabel.setText(message);
        regErrorLabel.setVisible(true);
        regSuccessLabel.setVisible(false);
    }

    /**
     * Displays a success message on the registration screen.
     *
     * @param message The success message to display.
     * 
     * @author Burak Özevin
     */
    private void showRegSuccess(String message) {
        regSuccessLabel.setText(message);
        regSuccessLabel.setVisible(true);
        regErrorLabel.setVisible(false);
    }
}
