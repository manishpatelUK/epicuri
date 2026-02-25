package uk.co.epicuri.serverapi.common.pojo.model.menu;

public class NutritionalInfo {
    private NutritionalElement key;
    private String value;
    private String unit;

    public NutritionalElement getKey() {
        return key;
    }

    public void setKey(NutritionalElement key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
