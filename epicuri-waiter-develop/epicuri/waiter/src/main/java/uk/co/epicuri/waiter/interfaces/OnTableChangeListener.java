package uk.co.epicuri.waiter.interfaces;

public interface OnTableChangeListener {
    void onTableSelected(String tableId);

    void onNoTableSelected();

    void onHighlightedTablesChanged(boolean selected);
}
