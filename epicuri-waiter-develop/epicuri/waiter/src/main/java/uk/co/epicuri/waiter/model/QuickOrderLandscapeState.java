package uk.co.epicuri.waiter.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uk.co.epicuri.waiter.service.CalculationService;

public class QuickOrderLandscapeState implements Serializable {
    private EpicuriSessionDetail currentSession;
    private CalculationService calculator;
    private ArrayList<EpicuriOrderItem> orders;
    private List<NewAdjustmentRequest> unsyncedDiscounts;

    public QuickOrderLandscapeState(EpicuriSessionDetail currentSession,
                                    CalculationService calculator,
                                    ArrayList<EpicuriOrderItem> orders,
                                    List<NewAdjustmentRequest> unsyncedDiscounts){
        this.currentSession = currentSession;
        this.calculator = calculator;
        this.orders = orders;
        this.unsyncedDiscounts = unsyncedDiscounts;
    }

    public EpicuriSessionDetail getCurrentSession() {
        return currentSession;
    }

    public CalculationService getCalculator() {
        return calculator;
    }

    public ArrayList<EpicuriOrderItem> getOrders() {
        return orders;
    }

    public List<NewAdjustmentRequest> getUnsyncedDiscounts() {
        return unsyncedDiscounts;
    }
}