package uk.co.epicuri.serverapi.common.pojo.model.menu;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.db.TableNames;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.menu.MenuItemView;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.List;

@Document(collection = TableNames.MENU_ITEMS)
public class MenuItem extends Deletable {
    @Indexed
    private String restaurantId;

    private String defaultPrinter;
    private ItemType type = ItemType.FOOD;
    private String name;
    private int price;
    private String description;
    private String imageURL;
    private List<String> modifierGroupIds = new ArrayList<>();
    private String taxTypeId;
    private boolean unavailable;
    private boolean omitPrinting;
    private boolean omitReporting;
    private String shortCode;
    private String colourHex = "#90A4AE";

    private List<String> allergyIds = new ArrayList<>();
    private List<String> dietaryIds = new ArrayList<>();

    private String plu;

    public MenuItem(){}

    public MenuItem(MenuItemView view) {
        if(StringUtils.isNotBlank(view.getId())) {
            setId(view.getId());
        }
        this.name = view.getName();
        if(view.getPrice() < 0) {
            view.setPrice(0);
        }
        this.price = MoneyService.toPenniesRoundNearest(view.getPrice());
        this.description = view.getDescription();
        this.taxTypeId = view.getTaxTypeId();
        this.defaultPrinter = view.getDefaultPrinter();
        this.unavailable = view.isUnavailable();
        this.modifierGroupIds = view.getModifierGroups();
        this.type = ItemType.valueOf(view.getMenuItemTypeId());
        this.imageURL = view.getImageUrl();
        this.shortCode = view.getShortCode();
        this.allergyIds = view.getAllergyIds();
        this.dietaryIds = view.getDietaryIds();
        if(view.getPlu() != null && view.getPlu().trim().length() > 0) {
            plu = view.getPlu();
        }
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getDefaultPrinter() {
        return defaultPrinter;
    }

    public void setDefaultPrinter(String defaultPrinter) {
        this.defaultPrinter = defaultPrinter;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trimToEmpty(name);
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<String> getModifierGroupIds() {
        return modifierGroupIds;
    }

    public void setModifierGroupIds(List<String> modifierGroupIds) {
        this.modifierGroupIds = modifierGroupIds;
    }

    public String getTaxTypeId() {
        return taxTypeId;
    }

    public void setTaxTypeId(String taxTypeId) {
        this.taxTypeId = taxTypeId;
    }

    public boolean isUnavailable() {
        return unavailable;
    }

    public void setUnavailable(boolean unavailable) {
        this.unavailable = unavailable;
    }

    public boolean isOmitPrinting() {
        return omitPrinting;
    }

    public void setOmitPrinting(boolean omitPrinting) {
        this.omitPrinting = omitPrinting;
    }

    public boolean isOmitReporting() {
        return omitReporting;
    }

    public void setOmitReporting(boolean omitReporting) {
        this.omitReporting = omitReporting;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getColourHex() {
        return colourHex;
    }

    public void setColourHex(String colourHex) {
        if(colourHex != null && colourHex.startsWith("#")) {
            this.colourHex = colourHex;
        } else {
            this.colourHex = "#000000";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public List<String> getAllergyIds() {
        return allergyIds;
    }

    public void setAllergyIds(List<String> allergyIds) {
        this.allergyIds = allergyIds;
    }

    public List<String> getDietaryIds() {
        return dietaryIds;
    }

    public void setDietaryIds(List<String> dietaryIds) {
        this.dietaryIds = dietaryIds;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }
}
