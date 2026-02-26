package uk.co.epicuri.waiter.model;

import java.io.Serializable;

/**
 * Created by manish on 28/12/2017.
 */

public enum NumericalAdjustmentType implements Serializable {
    MONETARY(0), PERCENTAGE(1);
    private final int id;
    NumericalAdjustmentType(int id) { this.id=  id; }

    public int getId() {
        return id;
    }
}
