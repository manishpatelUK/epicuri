package uk.co.epicuri.serverapi.common.pojo.menu;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import uk.co.epicuri.serverapi.common.pojo.RestaurantConstants;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuItemView implements Comparable<MenuItemView>{
    @JsonProperty("Id")
    private String id;

    @JsonProperty("DefaultPrinter")
    private String defaultPrinter;

    @JsonProperty("TypeName")
    private String typeName;

    @JsonProperty("MenuItemTypeId")
    private int menuItemTypeId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Price")
    private double price;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("imageURL")
    private String imageURL;

    @JsonProperty("DefaultCourses")
    private List<CourseView> defaultCourses = new ArrayList<>();

    @JsonProperty("ModifierGroups")
    private List<String> modifierGroups = new ArrayList<>();

    @JsonProperty("MenuGroups")
    private List<String> menuGroups;

    @JsonProperty("TaxTypeId")
    private String taxTypeId;

    @JsonProperty("Unavailable")
    private boolean unavailable;

    @JsonProperty("Ordering")
    private int ordering = 0; //new!

    @JsonProperty("OmitPrinting")
    private boolean omitPrinting;

    @JsonProperty("ServiceId")
    private String serviceId;

    @JsonProperty("Tags")
    private List<String> tags = Collections.unmodifiableList(new ArrayList<>());

    @JsonProperty("ShortCode")
    private String shortCode;

    @JsonProperty("priceInt")
    private int priceInt;

    @JsonProperty("colourHex")
    private String colourHex;

    @JsonProperty("plu")
    private String plu;

    private List<String> allergyIds = new ArrayList<>();
    private List<String> dietaryIds = new ArrayList<>();

    public MenuItemView(){}

    public MenuItemView(MenuItem menuItem, List<Course> courses, List<String> groups, int ordering) {
        this.id = menuItem.getId();
        this.defaultPrinter = menuItem.getDefaultPrinter();
        this.typeName = menuItem.getType().getName();
        this.menuItemTypeId = menuItem.getType().getId();
        this.name = menuItem.getName();
        this.price = MoneyService.toMoneyRoundNearest(menuItem.getPrice());
        this.description = menuItem.getDescription();
        this.imageURL = menuItem.getImageURL();
        this.defaultCourses = new ArrayList<>();
        this.defaultCourses = courses.stream().map(CourseView::new).collect(Collectors.toList());
        if(menuItem.getModifierGroupIds() != null) {
            this.modifierGroups = menuItem.getModifierGroupIds();
        }
        this.menuGroups = groups == null || groups.size() == 0 ? null : groups;
        this.taxTypeId = menuItem.getTaxTypeId();
        this.unavailable = menuItem.isUnavailable();
        this.ordering = ordering;
        this.omitPrinting = menuItem.isOmitPrinting();

        if(courses.size() >= 1) {
            this.serviceId = IDAble.extractParentId(courses.get(0).getId());
        }

        this.shortCode = menuItem.getShortCode();
        this.priceInt = menuItem.getPrice();
        this.colourHex = menuItem.getColourHex();
        if(menuItem.getAllergyIds() != null) {
            this.allergyIds = menuItem.getAllergyIds();
        }
        if(menuItem.getDietaryIds() != null) {
            this.dietaryIds = menuItem.getDietaryIds();
        }
        this.plu = menuItem.getPlu();
    }

    public MenuItemView(MenuItem menuItem, Course course, Service service, int ordering) {
        this.id = menuItem.getId();
        this.defaultPrinter = menuItem.getDefaultPrinter();
        this.typeName = menuItem.getType().getName();
        this.menuItemTypeId = menuItem.getType().getId();
        this.name = menuItem.getName();
        this.price = MoneyService.toMoneyRoundNearest(menuItem.getPrice());
        this.description = menuItem.getDescription();
        this.imageURL = menuItem.getImageURL();
        this.defaultCourses = new ArrayList<>();
        if(course == null) {
            course = RestaurantConstants.FALLBACK_COURSE;
        }
        this.defaultCourses.add(new CourseView(course));
        if(menuItem.getModifierGroupIds() != null) {
            this.modifierGroups = menuItem.getModifierGroupIds();
        }
        this.taxTypeId = menuItem.getTaxTypeId();
        this.unavailable = menuItem.isUnavailable();
        this.ordering = ordering;
        this.omitPrinting = menuItem.isOmitPrinting();
        if(service != null) {
            this.serviceId = service.getId();
        }
        this.shortCode = menuItem.getShortCode();
        this.priceInt = menuItem.getPrice();
        this.colourHex = menuItem.getColourHex();
        if(menuItem.getAllergyIds() != null) {
            this.allergyIds = menuItem.getAllergyIds();
        }
        if(menuItem.getDietaryIds() != null) {
            this.dietaryIds = menuItem.getDietaryIds();
        }
        this.plu = menuItem.getPlu();
    }

    public MenuItemView(MenuItem menuItem, int ordering) {
        this.id = menuItem.getId();
        this.defaultPrinter = menuItem.getDefaultPrinter();
        this.typeName = menuItem.getType().getName();
        this.menuItemTypeId = menuItem.getType().getId();
        this.name = menuItem.getName();
        this.price = MoneyService.toMoneyRoundNearest(menuItem.getPrice());
        this.description = menuItem.getDescription();
        this.imageURL = menuItem.getImageURL();
        this.defaultCourses = new ArrayList<>();
        if(menuItem.getModifierGroupIds() != null) {
            this.modifierGroups = menuItem.getModifierGroupIds();
        }
        this.taxTypeId = menuItem.getTaxTypeId();
        this.unavailable = menuItem.isUnavailable();
        this.ordering = ordering;
        this.omitPrinting = menuItem.isOmitPrinting();
        this.shortCode = menuItem.getShortCode();
        this.priceInt = menuItem.getPrice();
        this.colourHex = menuItem.getColourHex();
        if(menuItem.getAllergyIds() != null) {
            this.allergyIds = menuItem.getAllergyIds();
        }
        if(menuItem.getDietaryIds() != null) {
            this.dietaryIds = menuItem.getDietaryIds();
        }
        this.plu = menuItem.getPlu();
    }

    public MenuItemView(MenuItem menuItem, List<Course> courses, int ordering) {
        this.id = menuItem.getId();
        this.defaultPrinter = menuItem.getDefaultPrinter();
        this.typeName = menuItem.getType().getName();
        this.menuItemTypeId = menuItem.getType().getId();
        this.name = menuItem.getName();
        this.price = MoneyService.toMoneyRoundNearest(menuItem.getPrice());
        this.description = menuItem.getDescription();
        this.imageURL = menuItem.getImageURL();
        this.defaultCourses = courses.stream().map(CourseView::new).collect(Collectors.toList());
        if(menuItem.getModifierGroupIds() != null) {
            this.modifierGroups = menuItem.getModifierGroupIds();
        }
        //this.menuGroups = groups; // going to try without - see if it works
        this.taxTypeId = menuItem.getTaxTypeId();
        this.unavailable = menuItem.isUnavailable();
        this.ordering = ordering;
        this.omitPrinting = menuItem.isOmitPrinting();

        if(courses.size() >= 1) {
            this.serviceId = IDAble.extractParentId(courses.get(0).getId());
        }

        this.shortCode = menuItem.getShortCode();
        this.priceInt = menuItem.getPrice();
        this.colourHex = menuItem.getColourHex();
        if(menuItem.getAllergyIds() != null) {
            this.allergyIds = menuItem.getAllergyIds();
        }
        if(menuItem.getDietaryIds() != null) {
            this.dietaryIds = menuItem.getDietaryIds();
        }
        this.plu = menuItem.getPlu();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultPrinter() {
        return defaultPrinter;
    }

    public void setDefaultPrinter(String defaultPrinter) {
        this.defaultPrinter = defaultPrinter;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getMenuItemTypeId() {
        return menuItemTypeId;
    }

    public void setMenuItemTypeId(int menuItemTypeId) {
        this.menuItemTypeId = menuItemTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageURL;
    }

    public void setImageUrl(String imageUrl) {
        this.imageURL = imageUrl;
    }

    public List<CourseView> getDefaultCourses() {
        return defaultCourses;
    }

    public void setDefaultCourses(List<CourseView> defaultCourses) {
        this.defaultCourses = defaultCourses;
    }

    public List<String> getModifierGroups() {
        return modifierGroups;
    }

    public void setModifierGroups(List<String> modifierGroups) {
        this.modifierGroups = modifierGroups;
    }

    public List<String> getMenuGroups() {
        return menuGroups;
    }

    public void setMenuGroups(List<String> menuGroups) {
        this.menuGroups = menuGroups;
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

    public int getOrder() {
        return ordering;
    }

    public void setOrder(int order) {
        ordering = order;
    }

    public boolean isOmitPrinting() {
        return omitPrinting;
    }

    public void setOmitPrinting(boolean omitPrinting) {
        this.omitPrinting = omitPrinting;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        if(StringUtils.isBlank(shortCode)) {
            this.shortCode = null;
        } else {
            this.shortCode = shortCode.trim();
        }
    }

    public void setPriceInt(int priceInt) {
        this.priceInt = priceInt;
    }

    public int getPriceInt() {
        return priceInt;
    }

    public String getColourHex() {
        return colourHex;
    }

    public void setColourHex(String colourHex) {
        this.colourHex = colourHex;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int compareTo(MenuItemView o) {
        return Integer.compare(ordering,o.ordering);
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
}
