package uk.co.epicuri.serverapi.common.pojo.menu;

import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;

public class CategoryCloneView extends IdPojoAndName {
    private String menuId;

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }
}
