package uk.co.epicuri.waiter.model;

public enum WaiterAppFeature {
    LOGIN_MANAGER("Login Manager"),
    FLOOR_PLAN_MANAGER("Floorplan Manager"),
    MENU_MANAGER("Menu Manager"),
    CASH_UP("Cash Up / Close Service"),
    CASH_UP_SIMULATION("Cash Up (X Report)"),
    SESSION_HISTORY("Session History"),
    GENERIC_REFUND("Generic Refunds"),
    PORTAL("Portal Access"),
    ORDER_VOID("Void Orders"),
    DRAWER_KICK_NO_SALE("Cash Drawer Access"),
    MANUAL_DRAWER_KICK("Manual Cash Drawer Kick"),
    FORCE_CLOSE("Force Close"),
    ADD_DELETE_PAYMENT("Add/Delete Payments"),
    ADD_DELETE_DISCOUNT("Add/Delete Discounts"),
    PRICE_OVERRIDE("Price Override");

    private final String readableName;
    WaiterAppFeature(String readableName) {
        this.readableName = readableName;
    }

    public String getReadableName() {
        return readableName;
    }
}
