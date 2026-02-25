package uk.co.epicuri.serverapi.management.ui;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by manish.
 */
public class MappedGridPane extends GridPane {
    private Map<String,Node> components = new HashMap<>();

    public MappedGridPane() {
        super();
    }

    public void add(String fieldName, Node child, int columnIndex, int rowIndex) {
        super.add(child, columnIndex, rowIndex);
        components.put(fieldName, child);
    }

    public Map<String, Node> getComponents() {
        return components;
    }
}
