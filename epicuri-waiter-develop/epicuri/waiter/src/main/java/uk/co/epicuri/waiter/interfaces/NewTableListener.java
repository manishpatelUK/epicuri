package uk.co.epicuri.waiter.interfaces;

import uk.co.epicuri.waiter.model.EpicuriTable;

public interface NewTableListener {
    void createNewTable(String name, EpicuriTable.Shape shape);
    void updateTable(String tableId, String name, EpicuriTable.Shape shape);
}
