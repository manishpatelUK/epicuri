package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import uk.co.epicuri.serverapi.common.pojo.management.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@MgmtPojoModel(onNewTemplate = Default.class)
public class RestaurantDefault {
    @MgmtDisplayField
    @MgmtEditableField(editable = false)
    private String name;
    @MgmtCoerceTypeToObject
    private Object value;
    @MgmtEditableField(editable = false)
    private String measure;
    @MgmtEditableField(editable = false)
    private String description;
    @MgmtIgnoreField
    private int ordering;

    public static RestaurantDefault newDefault(String name, Object value) {
        RestaurantDefault restaurantDefault = new RestaurantDefault();
        restaurantDefault.setName(name);
        restaurantDefault.setValue(value);
        return restaurantDefault;
    }

    public RestaurantDefault(){}
    public RestaurantDefault(Default aDefault) {
        this.name = aDefault.getName();
        this.value = aDefault.getValue();
        this.measure = aDefault.getMeasure();
        this.description = aDefault.getDescription();
        this.ordering = aDefault.getOrdering();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAsOrDefault(Class<T> clazz, T defaultValue) {
        try {
            if (value != null) {
                return (T) value;
            }
        } catch (ClassCastException ex) {
            return defaultValue;
        }

        return defaultValue;
    }

    public String convertValueToString() {
        Class clazz = FixedDefaults.TYPE_MAP.get(name);
        if(clazz == null) {
            return value.toString();
        }

        if(clazz == String.class) {
            return getAsOrDefault(String.class, value.toString());
        } else if(clazz == Boolean.class) {
            return String.valueOf(getAsOrDefault(Boolean.class, (Boolean)value));
        } else if(clazz == Double.class) {
            return String.valueOf(getAsOrDefault(Number.class, (Number) value).doubleValue());
        } else if(clazz == Integer.class) {
            return String.valueOf(getAsOrDefault(Number.class, (Number) value).intValue());
        } else if(clazz == Long.class) {
            return String.valueOf(getAsOrDefault(Number.class, (Number) value).longValue());
        } else if(clazz == Short.class) {
            return String.valueOf(getAsOrDefault(Number.class, (Number)value).shortValue());
        }

        return value.toString();
    }

    public static Map<String,Object> asMap(Collection<RestaurantDefault> defaults) {
        Map<String,Object> map = new HashMap<>();
        for(RestaurantDefault restaurantDefault : defaults) {
            map.put(restaurantDefault.getName(), restaurantDefault.getValue());
        }
        return map;
    }

    @Override
    public boolean equals(Object object) {
        return EqualsBuilder.reflectionEquals(this, object);
    }
}
