package uk.co.epicuri.serverapi.management.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.common.pojo.common.StringMessage;
import uk.co.epicuri.serverapi.common.pojo.model.*;
import uk.co.epicuri.serverapi.common.pojo.model.booking.BookingStatics;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Default;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Staff;
import uk.co.epicuri.serverapi.common.pojo.model.session.AdjustmentType;
import uk.co.epicuri.serverapi.management.ManagementApplication;
import uk.co.epicuri.serverapi.management.controllers.wizards.WizardBaseController;
import uk.co.epicuri.serverapi.management.model.Environment;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.reflect.*;
import uk.co.epicuri.serverapi.management.reflect.ListViewBuilder;
import uk.co.epicuri.serverapi.management.ui.AreYouSurePopup;
import uk.co.epicuri.serverapi.management.ui.FileChooserPopup;
import uk.co.epicuri.serverapi.management.ui.GenericFormPopup;
import uk.co.epicuri.serverapi.management.ui.ReflectionView;
import uk.co.epicuri.serverapi.management.uploads.MenuUploader;
import uk.co.epicuri.serverapi.management.webservice.Endpoints;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish Patel
 */
public class MainController extends BaseController{
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

    @FXML private ComboBox<Environment> environmentBox;
    private final ObservableList<Environment> environments = FXCollections.observableArrayList();
    @FXML private Label environmentLabel;
    @FXML private TabPane tabView;

    public MainController() {
        super();

        LOGGER.info("Controller initialized");
    }

    @FXML
    private void initialize() {
        environmentBox.setItems(environments);
        environments.addAll(Environment.values());
        environmentBox.getSelectionModel().selectFirst();

        environmentLabel.setText("Environment NOT SET!");
    }

    public void environmentBoxChanged(ActionEvent actionEvent) {
        if(webService.getSelectedEnvironment() != environmentBox.getSelectionModel().getSelectedItem()) {
            webService.setSelectedEnvironment(environmentBox.getSelectionModel().getSelectedItem());
            environmentLabel.setText(webService.getSelectedEnvironment().toString());
            environmentLabel.setTooltip(new Tooltip(webService.getSelectedEnvironment().getUrl()));

            Stage stage = new Stage();
            createLoginDialog(stage);

        } else if(webService.getSelectedEnvironment() == null || webService.getSelectedEnvironment() == Environment.NONE) {
            environmentLabel.setText("Environment NOT SET!");
        }
    }

    private void createLoginDialog(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent parent = loader.load();
            LoginController loginController = loader.getController();
            loginController.setParentStage(stage);
            loginController.setOnFailedLogin(s -> {
                webService.setSelectedEnvironment(Environment.NONE);
                environmentBox.getSelectionModel().select(Environment.NONE);

                ManagementApplication.USER = null;
                ManagementApplication.PW = null;

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Login Error");
                alert.setContentText(s);
                alert.showAndWait();
            });

            loginController.setOnSuccessLogin(this::populateOnLogin);

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Credentials");
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Cannot create login screen", e);
            stage.close();
        }
    }

    private void populateOnLogin() {
        tabView.getTabs().clear();
        tabView.getTabs().add(createListTab("Restaurants", Restaurant.class));
        tabView.getTabs().add(createListTab("Cuisines", Cuisine.class));
        tabView.getTabs().add(createListTab("Countries", Country.class));
        tabView.getTabs().add(createListTab("Defaults", Default.class));
        tabView.getTabs().add(createListTab("Taxes", TaxRate.class));
        tabView.getTabs().add(createListTab("Adjustments", AdjustmentType.class));
        tabView.getTabs().add(createListTab("Preferences", Preference.class));
        tabView.getTabs().add(createListTab("Booking Statics", BookingStatics.class));
        tabView.getTabs().add(createMaintenanceTab());
    }

    private Tab createMaintenanceTab() {
        Tab tab = new Tab("Maintenance");
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10); //horizontal gap in pixels => that's what you are asking for
        gridPane.setVgap(10); //vertical gap in pixels
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        gridPane.add(new Label("Ensure Defaults"), 1, 1);
        gridPane.add(new Label("Trawls through all the restaurants defaults and adds any missing defaults. Run this when a new default has been added to the system"), 2, 1);
        Button defaultsCheck = new Button("Check & Fix");
        defaultsCheck.setOnMouseClicked(e -> {
            StringMessage message = webService.post("/Management/Maintenance/Defaults", "", StringMessage.class);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message.getMessage());
            alert.show();
        });
        gridPane.add(defaultsCheck, 3, 1);

        gridPane.add(new Label("Ensure Menu Ordering"), 1, 2);
        gridPane.add(new Label("Trawls through all the restaurant menus and sets the ordering according to getMenus"), 2, 2);
        Button menuOrderCheck = new Button("Check & Fix");
        menuOrderCheck.setOnMouseClicked(e -> {
            StringMessage message = webService.post("/Management/Maintenance/UpdateMenuOrdering", "", StringMessage.class);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message.getMessage());
            alert.show();
        });
        gridPane.add(menuOrderCheck, 3, 2);

        gridPane.add(new Label("Ensure Staff Permissions"), 1, 3);
        gridPane.add(new Label("Trawls through all the restaurant and ensures all staff permissions are set"), 2, 3);
        Button permissionCheck = new Button("Check & Fix");
        permissionCheck.setOnMouseClicked(e -> {
            StringMessage message = webService.post("/Management/Maintenance/UpdatePermissions", "", StringMessage.class);
        });
        gridPane.add(permissionCheck, 3, 3);

        ScrollPane scrollPane = new ScrollPane(gridPane);
        tab.setContent(scrollPane);
        return tab;
    }

    @SuppressWarnings("unchecked")
    private <T extends IDAble> Tab createListTab(String title, Class<T> clazz) {
        Tab tab = new Tab(title);
        HBox mainHBox = new HBox();
        mainHBox.setMaxHeight(ManagementApplication.INITIAL_WINDOW_HEIGHT-100);
        ListView<ModelWrapper<T>> listView = ListViewBuilder.newInstance().withClass(clazz).withWebService(webService).asList();

        VBox centreBox = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(centreBox);

        Button addButton = new Button("Add...");
        if(clazz == Restaurant.class) {
            listView.getItems().forEach(m -> m.setAppendFieldString("staffFacingId"));
            sortRestaurantListView(listView);
            addButton.setOnMouseClicked(x -> onAddNewRestaurant(clazz, listView, centreBox, scrollPane));
        } else {
            addButton.setOnMouseClicked(x -> onAddNew(listView, clazz));
        }
        Button deleteButton = new Button("Delete");
        deleteButton.setOnMouseClicked(x -> onDelete(listView, clazz, centreBox));

        HBox buttonBox = new HBox(addButton, deleteButton);
        VBox leftBox = new VBox(listView, buttonBox);
        if(clazz == Default.class) {
            leftBox.getChildren().add(defaultsResetButton());
        }
        if(clazz == AdjustmentType.class) {
            leftBox.getChildren().add(adjustmentsResetButton());
        }
        if(clazz == Preference.class) {
            leftBox.getChildren().add(preferencesResetButton());
        }
        if(clazz == BookingStatics.class) {
            leftBox.getChildren().add(staticsResetButton());
        }
        mainHBox.getChildren().add(leftBox);
        mainHBox.getChildren().add(scrollPane);

        listView.getSelectionModel().selectedItemProperty().addListener( l ->{
            populateMainArea(clazz, listView, centreBox, scrollPane);
        });
        tab.setContent(mainHBox);
        return tab;
    }

    private <T extends IDAble> void sortRestaurantListView(ListView<ModelWrapper<T>> listView) {
        listView.getItems().sort((c1,c2) -> {
            if(c1.getUnderlying() != null && ((Restaurant)c1.getUnderlying()).getStaffFacingId() != null) {
                String staffFacingId2 = ((Restaurant) c2.getUnderlying()).getStaffFacingId();
                String staffFacingId1 = ((Restaurant) c1.getUnderlying()).getStaffFacingId();
                return Integer.valueOf(staffFacingId2).compareTo(Integer.valueOf(staffFacingId1));
            }
            return 0;
        });
    }

    private Button defaultsResetButton() {
        Button button = new Button("Reset");
        button.setOnMouseClicked(e -> {
            webService.post("Management/Reset/Defaults", String.class, String.class);
        });
        return button;
    }

    private Button adjustmentsResetButton() {
        Button button = new Button("Reset");
        button.setOnMouseClicked(e -> {
            webService.post("Management/Reset/Adjustments", String.class, String.class);
        });
        return button;
    }

    private Button preferencesResetButton() {
        Button button = new Button("Reset");
        button.setOnMouseClicked(e -> {
            webService.post("Management/Reset/Preferences", String.class, String.class);
        });
        return button;
    }

    private Button staticsResetButton() {
        Button button = new Button("Reset");
        button.setOnMouseClicked(e -> {
            webService.post("Management/Reset/BookingStatics", String.class, String.class);
        });
        return button;
    }

    @SuppressWarnings("unchecked")
    private <T extends IDAble> void populateMainArea(Class<T> clazz, ListView<ModelWrapper<T>> listView, VBox centreBox, ScrollPane scrollPane) {
        ModelWrapper<T> modelWrapper = listView.getSelectionModel().getSelectedItem();
        if(modelWrapper == null || modelWrapper.getUnderlying() == null) {
            return;
        }
        centreBox.getChildren().clear();
        ReflectionView viewNode = PojoViewBuilder.newInstance().withClass(clazz).withInstance(modelWrapper.getUnderlying()).withWebService(webService).asView();

        if(clazz == Restaurant.class) {
            addStaff(viewNode, (Restaurant)modelWrapper.getUnderlying());
            addPrinters(viewNode, (Restaurant)modelWrapper.getUnderlying());
        }

        viewNode.getAffirmativeButton().setOnMouseClicked(e -> {
            /*if(!modelWrapper.hasChanged()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Nothing to save");
                alert.setContentText("No changes were detected - will not save");
                alert.showAndWait();
                return;
            }*/

            T objectOnServer = webService.get(Endpoints.MANAGEMENT + "/" + clazz.getCanonicalName() + "/" + modelWrapper.getId(), clazz);
            if(objectOnServer != null && objectOnServer.hashCode() != modelWrapper.getOriginalHashCode()) {
                AreYouSurePopup.show("Object has changed!",
                        "The object on the server has changed - save anyway?",
                        () -> webService.put(Endpoints.MANAGEMENT + "/" + clazz.getCanonicalName() + "/" + modelWrapper.getId(), modelWrapper.getUnderlying()));
            } else {
                webService.put(Endpoints.MANAGEMENT + "/" + clazz.getCanonicalName() + "/" + modelWrapper.getId(), modelWrapper.getUnderlying());
            }
        });
        viewNode.getRefreshButton().setOnMouseClicked(e -> {
            modelWrapper.setUnderlying(webService.get(Endpoints.MANAGEMENT + "/" + clazz.getCanonicalName() + "/" + modelWrapper.getId(), clazz));
            populateMainArea(clazz, listView, centreBox, scrollPane);
        });
        if(viewNode.getClearButton() != null) {
            viewNode.getClearButton().setOnMouseClicked(e -> {
                AreYouSurePopup.show("This will delete all orders forever. Are you sure?", () -> {
                    webService.delete(Endpoints.MANAGEMENT + "/Orders/" + modelWrapper.getId());
                });
            });
        }
        if(viewNode.getUploadMenusButton() != null) {
            viewNode.getUploadMenusButton().setOnMouseClicked(e -> {
                File file = FileChooserPopup.showCsvPopup("Choose file", ManagementApplication.PRIMARY_STAGE);
                if(file != null) {
                    MenuUploader uploader = new MenuUploader(webService, ((Restaurant)modelWrapper.getUnderlying()).getStaffFacingId());
                    List<String> messages = uploader.upload(file);
                    if(messages != null && messages.size() > 0) {
                        StringBuilder builder = new StringBuilder();
                        messages.forEach(s -> builder.append(s).append("\n\r"));
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Upload result");
                        alert.setContentText(builder.toString());
                        alert.showAndWait();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Failed to upload menu items");
                        alert.setContentText("There might be something wrong with the file, or it doesn't exist.");
                        alert.showAndWait();
                    }
                }
            });
        }

        centreBox.getChildren().add(viewNode);
        scrollPane.setContent(centreBox);
    }

    private <T extends IDAble> void onAddNew(ListView<ModelWrapper<T>> listView, Class<T> clazz) {
        GenericFormPopup.show(clazz, null, true, x -> {
            try {
                T returned = webService.post(Endpoints.MANAGEMENT + "/" + clazz.getCanonicalName(), x, clazz);
                listView.getItems().add(new ModelWrapper<>(returned));
            } catch (Exception ex) {
                LOGGER.error("Could not insert {}", x, ex);
            }
        });
    }

    private <T extends IDAble> void onAddNewRestaurant(Class<T> clazz, ListView<ModelWrapper<T>> listView, VBox centreBox, ScrollPane scrollPane) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wizards/RestaurantWizard1.fxml"));
            Pane main = loader.load();
            Scene scene = new Scene(main);
            Stage stage = new Stage();
            stage.setScene(scene);

            WizardBaseController controller = loader.getController();
            controller.setCurrentStage(stage);
            controller.setOnFinish(r -> {
                //noinspection unchecked
                ModelWrapper<T> modelWrapper = (ModelWrapper<T>) new ModelWrapper<>(r);
                modelWrapper.setAppendFieldString("staffFacingId");
                listView.getItems().add(modelWrapper);
                sortRestaurantListView(listView);
                listView.getSelectionModel().select(modelWrapper);
                populateMainArea(clazz, listView, centreBox, scrollPane);
            });

            stage.show();

        } catch (IOException ex) {
            LOGGER.error("Error in wizard", ex);
        }
    }

    private <T extends IDAble> void onDelete(ListView<ModelWrapper<T>> listView, Class<T> clazz, VBox centreBox) {
        AreYouSurePopup.show("Are you sure? THIS IS NOT REVERSIBLE", () -> {
            final ModelWrapper<T> selectedItem = listView.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                try {
                    webService.delete(Endpoints.MANAGEMENT + "/" + clazz.getCanonicalName()+ "/" + selectedItem.getUnderlying().getId());
                    listView.getItems().remove(selectedItem);
                    centreBox.getChildren().clear();
                } catch (Exception ex) {
                    LOGGER.error("Could not delete {}", selectedItem, ex);
                }
                if(listView.getItems().size() > 0) {
                    listView.getSelectionModel().select(0);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void addStaff(ReflectionView reflectionView, Restaurant restaurant) {
        WebService webService = WebService.getWebService();
        List<Staff> staffs;
        if(restaurant.getId() == null) {
            staffs = new ArrayList<>();
        } else {
            staffs = webService.getAsList(Endpoints.MANAGEMENT + "/Staffs/"+restaurant.getId(), Staff.class);
        }

        Button addButton = new Button("Add...");
        if(restaurant.getId() == null) {
            addButton.setDisable(true);
        }
        addButton.setOnMouseClicked(e2 -> {
            Staff newStaff = new Staff();
            newStaff.setRestaurantId(restaurant.getId());
            GenericFormPopup.show(Staff.class, newStaff, false, s -> {
                s.setRestaurantId(restaurant.getId());
                webService.post(Endpoints.MANAGEMENT + "/" + Staff.class.getCanonicalName(), s, Object.class);
            });
        });
        if(staffs.size() == 0) {
            reflectionView.getExtraVBox().getChildren().addAll(new HBox(10, new Label("No staff members"), addButton));
        } else {
            Label header = new Label("Staff");
            VBox staffBox = new VBox();
            staffBox.setBorder(new Border(new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            staffBox.getChildren().add(header);
            for(Staff staffMember : staffs) {
                Label label = new Label(staffMember.getName());
                Button editButton = new Button("Edit");
                editButton.setOnMouseClicked(e2 -> {
                    GenericFormPopup.show(Staff.class, staffMember, false, s -> {
                        webService.put(Endpoints.MANAGEMENT + "/" + Staff.class.getCanonicalName() + "/" + s.getId(), s);
                    });
                });
                HBox hBox = new HBox(10, label, editButton);
                staffBox.getChildren().addAll(hBox);
                if(staffMember.getDeleted() != null) {
                    Label deletedLabel = new Label(" [DELETED]");
                    hBox.getChildren().add(deletedLabel);
                }
            }
            reflectionView.getExtraVBox().getChildren().addAll(staffBox, addButton);
        }
    }
    private void addPrinters(ReflectionView reflectionView, Restaurant restaurant) {
        WebService webService = WebService.getWebService();
        List<Printer> printers;
        if(restaurant.getId() == null) {
            printers = new ArrayList<>();
        } else {
            printers = webService.getAsList(Endpoints.MANAGEMENT + "/Printers/"+restaurant.getId(), Printer.class);
        }

        Button addButton = new Button("Add...");
        if(restaurant.getId() == null) {
            addButton.setDisable(true);
        }
        addButton.setOnMouseClicked(e2 -> {
            Printer newPrinter = new Printer();
            newPrinter.setRestaurantId(restaurant.getId());
            GenericFormPopup.show(Printer.class, newPrinter, false, s -> {
                s.setRestaurantId(restaurant.getId());
                webService.post(Endpoints.MANAGEMENT + "/" + Printer.class.getCanonicalName(), s, Object.class);
            });
        });
        if(printers.size() == 0) {
            reflectionView.getExtraVBox().getChildren().addAll(new HBox(10, new Label("No printers"), addButton));
        } else {
            Label header = new Label("Printer");
            VBox printerBox = new VBox();
            printerBox.setBorder(new Border(new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            printerBox.getChildren().add(header);
            for(Printer printer : printers) {
                Label label = new Label(printer.getName());
                Button editButton = new Button("Edit");
                editButton.setOnMouseClicked(e2 -> {
                    GenericFormPopup.show(Printer.class, printer, false, s -> {
                        webService.put(Endpoints.MANAGEMENT + "/" + Printer.class.getCanonicalName() + "/" + s.getId(), s);
                    });
                });
                printerBox.getChildren().addAll(new HBox(10, label, editButton));
            }
            reflectionView.getExtraVBox().getChildren().addAll(printerBox, addButton);
        }
    }

}
