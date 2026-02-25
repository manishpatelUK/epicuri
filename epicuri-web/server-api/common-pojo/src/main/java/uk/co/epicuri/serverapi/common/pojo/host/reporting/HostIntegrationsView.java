package uk.co.epicuri.serverapi.common.pojo.host.reporting;

import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;

public class HostIntegrationsView {
    private String integration;
    private KVData kvData;

    public HostIntegrationsView(){}

    public HostIntegrationsView(ExternalIntegration integration, KVData kvData) {
        this.integration = integration.getKey();
        this.kvData = kvData;
    }

    public String getIntegration() {
        return integration;
    }

    public void setIntegration(String integration) {
        this.integration = integration;
    }

    public KVData getKvData() {
        return kvData;
    }

    public void setKvData(KVData kvData) {
        this.kvData = kvData;
    }
}
