package uk.co.epicuri.serverapi.common.pojo.external.paymentsense;

import java.util.ArrayList;
import java.util.List;

public class PACReports {
    private List<PACReport> reports = new ArrayList<>();

    public List<PACReport> getReports() {
        return reports;
    }

    public void setReports(List<PACReport> reports) {
        this.reports = reports;
    }
}
