package uk.co.epicuri.serverapi.common.pojo.model.session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish on 20/12/2017.
 */
public class OrderResponse extends SimpleSessionPayload {
    private List<HostBatchView> batches = new ArrayList<>();
    private HostSessionView hostSessionView;

    public List<HostBatchView> getBatches() {
        return batches;
    }

    public void setBatches(List<HostBatchView> batches) {
        this.batches = batches;
    }

    public HostSessionView getHostSessionView() {
        return hostSessionView;
    }

    public void setHostSessionView(HostSessionView hostSessionView) {
        this.hostSessionView = hostSessionView;
    }
}
