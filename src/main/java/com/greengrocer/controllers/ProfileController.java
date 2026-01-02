package com.greengrocer.controllers;

import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.User;
import com.greengrocer.utils.Argon2Hasher;
import com.greengrocer.utils.PasswordStrengthUtil;
import com.greengrocer.utils.SessionManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import com.greengrocer.dao.OrderDAO;
import com.greengrocer.models.Order;
import com.greengrocer.models.Order.OrderStatus;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Controller for the user profile management interface.
 * <p>
 * Allows users to view and update their personal information (name, email,
 * phone, address)
 * and change their password. Handles input validation, address district
 * selection,
 * and password strength checking.
 * </p>
 * 
 * @author Burak Özevin, Suhan Arda Öner
 */
public class ProfileController implements Initializable {

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
    private MFXComboBox<String> cityCombo;
    @FXML
    private MFXComboBox<String> districtCombo;
    @FXML
    private MFXTextField addressDetailsField;

    @FXML
    private MFXPasswordField oldPasswordField;
    @FXML
    private MFXPasswordField newPasswordField;
    @FXML
    private MFXPasswordField confirmPasswordField;
    @FXML
    private VBox passwordStrengthContainer;

    private final UserDAO userDAO = new UserDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final Argon2Hasher hasher = new Argon2Hasher();
    private User currentUser;

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the current user, initializes address combo boxes, loads user
     * information,
     * and sets up the password strength indicator listener.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = SessionManager.getInstance().getCurrentUser();
        setupAddressFields(); // Initialize combos first

        loadUserInfo();
        showUserInfo(); // Default view

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
     * Configures the address selection fields.
     * <p>
     * Populates the city combo box (currently defaults to Istanbul) and sets up
     * a listener to update the district combo box when the city changes.
     * </p>
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    private void setupAddressFields() {
        if (cityCombo != null) {
            cityCombo.setItems(FXCollections.observableArrayList("Istanbul"));
            // Do not select default here if we are loading user info,
            // but for a new user or fallback it might be okay.
            // loadUserInfo will override checks.

            // Populate districts
            updateDistricts("Istanbul");

            cityCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    updateDistricts(newVal);
                }
            });
        }
    }

    /**
     * Updates the district combo box based on the selected city.
     *
     * @param city The selected city name.
     * @author Burak Özevin, Suhan Arda Öner
     */
    private void updateDistricts(String city) {
        if (districtCombo == null)
            return;

        if ("Istanbul".equals(city)) {
            districtCombo.setItems(FXCollections.observableArrayList(
                    "Adalar", "Arnavutköy", "Ataşehir", "Avcılar", "Bağcılar", "Bahçelievler",
                    "Bakırköy", "Başakşehir", "Bayrampaşa", "Beşiktaş", "Beykoz", "Beylikdüzü",
                    "Beyoğlu", "Büyükçekmece", "Çatalca", "Çekmeköy", "Esenler", "Esenyurt",
                    "Eyüpsultan", "Fatih", "Gaziosmanpaşa", "Güngören", "Kadıköy", "Kağıthane",
                    "Kartal", "Küçükçekmece", "Maltepe", "Pendik", "Sancaktepe", "Sarıyer",
                    "Silivri", "Sultanbeyli", "Sultangazi", "Şile", "Şişli", "Tuzla",
                    "Ümraniye", "Üsküdar", "Zeytinburnu"));
        } else {
            districtCombo.getItems().clear();
        }
    }

    /**
     * Loads the current user's information into the UI fields.
     * <p>
     * Populates name, email, phone, and parses the address string to fill
     * the city, district, and details fields.
     * </p>
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    private void loadUserInfo() {
        if (currentUser != null) {
            nameField.setText(currentUser.getFullName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());

            String fullAddress = currentUser.getAddress();
            if (fullAddress != null && !fullAddress.isEmpty()) {
                String[] parts = fullAddress.split(" / ");
                if (parts.length >= 3) {
                    String city = parts[0];
                    String district = parts[1];

                    // Safely select city
                    if (cityCombo.getItems().contains(city)) {
                        cityCombo.getSelectionModel().selectItem(city);
                    } else {
                        // Fallback or add to items? For now, default to Istanbul if not found
                        cityCombo.getSelectionModel().selectItem("Istanbul");
                    }

                    // Update districts based on selected city (should happen via listener, but
                    // ensure it)
                    // If listener hasn't fired yet or we need immediate update:
                    if (districtCombo.getItems().isEmpty()) {
                        updateDistricts(cityCombo.getValue());
                    }

                    // Safely select district
                    if (districtCombo.getItems().contains(district)) {
                        districtCombo.getSelectionModel().selectItem(district);
                    }

                    // Re-join the details
                    StringBuilder details = new StringBuilder();
                    for (int i = 2; i < parts.length; i++) {
                        if (i > 2)
                            details.append(" / ");
                        details.append(parts[i]);
                    }
                    addressDetailsField.setText(details.toString());
                } else {
                    // Legacy or simple format
                    addressDetailsField.setText(fullAddress);
                    cityCombo.getSelectionModel().selectItem("Istanbul");
                }
            } else {
                // No address, default to Istanbul
                cityCombo.getSelectionModel().selectItem("Istanbul");
            }
        }
    }

    /**
     * Switches the view to the user information panel.
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    @FXML
    private void showUserInfo() {
        userInfoPane.setVisible(true);
        passwordPane.setVisible(false);
        setActiveButton(userInfoBtn);
    }

    /**
     * Switches the view to the password change panel.
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    @FXML
    private void showPasswordChange() {
        userInfoPane.setVisible(false);
        passwordPane.setVisible(true);
        setActiveButton(passwordBtn);
    }

    private void setActiveButton(MFXButton button) {
        userInfoBtn.getStyleClass().remove("sidebar-button-active");
        passwordBtn.getStyleClass().remove("sidebar-button-active");
        button.getStyleClass().add("sidebar-button-active");
    }

    /**
     * Saves the updated user information to the database.
     * <p>
     * Validates input fields, checks if address change is allowed (no active
     * orders),
     * constructs the full address string, and updates the user record.
     * </p>
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    @FXML
    private void saveUserInfo() {
        if (currentUser == null)
            return;

        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        String city = cityCombo.getValue();
        String district = districtCombo.getValue();
        String details = addressDetailsField.getText();

        if (city == null || city.isEmpty() || district == null || district.isEmpty() || details.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all address fields!");
            return;
        }

        String address = city + " / " + district + " / " + details;

        // Check if address changed
        String currentAddress = currentUser.getAddress();
        if (currentAddress != null && !currentAddress.equals(address)) {
            // Address changed, check for active orders
            try {
                List<Order> activeOrders = orderDAO.findByCustomer(currentUser.getId());
                boolean hasActiveOrders = activeOrders.stream()
                        .anyMatch(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.SELECTED);

                if (hasActiveOrders) {
                    showAlert(Alert.AlertType.ERROR, "Address Change Restricted",
                            "You cannot change your address while you have active orders (Pending or On the Way).");
                    return;
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not verify active orders: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name and email fields cannot be empty!");
            return;
        }

        currentUser.setFullName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);

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
     * Verifies the old password, checks if new passwords match, validates password
     * strength,
     * and updates the password in the database using Argon2 hashing.
     * </p>
     * 
     * @author Burak Özevin, Suhan Arda Öner
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
            // Update local user object hash as well if needed, or just rely on next login
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
     * Logs out the current user and redirects to the login screen.
     * 
     * @author Burak Özevin, Suhan Arda Öner
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
     * Navigates back to the appropriate dashboard based on the user's role.
     * 
     * @author Burak Özevin, Suhan Arda Öner
     */
    @FXML
    private void goBack() {
        try {
            String viewPath = "/views/customer.fxml";
            String title = "GreenGrocer - Customer Dashboard";

            if (currentUser != null && currentUser.getRole() == User.UserRole.OWNER) {
                viewPath = "/views/owner.fxml";
                title = "GreenGrocer - Owner Dashboard";
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            Parent root = loader.load();
            Scene scene = userInfoBtn.getScene();
            Stage stage = (Stage) scene.getWindow();
            scene.setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not return to the home page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(userInfoPane.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
