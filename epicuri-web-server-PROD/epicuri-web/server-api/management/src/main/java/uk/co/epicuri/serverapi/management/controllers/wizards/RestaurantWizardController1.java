package uk.co.epicuri.serverapi.management.controllers.wizards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionIdStrategy;
import uk.co.epicuri.serverapi.management.model.RestaurantFacade;

/**
 * Created by manish on 17/11/2017.
 */
public class RestaurantWizardController1 extends WizardBaseController {

    @FXML private Button cancelButton;
    @FXML private Button nextButton;
    @FXML private TextField restaurantNameField;
    @FXML private CheckBox manualIdCheckBox;
    @FXML private TextField manualIdTextField;
    @FXML private TextField startFromSessionIdField;
    @FXML public ChoiceBox<SessionIdStrategy> sessionIdStrategy;

    public RestaurantWizardController1() {
        restaurantFacade = new RestaurantFacade();
    }

    @FXML
    private void initialize() {
        cancelButton.setOnMouseClicked(e -> currentStage.close());
        nextButton.setOnMouseClicked(e -> onNextClicked());

        sessionIdStrategy.setItems(FXCollections.observableArrayList(SessionIdStrategy.values()));
        sessionIdStrategy.setValue(SessionIdStrategy.HASH);

        manualIdTextField.disableProperty().bind(manualIdCheckBox.selectedProperty().not());
    }

    private void onNextClicked() {
        if(checkNullEmpty(restaurantNameField)) {
            showPopup("Name field is null", null, "Name field is mandatory");
            return;
        }
        restaurantFacade.setName(restaurantNameField.getText().trim());

        if(manualIdCheckBox.isSelected()) {
            if(checkNullEmptyAndNotInt(manualIdTextField)) {
                showPopup("Restaurant ID", null, "ID needs to be an integer");
                return;
            } else {
                restaurantFacade.setManuallySetStaffId(Integer.parseInt(manualIdTextField.getText()));
            }
        }
        restaurantFacade.setAutosetId(!manualIdCheckBox.isSelected());

        if(StringUtils.isNotBlank(startFromSessionIdField.getText()) && sessionIdStrategy.getValue() == SessionIdStrategy.NUMERIC_ASCENDING) {
            if(checkNullEmptyAndNotInt(startFromSessionIdField)) {
                showPopup("Start billing number", null, "The start ID for sessions (billing number) needs to be a number");
                return;
            } else {
                restaurantFacade.setStartSessionIdFrom(Integer.parseInt(startFromSessionIdField.getText()));
            }
        }
        restaurantFacade.setSessionIdStrategy(sessionIdStrategy.getValue());

        next("/wizards/RestaurantWizard2.fxml");
    }

    @Override
    public void setRestaurantFacade(RestaurantFacade restaurantFacade) {
        restaurantNameField.setText(restaurantFacade.getName());
        manualIdCheckBox.setSelected(restaurantFacade.isAutosetId());
        if(restaurantFacade.getManuallySetStaffId() != null) {
            manualIdTextField.setText(String.valueOf(restaurantFacade.getManuallySetStaffId()));
        }
        sessionIdStrategy.setValue(restaurantFacade.getSessionIdStrategy());
        if(restaurantFacade.getStartSessionIdFrom() != null) {
            startFromSessionIdField.setText(String.valueOf(restaurantFacade.getStartSessionIdFrom()));
        }

        super.setRestaurantFacade(restaurantFacade);
    }
}
