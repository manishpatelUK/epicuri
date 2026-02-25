package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtCoerceTypeToObject;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtEditableField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.db.TableNames;

@Document(collection = TableNames.DEFAULTS)
public class Default extends IDAble{
    @MgmtDisplayField
    @Indexed(unique = true)
    private String name;
    @MgmtCoerceTypeToObject
    private Object value;
    private String measure;
    private String description;
    @MgmtIgnoreField
    private int ordering;
    @MgmtEditableField(editable = false)
    private String categorisation = "General";

    public Default(){}
    public Default(String name, Object value, String measure, String description, int ordering) {
        this.name = name;
        this.value = value;
        this.measure = measure;
        this.description = description;
        this.ordering = ordering;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Default aDefault = (Default) o;

        if (ordering != aDefault.ordering) return false;
        if (name != null ? !name.equals(aDefault.name) : aDefault.name != null) return false;
        if (value != null ? !value.equals(aDefault.value) : aDefault.value != null) return false;
        if (measure != null ? !measure.equals(aDefault.measure) : aDefault.measure != null) return false;
        if (description != null ? !description.equals(aDefault.description) : aDefault.description != null)
            return false;
        return categorisation != null ? categorisation.equals(aDefault.categorisation) : aDefault.categorisation == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (measure != null ? measure.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + ordering;
        result = 31 * result + (categorisation != null ? categorisation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Default{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", measure='" + measure + '\'' +
                ", description='" + description + '\'' +
                ", ordering=" + ordering +
                ", categorisation='" + categorisation + '\'' +
                '}';
    }
}
