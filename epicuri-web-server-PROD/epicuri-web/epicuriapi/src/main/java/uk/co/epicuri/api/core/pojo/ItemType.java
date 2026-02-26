package uk.co.epicuri.api.core.pojo;

/**
 * 28/08/2014
 */
public enum ItemType {
    FOOD(0, "Food"),
    DRINK(1, "Drink"),
    OTHER(2, "Other");

    private final int id;
    private final String name;
    private ItemType(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
