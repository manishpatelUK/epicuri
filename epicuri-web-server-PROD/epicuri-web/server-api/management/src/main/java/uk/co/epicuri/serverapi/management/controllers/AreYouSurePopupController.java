package uk.co.epicuri.serverapi.management.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.management.event.SimpleAction;
import uk.co.epicuri.serverapi.management.ui.AreYouSurePopup;

/**
 * Created by manish.
 */
public class AreYouSurePopupController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AreYouSurePopupController.class);

    @FXML
    public Label messageLabel;

    private SimpleAction onYesAction;
    private Stage parentStage;

    public AreYouSurePopupController() {
        super();

        LOGGER.info("Controller initialized");
    }

    @FXML
    private void initialize() {

    }

    public void setOnYes(SimpleAction action) {
        onYesAction = action;
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void onCancelButtonPressed(ActionEvent actionEvent) {
        parentStage.close();
    }

    public void onYesButtonPressed(ActionEvent actionEvent) {
        onYesAction.onAction();
        parentStage.close();
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }
}
