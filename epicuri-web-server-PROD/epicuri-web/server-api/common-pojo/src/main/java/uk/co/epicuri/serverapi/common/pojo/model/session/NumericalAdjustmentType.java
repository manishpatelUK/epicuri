package uk.co.epicuri.serverapi.common.pojo.model.session;

public enum NumericalAdjustmentType {
    ABSOLUTE, PERCENTAGE;

    /**
     * client apps (waiter app) uses a numerical id
     * @param id 0-> ABSOLUTE, 1-> PERCENTAGE
     * @return
     */
    public static NumericalAdjustmentType fromClientId(int id) {
        if (id == 1) {
            return PERCENTAGE;
        }
        else return ABSOLUTE;
    }

    public static int toClientId(NumericalAdjustmentType type) {
        if (type == PERCENTAGE) {
            return 1;
        }
        else return 0;
    }
}
