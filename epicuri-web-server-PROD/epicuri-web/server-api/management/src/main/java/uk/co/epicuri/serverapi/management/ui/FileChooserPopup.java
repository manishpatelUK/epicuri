package uk.co.epicuri.serverapi.management.ui;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uk.co.epicuri.serverapi.management.ManagementApplication;

import java.io.File;

/**
 * Created by manish.
 */
public class FileChooserPopup {
    public static File showImagePopup(String title) {
        return show(title, new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg"));
    }

    public static File showImagePopup(String title, Stage stage) {
        return show(title, stage, new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg"));
    }

    public static File showCsvPopup(String title, Stage stage) {
        return show(title, stage, new FileChooser.ExtensionFilter("Menu Upload Files", "*.csv"));
    }

    public static File show(String title, FileChooser.ExtensionFilter... filters) {
        return show(title, ManagementApplication.PRIMARY_STAGE, filters);
    }

    public static File show(String title, Stage stage, FileChooser.ExtensionFilter... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if(filters.length > 0) {
            fileChooser.getExtensionFilters().addAll(filters);
        }

        return fileChooser.showOpenDialog(stage);
    }
}
