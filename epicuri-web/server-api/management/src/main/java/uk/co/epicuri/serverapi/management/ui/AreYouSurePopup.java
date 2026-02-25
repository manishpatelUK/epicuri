package uk.co.epicuri.serverapi.management.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.management.event.SimpleAction;
import uk.co.epicuri.serverapi.management.controllers.AreYouSurePopupController;

import java.io.IOException;

/**
 * Created by manish.
 */
public class AreYouSurePopup {
    private static final Logger LOGGER = LoggerFactory.getLogger(AreYouSurePopup.class);

    private AreYouSurePopupController controller;

    public AreYouSurePopup(String message) {
        this("Are you sure?", message);
    }

    public AreYouSurePopup(String title, String message) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AreYouSurePopup.fxml"));
            Parent parent = loader.load();
            controller = loader.getController();
            controller.setParentStage(stage);
            controller.setMessage(message);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Cannot load file", e);
            stage.close();
        }
    }

    public void onYesPressed(SimpleAction action) {
        controller.setOnYes(action);
    }

    public static void show(String title, String message, SimpleAction onYesAction) {
        AreYouSurePopup popup = new AreYouSurePopup(title, message);
        popup.onYesPressed(onYesAction);
    }

    public static void show(String message, SimpleAction onYesAction) {
        AreYouSurePopup popup = new AreYouSurePopup(message);
        popup.onYesPressed(onYesAction);
    }
}
