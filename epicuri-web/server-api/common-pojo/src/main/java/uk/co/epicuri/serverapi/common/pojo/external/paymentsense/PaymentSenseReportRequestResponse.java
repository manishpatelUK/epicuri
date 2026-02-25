package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

public class PaymentSenseReportRequestResponse {
    private String requestId;
    private String location;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
