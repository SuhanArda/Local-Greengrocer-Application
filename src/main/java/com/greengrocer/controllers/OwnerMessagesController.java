package com.greengrocer.controllers;

import com.greengrocer.dao.MessageDAO;
import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.Message;
import com.greengrocer.models.User;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ThemeManager;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for Owner's WhatsApp-style messaging interface.
 * <p>
 * Manages the chat functionality for the store owner, allowing them to
 * communicate
 * with customers. Handles contact list management, message history, and
 * sending/receiving messages.
 * </p>
 * 
 * @author Burak Özevin
 */
public class OwnerMessagesController implements Initializable {

    /**
     * Button to navigate back to the previous screen.
     */
    @FXML
    private MFXButton backBtn;

    /**
     * Text field for searching contacts.
     */
    @FXML
    private javafx.scene.control.TextField searchField;

    /**
     * Container for the list of contacts.
     */
    @FXML
    private VBox contactsList;

    /**
     * Avatar image of the selected contact.
     */
    @FXML
    private Circle contactAvatar;

    /**
     * Label displaying the name of the selected contact.
     */
    @FXML
    private Label contactNameLabel;

    /**
     * Label displaying the status of the selected contact.
     */
    @FXML
    private Label contactStatusLabel;

    /**
     * Button to toggle between light and dark themes.
     */
    @FXML
    private MFXButton themeToggleBtn;

    /**
     * Scroll pane for the messages area.
     */
    @FXML
    private ScrollPane messagesScrollPane;

    /**
     * Container for the message bubbles.
     */
    @FXML
    private VBox messagesContainer;

    /**
     * Text area for typing new messages.
     */
    @FXML
    private TextArea messageInput;

    /**
     * Button to send the message.
     */
    @FXML
    private MFXButton sendBtn;

    /**
     * Data Access Object for message-related operations.
     */
    private final MessageDAO messageDAO;

    /**
     * Data Access Object for user-related operations.
     */
    private final UserDAO userDAO;

    /**
     * The currently logged-in user (Owner).
     */
    private User currentUser;

    /**
     * The currently selected customer for chatting.
     */
    private User selectedContact;

    /**
     * List of all customers who have messaged the owner.
     */
    private List<User> allCustomers = new ArrayList<>();

    /**
     * Default constructor.
     * <p>
     * Initializes the Data Access Objects.
     * </p>
     */
    public OwnerMessagesController() {
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the current user, applies the theme, initializes event handlers,
     * and loads the customer contact list.
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

        // Apply theme when scene is ready
        javafx.application.Platform.runLater(() -> {
            if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
                ThemeManager.getInstance().applyTheme(themeToggleBtn.getScene());
                updateThemeIcon();
            }
        });

        // Set up theme toggle button
        themeToggleBtn.setOnAction(event -> handleThemeToggle());

        setupEventHandlers();
        loadContacts();
    }

    /**
     * Toggles the application theme between light and dark modes.
     * <p>
     * Updates the theme manager and the theme toggle button icon.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void handleThemeToggle() {
        if (themeToggleBtn != null && themeToggleBtn.getScene() != null) {
            ThemeManager.getInstance().toggleTheme(themeToggleBtn.getScene());
            updateThemeIcon();
        }
    }

    /**
     * Updates the icon of the theme toggle button based on the current theme.
     * 
     * @author Burak Özevin
     */
    private void updateThemeIcon() {
        if (themeToggleBtn != null) {
            boolean isDark = ThemeManager.getInstance().isDarkTheme();
            String iconLiteral = isDark ? "fas-sun" : "fas-moon";
            String color = isDark ? "#ffeb3b" : "#333333";

            FontIcon icon = new FontIcon(iconLiteral);
            icon.setIconSize(20);
            icon.setIconColor(javafx.scene.paint.Color.web(color));

            themeToggleBtn.setGraphic(icon);
            themeToggleBtn.setText("");
        }
    }

    /**
     * Sets up event handlers for UI components.
     * <p>
     * Configures actions for the back button, send button, and search field.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void setupEventHandlers() {
        backBtn.setOnAction(event -> navigateBack());
        sendBtn.setOnAction(event -> sendMessage());

        // Search filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterContacts(newValue));
    }

    /**
     * Loads the list of customers who have messaged the owner.
     * <p>
     * Fetches received messages, extracts unique sender IDs, and creates contact
     * items
     * for each customer.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void loadContacts() {
        try {
            // Get all messages received by the owner
            List<Message> receivedMessages = messageDAO.findReceivedMessages(currentUser.getId());

            // Extract unique sender IDs (customers who have messaged the owner)
            Set<Integer> customerIds = receivedMessages.stream()
                    .map(Message::getSenderId)
                    .collect(Collectors.toSet());

            allCustomers.clear();
            contactsList.getChildren().clear();

            for (Integer id : customerIds) {
                User customer = userDAO.findById(id).orElse(null);
                if (customer != null && customer.getRole() == User.UserRole.CUSTOMER) {
                    allCustomers.add(customer);
                    createContactItem(customer);
                }
            }

            // If no customers have messaged, show a placeholder message
            if (allCustomers.isEmpty()) {
                Label noCustomersLabel = new Label("No customer messages yet");
                noCustomersLabel.setStyle("-fx-text-fill: #888; -fx-padding: 20;");
                contactsList.getChildren().add(noCustomersLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a visual item for a contact and adds it to the contact list.
     *
     * @param contact The {@link User} object representing the contact.
     * @author Burak Özevin
     */
    private void createContactItem(User contact) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("contact-item");
        item.setPadding(new Insets(10));

        Circle avatar = new Circle(20);
        avatar.getStyleClass().add("contact-avatar");

        VBox info = new VBox(2);
        Label name = new Label(contact.getFullName());
        name.getStyleClass().add("contact-name");

        // Show unread message count
        int unreadCount = getUnreadCount(contact.getId());
        String lastMsgText = unreadCount > 0 ? "📩 " + unreadCount + " new messages" : "Click to chat";
        Label lastMsg = new Label(lastMsgText);
        lastMsg.getStyleClass().add("contact-last-msg");

        info.getChildren().addAll(name, lastMsg);
        item.getChildren().addAll(avatar, info);

        item.setOnMouseClicked(e -> selectContact(contact));

        contactsList.getChildren().add(item);
    }

    /**
     * Calculates the number of unread messages from a specific sender.
     *
     * @param senderId The ID of the sender.
     * @return The count of unread messages.
     * @author Burak Özevin
     */
    private int getUnreadCount(int senderId) {
        try {
            List<Message> received = messageDAO.findReceivedMessages(currentUser.getId());
            return (int) received.stream()
                    .filter(m -> m.getSenderId() == senderId && !m.isRead())
                    .count();
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Selects a customer contact and loads the conversation history.
     *
     * @param contact The customer {@link User} to chat with.
     * @author Burak Özevin
     */
    private void selectContact(User contact) {
        this.selectedContact = contact;
        contactNameLabel.setText(contact.getFullName());
        contactStatusLabel.setText("Customer");
        contactAvatar.setVisible(true);

        loadMessages(contact);
    }

    /**
     * Loads messages exchanged with the selected customer.
     * <p>
     * Fetches both sent and received messages, filters them for the specific
     * customer,
     * marks unread messages as read, sorts them by timestamp, and displays them.
     * </p>
     *
     * @param contact The customer to load messages for.
     * @author Burak Özevin
     */
    private void loadMessages(User contact) {
        messagesContainer.getChildren().clear();
        try {
            List<Message> received = messageDAO.findReceivedMessages(currentUser.getId());
            List<Message> sent = messageDAO.findReceivedMessages(contact.getId());

            // Messages sent by current user (owner) to this customer
            List<Message> actualSent = sent.stream()
                    .filter(m -> m.getSenderId() == currentUser.getId())
                    .collect(Collectors.toList());

            // Messages received from this customer
            List<Message> actualReceived = received.stream()
                    .filter(m -> m.getSenderId() == contact.getId())
                    .collect(Collectors.toList());

            // Mark as read
            for (Message m : actualReceived) {
                if (!m.isRead()) {
                    messageDAO.markAsRead(m.getId());
                }
            }

            // Combine and sort by time
            List<Message> conversation = new ArrayList<>();
            conversation.addAll(actualSent);
            conversation.addAll(actualReceived);
            conversation.sort(Comparator.comparing(Message::getCreatedAt));

            for (Message msg : conversation) {
                addMessageBubble(msg);
            }

            // Scroll to bottom
            javafx.application.Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single message bubble to the chat interface.
     *
     * @param msg The {@link Message} to display.
     * @author Burak Özevin
     */
    private void addMessageBubble(Message msg) {
        boolean isMe = msg.getSenderId() == currentUser.getId();

        HBox bubbleContainer = new HBox();
        bubbleContainer.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubbleContainer.getStyleClass().add("message-row");

        VBox bubble = new VBox(5);
        bubble.getStyleClass().add(isMe ? "message-bubble-sent" : "message-bubble-received");
        bubble.setMaxWidth(400);

        Text text = new Text(msg.getContent());
        text.getStyleClass().add("message-text");
        text.getStyleClass().add(isMe ? "message-text-sent" : "message-text-received");
        TextFlow textFlow = new TextFlow(text);

        Label time = new Label(msg.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
        time.getStyleClass().add("message-time");
        time.getStyleClass().add(isMe ? "message-time-sent" : "message-time-received");
        time.setAlignment(Pos.BOTTOM_RIGHT);

        bubble.getChildren().addAll(textFlow, time);
        bubbleContainer.getChildren().add(bubble);

        messagesContainer.getChildren().add(bubbleContainer);
    }

    /**
     * Sends a new message to the selected customer.
     * <p>
     * Creates a new {@link Message} object, saves it to the database,
     * and updates the UI.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || selectedContact == null)
            return;

        Message msg = new Message();
        msg.setSenderId(currentUser.getId());
        msg.setReceiverId(selectedContact.getId());
        msg.setContent(content);
        msg.setSubject("Chat");
        msg.setRead(false);
        msg.setCreatedAt(LocalDateTime.now());

        try {
            messageDAO.create(msg);
            addMessageBubble(msg);
            messageInput.clear();
            javafx.application.Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filters the contact list based on the search query.
     *
     * @param query The search text to filter contacts by.
     * @author Burak Özevin
     */
    private void filterContacts(String query) {
        contactsList.getChildren().clear();
        String lowerQuery = query.toLowerCase().trim();

        if (lowerQuery.isEmpty()) {
            for (User customer : allCustomers) {
                createContactItem(customer);
            }
        } else {
            for (User customer : allCustomers) {
                if (customer.getFullName().toLowerCase().contains(lowerQuery)) {
                    createContactItem(customer);
                }
            }
        }
    }

    /**
     * Navigates back to the owner dashboard.
     * 
     * @author Burak Özevin
     */
    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/owner.fxml"));
            Parent root = loader.load();
            backBtn.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
