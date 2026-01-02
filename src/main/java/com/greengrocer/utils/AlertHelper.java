package com.greengrocer.utils;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialog;
import io.github.palexdev.materialfx.dialogs.MFXGenericDialogBuilder;
import io.github.palexdev.materialfx.dialogs.MFXStageDialog;
import io.github.palexdev.materialfx.enums.ScrimPriority;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Map;

/**
 * Helper class for displaying MaterialFX styled dialogs and alerts.
 * <p>
 * Provides static methods to show informational, error, warning, success, and
 * confirmation dialogs.
 * Utilizes {@link MFXGenericDialog} and {@link MFXStageDialog} for a consistent
 * and modern UI.
 * </p>
 * 
 * @author Burak Özevin
 */
public class AlertHelper {

    /**
     * Displays an informational dialog.
     *
     * @param owner   The owner {@link Stage} of the dialog.
     * @param title   The title of the dialog window.
     * @param content The message content to display.
     * 
     * @author Burak Özevin
     */
    public static void showInfo(Stage owner, String title, String content) {
        MFXGenericDialog dialog = MFXGenericDialogBuilder.build()
                .setContentText(content)
                .setHeaderText(title)
                .get();

        MFXStageDialog stageDialog = MFXGenericDialogBuilder.build(dialog)
                .toStageDialogBuilder()
                .initOwner(owner)
                .initModality(Modality.APPLICATION_MODAL)
                .setDraggable(true)
                .setTitle(title)
                .setScrimPriority(ScrimPriority.WINDOW)
                .setScrimOwner(true)
                .get();

        MFXButton okBtn = new MFXButton("OK");
        okBtn.setOnAction(e -> stageDialog.close());
        dialog.addActions(Map.entry(okBtn, e -> stageDialog.close()));

        stageDialog.showDialog();
    }

    /**
     * Displays an error dialog.
     * <p>
     * Prefixes the title with "Error: ".
     * </p>
     *
     * @param owner   The owner {@link Stage} of the dialog.
     * @param title   The title of the dialog window.
     * @param content The error message to display.
     * 
     * @author Burak Özevin
     */
    public static void showError(Stage owner, String title, String content) {
        showInfo(owner, "Error: " + title, content);
    }

    /**
     * Displays a warning dialog.
     * <p>
     * Prefixes the title with "Warning: ".
     * </p>
     *
     * @param owner   The owner {@link Stage} of the dialog.
     * @param title   The title of the dialog window.
     * @param content The warning message to display.
     * 
     * @author Burak Özevin
     */
    public static void showWarning(Stage owner, String title, String content) {
        showInfo(owner, "Warning: " + title, content);
    }

    /**
     * Displays a success dialog.
     * <p>
     * Prefixes the title with "Success: ".
     * </p>
     *
     * @param owner   The owner {@link Stage} of the dialog.
     * @param title   The title of the dialog window.
     * @param content The success message to display.
     * 
     * @author Burak Özevin
     */
    public static void showSuccess(Stage owner, String title, String content) {
        showInfo(owner, "Success: " + title, content);
    }

    /**
     * Displays a confirmation dialog with "Yes" and "No" buttons.
     * <p>
     * Blocks execution until the user makes a selection.
     * </p>
     *
     * @param owner   The owner {@link Stage} of the dialog.
     * @param title   The title of the dialog window.
     * @param content The confirmation question or message.
     * @return {@code true} if the user clicked "Yes"; {@code false} otherwise.
     * 
     * @author Burak Özevin
     */
    public static boolean showConfirm(Stage owner, String title, String content) {
        final boolean[] result = { false };

        MFXGenericDialog dialog = MFXGenericDialogBuilder.build()
                .setContentText(content)
                .setHeaderText(title)
                .get();

        MFXStageDialog stageDialog = MFXGenericDialogBuilder.build(dialog)
                .toStageDialogBuilder()
                .initOwner(owner)
                .initModality(Modality.APPLICATION_MODAL)
                .setDraggable(true)
                .setTitle(title)
                .get();

        MFXButton yesBtn = new MFXButton("Yes");
        MFXButton noBtn = new MFXButton("No");

        yesBtn.setOnAction(e -> {
            result[0] = true;
            stageDialog.close();
        });
        noBtn.setOnAction(e -> {
            result[0] = false;
            stageDialog.close();
        });

        dialog.addActions(
                Map.entry(yesBtn, e -> {
                }),
                Map.entry(noBtn, e -> {
                }));

        stageDialog.showAndWait();
        return result[0];
    }
}
