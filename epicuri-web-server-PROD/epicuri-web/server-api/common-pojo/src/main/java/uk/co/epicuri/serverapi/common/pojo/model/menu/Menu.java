package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtEditableField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtLong2DateConvert;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Document(collection = TableNames.MENUS)
public class Menu extends Deletable {
    @MgmtDisplayField
    private String name;

    @MgmtEditableField(editable = false)
    @Indexed
    private String restaurantId;

    @MgmtEditableField(editable = false)
    @MgmtLong2DateConvert
    private long lastUpdate;

    private boolean active = true;

    private String imageURL;

    private int order = Integer.MAX_VALUE;

    @MgmtIgnoreField
    private List<Category> categories = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trimToEmpty(name);
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        Collections.sort(this.categories, Category.COMPARE_CATEGORY);
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
