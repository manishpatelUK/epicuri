package uk.co.epicuri.serverapi.management.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.management.ManagementApplication;
import uk.co.epicuri.serverapi.management.event.ParameterisedSimpleAction;
import uk.co.epicuri.serverapi.management.event.SimpleAction;

/**
 * Created by manish
 */
public class LoginController extends BaseController{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @FXML
    public TextField userNameField;

    @FXML
    public PasswordField passwordField;

    private Stage parentStage;
    private SimpleAction onSuccessLogin;
    private ParameterisedSimpleAction<String> onFailedLogin;

    public LoginController() {
        super();
    }

    @FXML
    private void initialize() {

    }

    public void onKeyPressed(KeyEvent keyEvent){
        if (keyEvent.getCode() == KeyCode.ENTER)  {
            loginPressed(null);
        }
    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void loginPressed(ActionEvent actionEvent) {
        if(userNameField.getText() == null || passwordField.getText() == null) {
            return;
        }

        String username = userNameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            webService.doLogin(username, password);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed");
            alert.setContentText("Something went wrong:\n\r" + ex);

            alert.showAndWait();

            LOGGER.info("Login failed");
            if(onFailedLogin != null) {
                onFailedLogin.onAction(ExceptionUtils.getStackTrace(ex));
            }
        }
        if(!webService.isValid()) {
            LOGGER.info("Login failed");
            if(onFailedLogin != null) {
                onFailedLogin.onAction("Username/password was incorrect (probably)");
            }
        } else {
            if(onSuccessLogin != null) {
                ManagementApplication.USER = username;
                ManagementApplication.PW = password;
                onSuccessLogin.onAction();
            }
        }

        parentStage.close();
    }

    public void cancelPressed(ActionEvent actionEvent) {
        parentStage.close();
    }

    public void setOnFailedLogin(ParameterisedSimpleAction<String> action) {
        this.onFailedLogin = action;
    }

    public void setOnSuccessLogin(SimpleAction action) {
        this.onSuccessLogin = action;
    }
}
