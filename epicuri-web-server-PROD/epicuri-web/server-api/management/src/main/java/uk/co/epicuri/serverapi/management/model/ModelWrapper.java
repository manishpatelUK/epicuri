package uk.co.epicuri.serverapi.management.model;

import javafx.beans.property.Property;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.lang.reflect.Field;

/**
 * Created by manish.
 */
public class ModelWrapper<T extends IDAble> extends IDAble {
    protected T underlying;
    protected Field displayField;
    protected Field appendField;
    protected int originalHashCode;

    public ModelWrapper(T underlying) {
        this.underlying = underlying;
        if(underlying != null) {
            this.setId(underlying.getId());
            this.originalHashCode = underlying.hashCode();
        }

        if(underlying == null) {
            displayField = null;
        } else {
            Field[] fields = underlying.getClass().getDeclaredFields();
            for(Field field : fields) {
                if(field.isAnnotationPresent(MgmtDisplayField.class)) {
                    displayField = field;
                    displayField.setAccessible(true);
                    break;
                }
            }
        }
    }

    public T getUnderlying() {
        return underlying;
    }

    public void setUnderlying(T underlying) {
        if(this.underlying == null && underlying != null) {
            this.originalHashCode = underlying.hashCode();
        }
        this.underlying = underlying;
    }

    public void setAppendFieldString(String fieldName) {
        Field[] fields = underlying.getClass().getDeclaredFields();
        for(Field field : fields) {
            if(field.getName().equals(fieldName)) {
                field.setAccessible(true);
                appendField = field;
                break;
            }
        }
    }

    public String getDisplayString() {
        if(displayField == null) {
            if(underlying == null) {
                return "null";
            } else {
                return underlying.toString();
            }
        }

        try {
            final Object object = displayField.get(underlying);
            String value;
            if(object != null) {
                value = object.toString();
            } else {
                value = underlying.toString();
            }

            if(appendField != null) {
                Object append = appendField.get(underlying);
                if(append != null) {
                    value += " [" + append.toString() + "]";
                }
            }
            return value;
        } catch (IllegalAccessException e) {
            return "no name[error] "+underlying.getClass().getName();
        }
    }

    public int getOriginalHashCode() {
        return originalHashCode;
    }

    public boolean hasChanged() {
        if(underlying == null) {
            return originalHashCode != 0;
        } else {
            return originalHashCode != underlying.hashCode();
        }
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
