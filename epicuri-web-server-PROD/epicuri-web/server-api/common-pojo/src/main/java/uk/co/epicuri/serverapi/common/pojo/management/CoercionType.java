package uk.co.epicuri.serverapi.common.pojo.management;

/**
 * Created by manish.
 */
public enum CoercionType {
    NONE(null),
    INTEGER(Integer.class),
    DOUBLE(Double.class),
    STRING(String.class),
    BOOLEAN(Boolean.class);

    private final Class clazz;
    CoercionType(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }
}
