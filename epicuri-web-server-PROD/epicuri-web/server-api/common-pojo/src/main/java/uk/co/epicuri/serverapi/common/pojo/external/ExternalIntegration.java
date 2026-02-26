package uk.co.epicuri.serverapi.common.pojo.external;

import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;

@MgmtPojoModel
public enum ExternalIntegration {
    MEWS("MEWS"),
    PAYMENT_SENSE("PAYMENT_SENSE"),
    STRIPE("STRIPE"),
    EPICURI_ONLINE_ORDERS("EPICURI_ONLINE_ORDERS"),
    MARKET_MAN("MARKET_MAN"),
    XERO("XERO"),
    NONE("NONE");

    private final String key;
    ExternalIntegration(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
