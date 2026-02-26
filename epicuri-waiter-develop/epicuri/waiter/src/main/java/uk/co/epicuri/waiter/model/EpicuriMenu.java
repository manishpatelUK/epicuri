package uk.co.epicuri.waiter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.joda.money.Money;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.epicuri.waiter.service.MoneyService;
import uk.co.epicuri.waiter.ui.menueditor.MenuLevelFragment.Level;

public class EpicuriMenu implements Serializable {
    private static final String TAG_NAME = "MenuName";
    private static final String TAG_ACTIVE = "Active";
    private static final String TAG_ID = "Id";

    private static final String TAG_CATEGORIES = "MenuCategories";
    private static final String TAG_ITEMS = "MenuItems";

    private static final String TAG_MODIFIERGROUPS = "ModifierGroups";

    private static final String TAG_MODIFIERGROUP_NAME = "GroupName";
    private static final String TAG_MODIFIERGROUP_ID = "Id";
    private static final String TAG_MODIFIERGROUP_LOWER_LIMIT = "LowerLimit";
    private static final String TAG_MODIFIERGROUP_UPPER_LIMIT = "UpperLimit";
    private static final String TAG_MODIFIERGROUP_VALUES = "Modifiers";

    private static final String TAG_MODIFIERVALUE_ID = "Id";
    private static final String TAG_MODIFIERVALUE_NAME = "ModifierValue";
    private static final String TAG_MODIFIERVALUE_PRICE = "Price";
    private static final String TAG_MODIFIERVALUE_TAXTYPEID = "TaxTypeId";
    private static final String TAG_MODIFIERVALUE_PRICE_INT = "PriceInt";
    private static final String TAG_MODIFIERVALUE_PLU = "plu";
    private final String id;
    private final String name;
    private final boolean active;
    private final List<Category> categories;

    public EpicuriMenu(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getString(TAG_ID);
        name = jsonObject.getString(TAG_NAME);
        active = jsonObject.getBoolean(TAG_ACTIVE);
        categories = new ArrayList<EpicuriMenu.Category>();

        JSONArray categoriesJson = jsonObject.getJSONArray(TAG_CATEGORIES);
        for (int i = 0; i < categoriesJson.length(); i++) {
            JSONObject categoryJson = categoriesJson.getJSONObject(i);

            EpicuriMenu.Category category = new EpicuriMenu.Category(categoryJson);

            categories.add(category);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Category getCategory(String id) {
        for (Category c : categories) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder cats = new StringBuilder("(");
        for (Category c : categories) {
            cats.append(c.toString());
        }
        cats.append(")");
        return String.format("epicuriMenu: %s, %s", name, cats);
    }

    public interface MenuLevel {
        String getId();

        String getName();

        Level getType();

        int getOrderIndex();
    }

    public static class Category implements MenuLevel, Parcelable, Serializable {
        public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<EpicuriMenu.Category>() {

            @Override
            public Category[] newArray(int size) {
                return new Category[size];
            }

            @Override
            public Category createFromParcel(Parcel source) {
                return new Category(source);
            }
        };
        private static final String TAG_CATEGORY_NAME = "CategoryName";
        private static final String TAG_CATEGORY_ID = "Id";
        private static final String TAG_CATEGORY_GROUPS = "MenuGroups";
        private static final String TAG_CATEGORY_ORDER = "Order";
        private static final String TAG_CATEGORY_DEFAULT_COURSES = "DefaultCourses";
        private final String id;
        private final String name;
        private final int orderIndex;
        private final List<Group> groups;
        private final ArrayList<Course> defaultCourses;

        public Category(JSONObject categoryJson) throws JSONException {
            name = categoryJson.getString(TAG_CATEGORY_NAME);
            id = categoryJson.getString(TAG_CATEGORY_ID);
            orderIndex = categoryJson.getInt(TAG_CATEGORY_ORDER);

            JSONArray groupsJson = categoryJson.getJSONArray(TAG_CATEGORY_GROUPS);
            groups = new ArrayList<EpicuriMenu.Group>(groupsJson.length());
            for (int j = 0; j < groupsJson.length(); j++) {
                JSONObject groupJson = groupsJson.getJSONObject(j);

                EpicuriMenu.Group group = new EpicuriMenu.Group(groupJson, id);
                groups.add(group);
            }

            JSONArray coursesJson = categoryJson.getJSONArray(TAG_CATEGORY_DEFAULT_COURSES);
            defaultCourses = new ArrayList<EpicuriMenu.Course>(coursesJson.length());
            for (int i = 0; i < coursesJson.length(); i++) {
                JSONObject courseJson = coursesJson.getJSONObject(i);

                EpicuriMenu.Course course = new Course(courseJson);
                defaultCourses.add(course);
            }
        }

        private Category(Parcel in) {
            id = in.readString();
            name = in.readString();
            orderIndex = in.readInt();
            groups = in.createTypedArrayList(Group.CREATOR);
            defaultCourses = in.createTypedArrayList(Course.CREATOR);
        }

        public String getId() {
            return id;
        }

        @Override
        public Level getType() {
            return Level.CATEGORY;
        }

        public String getName() {
            return name;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public List<Item> getItems() {
            List<EpicuriMenu.Item> items = new ArrayList<>(1);

            if (getGroups() == null) return items;

            for (EpicuriMenu.Group group : getGroups()) {
                items.addAll(group.getItems());
            }

            return items;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        public Group getGroup(String id) {
            for (Group g : groups) {
                if (g.getId().equals(id)) return g;
            }
            return null;
        }

        public String[] getDefaultCourseIds() {
            String[] courseIds = new String[defaultCourses.size()];
            for (int i = 0; i < defaultCourses.size(); i++) {
                courseIds[i] = defaultCourses.get(i).getId();
            }
            return courseIds;
        }

        public ArrayList<Course> getDefaultCourses() {
            return defaultCourses;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeInt(orderIndex);
            dest.writeTypedList(groups);
            dest.writeTypedList(defaultCourses);
        }
    }

    public static class Group implements MenuLevel, Parcelable, Serializable {
        public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<EpicuriMenu.Group>() {

            @Override
            public Group[] newArray(int size) {
                return new Group[size];
            }

            @Override
            public Group createFromParcel(Parcel source) {
                return new Group(source);
            }
        };
        private static final String TAG_GROUP_ID = "Id";
        private static final String TAG_GROUP_NAME = "GroupName";
        private static final String TAG_GROUP_ORDER = "Order";
        private final String id;
        private final String categoryId;
        private final String name;
        private final int orderIndex;
        private final ArrayList<Item> items;

        public Group(JSONObject groupJson, String categoryId) throws JSONException {
            id = groupJson.getString(TAG_GROUP_ID);
            name = groupJson.getString(TAG_GROUP_NAME);
            orderIndex = groupJson.getInt(TAG_GROUP_ORDER);
            this.categoryId = categoryId;

            JSONArray itemsJson = groupJson.getJSONArray(TAG_ITEMS);
            items = new ArrayList<EpicuriMenu.Item>(itemsJson.length());
            for (int k = 0; k < itemsJson.length(); k++) {
                JSONObject itemJson = itemsJson.getJSONObject(k);
                EpicuriMenu.Item item = new EpicuriMenu.Item(itemJson);
                items.add(item);
            }
//            sortByName();
        }

        private Group(Parcel in) {
            id = in.readString();
            categoryId = in.readString();
            name = in.readString();
            orderIndex = in.readInt();
            items = in.createTypedArrayList(Item.CREATOR);
        }

        public void sortByName(){
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item lhs, Item rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
        }

        public void sortByCode(){
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item lhs, Item rhs) {
                    return lhs.getShortCode().compareTo(rhs.getShortCode());
                }
            });
        }

        public String getId() {
            return id;
        }

        public String getCategoryId() {
            return categoryId;
        }

        @Override
        public Level getType() {
            return Level.GROUP;
        }

        public String getName() {
            return name;
        }

        public ArrayList<String> getItemIds() {
            ArrayList<String> itemIds = new ArrayList<String>(items.size());
            for (int i = 0; i < items.size(); i++) {
                itemIds.add(items.get(i).getId());
            }
            return itemIds;
        }

        public ArrayList<Item> getItems() {
            return items;
        }

        public int getOrderIndex() {
            return orderIndex;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(categoryId);
            dest.writeString(name);
            dest.writeInt(orderIndex);
            dest.writeTypedList(items);
        }
    }

    public static class Item implements Parcelable, MenuLevel, Serializable {
        public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
            public Item createFromParcel(Parcel in) {
                return new Item(in);
            }

            public Item[] newArray(int size) {
                return new Item[size];
            }
        };
        static final String TAG_ITEM_SHORT_CODE = "ShortCode";
        static final String TAG_ITEM_ALLERGENS = "Allergens";
        static final String TAG_ITEM_DIET = "Diet";
        static final String TAG_ITEM_COLOUR = "colourHex";
        static final String TAG_ALLERGIES = "allergyIds";
        static final String TAG_DIETS = "dietaryIds";
        static final String TAG_IMAGE_URL = "imageURL";
        private static final String TAG_ITEM_NAME = "Name";
        private static final String TAG_ITEM_DESCRIPTION = "Description";
        private static final String TAG_ITEM_PRICE = "Price";
        private static final String TAG_ITEM_PRICE_INT = "priceInt";
        private static final String TAG_ITEM_DEFAULT_COURSES = "DefaultCourses";
        private static final String TAG_ITEM_DEFAULT_COURSE_SERVICE_ID = "ServiceId";
        private static final String TAG_ITEM_ID = "Id";
        private static final String TAG_ITEM_DEFAULT_PRINTER_ID = "DefaultPrinter";
        private static final String TAG_ITEM_MENU_GROUPS = "MenuGroups";
        private static final String TAG_ITEM_TAGS = "Tags";
        private static final String TAG_ITEM_TAX_RATE_ID = "TaxTypeId";
        private static final String TAG_ITEM_TYPE_ID = "MenuItemTypeId";
        private static final String TAG_ITEM_UNAVAILABLE = "Unavailable";
        private static final String TAG_ITEM_PLU = "plu";
        private static final String TAG_ITEM_ORDER = "Order";
        private final String name;
        private final Map<String, Course> courseByService;
        private final Money price;
        private final String id;
        private final String defaultPrinterId;
        private final String description;
        private final String[] modifierGroupIds;
        private final Tag[] tags;
        private final String[] menuGroups;
        private final String taxTypeid;
        private final int itemTypeId;
        private final boolean unavailable;
        private final String shortCode;
        private final String allergens;
        private final String diet;
        private final int priceInt;
        private final String colourHex;
        private String orderId = "0";
        private ArrayList<String> allergiesKeys;
        private ArrayList<String> dietsKeys;
        private String imageUrl;
        private String plu;

        public Item(JSONObject itemJson) throws JSONException {
            name = itemJson.getString(TAG_ITEM_NAME);
            courseByService = new HashMap<>();
            if (!itemJson.isNull(TAG_ITEM_DEFAULT_COURSES)) {
                JSONArray defaultCoursesJson = itemJson.getJSONArray(TAG_ITEM_DEFAULT_COURSES);
                for (int i = 0; i < defaultCoursesJson.length(); i++) {
                    JSONObject aCourseJson = defaultCoursesJson.getJSONObject(i);
                    Course aCourse = new Course(aCourseJson);
                    String serviceId = aCourseJson.getString(TAG_ITEM_DEFAULT_COURSE_SERVICE_ID);
                    courseByService.put(serviceId, aCourse);
                }
            }
            id = itemJson.getString(TAG_ITEM_ID);
            defaultPrinterId = itemJson.getString(TAG_ITEM_DEFAULT_PRINTER_ID);
            taxTypeid = itemJson.getString(TAG_ITEM_TAX_RATE_ID);
            itemTypeId = itemJson.getInt(TAG_ITEM_TYPE_ID);
            unavailable = itemJson.getBoolean(TAG_ITEM_UNAVAILABLE);
            if (itemJson.has(TAG_ITEM_SHORT_CODE)) {
                shortCode = itemJson.getString(TAG_ITEM_SHORT_CODE);
            } else {
                shortCode = "";
            }
            if (itemJson.has(TAG_ITEM_DESCRIPTION)) {
                description = itemJson.getString(TAG_ITEM_DESCRIPTION);
            } else {
                description = "DESCRIPTION MISSING";
            }
            if (itemJson.has(TAG_ITEM_PRICE)) {
                price = Money.of(LocalSettings.getCurrencyUnit(), itemJson.getDouble(TAG_ITEM_PRICE));
            } else {
                price = Money.zero(LocalSettings.getCurrencyUnit());
            }

            if (itemJson.has(TAG_ITEM_PRICE_INT)) {
                priceInt = itemJson.getInt(TAG_ITEM_PRICE_INT);
            } else {
                priceInt = MoneyService.toPenniesRoundNearest(price.getAmount());
            }

            JSONArray modifierGroupIdsJson = itemJson.getJSONArray(TAG_MODIFIERGROUPS);
            modifierGroupIds = new String[modifierGroupIdsJson.length()];
            for (int i = 0; i < modifierGroupIdsJson.length(); i++) {
                modifierGroupIds[i] = modifierGroupIdsJson.getString(i);
            }

            JSONArray tagsArrayJson = itemJson.getJSONArray(TAG_ITEM_TAGS);
            tags = new Tag[tagsArrayJson.length()];
            for (int i = 0; i < tags.length; i++) {
                tags[i] = new Tag(tagsArrayJson.getJSONObject(i));
            }

            if (itemJson.isNull(TAG_ITEM_MENU_GROUPS)) {
                menuGroups = new String[0];
            } else {
                JSONArray menuGroupsJson = itemJson.getJSONArray(TAG_ITEM_MENU_GROUPS);
                menuGroups = new String[menuGroupsJson.length()];
                for (int i = 0; i < menuGroups.length; i++) {
                    menuGroups[i] = menuGroupsJson.getString(i);
                }
            }

            allergens = itemJson.has(TAG_ITEM_ALLERGENS) ? itemJson.getString(TAG_ITEM_ALLERGENS)
                    : "";

            diet = itemJson.has(TAG_ITEM_DIET) ? itemJson.getString(TAG_ITEM_DIET) : "";

            if (itemJson.has(TAG_ITEM_COLOUR)) {
                colourHex = itemJson.getString(TAG_ITEM_COLOUR);
            } else {
                colourHex = "#90A4AE";
            }

            allergiesKeys = new ArrayList<>();
            if (itemJson.isNull(TAG_ALLERGIES)) {
                allergiesKeys = new ArrayList<>();
            } else {
                JSONArray allergiesArray = itemJson.getJSONArray(TAG_ALLERGIES);
                for (int i = 0; i < allergiesArray.length(); i++) {
                    allergiesKeys.add(allergiesArray.getString(i));
                }
            }

            dietsKeys = new ArrayList<>();
            if (itemJson.isNull(TAG_DIETS)) {
                dietsKeys = new ArrayList<>();
            } else {
                JSONArray dietsArray = itemJson.getJSONArray(TAG_DIETS);
                for (int i = 0; i < dietsArray.length(); i++) {
                    dietsKeys.add(dietsArray.getString(i));
                }
            }

            if (itemJson.isNull(TAG_IMAGE_URL)) {
                imageUrl = "";
            } else {
                imageUrl = itemJson.getString(TAG_IMAGE_URL);
            }

            if(itemJson.has(TAG_ITEM_PLU)) {
                plu = itemJson.getString(TAG_ITEM_PLU);
            } else {
                plu = "";
            }
        }

        private Item(Parcel in) {
            name = in.readString();

            {
                String[] keys = in.createStringArray();
                courseByService = new HashMap<>(keys.length);
                Course[] courses = in.createTypedArray(Course.CREATOR);
                for (int i = 0; i < keys.length; i++) {
                    courseByService.put(keys[i], courses[i]);
                }
            }
            price = (Money) in.readSerializable();
            id = in.readString();
            defaultPrinterId = in.readString();
            taxTypeid = in.readString();
            itemTypeId = in.readInt();
            description = in.readString();
            modifierGroupIds = in.createStringArray();
            {
                Parcelable[] tmp = in.readParcelableArray(Tag.class.getClassLoader());
                tags = new Tag[tmp.length];
                for (int i = 0; i < tags.length; i++) {
                    tags[i] = (Tag) tmp[i];
                }
            }
            menuGroups = in.createStringArray();
            unavailable = in.readByte() == (byte) 0x1;
            shortCode = in.readString();
            allergens = in.readString();
            diet = in.readString();
            priceInt = in.readInt();
            colourHex = in.readString();
            allergiesKeys = (ArrayList<String>) in.readSerializable();
            dietsKeys = (ArrayList<String>) in.readSerializable();
            imageUrl = in.readString();
            plu = in.readString();
        }

        /**
         * id of this item if it's attached to an order - 0 is not attached
         */
        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getName() {
            return name;
        }

        public Course getCourseForService(String serviceId) {
            return courseByService.get(serviceId);
        }

        public Money getPrice() {
            return price;
        }

        public String getId() {
            return id;
        }

        @Override
        public Level getType() {
            return Level.ITEM;
        }

        public int getItemTypeId() {
            return itemTypeId;
        }

        @Override
        public int getOrderIndex() {
            return 0;
        }

        public String getDescription() {
            return description;
        }

        public String[] getModifierGroupIds() {
            return modifierGroupIds;
        }

        public String getDefaultPrinterId() {
            return defaultPrinterId;
        }

        public Tag[] getTags() {
            return tags;
        }

        public String[] getMenuGroups() {
            return menuGroups;
        }

        public String getTaxTypeid() {
            return taxTypeid;
        }

        public boolean isUnavailable() {
            return unavailable;
        }

        public String getShortCode() {
            return shortCode;
        }

        public String getAllergens() {
            return allergens;
        }

        public String getDiet() {
            return diet;
        }

        public int getPriceInt() {
            return priceInt;
        }

        public String getColourHex() {
            return colourHex;
        }

        public ArrayList<String> getAllergiesKeys() {
            return allergiesKeys;
        }

        public ArrayList<String> getDietsKeys() {
            return dietsKeys;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getPlu() {
            return plu;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            return id != null ? id.equals(item.id) : item.id == null;

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject response = new JSONObject();
            response.put(TAG_ITEM_NAME, name);
//			if(null != course){
//				response.put(TAG_ITEM_DEFAULT_COURSE, course.toJSON());
//			}
            response.put(TAG_ITEM_ID, id);
            response.put(TAG_ITEM_DESCRIPTION, description);
            response.put(TAG_ITEM_PRICE, price.getAmount().toPlainString());
            return response;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);

            {
                String[] keyArray = new String[courseByService.size()];
                Course[] courseArray = new Course[courseByService.size()];
                int i = 0;
                for (Course course : courseByService.values()) {
                    courseArray[i] = course;
                    keyArray[i] = course.getId();
                    i++;
                }
                dest.writeStringArray(keyArray);
                dest.writeTypedArray(courseArray, 0);
            }
            dest.writeSerializable(price);
            dest.writeString(id);
            dest.writeString(defaultPrinterId);
            dest.writeString(taxTypeid);
            dest.writeInt(itemTypeId);
            dest.writeString(description);
            dest.writeStringArray(modifierGroupIds);
            dest.writeParcelableArray(tags, 0);
            dest.writeStringArray(menuGroups);
            dest.writeByte(unavailable ? (byte) 0x1 : (byte) 0x0);
            dest.writeString(shortCode);
            dest.writeString(allergens);
            dest.writeString(diet);
            dest.writeInt(priceInt);
            dest.writeString(colourHex);
            dest.writeSerializable(allergiesKeys);
            dest.writeSerializable(dietsKeys);
            dest.writeString(imageUrl);
            dest.writeString(plu);
        }

        public enum ItemType {
            FOOD(0, "Food"),
            DRINK(1, "Drink"),
            OTHER(2, "Other");

            private final int id;
            private final String name;

            ItemType(int id, String name) {
                this.id = id;
                this.name = name;
            }

            public static ItemType fromId(int id) {
                if (id == 0) {
                    return FOOD;
                } else if (id == 1) {
                    return DRINK;
                } else if (id == 2) {
                    return OTHER;
                } else {
                    return null;
                }
            }

            public int getId() {
                return id;
            }

            @Override
            public String toString() {
                return name;
            }
        }
    }

    public static class ModifierGroup implements Parcelable, Serializable {
        public static final Parcelable.Creator<ModifierGroup> CREATOR = new Parcelable.Creator<ModifierGroup>() {
            public ModifierGroup createFromParcel(Parcel in) {
                return new ModifierGroup(in);
            }

            public ModifierGroup[] newArray(int size) {
                return new ModifierGroup[size];
            }
        };
        private final int lowerLimit;
        private final int upperLimit;
        private final String name;
        private final String id;
        private ModifierValue[] modifierValues;

        public ModifierGroup(JSONObject modifierGroupJson) throws JSONException {

            name = modifierGroupJson.getString(TAG_MODIFIERGROUP_NAME);
            id = modifierGroupJson.getString(TAG_MODIFIERGROUP_ID);

            if (modifierGroupJson.has(TAG_MODIFIERGROUP_LOWER_LIMIT)) {
                lowerLimit = modifierGroupJson.getInt(TAG_MODIFIERGROUP_LOWER_LIMIT);
            } else {
                lowerLimit = 0;
            }

            if (modifierGroupJson.has(TAG_MODIFIERGROUP_UPPER_LIMIT)) {
                upperLimit = modifierGroupJson.getInt(TAG_MODIFIERGROUP_UPPER_LIMIT);
            } else {
                upperLimit = 0;
            }

            JSONArray modifierValuesJson = modifierGroupJson.getJSONArray(TAG_MODIFIERGROUP_VALUES);
            modifierValues = new ModifierValue[modifierValuesJson.length()];

            for (int j = 0; j < modifierValuesJson.length(); j++) {
                JSONObject modifierValueJson = modifierValuesJson.getJSONObject(j);
                modifierValues[j] = new ModifierValue(modifierValueJson);
            }
        }

        private ModifierGroup(Parcel in) {
            lowerLimit = in.readInt();
            upperLimit = in.readInt();
            name = in.readString();
            id = in.readString();
            Parcelable[] tmpArray = in.readParcelableArray(ModifierValue.class.getClassLoader());
            modifierValues = new ModifierValue[tmpArray.length];
            for (int i = 0; i < modifierValues.length; i++) {
                modifierValues[i] = (ModifierValue) tmpArray[i];
            }
        }

        public int getLowerLimit() {
            return lowerLimit;
        }

        public int getUpperLimit() {
            return upperLimit;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public ModifierValue[] getModifierValues() {
            return modifierValues;
        }

        public void setModifierValues(ModifierValue[] modifierValues) {
            this.modifierValues = modifierValues;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ModifierGroup that = (ModifierGroup) o;

            return id != null ? id.equals(that.id) : that.id == null;

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(lowerLimit);
            dest.writeInt(upperLimit);
            dest.writeString(name);
            dest.writeString(id);
            dest.writeParcelableArray(modifierValues, 0);
        }
    }

    public static class ModifierValue implements Parcelable, CharSequence, Serializable {
        public static final Parcelable.Creator<ModifierValue> CREATOR = new Parcelable.Creator<EpicuriMenu.ModifierValue>() {

            @Override
            public ModifierValue[] newArray(int size) {
                return new ModifierValue[size];
            }

            @Override
            public ModifierValue createFromParcel(Parcel source) {
                return new ModifierValue(source);
            }
        };
        private final int priceInt;
        private String id;
        private Money price;
        private String name;
        private String taxTypeId;
        private String formatted;
        private String plu;

        public ModifierValue(JSONObject objectJSON) throws JSONException {
            id = objectJSON.getString(TAG_MODIFIERVALUE_ID);
            name = objectJSON.getString(TAG_MODIFIERVALUE_NAME);
            taxTypeId = objectJSON.getString(TAG_MODIFIERVALUE_TAXTYPEID);
            price = Money.of(LocalSettings.getCurrencyUnit(), objectJSON.getDouble(TAG_MODIFIERVALUE_PRICE));
            formatted = String.format("%s %s", name, LocalSettings.formatMoneyAmount(price, true));
            if (objectJSON.has(TAG_MODIFIERVALUE_PRICE_INT)) {
                priceInt = objectJSON.getInt(TAG_MODIFIERVALUE_PRICE_INT);
            } else {
                priceInt = MoneyService.toPenniesRoundNearest(price.getAmount());
            }
            if(objectJSON.has(TAG_MODIFIERVALUE_PLU)) {
                plu = objectJSON.getString(TAG_MODIFIERVALUE_PLU);
            } else {
                plu = "";
            }
        }

        private ModifierValue(Parcel in) {
            id = in.readString();
            name = in.readString();
            price = (Money) in.readSerializable();
            taxTypeId = in.readString();
            priceInt = in.readInt();
            formatted = String.format("%s %s", name, LocalSettings.formatMoneyAmount(price, true));
            plu = in.readString();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
            formatted = String.format("%s %s", name, LocalSettings.formatMoneyAmount(price, true));
        }

        public Money getPrice() {
            return price;
        }

        public void setPrice(Money price) {
            this.price = price;
            formatted = String.format("%s %s", name, LocalSettings.formatMoneyAmount(price, true));
        }

        public String getTaxTypeId() {
            return taxTypeId;
        }

        public void setTaxTypeId(String taxTypeId) {
            this.taxTypeId = taxTypeId;
        }

        public String getPlu() {
            return plu;
        }

        public void setPlu(String plu) {
            this.plu = plu;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject response = new JSONObject();
            response.put(TAG_MODIFIERVALUE_ID, id);
            response.put(TAG_MODIFIERVALUE_NAME, name);
            response.put(TAG_MODIFIERVALUE_PRICE, price.getAmount().toPlainString());
            response.put(TAG_MODIFIERVALUE_PLU, plu);
            return response;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ModifierValue that = (ModifierValue) o;

            return id != null ? id.equals(that.id) : that.id == null;

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public char charAt(int index) {
            return formatted.charAt(index);
        }

        @Override
        public int length() {
            return formatted.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return formatted.subSequence(start, end);
        }

        @Override
        public String toString() {
            return formatted;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeSerializable(price);
            dest.writeString(taxTypeId);
            dest.writeInt(priceInt);
            dest.writeString(plu);
        }
    }


    /*
     * [{"Id":1,"Name":"Starters","Ordering":0},{"Id":2,"Name":"Mains","Ordering":1},{"Id":3,"Name":"Deserts","Ordering":2},{"Id":4,"Name":"Drinks","Ordering":3}]
     */
    public static class Course implements Parcelable, Serializable {
        public static final Creator<Course> CREATOR = new Creator<Course>() {
            public Course createFromParcel(Parcel source) {
                return new Course(source);
            }

            public Course[] newArray(int size) {
                return new Course[size];
            }
        };
        private static final String TAG_ID = "Id";
        private static final String TAG_NAME = "Name";
        private static final String TAG_SERVICE = "ServiceId";
        private static final String TAG_ORDERING = "Ordering";
        private final String id;
        private final String name;
        private final int ordering;
        private String serviceId;

        private Course(String name) {
            id = "-1";
            this.name = name;
            serviceId = "-1";
            ordering = 0;
        }

        public Course(JSONObject courseJson) throws JSONException {
            id = courseJson.getString(TAG_ID);
            name = courseJson.getString(TAG_NAME);
            if (courseJson.has(TAG_SERVICE) && !courseJson.getString(TAG_SERVICE).equals("null")) {
                serviceId = courseJson.getString(TAG_SERVICE);
            }
            ordering = courseJson.getInt(TAG_ORDERING);
        }

        private Course(Parcel in) {
            this.id = in.readString();
            this.name = in.readString();
            this.serviceId = in.readString();
            this.ordering = in.readInt();
        }

        public static Course getDummyCourse(String name) {
            return new Course(name);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getServiceId() {
            return serviceId;
        }

        public int getOrdering() {
            if (name.equals("Self Service")) {
                // hack this into the last position in ordering
                return Integer.MAX_VALUE;
            }
            return ordering;
        }

        public JSONObject toJSON() throws JSONException {
            JSONObject response = new JSONObject();
            response.put(TAG_NAME, name);
            response.put(TAG_ID, id);
            response.put(TAG_ORDERING, ordering);
            return response;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Course course = (Course) o;

            return id != null ? id.equals(course.id) : course.id == null;

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.name);
            dest.writeString(this.serviceId);
            dest.writeInt(this.ordering);
        }

    }

    /*
     * GET api/Printer
     * [{"Name":"Bar","IP":"172.16.0.1","Id":4}]
     */
    public static class Printer implements Parcelable, Serializable {
        public static final Creator<Printer> CREATOR = new Creator<Printer>() {
            public Printer createFromParcel(Parcel source) {
                return new Printer(source);
            }

            public Printer[] newArray(int size) {
                return new Printer[size];
            }
        };
        private final String id;
        private final String name;
        private final String ip;
        private final String printerType;
        private final String macAddress;

        public Printer(String id, String name, String ip, String printerType, String macAddress) {
            this.id = id;
            this.name = name;
            this.ip = ip;
            this.printerType = printerType;
            this.macAddress = macAddress;
        }

        public Printer(JSONObject printerObject) throws JSONException {
            name = printerObject.getString("Name");
            id = printerObject.getString("Id");
            if (!printerObject.isNull("IP")) {
                ip = printerObject.getString("IP");
            } else {
                ip = "";
            }
            if(printerObject.has("printerType")) {
                printerType = printerObject.getString("printerType");
            } else {
                printerType = "";
            }
            if(printerObject.has("macAddress")) {
                macAddress = printerObject.getString("macAddress");
            } else {
                macAddress = "";
            }
        }

        private Printer(Parcel in) {
            this.name = in.readString();
            this.ip = in.readString();
            this.id = in.readString();
            this.printerType = in.readString();
            this.macAddress = in.readString();
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getIpAddress() {
            return ip;
        }

        public String getPrinterType() {
            return printerType;
        }

        public String getMacAddress() {
            return macAddress;
        }

        @Override
        public String toString() {
            return name;
        }

        public boolean isPhysical() {
            return !TextUtils.isEmpty(ip);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.ip);
            dest.writeString(this.id);
            dest.writeString(this.printerType);
            dest.writeString(this.macAddress);
        }

        public String toJson() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("ip", ip);
                jsonObject.put("id", id);
                jsonObject.put("printerType", printerType);
                jsonObject.put("macAddress", macAddress);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return jsonObject.toString();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Printer printer = (Printer) o;

            if (id != null ? !id.equals(printer.id) : printer.id != null) return false;
            if (name != null ? !name.equals(printer.name) : printer.name != null) return false;
            if (ip != null ? !ip.equals(printer.ip) : printer.ip != null) return false;
            if (printerType != null ? !printerType.equals(printer.printerType) : printer.printerType != null)
                return false;
            return macAddress != null ? macAddress.equals(printer.macAddress) : printer.macAddress == null;
        }

        @Override public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (ip != null ? ip.hashCode() : 0);
            result = 31 * result + (printerType != null ? printerType.hashCode() : 0);
            result = 31 * result + (macAddress != null ? macAddress.hashCode() : 0);
            return result;
        }
    }

    public static class Tag implements Parcelable {
        public static final Parcelable.Creator<Tag> CREATOR = new Creator<EpicuriMenu.Tag>() {

            @Override
            public Tag[] newArray(int size) {
                return new Tag[size];
            }

            @Override
            public Tag createFromParcel(Parcel source) {
                return new Tag(source);
            }
        };
        private static final String TAG_TAG = "Tag";
        private static final String TAG_ID = "Id";
        private final String tag;
        private final int id;

        public Tag(JSONObject tagJson) throws JSONException {
            tag = tagJson.getString(TAG_TAG);
            id = tagJson.getInt(TAG_ID);
        }

        private Tag(Parcel in) {
            tag = in.readString();
            id = in.readInt();
        }

        public String getTag() {
            return tag;
        }

        public int getId() {
            return id;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(tag);
            dest.writeInt(id);
        }
    }
}

