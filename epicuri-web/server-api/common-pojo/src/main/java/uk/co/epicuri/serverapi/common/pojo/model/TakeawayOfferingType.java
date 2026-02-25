package uk.co.epicuri.serverapi.common.pojo.model;

public enum TakeawayOfferingType {
    NOT_OFFERED(0),
    DELIVERY_ONLY(1),
    COLLECTION_ONLY(2),
    DELIVERY_AND_COLLECTION(3);

    private final int apiExpose;
    TakeawayOfferingType(int apiExpose) {
        this.apiExpose = apiExpose;
    }

    public int getApiExpose() {
        return apiExpose;
    }
}
