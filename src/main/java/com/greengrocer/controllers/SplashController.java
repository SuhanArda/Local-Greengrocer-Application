package com.greengrocer.controllers;

import javafx.animation.ParallelTransition;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * Controller for the splash screen.
 * <p>
 * Displays a splash screen with a fade-in animation and then transitions to the
 * login screen.
 * This provides a smooth startup experience for the application.
 * </p>
 * 
 * @author Suhan Arda Öner
 */
public class SplashController implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private Pane animationContainer;

    private static final String[] EMOJIS = {
            "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆",
            "🥑",
            "🥦", "🥬", "🥒", "🌶️", "🫑", "🌽", "🥕", "🫒", "🧄", "🧅", "🥔", "🍠", "🥐", "🥯", "🍞", "🥖", "🧀", "🥚",
            "🍳",
            "🧈", "🥞", "🧇", "🥓", "🥩", "🍗", "🍖", "🦴", "🌭", "🍔", "🍟", "🍕", "🥪", "🥙", "🧆", "🌮", "🌯", "🫔",
            "🥗"
    };

    /**
     * Initializes the controller class.
     * <p>
     * Starts the splash screen animation.
     * </p>
     *
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     * 
     * @author Suhan Arda Öner
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startAnimation();
    }

    /**
     * Starts the splash screen animation.
     * <p>
     * Creates an explosion effect with fruit emojis, animating their translation,
     * rotation, and scaling.
     * Upon completion, it triggers the transition to the login screen.
     * </p>
     * 
     * @author Suhan Arda Öner
     */
    private void startAnimation() {
        int emojiCount = 60;
        Random random = new Random();
        ParallelTransition explosion = new ParallelTransition();

        double centerX = 960 / 2.0; // Approx center, will rely on layout bounds ideally but fixed for now
        double centerY = 540 / 2.0;

        for (int i = 0; i < emojiCount; i++) {
            String emoji = EMOJIS[random.nextInt(EMOJIS.length)];
            Label emojiLabel = new Label(emoji);
            emojiLabel.setStyle("-fx-font-size: 30px;");

            // Start at center
            emojiLabel.setLayoutX(centerX);
            emojiLabel.setLayoutY(centerY);
            emojiLabel.setOpacity(0); // Start invisible, fade in? Or just appear logic
            // Actually, let's keep them visible but packed

            animationContainer.getChildren().add(emojiLabel);

            // Random destination (off screen or edges)
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = 500 + random.nextDouble() * 400; // Far enough to clear screen
            double endX = Math.cos(angle) * distance;
            double endY = Math.sin(angle) * distance;

            // Translate
            TranslateTransition translate = new TranslateTransition(Duration.seconds(5), emojiLabel);
            translate.setByX(endX);
            translate.setByY(endY);

            // Rotate
            RotateTransition rotate = new RotateTransition(Duration.seconds(5), emojiLabel);
            rotate.setByAngle(random.nextDouble() * 720 - 360);

            // Scale
            ScaleTransition scale = new ScaleTransition(Duration.seconds(5), emojiLabel);
            scale.setFromX(0.1);
            scale.setFromY(0.1);
            scale.setToX(1.5 + random.nextDouble());
            scale.setToY(1.5 + random.nextDouble());

            // Fade In/Out logic if needed, but request was just "scatter"
            // Let's ensure they are visible immediately
            emojiLabel.setOpacity(1.0);

            explosion.getChildren().addAll(translate, rotate, scale);
        }

        explosion.setOnFinished(e -> loadLoginScreen());
        explosion.play();
    }

    /**
     * Loads the login screen and transitions to it.
     * <p>
     * Loads the {@code login.fxml} view, sets the scene, and applies a fade-in
     * transition
     * for the new scene.
     * </p>
     * 
     * @author Suhan Arda Öner
     */
    private void loadLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // Apply main styles
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("GreenGrocer - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
