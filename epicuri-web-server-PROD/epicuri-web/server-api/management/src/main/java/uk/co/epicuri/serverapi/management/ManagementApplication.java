package uk.co.epicuri.serverapi.management;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Manish Patel
 */
public class ManagementApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementApplication.class);
    public static Stage PRIMARY_STAGE;
    public static double INITIAL_WINDOW_WIDTH;
    public static double INITIAL_WINDOW_HEIGHT;

    public static String USER;
    public static String PW;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("Start up...");
        LOGGER.info("Java Version {}", System.getProperty("java.version"));
        LOGGER.info("Java FX Version {}", System.getProperty("javafx.runtime.version"));

        PRIMARY_STAGE = stage;

        stage.setOnCloseRequest(e -> {
            Platform.exit();
        });
        stage.setTitle("Epicuri Management Portal");

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        //set Stage boundaries to visible bounds of the main screen
        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        INITIAL_WINDOW_WIDTH = primaryScreenBounds.getWidth() * 0.75;
        stage.setWidth(INITIAL_WINDOW_WIDTH);
        INITIAL_WINDOW_HEIGHT = primaryScreenBounds.getHeight() * 0.75;
        stage.setHeight(INITIAL_WINDOW_HEIGHT);

        Pane main = FXMLLoader.load(getClass().getResource("/MainView.fxml"));
        Scene scene = new Scene(main);
        stage.setScene(scene);
        stage.show();
    }
}
