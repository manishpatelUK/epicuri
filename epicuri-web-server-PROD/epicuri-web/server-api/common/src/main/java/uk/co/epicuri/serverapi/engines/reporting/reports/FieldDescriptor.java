package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.opencsv.bean.CsvBindByName;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;

import java.lang.reflect.Field;
import java.util.*;

public class FieldDescriptor {
    private final Map<Integer, Tuple<String,String>> fieldDescriptions;
    private final Map<Integer,Field> indexToFieldMap = new HashMap<>();
    private final Map<String,Field> fieldNameToFieldMap = new HashMap<>();
    private final Map<Integer,String> indexToActualColumnNameMap = new HashMap<>();
    private final Class<? extends ReportLine> clazz;
    private final Set<Integer> dynamicColumns = new HashSet<>();

    public FieldDescriptor(Class<? extends ReportLine> clazz) {
        this.clazz = clazz;
        this.fieldDescriptions = getDescriptions(clazz);
    }

    private Map<Integer, Tuple<String,String>> getDescriptions(Class<? extends ReportLine> clazz) {
        Field[] fields = clazz.getDeclaredFields();

        Map<Integer,Tuple<String,String>> headers = new TreeMap<>();
        for(Field field : fields) {
            CsvBindByName csvBindByName = field.getAnnotation(CsvBindByName.class);
            CsvSortOrder csvSortOrder = field.getAnnotation(CsvSortOrder.class);
            if(csvBindByName == null || csvSortOrder == null) {
                continue;
            }

            field.setAccessible(true);

            String fieldName = field.getName();
            String headerName = csvBindByName.column();
            if(!headers.containsKey(headers.size())) {
                update(headers, csvSortOrder, fieldName, headerName, field);
            }
            fieldNameToFieldMap.put(field.getName(), field);
        }

        return headers;
    }

    private void update(Map<Integer, Tuple<String, String>> headers, CsvSortOrder csvSortOrder, String fieldName, String headerName, Field field) {
        headers.put(csvSortOrder.order(), new Tuple<>(fieldName, headerName));
        DynamicColumn dynamicColumn = field.getAnnotation(DynamicColumn.class);
        if(dynamicColumn != null) {
            dynamicColumns.add(csvSortOrder.order());
        }
    }

    Map<Integer, Tuple<String, String>> getFieldDescriptions() {
        return fieldDescriptions;
    }

    private int lineLength() {
        return fieldDescriptions.size();
    }

    public String[] initHeaderRecord(List<? extends ReportLine> lines) {
        return isDynamicColumns() ? getDynamicHeaders(lines) : getNonDynamicHeaders();
    }

    private String[] getDynamicHeaders(List<? extends ReportLine> lines) {
        Map<String, Set<String>> fieldToActualColumnNames = getDynamicHeaderNames(lines);

        List<String> headers = new ArrayList<>();
        Map<String,String> headerNameToFieldName = new HashMap<>();
        for (int i = 0; i < lineLength(); i++) {
            String fieldName = fieldDescriptions.get(i).getA();
            if(fieldToActualColumnNames.containsKey(fieldName)) {
                Set<String> actualColumnNames = fieldToActualColumnNames.get(fieldName);
                headers.addAll(actualColumnNames);
                actualColumnNames.forEach(x -> headerNameToFieldName.put(x, fieldName));
            } else {
                String headerName = fieldDescriptions.get(i).getB();
                headers.add(headerName);
                headerNameToFieldName.put(headerName, fieldName);
            }
        }

        for(int i = 0; i < headers.size(); i++) {
            indexToActualColumnNameMap.put(i, headers.get(i));
            indexToFieldMap.put(i, fieldNameToFieldMap.get(headerNameToFieldName.get(headers.get(i))));
        }

        String[] array = new String[headers.size()];
        return headers.toArray(array);
    }

    private Map<String, Set<String>> getDynamicHeaderNames(List<? extends ReportLine> lines) {
        Map<String,Set<String>> fieldToActualColumnNames = new HashMap<>();
        for(ReportLine line : lines) {
            Map<String, Set<String>> fieldNameToActualName = line.getFieldNameToActualName();
            for(String fieldName : fieldNameToActualName.keySet()) {
                if(!fieldToActualColumnNames.containsKey(fieldName)) {
                    fieldToActualColumnNames.put(fieldName, new TreeSet<>());
                }
                fieldToActualColumnNames.get(fieldName).addAll(fieldNameToActualName.get(fieldName));
            }
        }
        return fieldToActualColumnNames;
    }

    private String[] getNonDynamicHeaders() {
        String[] headers = new String[lineLength()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = fieldDescriptions.get(i).getB();
            String fieldName = fieldDescriptions.get(i).getA();
            try {
                Field declaredField = clazz.getDeclaredField(fieldName);
                fieldNameToFieldMap.put(fieldName, declaredField);
                indexToFieldMap.put(i, declaredField);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return headers;
    }

    public Field getFieldFor(int i) {
        return indexToFieldMap.get(i);
    }

    public boolean isDynamicColumns() {
        return dynamicColumns.size() > 0;
    }

    public String getColumnNameForIndex(int index) {
        if(!isDynamicColumns()) {
            throw new IllegalArgumentException("Cannot call this method when columns are not dynamic");
        }
        return indexToActualColumnNameMap.get(index);
    }
}
