package uk.co.epicuri.serverapi.management.reflect;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.ArrayUtils;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.LatLongPair;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.management.event.ParameterisedSimpleAction;
import uk.co.epicuri.serverapi.management.ui.MappedGridPane;
import uk.co.epicuri.serverapi.management.ui.ReflectionView;
import uk.co.epicuri.serverapi.management.webservice.WebService;

import java.lang.reflect.Field;

/**
 * Created by manish
 */
public class ReflectViewCreator<T> {
    private WebService webService;

    ReflectViewCreator() {

    }

    public void setWebService(WebService webService) {
        this.webService = webService;
    }

    public ReflectionView createStringViewNode(String instance, ParameterisedSimpleAction<String> action) {
        Button okButton = new Button("Save");
        Button refreshButton = new Button("Refresh");
        ButtonBar controlButtons = new ButtonBar();
        controlButtons.getButtons().add(okButton);
        controlButtons.getButtons().add(refreshButton);
        VBox vBox = new VBox();

        GridPane pane = ReflectUICreator.createSingleStringGridPane(instance);

        ReflectionView view = new ReflectionView(13, pane, vBox, controlButtons);
        view.setAffirmativeButton(okButton);
        view.setRefreshButton(refreshButton);
        view.setExtraVBox(vBox);
        view.setPadding(new Insets(10,15,10,15));
        return view;
    }

    public ReflectionView createViewNode(Class<T> clazz, T instance) {
        Field[] fields = clazz.getDeclaredFields();
        if(instance instanceof IDAble) {
            try {
                Field idField = IDAble.class.getDeclaredField("id");
                fields = ArrayUtils.add(fields, idField);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        MappedGridPane pane = ReflectUICreator.createGridPane(fields, instance);

        Button okButton = new Button("Save");
        Button refreshButton = new Button("Refresh");
        ButtonBar controlButtons = new ButtonBar();
        controlButtons.getButtons().add(okButton);
        controlButtons.getButtons().add(refreshButton);
        VBox vBox = new VBox();

        ReflectionView view = new ReflectionView(13, pane, vBox, controlButtons);
        view.setAffirmativeButton(okButton);
        view.setRefreshButton(refreshButton);
        view.setExtraVBox(vBox);
        if(clazz == Restaurant.class){
            Button clearOrders = new Button("Clear Live Data");
            controlButtons.getButtons().add(clearOrders);
            view.setClearButton(clearOrders);
            Button uploadMenus = new Button("Upload menu file");
            controlButtons.getButtons().add(uploadMenus);
            view.setUploadMenusButton(uploadMenus);
        }
        if(clazz == LatLongPair.class){
            Button postcodeLookup = new Button("Lookup Postcode");
            controlButtons.getButtons().add(postcodeLookup);
            Field[] finalFields = fields;
            postcodeLookup.setOnMouseClicked(e -> {
                postCodeClicked((LatLongPair)instance, finalFields, pane);
            });
        }
        if(clazz == KVData.class) {
            Button clear = new Button("Clear Internal");
            controlButtons.getButtons().add(clear);
            view.setClearButton(clear);
        }
        view.setPadding(new Insets(10,15,10,15));
        return view;
    }

    private void postCodeClicked(LatLongPair original, Field[] fields, MappedGridPane pane) {
        HBox hBox = new HBox();
        hBox.getChildren().add(new Label("Post code"));
        TextField field = new TextField();
        hBox.getChildren().add(field);
        Button lookup = new Button("Lookup");
        hBox.getChildren().add(lookup);
        hBox.setPadding(new Insets(10,15,10,15));

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(hBox));

        lookup.setOnMouseClicked(e -> {
            String postCode = field.getText().trim().toLowerCase().replaceAll("\\s", "");
            LatLongPair pair = webService.postCodeLookup(postCode);
            if(pair != null) {
                original.setLongitude(pair.getLongitude());
                original.setLatitude(pair.getLatitude());
                stage.close();
                ReflectUICreator.refreshMappedGridPane(fields, original, pane);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not get postcode");
                alert.setContentText(postCode + ": did not yield any results");
                alert.showAndWait();
            }
        });

        stage.show();
    }
}
