package uk.co.epicuri.serverapi.management.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;

/**
 * Created by manish.
 */
public class ReflectionView extends VBox {
    private Button affirmativeButton, refreshButton, clearOrdersButton, uploadMenusButton;
    private VBox extraVBox;
    private MappedGridPane parentPane;

    public ReflectionView() {
    }

    public ReflectionView(double spacing) {
        super(spacing);
    }

    public ReflectionView(Node... children) {
        super(children);
    }

    public ReflectionView(double spacing, Node... children) {
        super(spacing, children);
        for(Node child : children) {
            if(child instanceof MappedGridPane) {
                parentPane = (MappedGridPane)child;
                break;
            }
        }
    }

    public Button getAffirmativeButton() {
        return affirmativeButton;
    }

    public void setAffirmativeButton(Button affirmativeButton) {
        this.affirmativeButton = affirmativeButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public void setRefreshButton(Button refreshButton) {
        this.refreshButton = refreshButton;
    }

    public VBox getExtraVBox() {
        return extraVBox;
    }

    public void setExtraVBox(VBox extraVBox) {
        this.extraVBox = extraVBox;
    }

    public Button getClearButton() {
        return clearOrdersButton;
    }

    public void setClearButton(Button clearOrdersButton) {
        this.clearOrdersButton = clearOrdersButton;
    }

    public MappedGridPane getParentPane() {
        return parentPane;
    }

    public Button getUploadMenusButton() {
        return uploadMenusButton;
    }

    public void setUploadMenusButton(Button uploadMenusButton) {
        this.uploadMenusButton = uploadMenusButton;
    }
}
