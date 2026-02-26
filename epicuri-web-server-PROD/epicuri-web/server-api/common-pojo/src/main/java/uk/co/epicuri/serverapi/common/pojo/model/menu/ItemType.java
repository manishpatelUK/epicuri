package uk.co.epicuri.serverapi.common.pojo.model.menu;

public enum ItemType {
    FOOD(0, "Food"),
    DRINK(1, "Drink"),
    OTHER(2, "Other");

    private final int id;
    private final String name;
    ItemType(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static ItemType valueOf(int value) {
        if(value == 0) {
            return FOOD;
        } else if(value == 1) {
            return DRINK;
        } else return OTHER;
    }
}
