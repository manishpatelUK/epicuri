package uk.co.epicuri.serverapi.management.controllers.wizards;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.management.event.ParameterisedSimpleAction;
import uk.co.epicuri.serverapi.management.event.SimpleAction;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.model.RestaurantFacade;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.io.IOException;

public class WizardBaseController {
    protected WebService webService = WebService.getWebService();

    protected Stage currentStage;
    protected RestaurantFacade restaurantFacade;
    protected ParameterisedSimpleAction<Restaurant> onFinish;
    private ObservableList<ModelWrapper<Restaurant>> currentList;

    protected void next(String fxml) {
        currentStage.close();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Pane main = null;
        try {
            main = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(main);
        Stage stage = new Stage();
        stage.setScene(scene);

        WizardBaseController controller = loader.getController();
        controller.setCurrentStage(stage);
        controller.setRestaurantFacade(restaurantFacade);
        controller.setOnFinish(onFinish);

        stage.show();
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void setRestaurantFacade(RestaurantFacade restaurantFacade) {
        this.restaurantFacade = restaurantFacade;
    }

    public void showPopup(String title, String header, String message) {
        showPopup(title, header, message, Alert.AlertType.WARNING);
    }

    public void showPopup(String title, String header, String message, Alert.AlertType type) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean checkNullEmpty(TextInputControl textField) {
        if(textField == null || textField.getText() == null) {
            return true;
        }

        return StringUtils.isBlank(textField.getText().trim());
    }

    public boolean checkNullEmptyAndNotInt(TextInputControl textField) {
        if(checkNullEmpty(textField)) {
            return true;
        }

        try {
            Integer.parseInt(textField.getText().trim());
        } catch (NumberFormatException ex) {
            return true;
        }

        return false;
    }

    public void setOnFinish(ParameterisedSimpleAction<Restaurant> onFinish) {
        this.onFinish = onFinish;
    }
}
