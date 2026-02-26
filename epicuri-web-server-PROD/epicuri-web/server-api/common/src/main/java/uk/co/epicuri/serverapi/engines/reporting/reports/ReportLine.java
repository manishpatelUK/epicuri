package uk.co.epicuri.serverapi.engines.reporting.reports;

import java.util.Map;
import java.util.Set;

public interface ReportLine {
    void updateDynamicField(String dynamicFieldName, String actualHeaderName, String value) throws NoSuchFieldException;
    boolean hasDynamicFields();
    Map<String, String> getColumnToValues();
    Map<String, Set<String>> getFieldNameToActualName();
}
