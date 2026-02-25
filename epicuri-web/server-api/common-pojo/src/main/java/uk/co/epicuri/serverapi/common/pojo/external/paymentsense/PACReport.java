package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PACReport extends ReportType {
    private String reportTime;
    private String reportResult;
    private String tpi;
    private String requestId;
    private String location; // e.g. https://ss890b840000.test.connect.paymentsense.cloud:443/pat/reports/3ea84e61-d871-45df-bbe8-b3e9c1e4487f
    private List<String> notifications = new ArrayList<>();

    private ReportBalance balances;
    private Map<String,Acquirer> banking = new HashMap<>();

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getReportResult() {
        return reportResult;
    }

    public void setReportResult(String reportResult) {
        this.reportResult = reportResult;
    }

    public String getTpi() {
        return tpi;
    }

    public void setTpi(String tpi) {
        this.tpi = tpi;
    }

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

    public List<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
    }

    public ReportBalance getBalances() {
        return balances;
    }

    public void setBalances(ReportBalance balances) {
        this.balances = balances;
    }

    public Map<String, Acquirer> getBanking() {
        return banking;
    }

    public void setBanking(Map<String, Acquirer> banking) {
        this.banking = banking;
    }
}
