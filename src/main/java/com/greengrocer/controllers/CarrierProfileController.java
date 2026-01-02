package com.greengrocer.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.User;
import com.greengrocer.utils.Argon2Hasher;
import com.greengrocer.utils.PasswordStrengthUtil;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ThemeManager;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the carrier profile screen.
 * <p>
 * Allows carriers to view and update their personal information (name, email,
 * phone)
 * and change their password.
 * </p>
 * 
 * @author Elif Zeynep Talay
 */
public class CarrierProfileController implements Initializable {

    @FXML
    private MFXButton userInfoBtn;
    @FXML
    private MFXButton passwordBtn;

    @FXML
    private VBox userInfoPane;
    @FXML
    private VBox passwordPane;

    @FXML
    private MFXTextField nameField;
    @FXML
    private MFXTextField emailField;
    @FXML
    private MFXTextField phoneField;

    @FXML
    private MFXPasswordField oldPasswordField;
    @FXML
    private MFXPasswordField newPasswordField;
    @FXML
    private MFXPasswordField confirmPasswordField;
    @FXML
    private VBox passwordStrengthContainer;

    private final UserDAO userDAO = new UserDAO();
    private final Argon2Hasher hasher = new Argon2Hasher();
    private User currentUser;

    /**
     * Initializes the controller class.
     * <p>
     * Loads the current user's information, sets up the initial view (User Info),
     * initializes the theme, and sets up the password strength indicator listener.
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
        loadUserInfo();
        showUserInfo();

        // Apply theme
        javafx.application.Platform.runLater(() -> {
            if (userInfoPane != null && userInfoPane.getScene() != null) {
                ThemeManager.getInstance().applyTheme(userInfoPane.getScene());
            }
        });

        // Setup password strength indicator
        if (newPasswordField != null && passwordStrengthContainer != null) {
            newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
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
     * Loads the current user's information into the input fields.
     * 
     * @author Elif Zeynep Talay
     */
    private void loadUserInfo() {
        if (currentUser != null) {
            nameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
        }
    }

    /**
     * Switches the view to the User Information tab.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void showUserInfo() {
        userInfoPane.setVisible(true);
        passwordPane.setVisible(false);
        setActiveButton(userInfoBtn);
    }

    /**
     * Switches the view to the Password Change tab.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void showPasswordChange() {
        userInfoPane.setVisible(false);
        passwordPane.setVisible(true);
        setActiveButton(passwordBtn);
    }

    /**
     * Sets the specified button as active in the sidebar.
     *
     * @param button The button to set as active.
     * 
     * @author Elif Zeynep Talay
     */
    private void setActiveButton(MFXButton button) {
        userInfoBtn.getStyleClass().remove("sidebar-button-active");
        passwordBtn.getStyleClass().remove("sidebar-button-active");
        button.getStyleClass().add("sidebar-button-active");
    }

    /**
     * Saves the updated user information to the database.
     * <p>
     * Validates that name and email are not empty before updating.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void saveUserInfo() {
        if (currentUser == null)
            return;

        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name and email fields cannot be empty!");
            return;
        }

        currentUser.setFullName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        try {
            userDAO.update(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Your information has been updated.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Update failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the password change process.
     * <p>
     * Validates the old password, checks that new passwords match, and enforces
     * password strength requirements before updating the password in the database.
     * </p>
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void changePassword() {
        if (currentUser == null)
            return;

        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields!");
            return;
        }

        if (!hasher.verify(oldPass.toCharArray(), currentUser.getPassword())) {
            showAlert(Alert.AlertType.ERROR, "Error", "Your current password is incorrect!");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Error", "New passwords do not match!");
            return;
        }

        // Check password strength
        PasswordStrengthUtil.PasswordStrength strength = PasswordStrengthUtil.evaluatePassword(newPass);
        if (strength.getLevel() == PasswordStrengthUtil.StrengthLevel.WEAK) {
            showAlert(Alert.AlertType.ERROR, "Weak Password", "Password is too weak! " + strength.getMessage());
            return;
        }

        try {
            userDAO.updatePassword(currentUser.getId(), newPass);
            currentUser.setPassword(hasher.hash(newPass.toCharArray()));

            showAlert(Alert.AlertType.INFORMATION, "Success", "Your password has been changed successfully.");
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password could not be changed: " + e.getMessage());
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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
            Stage stage = (Stage) userInfoBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates back to the main carrier dashboard.
     * 
     * @author Elif Zeynep Talay
     */
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/carrier.fxml"));
            Parent root = loader.load();
            Scene scene = userInfoBtn.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle("GreenGrocer - Carrier");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not return to the home page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays an alert with the specified type, title, and content.
     *
     * @param type    The type of alert to display.
     * @param title   The title of the alert.
     * @param content The content text of the alert.
     * 
     * @author Elif Zeynep Talay
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(userInfoPane.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
