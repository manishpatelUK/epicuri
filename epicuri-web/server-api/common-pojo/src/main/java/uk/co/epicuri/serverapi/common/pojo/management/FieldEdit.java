package uk.co.epicuri.serverapi.common.pojo.management;

/**
 * Created by manish
 */
public class FieldEdit {
    private boolean nullify;
    private String fieldName;
    private Object editedObject;

    public boolean isNullify() {
        return nullify;
    }

    public void setNullify(boolean nullify) {
        this.nullify = nullify;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getEditedObject() {
        return editedObject;
    }

    public void setEditedObject(Object editedObject) {
        this.editedObject = editedObject;
    }
}
