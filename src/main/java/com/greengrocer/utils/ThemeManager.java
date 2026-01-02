package com.greengrocer.utils;

import javafx.scene.Scene;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Singleton class for managing the application's visual theme (Light/Dark).
 * <p>
 * Handles loading and saving theme preferences to a local file, applying CSS
 * stylesheets
 * to scenes, and toggling between modes.
 * </p>
 * 
 * @author Burak Özevin
 */
public class ThemeManager {

    private static ThemeManager instance;
    private static final String PREFERENCES_FILE = ".greengrocer_preferences";
    private static final String THEME_KEY = "theme";
    private static final String LIGHT_THEME = "light";
    private static final String DARK_THEME = "dark";

    private String currentTheme;
    private Properties preferences;
    private Path preferencesPath;

    private ThemeManager() {
        preferencesPath = Paths.get(System.getProperty("user.home"), PREFERENCES_FILE);
        preferences = new Properties();
        loadPreferences();
    }

    /**
     * Retrieves the singleton instance of the ThemeManager.
     *
     * @return The single {@link ThemeManager} instance.
     * 
     * @author Burak Özevin
     */
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Loads theme preferences from the local file system.
     * <p>
     * Defaults to the light theme if the preference file does not exist or cannot
     * be read.
     * </p>
     * 
     * @author Burak Özevin
     */
    private void loadPreferences() {
        try {
            if (Files.exists(preferencesPath)) {
                try (InputStream is = Files.newInputStream(preferencesPath)) {
                    preferences.load(is);
                }
            }
            currentTheme = preferences.getProperty(THEME_KEY, LIGHT_THEME);
        } catch (IOException e) {
            currentTheme = LIGHT_THEME;
            e.printStackTrace();
        }
    }

    /**
     * Saves the current theme preference to the local file system.
     * 
     * @author Burak Özevin
     */
    private void savePreferences() {
        try (OutputStream os = Files.newOutputStream(preferencesPath)) {
            preferences.setProperty(THEME_KEY, currentTheme);
            preferences.store(os, "GreenGrocer Application Preferences");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the dark theme is currently active.
     *
     * @return {@code true} if dark theme is active; {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public boolean isDarkTheme() {
        return DARK_THEME.equals(currentTheme);
    }

    /**
     * Gets the current theme identifier string.
     *
     * @return "dark" or "light".
     * 
     * @author Burak Özevin
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Toggles the theme between light and dark modes for the given scene.
     *
     * @param scene The {@link Scene} to apply the new theme to.
     * 
     * @author Burak Özevin
     */
    public void toggleTheme(Scene scene) {
        if (isDarkTheme()) {
            setLightTheme(scene);
        } else {
            setDarkTheme(scene);
        }
    }

    /**
     * Sets the application to dark theme.
     *
     * @param scene The {@link Scene} to apply the theme to.
     * 
     * @author Burak Özevin
     */
    public void setDarkTheme(Scene scene) {
        currentTheme = DARK_THEME;
        applyTheme(scene);
        savePreferences();
    }

    /**
     * Sets the application to light theme.
     *
     * @param scene The {@link Scene} to apply the theme to.
     * 
     * @author Burak Özevin
     */
    public void setLightTheme(Scene scene) {
        currentTheme = LIGHT_THEME;
        applyTheme(scene);
        savePreferences();
    }

    /**
     * Applies the current theme's stylesheets to the specified scene.
     * <p>
     * Clears existing stylesheets and adds the appropriate CSS files.
     * </p>
     *
     * @param scene The {@link Scene} to update.
     * 
     * @author Burak Özevin
     */
    public void applyTheme(Scene scene) {
        if (scene == null)
            return;

        String lightCss = getClass().getResource("/styles/styles.css").toExternalForm();
        String darkCss = getClass().getResource("/styles/styles-dark.css").toExternalForm();

        scene.getStylesheets().clear();
        scene.getStylesheets().add(lightCss);

        if (isDarkTheme()) {
            scene.getStylesheets().add(darkCss);
        }
    }

    /**
     * Gets the icon literal for the theme toggle button.
     *
     * @return "fas-sun" for dark theme (to switch to light), "fas-moon" for light
     *         theme.
     * 
     * @author Burak Özevin
     */
    public String getThemeIcon() {
        return isDarkTheme() ? "fas-sun" : "fas-moon";
    }

    /**
     * Initializes a theme toggle button with the correct icon and click handler.
     * <p>
     * Sets up a listener to apply the theme whenever the button's scene changes.
     * </p>
     *
     * @param themeToggleBtn The
     *                       {@link io.github.palexdev.materialfx.controls.MFXButton}
     *                       to initialize.
     */
    public void initializeTheme(io.github.palexdev.materialfx.controls.MFXButton themeToggleBtn) {
        // Set initial icon
        updateThemeIcon(themeToggleBtn);

        // Apply current theme to the scene when the button is attached to a scene
        themeToggleBtn.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyTheme(newScene);
            }
        });

        // Handle click
        themeToggleBtn.setOnAction(e -> {
            toggleTheme(themeToggleBtn.getScene());
            updateThemeIcon(themeToggleBtn);
        });
    }

    /**
     * Updates the icon of the theme toggle button based on the current theme.
     *
     * @param btn The button to update.
     * 
     * @author Burak Özevin
     */
    private void updateThemeIcon(io.github.palexdev.materialfx.controls.MFXButton btn) {
        if (btn.getGraphic() instanceof org.kordamp.ikonli.javafx.FontIcon) {
            org.kordamp.ikonli.javafx.FontIcon icon = (org.kordamp.ikonli.javafx.FontIcon) btn.getGraphic();
            icon.setIconLiteral(getThemeIcon());
            icon.setIconColor(javafx.scene.paint.Color.valueOf(isDarkTheme() ? "#FFD54F" : "#757575"));
        }
    }
}
