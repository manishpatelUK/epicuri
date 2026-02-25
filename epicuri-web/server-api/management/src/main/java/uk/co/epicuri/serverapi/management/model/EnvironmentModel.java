package uk.co.epicuri.serverapi.management.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Created by manish
 */
public class EnvironmentModel  {

    private final ObservableList<Environment> environments = FXCollections.observableArrayList();

    public EnvironmentModel() {
        environments.addAll(Environment.values());
    }

    public ObservableList<Environment> getEnvironments() {
        return environments;
    }
}
