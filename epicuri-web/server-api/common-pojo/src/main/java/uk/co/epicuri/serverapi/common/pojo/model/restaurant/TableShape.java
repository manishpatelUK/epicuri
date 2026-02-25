package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

/**
 * Created by manish
 */
public enum TableShape {
    SQUARE(0),
    CIRCLE(1);

    private final int ordinal;

    TableShape(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public static TableShape fromInt(int ordinal) {
        if(ordinal == 0) {
            return SQUARE;
        } else {
            return CIRCLE;
        }
    }
}
