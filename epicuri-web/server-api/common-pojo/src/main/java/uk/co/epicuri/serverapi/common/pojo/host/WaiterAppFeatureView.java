package uk.co.epicuri.serverapi.common.pojo.host;

import uk.co.epicuri.serverapi.common.pojo.model.restaurant.WaiterAppFeature;

public class WaiterAppFeatureView {
    private WaiterAppFeature capability;
    private String capabilityReadableName;
    private boolean enabled = true;

    public WaiterAppFeatureView(){}
    public WaiterAppFeatureView(WaiterAppFeature feature, boolean enabled) {
        this.capability = feature;
        this.capabilityReadableName = feature.getReadableName();
        this.enabled = enabled;
    }

    public WaiterAppFeature getCapability() {
        return capability;
    }

    public String getCapabilityReadableName() {
        return capabilityReadableName;
    }

    public void setCapabilityReadableName(String capabilityReadableName) {
        this.capabilityReadableName = capabilityReadableName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
