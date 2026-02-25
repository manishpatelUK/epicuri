package uk.co.epicuri.serverapi.engines.reporting.reports;

import com.google.common.collect.Lists;
import com.opencsv.bean.CsvBindByName;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.repository.BaseIT;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class FieldDescriptorTest extends BaseIT {

    @Test
    public void testFieldDescriptions() throws Exception {
        testFieldDescriptions(AdjustmentReportLine.class);
        testFieldDescriptions(AggregatedItemsReportLine.class);
        testFieldDescriptions(CashUpReportLine.class);
        testFieldDescriptions(CustomerDetailsReportLine.class);
        testFieldDescriptions(ItemDetailsReportLine.class);
        testFieldDescriptions(ReservationLine.class);
        testFieldDescriptions(RevenueReportLine.class);
    }

    private void testFieldDescriptions(Class<? extends ReportLine> clazz) throws Exception {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(clazz);
        Map<Integer, Tuple<String, String>> fieldNamesAndIndices = fieldDescriptor.getFieldDescriptions();

        List<String> names = new ArrayList<>();
        fieldNamesAndIndices.values().forEach(t -> names.add(t.getB()));
        assertEquals(names.size(), new HashSet<>(names).size());

        Field[] fields = clazz.getDeclaredFields();

        int expectedHeaders = 0;
        for(Field field : fields) {
            CsvBindByName csvBindByName = field.getAnnotation(CsvBindByName.class);
            if (csvBindByName != null) {
                expectedHeaders++;
            }
        }

        assertEquals(expectedHeaders, fieldNamesAndIndices.size());

        // all orders are filled (no gaps)
        List<Integer> sorted = fieldNamesAndIndices.keySet().stream().sorted().collect(Collectors.toList());
        assertEquals(0, sorted.get(0).intValue());
        assertEquals(fieldNamesAndIndices.size()-1, sorted.get(fieldNamesAndIndices.size()-1).intValue());
    }

    @Test
    public void testIsDynamic() {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(CashUpReportLine.class);
        assertTrue(fieldDescriptor.isDynamicColumns());

        fieldDescriptor = new FieldDescriptor(AggregatedItemsReportLine.class);
        assertFalse(fieldDescriptor.isDynamicColumns());
    }

    @Test
    public void initHeaderRecordNonDynamic() {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(AggregatedItemsReportLine.class);
        AggregatedItemsReportLine line = new AggregatedItemsReportLine();
        String[] headers = fieldDescriptor.initHeaderRecord(Lists.newArrayList(line));
        String[] expected = new String[]{"Item ID/SKU", "Item Name", "Price", "Last Sold At", "Quantity", "Value(inc. mods)", "Value(exc. mods)", "Average Sales Price", "Type", "Tax Name", "Tax Rate"};
        assertArrayEquals(expected, headers);
    }

    @Test
    public void initHeaderRecordDynamic1() {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(CashUpReportLine.class);
        String[] headers = fieldDescriptor.initHeaderRecord(new ArrayList<>());
        String[] expected = new String[]{"Start","End","On Premise Count","On Premise Value","Takeaway Count","Takeaway Value","Unpaid On Premise Count","Unpaid On Premise Value","Unpaid Takeaway Count","Unpaid Takeaway Value","Total Voids","Food Items Count","Food Items Value","Drink Items Count","Drink Items Value","Other Items Count","Other Items Value","Delivery Charges","Total Sales (before adjustments)","Total Bill Adjustments","DiscountTypes","Total Sales (after adjustments)","Total VAT Charged","Net Sales","Tips (inc on bill)","Over-Payments","Total Payments (inc over-payments/tips)","PaymentTypes","Refunds","Unique ID"};
        assertArrayEquals(expected, headers);
    }

    @Test
    public void initHeaderRecordDynamic2() {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(CashUpReportLine.class);
        CashUpReportLine line = createCashupLine();
        String[] headers = fieldDescriptor.initHeaderRecord(Lists.newArrayList(line));
        String[] expected = new String[]{"Start","End","On Premise Count","On Premise Value","Takeaway Count","Takeaway Value","Unpaid On Premise Count","Unpaid On Premise Value","Unpaid Takeaway Count","Unpaid Takeaway Value","Total Voids","Food Items Count","Food Items Value","Drink Items Count","Drink Items Value","Other Items Count","Other Items Value","Delivery Charges","Total Sales (before adjustments)","Total Bill Adjustments","DiscountTypes","Total Sales (after adjustments)","Total VAT Charged","Net Sales","Tips (inc on bill)","Over-Payments","Total Payments (inc over-payments/tips)","PaymentTypes","Refunds","Unique ID"};
        assertArrayEquals(expected, headers);
    }

    @Test
    public void initHeaderRecordDynamic3() throws Exception {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(CashUpReportLine.class);
        CashUpReportLine line = createCashupLine();
        line.updateDynamicField("paymentTypes", "CASH", "10");
        line.updateDynamicField("paymentTypes", "CARD", "20");
        String[] headers = fieldDescriptor.initHeaderRecord(Lists.newArrayList(line));
        String[] expected = new String[]{"Start","End","On Premise Count","On Premise Value","Takeaway Count","Takeaway Value","Unpaid On Premise Count","Unpaid On Premise Value","Unpaid Takeaway Count","Unpaid Takeaway Value","Total Voids","Food Items Count","Food Items Value","Drink Items Count","Drink Items Value","Other Items Count","Other Items Value","Delivery Charges","Total Sales (before adjustments)","Total Bill Adjustments","DiscountTypes","Total Sales (after adjustments)","Total VAT Charged","Net Sales","Tips (inc on bill)","Over-Payments","Total Payments (inc over-payments/tips)","CARD","CASH","Refunds","Unique ID"};
        assertArrayEquals(expected, headers);
    }

    @Test
    public void testGetFieldForIndex1() throws Exception {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(AggregatedItemsReportLine.class);
        AggregatedItemsReportLine line = new AggregatedItemsReportLine();
        // "Item ID/SKU", "Item Name", "Price", "Last Sold At", "Quantity", "Value(inc. mods)", "Value(exc. mods)", "Average Sales Price", "Type", "Tax Name", "Tax Rate"
        String[] headers = fieldDescriptor.initHeaderRecord(Lists.newArrayList(line));

        for(int i = 0; i < headers.length; i++) {
            String header = headers[i];
            Field field = fieldDescriptor.getFieldFor(i);
            assertEquals(header, field.getDeclaredAnnotation(CsvBindByName.class).column());
        }
    }

    @Test
    public void testGetFieldForIndex2() throws Exception {
        FieldDescriptor fieldDescriptor = new FieldDescriptor(CashUpReportLine.class);
        CashUpReportLine line = createCashupLine();
        line.updateDynamicField("paymentTypes", "CASH", "10");
        line.updateDynamicField("paymentTypes", "CARD", "20");
        String[] headers = fieldDescriptor.initHeaderRecord(Lists.newArrayList(line));
        for(int i = 0; i < headers.length; i++) {
            //26 and 27 are dynamic
            if(i == 27 || i == 28) {
                Field field = fieldDescriptor.getFieldFor(i);
                assertEquals("paymentTypes", field.getName());
            } else {
                String header = headers[i];
                Field field = fieldDescriptor.getFieldFor(i);
                assertEquals(header, field.getDeclaredAnnotation(CsvBindByName.class).column());
            }
        }
    }

    private CashUpReportLine createCashupLine() {
        CashUpReportLine line = new CashUpReportLine();
        line.setStartDate("0");
        line.setEndDate("1");
        line.setOnPremiseCount("2");
        line.setOnPremiseValue("3");
        line.setTakeawaysCount("4");
        line.setTakeawaysValue("5");
        line.setUnpaidOnPremiseCount("6");
        line.setUnpaidOnPremiseValue("7");
        line.setUnpaidTakeawayCount("8");
        line.setUnpaidOnPremiseValue("9");
        line.setTotalUnpaid("10");
        line.setFoodCount("11");
        line.setGrossFoodAmount("12");
        line.setDrinkCount("13");
        line.setGrossDrinkAmount("14");
        line.setOtherCount("15");
        line.setGrossOtherAmount("16");
        line.setDeliveryCharges("17");
        line.setTotalSales("18");
        line.setTotalAdjustments("19");
        line.setTotalSalesAfterAdjustments("20");
        line.setTotalVATCharged("21");
        line.setNetSales("22");
        line.setTips("23");
        line.setOverpayments("24");
        line.setTotalPayments("25");
        line.setId("26");
        return line;
    }
}