package uk.co.epicuri.serverapi.management.controllers.wizards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.ReceiptType;
import uk.co.epicuri.serverapi.common.pojo.model.TakeawayOfferingType;
import uk.co.epicuri.serverapi.management.model.RestaurantFacade;
import uk.co.epicuri.serverapi.management.reflect.ReflectUICreator;
import uk.co.epicuri.serverapi.management.ui.FileChooserPopup;

import java.io.File;

public class RestaurantWizardController3 extends WizardBaseController {
    @FXML private TextField vatNumberField;
    @FXML private TextField receiptFooterField;
    @FXML private Button uploadReceiptButton;
    @FXML private Label receiptImageUploadedLabel;
    @FXML private RadioButton normalReceiptRadio;
    @FXML private RadioButton hotelReceiptRadio;
    @FXML private ComboBox<String> currencyBox;
    @FXML private ComboBox<String> timezoneBox;
    @FXML private TextField guestImageURLField;
    @FXML private ChoiceBox<TakeawayOfferingType> takeawayOfferingBox;
    @FXML private Button nextButton;
    @FXML private Button backButton;

    @FXML
    private void initialize() {
        backButton.setOnMouseClicked(e -> next("/wizards/RestaurantWizard2.fxml"));
        nextButton.setOnMouseClicked(e -> onNextClicked());

        ToggleGroup toggleGroup = new ToggleGroup();
        normalReceiptRadio.setToggleGroup(toggleGroup);
        hotelReceiptRadio.setToggleGroup(toggleGroup);

        uploadReceiptButton.setOnMouseClicked(e -> attemptSetImageReceipt());

        takeawayOfferingBox.setItems(FXCollections.observableArrayList(TakeawayOfferingType.values()));
        currencyBox.setItems(ReflectUICreator.getObservableListFromFile("currencies.txt"));
        timezoneBox.setItems(ReflectUICreator.getObservableListFromFile("timezones.txt"));

        currencyBox.setVisibleRowCount(10);
        currencyBox.getSelectionModel().select("GBP");
        timezoneBox.setVisibleRowCount(10);
        timezoneBox.getSelectionModel().select("Europe/London");
    }

    private void attemptSetImageReceipt() {
        File image = FileChooserPopup.showImagePopup("Select bill receipt image", currentStage);
        if(image != null && image.getName().endsWith("png")) {
            image = null;
        }
        if(image != null) {
            restaurantFacade.setReceiptImageFile(image);
            receiptImageUploadedLabel.setText("[File added]");
        }
    }

    private void onNextClicked() {
        restaurantFacade.setVatNumber(checkNullEmpty(vatNumberField) ? null : vatNumberField.getText().trim());
        restaurantFacade.setReceiptFooterField(checkNullEmpty(receiptFooterField) ? null : receiptFooterField.getText().trim());
        if(hotelReceiptRadio.isSelected()) {
            restaurantFacade.setReceiptType(ReceiptType.HOTEL);
        } else {
            restaurantFacade.setReceiptType(ReceiptType.NORMAL);
        }
        restaurantFacade.setCurrency(currencyBox.getValue());
        restaurantFacade.setTimezone(timezoneBox.getValue());
        restaurantFacade.setGuestImageURL(checkNullEmpty(guestImageURLField) ? null : guestImageURLField.getText().trim());
        restaurantFacade.setTakeawayOfferingType(takeawayOfferingBox.getValue());

        next("/wizards/RestaurantWizard4.fxml");
    }

    @Override
    public void setRestaurantFacade(RestaurantFacade restaurantFacade) {
        vatNumberField.setText(restaurantFacade.getVatNumber());
        receiptFooterField.setText(restaurantFacade.getReceiptFooterField());
        if(restaurantFacade.getReceiptType() == ReceiptType.HOTEL) {
            hotelReceiptRadio.setSelected(true);
        } else {
            normalReceiptRadio.setSelected(true);
        }

        if(StringUtils.isNotBlank(restaurantFacade.getCurrency())) {
            currencyBox.setValue(restaurantFacade.getCurrency());
        }

        if(StringUtils.isNotBlank(restaurantFacade.getTimezone())) {
            timezoneBox.setValue(restaurantFacade.getTimezone());
        }

        guestImageURLField.setText(restaurantFacade.getGuestImageURL());

        if(restaurantFacade.getTakeawayOfferingType() != null) {
            takeawayOfferingBox.setValue(restaurantFacade.getTakeawayOfferingType());
        }

        super.setRestaurantFacade(restaurantFacade);
    }
}
