package uk.co.epicuri.serverapi.management.ui;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.epicuri.serverapi.management.ManagementApplication;
import uk.co.epicuri.serverapi.management.event.ParameterisedSimpleAction;
import uk.co.epicuri.serverapi.management.reflect.PojoViewBuilder;
import uk.co.epicuri.serverapi.management.webservice.WebService;

/**
 * Created by manish
 */
public class GenericFormPopup<T> extends HBox{
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericFormPopup.class);

    private Class<T> clazz;
    private T instance;
    private Stage stage = new Stage();
    private ParameterisedSimpleAction<T> closeAction;

    public static <T> void show(Class<T> clazz, T instance, boolean createOnNull, ParameterisedSimpleAction<T> closeAction) {
        GenericFormPopup<T> popup = new GenericFormPopup<>(clazz, instance, createOnNull);
        popup.setOnClose(closeAction);
        popup.show();
    }

    public static <T> void showStringPopup(ParameterisedSimpleAction<String> closeAction) {
        GenericFormPopup<T> popup = new GenericFormPopup<>(closeAction);
        popup.show();
    }

    public GenericFormPopup(Class<T> clazz, T instance, boolean createOnNull) {
        this.clazz = clazz;
        if(instance == null && createOnNull) {
            try {
                this.instance = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Cannot instantiate", e);
            }
        } else {
            this.instance = instance;
        }
        constructForm();
    }

    public GenericFormPopup(ParameterisedSimpleAction<String> action) {
        constructStringForm(action);
    }

    public void show() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(new Scene(this));
        stage.show();
    }

    public void setOnClose(ParameterisedSimpleAction<T> action) {
        this.closeAction = action;
    }

    public T getInstance() {
        return instance;
    }

    private void close() {
        stage.close();
    }

    @SuppressWarnings("unchecked")
    private void constructForm() {
        ReflectionView viewNode = getReflectionView(clazz, instance);
        constructForm(viewNode);
    }

    private void constructForm(ReflectionView viewNode) {
        addScroll(viewNode);

        viewNode.getAffirmativeButton().setOnMouseClicked(x -> {
            if(closeAction != null) {
                closeAction.onAction(instance);
            }
            close();
        });

        viewNode.getRefreshButton().setVisible(false);
    }

    private void addScroll(ReflectionView viewNode) {
        ScrollPane scroll = new ScrollPane(viewNode);
        getChildren().add(scroll);
        scroll.setMaxHeight(ManagementApplication.INITIAL_WINDOW_HEIGHT);
    }

    private void constructStringForm(ParameterisedSimpleAction<String> action) {
        ReflectionView viewNode = getReflectionStringView((String)instance, action);
        addScroll(viewNode);

        MappedGridPane pane = viewNode.getParentPane();
        if(pane.getComponents().containsKey("actionableTextField")) {
            viewNode.getAffirmativeButton().setOnMouseClicked(x -> {
                action.onAction(((TextField)pane.getComponents().get("actionableTextField")).getText());
                close();
            });

        } else {
            viewNode.getAffirmativeButton().setOnMouseClicked(x -> {
                close();
            });
        }

        viewNode.getRefreshButton().setVisible(false);
    }

    @SuppressWarnings("unchecked")
    private static ReflectionView getReflectionView(Class clazz, Object instance) {
        return PojoViewBuilder.newInstance().withClass(clazz).withWebService(WebService.getWebService()).withInstance(instance).asView();
    }

    @SuppressWarnings("unchecked")
    private static ReflectionView getReflectionStringView(String instance, ParameterisedSimpleAction<String> action) {
        return PojoViewBuilder.newInstance().withClass(String.class).withWebService(WebService.getWebService()).withInstance(instance).asView(action);
    }
}
