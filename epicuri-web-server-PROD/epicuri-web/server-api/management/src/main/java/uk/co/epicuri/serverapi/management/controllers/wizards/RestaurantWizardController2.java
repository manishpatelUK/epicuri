package uk.co.epicuri.serverapi.management.controllers.wizards;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.model.Country;
import uk.co.epicuri.serverapi.common.pojo.model.Cuisine;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.management.model.RestaurantFacade;
import uk.co.epicuri.serverapi.management.ui.GenericFormPopup;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;

import java.util.List;

public class RestaurantWizardController2 extends WizardBaseController {
    @FXML private TextField streetField;
    @FXML private TextField townField;
    @FXML private TextField cityField;
    @FXML private TextField postalCodeField;
    @FXML private Button lookupPostcodeButton;
    @FXML private TextField longField;
    @FXML private TextField latField;
    @FXML private TextArea descriptionField;
    @FXML private TextField telephone1Field;
    @FXML private TextField telephone2Field;
    @FXML private TextField publicEmailField;
    @FXML private TextField internalEmailField;
    @FXML private ChoiceBox<Cuisine> cuisineBox;
    @FXML private Button newCuisineButton;
    @FXML private TextField websiteField;
    @FXML private Button nextButton;
    @FXML private Button backButton;
    @FXML private ChoiceBox<Country> countryBox;

    @FXML
    private void initialize() {
        backButton.setOnMouseClicked(e -> next("/wizards/RestaurantWizard1.fxml"));
        nextButton.setOnMouseClicked(e -> onNextClicked());

        cuisineBox.setConverter(new StringConverter<Cuisine>() {
            @Override
            public String toString(Cuisine object) {
                return object.getName();
            }

            @Override
            public Cuisine fromString(String string) {
                return null;
            }
        });

        refreshCuisines();
        newCuisineButton.setOnMouseClicked(e -> {
            GenericFormPopup.show(Cuisine.class, null, true, c -> {
                Cuisine cuisine = webService.post(Endpoints.MANAGEMENT + "/" + Cuisine.class.getCanonicalName(), c, Cuisine.class);
                refreshCuisines();
                cuisineBox.setValue(cuisine);
            });
        });

        refreshCountries();
        countryBox.setConverter(new StringConverter<Country>() {
            @Override
            public String toString(Country object) {
                return object.getName();
            }

            @Override
            public Country fromString(String string) {
                return null;
            }
        });

        lookupPostcodeButton.setOnMouseClicked(e -> {
            if(StringUtils.isBlank(postalCodeField.getText())) {
                showPopup("Postcode required", null, "Cannot do a search without a postcode", Alert.AlertType.ERROR);
                return;
            }

            LatLongPair latLongPair;
            try {
                latLongPair = webService.postCodeLookup(postalCodeField.getText().trim());
            } catch (Exception ex) {
                showPopup("Error getting postcode", null, "Cannot do a search right now...", Alert.AlertType.ERROR);
                return;
            }

            if(latLongPair == null) {
                showPopup("Error getting postcode", null, "Postcode did not yield any results", Alert.AlertType.ERROR);
                return;
            }

            latField.setText(String.valueOf(latLongPair.getLatitude()));
            longField.setText(String.valueOf(latLongPair.getLongitude()));
        });
    }

    private void refreshCuisines() {
        List<Cuisine> cuisines = webService.getAsList(Endpoints.MANAGEMENT + "/" + Cuisine.class.getCanonicalName(),Cuisine.class);
        cuisineBox.setItems(FXCollections.observableArrayList(cuisines));
    }

    private void refreshCountries() {
        List<Country> countries = webService.getAsList(Endpoints.MANAGEMENT + "/" + Country.class.getCanonicalName(),Country.class);
        countryBox.setItems(FXCollections.observableArrayList(countries));
    }

    private void onNextClicked() {
        if(StringUtils.isBlank(latField.getText().trim()) || StringUtils.isBlank(longField.getText().trim())) {
            showPopup("Positioning required", null, "Ensure long/lat fields are filled in");
            return;
        }

        //check addresses
        if(checkNullEmpty(streetField)
                || checkNullEmpty(townField)
                || checkNullEmpty(cityField)
                || checkNullEmpty(postalCodeField)) {
            showPopup("Required fields not filled", "Address fields", "All address fields are required", Alert.AlertType.ERROR);
            return;
        }

        restaurantFacade.getAddress().setStreet(streetField.getText().trim());
        restaurantFacade.getAddress().setTown(townField.getText().trim());
        restaurantFacade.getAddress().setCity(cityField.getText().trim());
        restaurantFacade.getAddress().setPostcode(postalCodeField.getText().trim());
        if(!checkNullEmpty(latField)) {
            try {
                restaurantFacade.setLatitude(Double.parseDouble(latField.getText().trim()));
            } catch (NumberFormatException ex) {
                showPopup("Not a number", null, "Latitude field must be decimal");
            }
        }
        if(!checkNullEmpty(longField)) {
            try {
                restaurantFacade.setLongitude(Double.parseDouble(latField.getText().trim()));
            } catch (NumberFormatException ex) {
                showPopup("Not a number", null, "Longitude field must be decimal");
            }
        }

        restaurantFacade.setDescription(checkNullEmpty(descriptionField) ? null : descriptionField.getText().trim());
        if(checkNullEmpty(telephone1Field)) {
            showPopup("Required fields not filled", "Telephone fields", "Telephone 1 field is required", Alert.AlertType.ERROR);
            return;
        }
        restaurantFacade.setTelephone1(telephone1Field.getText().trim());
        restaurantFacade.setTelephone2(checkNullEmpty(telephone2Field) ? null : telephone2Field.getText().trim());

        if(checkNullEmpty(publicEmailField)) {
            showPopup("Required fields not filled", "Email fields", "Public email field is required", Alert.AlertType.ERROR);
            return;
        }
        restaurantFacade.setPublicEmail(publicEmailField.getText().trim());
        if(checkNullEmpty(internalEmailField)) {
            showPopup("Required fields not filled", "Email fields", "Internal email field is required", Alert.AlertType.ERROR);
            return;
        }
        restaurantFacade.setInternalEmail(internalEmailField.getText().trim());
        restaurantFacade.setCuisine(cuisineBox.getValue());
        restaurantFacade.setWebsite(checkNullEmpty(websiteField) ? null : websiteField.getText().trim());
        restaurantFacade.setCountry(countryBox.getValue());

        next("/wizards/RestaurantWizard3.fxml");
    }

    @Override
    public void setRestaurantFacade(RestaurantFacade restaurantFacade) {
        streetField.setText(restaurantFacade.getAddress().getStreet());
        townField.setText(restaurantFacade.getAddress().getTown());
        cityField.setText(restaurantFacade.getAddress().getCity());
        postalCodeField.setText(restaurantFacade.getAddress().getPostcode());
        if(restaurantFacade.getLatitude() != null) {
            latField.setText(String.valueOf(restaurantFacade.getLatitude()));
        }
        if(restaurantFacade.getLongitude() != null) {
            longField.setText(String.valueOf(restaurantFacade.getLongitude()));
        }
        descriptionField.setText(restaurantFacade.getDescription());
        telephone1Field.setText(restaurantFacade.getTelephone1());
        telephone2Field.setText(restaurantFacade.getTelephone2());
        publicEmailField.setText(restaurantFacade.getPublicEmail());
        internalEmailField.setText(restaurantFacade.getInternalEmail());
        if(restaurantFacade.getCuisine() != null) {
            cuisineBox.setValue(restaurantFacade.getCuisine());
        }
        websiteField.setText(restaurantFacade.getWebsite());
        if(restaurantFacade.getCountry() != null) {
            countryBox.setValue(restaurantFacade.getCountry());
        }

        super.setRestaurantFacade(restaurantFacade);
    }
}
