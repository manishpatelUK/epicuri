package uk.co.epicuri.serverapi.common.pojo.common;

public class IdPojoAndName extends IdPojo {
    private String name;

    public IdPojoAndName() {}

    public IdPojoAndName(String id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
