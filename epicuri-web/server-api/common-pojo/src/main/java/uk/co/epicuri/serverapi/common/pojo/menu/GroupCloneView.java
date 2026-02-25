package uk.co.epicuri.serverapi.common.pojo.menu;

import uk.co.epicuri.serverapi.common.pojo.common.IdPojoAndName;

public class GroupCloneView extends IdPojoAndName {
    private String categoryId;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
