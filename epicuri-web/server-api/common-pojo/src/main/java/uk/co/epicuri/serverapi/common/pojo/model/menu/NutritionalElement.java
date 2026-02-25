package uk.co.epicuri.serverapi.common.pojo.model.menu;

public enum NutritionalElement {
    ENERGY("energy"),

    //fats
    FAT("fat"),
    FAT_SATURATE("saturates"),
    FAT_MONO_UNSATURATE("mono-unsaturates"),
    FAT_POLY_UNSTAURATE("polyunsaturates"),

    //carbs
    CARBOHYDRATE("carbohydrate"),
    SUGAR("sugar"),
    POLYOLS("polyols"),
    STARCH("starch"),

    FIBRE("fibre"),
    PROTEIN("protein"),
    SALT("salt"),
    VITAMINS_AND_MINERALS("vitamins/minerals");

    private final String name;
    NutritionalElement(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
