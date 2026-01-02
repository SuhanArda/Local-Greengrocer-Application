package com.greengrocer.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import com.greengrocer.dao.MessageDAO;
import com.greengrocer.dao.UserDAO;
import com.greengrocer.models.Message;
import com.greengrocer.models.User;
import com.greengrocer.utils.SessionManager;
import com.greengrocer.utils.ThemeManager;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Controller for the messaging interface.
 * <p>
 * Manages the chat functionality between users (e.g., Customer and Owner).
 * Handles displaying contact lists, sending/receiving messages, and real-time
 * updates.
 * </p>
 * 
 * @author Burak Özevin
 */
public class MessagesController implements Initializable {

    /**
     * Button to navigate back to the previous screen.
     */
    @FXML
    private MFXButton backBtn;

    /**
     * Text field for searching contacts.
     */
    @FXML
    private MFXTextField searchField;

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
    private ScrollPane messagesScrollPane; // Changed to standard ScrollPane for setVValue

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
     * The currently logged-in user.
     */
    private User currentUser;

    /**
     * The currently selected contact for chatting.
     */
    private User selectedContact;

    /**
     * Default constructor.
     * <p>
     * Initializes the Data Access Objects.
     * </p>
     */
    public MessagesController() {
        this.messageDAO = new MessageDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Initializes the controller class.
     * <p>
     * Sets up the current user, applies the theme, initializes event handlers,
     * and loads the contact list.
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

            org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon(iconLiteral);
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

        // Simple search filter
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterContacts(newValue));
    }

    /**
     * Loads the list of contacts for the current user.
     * <p>
     * Identifies contacts based on previous message history (sent or received).
     * For customers, explicitly adds the store owner to the contact list if not
     * already present.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void loadContacts() {
        try {
            // Fetch all messages for the current user to find contacts
            List<Message> receivedMessages = messageDAO.findReceivedMessages(currentUser.getId());
            List<Message> sentMessages = messageDAO.findSentMessages(currentUser.getId());

            // Extract unique contact IDs from both received and sent messages
            Set<Integer> contactIds = new HashSet<>();
            receivedMessages.stream()
                    .map(Message::getSenderId)
                    .forEach(contactIds::add);
            sentMessages.stream()
                    .map(Message::getReceiverId)
                    .forEach(contactIds::add);

            contactsList.getChildren().clear();

            // For customers, always add Owner to the contact list
            if (currentUser.getRole() == User.UserRole.CUSTOMER) {
                User owner = userDAO.getOwner().orElse(null);
                if (owner != null && owner.getId() != currentUser.getId()) {
                    contactIds.add(owner.getId());
                }
            }

            // Create contact items, ensuring owner appears first for customers
            List<Integer> sortedContactIds = new ArrayList<>(contactIds);
            if (currentUser.getRole() == User.UserRole.CUSTOMER) {
                User owner = userDAO.getOwner().orElse(null);
                if (owner != null && sortedContactIds.contains(owner.getId())) {
                    // Move owner to the front
                    sortedContactIds.remove((Integer) owner.getId());
                    sortedContactIds.add(0, owner.getId());
                }
            }

            for (Integer id : sortedContactIds) {
                User contact = userDAO.findById(id).orElse(null);
                if (contact != null) {
                    createContactItem(contact);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a visual item for a contact and adds it to the contact list.
     *
     * @param contact The {@link User} object representing the contact.
     * 
     * @author Burak Özevin
     */
    private void createContactItem(User contact) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("contact-item");
        item.setPadding(new javafx.geometry.Insets(10));

        Circle avatar = new Circle(20);
        avatar.getStyleClass().add("contact-avatar");

        VBox info = new VBox(2);
        Label name = new Label(contact.getFullName());
        name.getStyleClass().add("contact-name");
        Label lastMsg = new Label("Click to view messages"); // Placeholder
        lastMsg.getStyleClass().add("contact-last-msg");

        info.getChildren().addAll(name, lastMsg);
        item.getChildren().addAll(avatar, info);

        item.setOnMouseClicked(e -> selectContact(contact));

        contactsList.getChildren().add(item);
    }

    /**
     * Selects a contact and loads the conversation history.
     *
     * @param contact The {@link User} to chat with.
     * 
     * @author Burak Özevin
     */
    private void selectContact(User contact) {
        this.selectedContact = contact;
        contactNameLabel.setText(contact.getFullName());
        contactStatusLabel.setText("Online"); // Placeholder status
        contactAvatar.setVisible(true);

        loadMessages(contact);
    }

    /**
     * Loads messages exchanged with the selected contact.
     * <p>
     * Fetches both sent and received messages, filters them for the specific
     * contact,
     * sorts them by timestamp, and displays them in the chat area.
     * </p>
     *
     * @param contact The contact to load messages for.
     * 
     * @author Burak Özevin
     */
    private void loadMessages(User contact) {
        messagesContainer.getChildren().clear();
        try {
            List<Message> received = messageDAO.findReceivedMessages(currentUser.getId());
            List<Message> sent = messageDAO.findReceivedMessages(contact.getId()); // Messages sent by current user to
                                                                                   // contact (received by contact)

            // Filter sent messages to only those from current user
            List<Message> actualSent = sent.stream()
                    .filter(m -> m.getSenderId() == currentUser.getId())
                    .collect(Collectors.toList());

            // Filter received messages to only those from contact
            List<Message> actualReceived = received.stream()
                    .filter(m -> m.getSenderId() == contact.getId())
                    .collect(Collectors.toList());

            // Combine and sort
            java.util.ArrayList<Message> conversation = new java.util.ArrayList<>();
            conversation.addAll(actualSent);
            conversation.addAll(actualReceived);
            conversation.sort((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()));

            for (Message msg : conversation) {
                addMessageBubble(msg);
            }

            // Scroll to bottom
            messagesScrollPane.setVvalue(1.0); // Fixed method name case

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a single message bubble to the chat interface.
     *
     * @param msg The {@link Message} to display.
     * 
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
     * Sends a new message to the selected contact.
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
        msg.setSubject("Chat"); // Default subject
        msg.setRead(false);
        msg.setCreatedAt(java.time.LocalDateTime.now());

        try {
            messageDAO.create(msg);
            addMessageBubble(msg);
            messageInput.clear();
            messagesScrollPane.setVvalue(1.0); // Fixed method name case
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filters the contact list based on the search query.
     *
     * @param query The search text to filter contacts by.
     * 
     * @author Burak Özevin
     */
    private void filterContacts(String query) {
        // Implement filtering logic here
    }

    /**
     * Navigates back to the customer dashboard.
     * 
     * @author Burak Özevin
     */
    private void navigateBack() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/customer.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = backBtn.getScene();
            scene.setRoot(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
