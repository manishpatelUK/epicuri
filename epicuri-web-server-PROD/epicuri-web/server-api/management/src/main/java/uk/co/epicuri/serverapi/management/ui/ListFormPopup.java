package uk.co.epicuri.serverapi.management.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.management.event.ParameterisedSimpleAction;
import uk.co.epicuri.serverapi.management.model.ModelWrapper;
import uk.co.epicuri.serverapi.management.reflect.ListViewBuilder;
import uk.co.epicuri.serverapi.management.webservice.WebService;

/**
 * Created by Manish Patel
 */
public class ListFormPopup<T> extends HBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListFormPopup.class);

    private final Class<T> clazz;
    private final ParameterisedSimpleAction<T> closeAction;
    private final String message;
    private final WebService webService;
    private final Stage stage = new Stage();

    public static <T> void show(Class<T> clazz,
                                ParameterisedSimpleAction<T> closeAction,
                                String message) {
        ListFormPopup<T> popup= new ListFormPopup<>(clazz, closeAction, message);
        popup.constructForm();
        popup.show();
    }

    public ListFormPopup(Class<T> clazz, ParameterisedSimpleAction<T> closeAction, String message){
        this.clazz = clazz;
        this.closeAction = closeAction;
        this.message = message;
        this.webService = WebService.getWebService();
    }

    @SuppressWarnings("unchecked")
    private void constructForm() {
        ListViewBuilder listViewBuilder = ListViewBuilder.newInstance();
        ListView<ModelWrapper> listView = listViewBuilder.withClass(clazz).withWebService(webService).asList();

        ScrollPane scrollPane = new ScrollPane(listView);
        Button okButton = new Button("OK");
        okButton.setOnMouseClicked(e -> {
            if(closeAction != null) {
                ModelWrapper modelWrapper = listView.getSelectionModel().getSelectedItem();
                if(modelWrapper != null) {
                    closeAction.onAction((T) modelWrapper.getUnderlying());
                }
            }
            close();
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnMouseClicked(e -> close());

        getChildren().addAll(new Label(message), scrollPane, new VBox(cancelButton, okButton));
    }

    private void close() {
        stage.close();
    }

    public void show() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(this));
        stage.show();
    }
}
