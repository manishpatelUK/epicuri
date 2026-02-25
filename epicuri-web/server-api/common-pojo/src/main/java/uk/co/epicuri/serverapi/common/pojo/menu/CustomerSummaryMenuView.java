package uk.co.epicuri.serverapi.common.pojo.menu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Menu;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerSummaryMenuView {
    @JsonProperty("MenuId")
    private String menuId;

    @JsonProperty("MenuName")
    private String menuName;

    @JsonProperty("TakeAwayMenu")
    private boolean takeawayMenu;

    @JsonProperty("imageURL")
    private String imageURL;

    public CustomerSummaryMenuView(){}

    public CustomerSummaryMenuView(Restaurant restaurant, Menu menu){
        this.menuId = menu.getId();
        this.menuName = menu.getName();
        this.takeawayMenu = menu.getId() != null && menu.getId().equals(restaurant.getTakeawayMenu());
        this.imageURL = menu.getImageURL();
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public boolean isTakeawayMenu() {
        return takeawayMenu;
    }

    public void setTakeawayMenu(boolean takeawayMenu) {
        this.takeawayMenu = takeawayMenu;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
