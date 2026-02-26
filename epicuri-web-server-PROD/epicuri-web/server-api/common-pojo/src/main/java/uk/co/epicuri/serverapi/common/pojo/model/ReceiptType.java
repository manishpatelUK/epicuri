package uk.co.epicuri.serverapi.common.pojo.model;

public enum ReceiptType {
    NORMAL(0),
    HOTEL(1);

    private final int apiExpose;
    ReceiptType(int apiExpose){

        this.apiExpose = apiExpose;
    }

    public int getApiExpose() {
        return apiExpose;
    }
}
