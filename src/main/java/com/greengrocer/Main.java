package com.greengrocer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The main entry point for the GreenGrocer application.
 * <p>
 * Initializes the JavaFX application, loads the splash screen, and sets up the
 * primary stage.
 * </p>
 * 
 * @author Burak Özevin, Elif Zeynep Talay, Ramazan Birkan Öztürk
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application.
     * <p>
     * Loads the {@code splash.fxml} view, sets the application icon, and displays
     * the primary stage.
     * </p>
     *
     * @param primaryStage The primary stage for this application, onto which
     *                     the application scene can be set.
     * 
     * @author Burak Özevin, Elif Zeynep Talay, Ramazan Birkan Öztürk
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Seed default images if missing
        com.greengrocer.utils.DatabaseSeeder.seedImages();

        // Load the splash screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/splash.fxml"));
        Parent root = loader.load();

        // Create scene with initial size 960x540
        Scene scene = new Scene(root, 960, 540);

        // Load stylesheet
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        // Configure stage
        primaryStage.setTitle("Group06 GreenGrocer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(450);
        primaryStage.centerOnScreen();

        // Show the application
        primaryStage.show();
    }

    /**
     * The main method, serving as the entry point for the application.
     *
     * @param args Command-line arguments passed to the application.
     * 
     * @author Burak Özevin, Elif Zeynep Talay, Ramazan Birkan Öztürk
     */
    public static void main(String[] args) {
        launch(args);
    }
}
