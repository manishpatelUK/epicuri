package uk.co.epicuri.serverapi.management.controllers.wizards;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uk.co.epicuri.serverapi.common.pojo.common.IdPojo;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.Printer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.*;
import uk.co.epicuri.serverapi.management.model.FloorFacade;
import uk.co.epicuri.serverapi.management.model.RestaurantFacade;
import uk.co.epicuri.serverapi.management.ui.AreYouSurePopup;
import uk.co.epicuri.serverapi.management.ui.FileChooserPopup;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class RestaurantWizardController4 extends WizardBaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestaurantWizardController4.class);

    @FXML private ListView<Printer> printerList;
    @FXML private TextField printerNameField;
    @FXML private TextField printerIpAddressField;
    @FXML private Button addPrinterButton;
    @FXML private Button deletePrinterButton;
    @FXML private ChoiceBox<Printer> defaultBillingPrinterBox;
    @FXML private ChoiceBox<Printer> takeawayPrinterBox;
    @FXML private ListView<FloorFacade> floorList;
    @FXML private TextField floorNameField;
    @FXML private TextField capacityField;
    @FXML private Button uploadFloorImageButton;
    @FXML private Button addFloorButton;
    @FXML private Button deleteFloorButton;
    @FXML private Label floorFileUploadedLabel;
    @FXML private Button finishButton;
    @FXML private Button backButton;

    private ObservableList<Printer> printers = FXCollections.observableArrayList();
    private ObservableList<FloorFacade> floors = FXCollections.observableArrayList();

    private File chosenFloorFile;

    @FXML
    private void initialize() {
        backButton.setOnMouseClicked(e -> next("/wizards/RestaurantWizard3.fxml"));
        finishButton.setOnMouseClicked(e -> onFinishedClicked());

        printerList.setItems(printers);
        printerList.setCellFactory(getPrinterListCellFactory());
        StringConverter<Printer> printerConverter = getPrinterConverter();
        defaultBillingPrinterBox.setItems(printers);
        defaultBillingPrinterBox.setConverter(printerConverter);
        defaultBillingPrinterBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> restaurantFacade.setDefaultBillingPrinter(newValue));
        takeawayPrinterBox.setItems(printers);
        takeawayPrinterBox.setConverter(printerConverter);
        takeawayPrinterBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> restaurantFacade.setTakeawayPrinter(newValue));

        addPrinterButton.setOnMouseClicked(e -> {
            Printer printer = new Printer();
            printer.setName(printerNameField.getText().trim());
            printer.setIp(printerIpAddressField.getText().trim());
            printers.add(printer);
            printerNameField.clear();
            printerIpAddressField.clear();
        });
        deletePrinterButton.setOnMouseClicked(e -> deleteFromList(printerList, printers));
        printerList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                printerNameField.setText(newValue.getName());
                printerIpAddressField.setText(newValue.getIp());
            }
        });

        floorList.setItems(floors);
        floorList.setCellFactory(getFloorCellFactory());
        addFloorButton.setOnMouseClicked(e -> {
            if(chosenFloorFile == null) {
                showPopup("Floor image", null, "Missing floor image");
                return;
            }
            FloorFacade floorFacade = new FloorFacade();
            floorFacade.setName(floorNameField.getText().trim());
            try {
                floorFacade.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            } catch(NumberFormatException ex) {
                showPopup("Error", null, "Capacity must be a number");
                return;
            }
            floorFacade.setImage(chosenFloorFile);
            chosenFloorFile = null;
            floorFileUploadedLabel.setText("[empty]");
            floors.add(floorFacade);

            floorNameField.clear();
            capacityField.clear();
        });
        deleteFloorButton.setOnMouseClicked(e -> deleteFromList(floorList, floors));
        floorList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                chosenFloorFile = newValue.getImage();
                floorNameField.setText(newValue.getName());
                capacityField.setText(String.valueOf(newValue.getCapacity()));
            }
        });
        uploadFloorImageButton.setOnMouseClicked(e -> {
            chosenFloorFile = FileChooserPopup.showImagePopup("Select image: 1100px by 675px JPEG/JPG", currentStage);
            if(chosenFloorFile != null && chosenFloorFile.getName().endsWith("png")) {
                chosenFloorFile = null;
            }
            if(chosenFloorFile != null) {
                floorFileUploadedLabel.setText("[File added]");
            }
        });
    }

    private <T> void deleteFromList(ListView<T> listView, ObservableList<T> observableList) {
        if(listView.getSelectionModel().getSelectedItem() == null) {
            return;
        }
        observableList.remove(listView.getSelectionModel().getSelectedItem());
        listView.refresh();
    }

    @Override
    public void setRestaurantFacade(RestaurantFacade restaurantFacade) {
        printerList.getItems().addAll(restaurantFacade.getPrinters());
        if(restaurantFacade.getDefaultBillingPrinter() != null) {
            defaultBillingPrinterBox.setValue(restaurantFacade.getDefaultBillingPrinter());
        }
        if(restaurantFacade.getTakeawayPrinter() != null) {
            takeawayPrinterBox.setValue(restaurantFacade.getTakeawayPrinter());
        }
        floorList.getItems().addAll(restaurantFacade.getFloors());

        super.setRestaurantFacade(restaurantFacade);
    }

    private Callback<ListView<Printer>, ListCell<Printer>> getPrinterListCellFactory() {
        return new Callback<ListView<Printer>, ListCell<Printer>>() {
            @Override
            public ListCell<Printer> call(ListView<Printer> param) {
                return new ListCell<Printer>() {
                    @Override
                    public void updateItem(Printer item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null) {
                            setText(item.getName());
                        }
                    }
                };
            }
        };
    }

    private Callback<ListView<FloorFacade>, ListCell<FloorFacade>> getFloorCellFactory() {
        return new Callback<ListView<FloorFacade>, ListCell<FloorFacade>>() {
            @Override
            public ListCell<FloorFacade> call(ListView<FloorFacade> param) {
                return new ListCell<FloorFacade>() {
                    @Override
                    public void updateItem(FloorFacade item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null) {
                            setText(item.getName());
                        }
                    }
                };
            }
        };
    }

    private StringConverter<Printer> getPrinterConverter() {
        return new StringConverter<Printer>() {
            @Override
            public String toString(Printer object) {
                return object.getName();
            }

            @Override
            public Printer fromString(String string) {
                return null;
            }
        };
    }

    private void onFinishedClicked() {
        AreYouSurePopup.show("Create a new restaurant with these parameters? Name: " + restaurantFacade.getName(), this::createRestaurant);
    }

    private void createRestaurant() {
        finishButton.setDisable(true);

        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantFacade.getName());
        if(restaurantFacade.isAutosetId()) {
            String nextId = webService.get(Endpoints.MANAGEMENT + "/RestaurantId", IdPojo.class).getId();
            restaurant.setStaffFacingId(nextId);
        } else {
            restaurant.setStaffFacingId(restaurantFacade.getManuallySetStaffId().toString());
        }
        try {
            restaurant = webService.post(Endpoints.MANAGEMENT + "/" + Restaurant.class.getCanonicalName(), restaurant, Restaurant.class);
        } catch (Exception ex) {
            showPopup("Error", "Could not create restaurant", "Something went wrong. Call Manish!");
            finishButton.setDisable(false);
            return;
        }

        restaurant.setSessionIdStrategy(restaurantFacade.getSessionIdStrategy());
        //todo start session id number
        restaurant.setAddress(restaurantFacade.getAddress());
        LatLongPair latLongPair = new LatLongPair(restaurantFacade.getLatitude(), restaurantFacade.getLongitude());
        restaurant.setPosition(latLongPair);
        restaurant.setDescription(restaurantFacade.getDescription());
        restaurant.setPhoneNumber1(restaurantFacade.getTelephone1());
        restaurant.setPhoneNumber2(restaurantFacade.getTelephone2());
        restaurant.setPublicEmailAddress(restaurantFacade.getPublicEmail());
        restaurant.setInternalEmailAddress(restaurantFacade.getInternalEmail());
        restaurant.setWebsite(restaurantFacade.getWebsite());
        if(restaurantFacade.getCuisine() != null && restaurantFacade.getCuisine().getId() != null) {
            restaurant.setCuisineId(restaurantFacade.getCuisine().getId());
        }
        if(restaurantFacade.getCountry() != null && restaurantFacade.getCountry().getId() != null) {
            restaurant.setCountryId(restaurantFacade.getCountry().getId());
        }
        restaurant.setVatNumber(restaurantFacade.getVatNumber());
        restaurant.setGuestLogoURL(restaurantFacade.getGuestImageURL());
        restaurant.setReceiptFooter(restaurantFacade.getReceiptFooterField());
        restaurant.setReceiptType(restaurantFacade.getReceiptType());
        restaurant.setISOCurrency(restaurantFacade.getCurrency());
        restaurant.setIANATimezone(restaurantFacade.getTimezone());
        restaurant.setTakeawayOffered(restaurantFacade.getTakeawayOfferingType());

        restaurant = saveRestaurant(restaurant);

        if(restaurantFacade.getReceiptImageFile() != null) {
            byte[] bytes = getBytes(restaurantFacade.getReceiptImageFile());
            if (bytes.length > 0) {
                Map<String,Object> queryMap = new HashMap<>();
                IdPojo idPojo = webService.post(Endpoints.MANAGEMENT + "/" + Restaurant.RECEIPT_IMAGE_ENDPOINT + "/" + restaurant.getId(), bytes, IdPojo.class, queryMap, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM);
                restaurant.setReceiptImageURL(idPojo.getId());
                restaurant = saveRestaurant(restaurant);
            }
        }

        restaurantFacade.getPrinters().clear();
        for(Printer printer : printers) {
            restaurantFacade.getPrinters().add(printer);
        }

        for(Printer printer : restaurantFacade.getPrinters()) {
            printer.setRestaurantId(restaurant.getId());
            IDAble idAble = webService.post(Endpoints.MANAGEMENT + "/" + Printer.class.getCanonicalName(), printer, IDAble.class);
            printer.setId(idAble.getId());
        }

        if(restaurantFacade.getDefaultBillingPrinter() != null) {
            restaurant.setDefaultBillingPrinterId(restaurantFacade.getDefaultBillingPrinter().getId());
        }

        if(restaurantFacade.getTakeawayPrinter() != null) {
            restaurant.setDefaultTakeawayPrinterId(restaurantFacade.getTakeawayPrinter().getId());
        }

        restaurantFacade.getFloors().clear();
        for(FloorFacade floor : floors) {
            restaurantFacade.getFloors().add(floor);
        }

        for(FloorFacade floor : restaurantFacade.getFloors()) {
            Floor newFloor = new Floor(restaurant.getId());
            newFloor.setCapacity(floor.getCapacity());
            newFloor.setName(floor.getName());

            byte[] bytes = getBytes(floor.getImage());
            if (bytes.length > 0) {
                Map<String,Object> queryMap = new HashMap<>();
                IdPojo idPojo = webService.post(Endpoints.MANAGEMENT + "/" + Floor.FLOOR_IMAGE_ENDPOINT + "/" + newFloor.getId(), bytes, IdPojo.class, queryMap, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM);
                newFloor.setImageURL(idPojo.getId());
            }

            Layout layout = new Layout(newFloor);
            layout.setName("Default Layout");
            newFloor.getLayouts().add(layout);
        }
        restaurant = saveRestaurant(restaurant);

        showPopup("Created", null, "Restaurant [" + restaurant.getName() + "] created", Alert.AlertType.INFORMATION);

        if(onFinish != null) {
            onFinish.onAction(restaurant);
        }
        currentStage.close();
    }

    private Restaurant saveRestaurant(Restaurant restaurant) {
        webService.put(Endpoints.MANAGEMENT + "/" + Restaurant.class.getCanonicalName() + "/" + restaurant.getId(), restaurant);
        restaurant = webService.get(Endpoints.MANAGEMENT + "/" + Restaurant.class.getCanonicalName() + "/" + restaurant.getId(), Restaurant.class);
        return restaurant;
    }

    private byte[] getBytes(File file) {
        byte[] bytes = new byte[0];
        try {
            if(file != null) {
                bytes = Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            LOGGER.error("Cannot read file", e);
        }
        return bytes;
    }
}

