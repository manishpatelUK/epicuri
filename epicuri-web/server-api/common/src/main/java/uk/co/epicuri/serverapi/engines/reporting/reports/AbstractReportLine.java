package uk.co.epicuri.serverapi.engines.reporting.reports;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractReportLine implements ReportLine {
    private final Class<? extends ReportLine> clazz;
    private final Map<String,String> columnToValues = new HashMap<>();
    private final Map<String, Set<String>> fieldNameToActualName = new HashMap<>();

    public AbstractReportLine(Class<? extends ReportLine> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void updateDynamicField(String dynamicFieldName, String actualHeaderName, String value) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(dynamicFieldName);
        if(field.getDeclaredAnnotation(DynamicColumn.class) == null) {
            throw new IllegalArgumentException("Field " + actualHeaderName + " is not declared dynamic");
        }
        if(!fieldNameToActualName.containsKey(dynamicFieldName)) {
            fieldNameToActualName.put(dynamicFieldName, new TreeSet<>());
        }
        fieldNameToActualName.get(dynamicFieldName).add(actualHeaderName);
        columnToValues.put(actualHeaderName, value);
    }

    @Override
    public boolean hasDynamicFields() {
        return columnToValues.size() > 0;
    }

    public Map<String, String> getColumnToValues() {
        return columnToValues;
    }

    public Map<String, Set<String>> getFieldNameToActualName() {
        return fieldNameToActualName;
    }
}
