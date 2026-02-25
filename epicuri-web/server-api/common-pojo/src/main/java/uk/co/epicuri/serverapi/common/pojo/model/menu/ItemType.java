package uk.co.epicuri.serverapi.common.pojo.model.menu;

public enum ItemType {
    FOOD(0, "Food", "FOOD"),
    DRINK(1, "Drink", "DRINK"),
    OTHER(2, "Other", "OTHER"),

    ALL(33, "All", "ALL");

    private final int id;
    private final String name;
    private final String requestName;

    ItemType(int id, String name, String requestName){
        this.id = id;
        this.name = name;
        this.requestName = requestName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRequestName() {
        return requestName;
    }

    public static ItemType valueOf(int value) {
        if(value == 0) {
            return FOOD;
        } else if(value == 1) {
            return DRINK;
        } else return OTHER;
    }

    public static ItemType valueOfRequestName(String requestName) {
        if("FOOD".equals(requestName)) {
            return FOOD;
        } else if("DRINK".equals(requestName)) {
            return DRINK;
        } else if("OTHER".equals(requestName)) {
            return OTHER;
        }

        return ALL;
    }
}
