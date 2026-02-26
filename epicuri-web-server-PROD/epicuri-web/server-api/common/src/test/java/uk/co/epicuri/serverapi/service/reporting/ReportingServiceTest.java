package uk.co.epicuri.serverapi.service.reporting;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.external.mews.MewsConstants;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.CSVWrapper;
import uk.co.epicuri.serverapi.common.pojo.host.reporting.ReportingConstraints;
import uk.co.epicuri.serverapi.common.pojo.model.ActivityInstantiationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.CashUp;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.reporting.reports.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.ZoneId;
import java.util.List;

import static org.junit.Assert.*;

public class ReportingServiceTest extends BaseReportingServiceTest {
    @Autowired
    private ReportingService reportingService;

    @Override
    public void setUp() throws Exception{
        super.setUp();
    }

    @Test
    public void createConstraints() throws Exception {
        ReportingConstraints constraints1 = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        assertTrue(constraints1.getIntegrations().containsAll(restaurant1.getIntegrations().keySet()));
        assertEquals(1498604400000L, constraints1.getStart());
        assertEquals(1498777199999L, constraints1.getEnd());
        assertEquals(restaurant1.getId(), constraints1.getRestaurantId());
        assertEquals(ZoneId.of("Europe/London"), constraints1.getZoneId());
        assertEquals("GBP", constraints1.getCurrency());

        ReportingConstraints constraints2 = reportingService.createConstraints(token, "14-03-2019 17:00", "15-03-2019 10:00");
        assertEquals(1552582800000L, constraints2.getStart());
        assertEquals(1552644000000L, constraints2.getEnd());
    }

    @Test
    public void getCleanedSessionsAndOrders1() throws Exception {
        Tuple<List<Session>, List<Order>> data = reportingService.getCleanedSessionsAndOrders(restaurant3.getId(), Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(0, data.getA().size());
        assertEquals(0, data.getB().size());
    }

    @Test
    public void getCleanedSessionsAndOrders2() throws Exception {
        Tuple<List<Session>, List<Order>> data = reportingService.getCleanedSessionsAndOrders(restaurant1.getId(), Long.MIN_VALUE, Long.MAX_VALUE);
        assertEquals(2, data.getA().size());
        assertEquals(3, data.getB().size());
    }

    @Test
    public void getCleanedSessionsAndOrders3() throws Exception {
        Tuple<List<Session>, List<Order>> data = reportingService.getCleanedSessionsAndOrders(restaurant1.getId(), Long.MIN_VALUE, bst1);
        assertEquals(0, data.getA().size());
        assertEquals(0, data.getB().size());
    }

    @Test
    public void getCleanedSessionsAndOrders4() throws Exception {
        Tuple<List<Session>, List<Order>> data = reportingService.getCleanedSessionsAndOrders(restaurant1.getId(), bst2 - 1001, bst2);
        assertEquals(2, data.getA().size());
        assertEquals(3, data.getB().size());
    }

    @Test
    public void getCleanedSessionsAndOrders5() throws Exception {
        Tuple<List<Session>, List<Order>> data = reportingService.getCleanedSessionsAndOrders(restaurant1.getId(), bst1 - 1001, bst2);
        assertEquals(2, data.getA().size());
        assertEquals(3, data.getB().size());
    }

    @Test
    public void getOrders() throws Exception {
        List<Order> orders = reportingService.getOrders(restaurant1.getId(), bst1 - 1001, bst2);
        assertEquals(3, orders.size());
    }

    @Test
    public void getCustomerDetailsReportLines() throws Exception {
        setUpBooking(1498690801000L, booking1, customer1);
        setUpBooking(1498690801000L, booking2, customer1);
        setUpBooking(1498690801000L, booking3, customer2);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<CustomerDetailsReportLine> lines = reportingService.getCustomerDetailsReportLines(constraints);
        assertEquals(2, lines.size());

        //todo more detailed tests
    }

    private void setUpBooking(long time, Booking booking, Customer customer) {
        booking.setCustomerId(customer.getId());
        booking.setRestaurantId(restaurant1.getId());
        booking.setTargetTime(time);
        bookingRepository.save(booking);
    }

    @Test
    public void getAggregatedItemsReportLines() throws Exception {
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AggregatedItemsReportLine> lines = reportingService.getAggregatedItemsReportLines(constraints);
        assertEquals(3, lines.size());
        AggregatedItemsReportLine menuItem1Line = lines.stream().filter(l -> l.getItemId().equals(menuItem1.getId())).findFirst().orElse(new AggregatedItemsReportLine());
        assertItemEquals(menuItem1, menuItem1Line);
        assertEquals("1", menuItem1Line.getQuantity());
        assertEquals("0.10", menuItem1Line.getValue());
        assertEquals("0.10", menuItem1Line.getValueExcludingMods());
        assertEquals("0.10", menuItem1Line.getAverageSalesPrice());
        assertEquals("DRINK", menuItem1Line.getType());
        assertEquals(tax1.getName(), menuItem1Line.getTaxName());
        assertEquals(String.format("%.2f",tax1.getRate()/10D) + "%", menuItem1Line.getTaxRate());
        AggregatedItemsReportLine menuItem2Line = lines.stream().filter(l -> l.getItemId().equals(menuItem2.getId())).findFirst().orElse(new AggregatedItemsReportLine());
        assertItemEquals(menuItem2, menuItem2Line);
        assertEquals("2", menuItem2Line.getQuantity());
        assertEquals("2.66", menuItem2Line.getValue());
        assertEquals("0.40", menuItem2Line.getValueExcludingMods());
        assertEquals("1.33", menuItem2Line.getAverageSalesPrice());
        assertEquals("FOOD", menuItem2Line.getType());
        assertEquals(tax2.getName(), menuItem2Line.getTaxName());
        assertEquals(String.format("%.2f",tax2.getRate()/10D) + "%", menuItem2Line.getTaxRate());
        AggregatedItemsReportLine menuItem3Line = lines.stream().filter(l -> l.getItemId().equals(menuItem3.getId())).findFirst().orElse(new AggregatedItemsReportLine());
        assertItemEquals(menuItem3, menuItem3Line);
        assertEquals("3", menuItem3Line.getQuantity());
        assertEquals("0.90", menuItem3Line.getValue());
        assertEquals("0.90", menuItem3Line.getValueExcludingMods());
        assertEquals("0.30", menuItem3Line.getAverageSalesPrice());
        assertEquals("OTHER", menuItem3Line.getType());
        assertEquals(tax3.getName(), menuItem3Line.getTaxName());
        assertEquals(String.format("%.2f",tax3.getRate()/10D) + "%", menuItem3Line.getTaxRate());
    }

    @Test
    public void getAggregatedItemsReportLinesWithPLU() throws Exception {
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        constraints.setAggregateByPLU(true);

        List<AggregatedItemsReportLine> lines = reportingService.getAggregatedItemsReportLines(constraints);
        assertEquals(1, lines.size());
        AggregatedItemsReportLine item = lines.get(0);
        assertEquals("6", item.getQuantity());
        assertEquals("3.66", item.getValue());
        assertEquals("1.40", item.getValueExcludingMods());
        assertEquals("0.61", item.getAverageSalesPrice());
        assertEquals("MIXED", item.getType());
        assertEquals("MIXED", item.getTaxName());
        assertEquals("MIXED", item.getTaxRate());

        assertEquals("m1", item.getItemId());
        assertEquals("MIXED", item.getItemName());
        assertEquals("MIXED", item.getPrice());
        assertEquals("2017-06-29 00:00:02", item.getLastSold());
    }

    @Test
    public void getAggregatedItemsReportLinesWithRefunds() throws Exception {
        SessionArchive sessionArchive = sessionArchiveRepository.findBySessionId(session1.getId());
        sessionArchive.getSession().setSessionType(SessionType.REFUND);
        sessionArchiveRepository.save(sessionArchive);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AggregatedItemsReportLine> lines = reportingService.getAggregatedItemsReportLines(constraints);
        assertEquals(3, lines.size());

        AggregatedItemsReportLine menuItem1Line = lines.stream().filter(l -> l.getItemId().equals(menuItem1.getId())).findFirst().orElse(new AggregatedItemsReportLine());
        assertItemEquals(menuItem1, menuItem1Line);
        assertEquals("-1", menuItem1Line.getQuantity());
        assertEquals("-0.10", menuItem1Line.getValue());
        assertEquals("-0.10", menuItem1Line.getValueExcludingMods());
        assertEquals("0", menuItem1Line.getAverageSalesPrice());
        assertEquals("DRINK", menuItem1Line.getType());
        assertEquals(tax1.getName(), menuItem1Line.getTaxName());
        assertEquals(String.format("%.2f",tax1.getRate()/10D) + "%", menuItem1Line.getTaxRate());
        AggregatedItemsReportLine menuItem2Line = lines.stream().filter(l -> l.getItemId().equals(menuItem2.getId())).findFirst().orElse(new AggregatedItemsReportLine());
        assertItemEquals(menuItem2, menuItem2Line);
        assertEquals("-2", menuItem2Line.getQuantity());
        assertEquals("-2.66", menuItem2Line.getValue());
        assertEquals("-0.40", menuItem2Line.getValueExcludingMods());
        assertEquals("0", menuItem2Line.getAverageSalesPrice());
        assertEquals("FOOD", menuItem2Line.getType());
        assertEquals(tax2.getName(), menuItem2Line.getTaxName());
        assertEquals(String.format("%.2f",tax2.getRate()/10D) + "%", menuItem2Line.getTaxRate());
        AggregatedItemsReportLine menuItem3Line = lines.stream().filter(l -> l.getItemId().equals(menuItem3.getId())).findFirst().orElse(new AggregatedItemsReportLine());
        assertItemEquals(menuItem3, menuItem3Line);
        assertEquals("-3", menuItem3Line.getQuantity());
        assertEquals("-0.90", menuItem3Line.getValue());
        assertEquals("-0.90", menuItem3Line.getValueExcludingMods());
        assertEquals("0", menuItem3Line.getAverageSalesPrice());
        assertEquals("OTHER", menuItem3Line.getType());
        assertEquals(tax3.getName(), menuItem3Line.getTaxName());
        assertEquals(String.format("%.2f",tax3.getRate()/10D) + "%", menuItem3Line.getTaxRate());
    }

    private void assertItemEquals(MenuItem item, AggregatedItemsReportLine line) {
        assertEquals(item.getId(), line.getItemId());
        assertEquals(item.getName(), line.getItemName());
        assertEquals(String.format("%.2f", MoneyService.toMoneyRoundNearest(item.getPrice())), line.getPrice());
        assertEquals("2017-06-29 00:00:02", line.getLastSold());
    }


    @Test
    public void getPaymentReportLines() throws Exception {
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(constraints);
        assertEquals(2, lines.size());

        assertEquals("2017-06-29 00:00:01", lines.get(0).getDate());
        assertEquals("SEATED", lines.get(0).getSessionType());
        assertEquals(staff1.getId(), lines.get(0).getStaffId());
        assertEquals(staff1.getUserName(), lines.get(0).getStaffName());
        assertEquals(adjustmentType1.getType().toString(), lines.get(0).getAdjustmentType());
        assertEquals(adjustmentType1.getName(), lines.get(0).getAdjustmentName());
        assertEquals("1.66", lines.get(0).getAdjustmentValue());
        assertEquals("GBP", lines.get(0).getCurrency());

        assertEquals("2017-06-29 00:00:02", lines.get(1).getDate());
        assertEquals("SEATED", lines.get(1).getSessionType());
        assertEquals("1", lines.get(1).getStaffId());
        assertEquals("[NOT AVAILABLE]", lines.get(1).getStaffName());
        assertEquals(adjustmentType2.getType().toString(), lines.get(1).getAdjustmentType());
        assertEquals(adjustmentType2.getName(), lines.get(1).getAdjustmentName());
        assertEquals("2.00", lines.get(1).getAdjustmentValue());
        assertEquals("GBP", lines.get(1).getCurrency());
    }

    @Test
    public void getPaymentReportLinesWithVoids() throws Exception {
        addCashOverpayment();
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        session.getAdjustments().get(0).setVoided(true);
        session.getAdjustments().get(0).setVoidedByStaffId(staff1.getId());
        sessionArchiveRepository.save(archive);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(constraints);
        AdjustmentReportLine line = lines.stream().filter(l -> l.getAdjustmentVoided().equals("Y")).findFirst().orElse(null);
        assertNotNull(line);
        assertEquals(staff1.getUserName(), line.getAdjustmentVoidedByUser());
        assertEquals(staff1.getId(), line.getAdjustmentVoidedByID());


    }

    @Test
    public void getPaymentReportLines_AdjustmentsWithChange() throws Exception {
        addCashOverpayment();
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(constraints);
        assertEquals(3, lines.size());

        AdjustmentReportLine discount1 = lines.get(0);
        AdjustmentReportLine changeable1 = lines.get(1); // will be a payment of 200
        AdjustmentReportLine changeable2 = lines.get(2); // will be a payment of 200

        assertEquals("1.00", changeable1.getAdjustmentValue());
        assertEquals("1.00", changeable2.getAdjustmentValue());
    }

    @Test
    public void getPaymentReportLines_AdjustmentsMixed() throws Exception {
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        session.getAdjustments().get(1).setValue(300); //added £1
        session.getAdjustments().get(1).getAdjustmentType().setSupportsChange(false);
        sessionArchiveRepository.save(archive);
        addCashOverpayment();
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(constraints);
        assertEquals(3, lines.size());

        AdjustmentReportLine discount1 = lines.get(0);
        AdjustmentReportLine nonChangeable1 = lines.get(1); // will be a payment of 200
        AdjustmentReportLine changeable2 = lines.get(2); // will be a payment of 200

        assertEquals("3.00", nonChangeable1.getAdjustmentValue());
        assertEquals("0.00", changeable2.getAdjustmentValue());
    }

    @Test
    public void getPaymentReportLinesWithMews() throws Exception {
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        Adjustment payment2 = new Adjustment(session.getId());
        payment2.setValue(200);
        payment2.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        payment2.setAdjustmentType(adjustmentType2);
        payment2.setStaffId(staff1.getId());
        payment2.setCreated(bst1 + 3000);
        payment2.getSpecialAdjustmentData().put(MewsConstants.FIRST_NAME, "foo");
        payment2.getSpecialAdjustmentData().put(MewsConstants.LAST_NAME, "bar");
        payment2.getSpecialAdjustmentData().put(MewsConstants.ROOM_NO, "3");
        payment2.getSpecialAdjustmentData().put(MewsConstants.CHARGE_ID, "123");
        payment2.getAdjustmentType().setSupportsChange(false);
        session.getAdjustments().add(payment2);
        sessionArchiveRepository.save(archive);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(constraints);
        assertEquals(3, lines.size());

        assertEquals("2017-06-29 00:00:03", lines.get(2).getDate());
        assertEquals("SEATED", lines.get(2).getSessionType());
        assertEquals("1", lines.get(2).getSessionId());
        assertEquals(staff1.getId(), lines.get(2).getStaffId());
        assertEquals(staff1.getUserName(), lines.get(2).getStaffName());
        assertEquals(adjustmentType2.getType().toString(), lines.get(2).getAdjustmentType());
        assertEquals(adjustmentType2.getName(), lines.get(2).getAdjustmentName());
        assertEquals("2.00", lines.get(2).getAdjustmentValue());
        assertEquals("GBP", lines.get(2).getCurrency());
        assertEquals("foo bar", lines.get(2).getMewsName());
        assertEquals("3", lines.get(2).getMewsRoomNumber());
        assertEquals("123", lines.get(2).getMewsChargeId());
    }

    @Test
    public void getPaymentReportLinesWithVoid() throws Exception {
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        session.getAdjustments().get(0).setValue(session.getAdjustments().get(0).getValue()-1);
        sessionArchiveRepository.save(archive);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<AdjustmentReportLine> lines = reportingService.getPaymentReportLines(constraints);
        assertEquals(2, lines.size());

        assertEquals("Y", lines.get(0).getSessionVoided());
        assertEquals("Y", lines.get(1).getSessionVoided());
    }

    @Test
    public void getItemDetailsReportLines() throws Exception {
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<ItemDetailsReportLine> lines = reportingService.getItemDetailsReportLines(constraints);
        assertEquals(3, lines.size());

        assertEquals("2017-06-29 00:00:01", lines.get(1).getDate());
        assertEquals("2017-06-29 00:00:02", lines.get(1).getOrderDateTime());
        assertEquals("1", lines.get(1).getSessionId());
        assertEquals("SEATED", lines.get(1).getSessionType());
        assertEquals(table1.getName(), lines.get(1).getTableNumbers());
        assertEquals(staff1.getId(), lines.get(1).getStaffId());
        assertEquals(staff1.getName(), lines.get(1).getStaffName());
        assertEquals(ActivityInstantiationConstant.UNKNOWN.name(), lines.get(1).getOrigin());
        assertEquals(menuItem2.getId(), lines.get(1).getMenuItemId());
        assertEquals(menuItem2.getName(), lines.get(1).getMenuItemName());
        assertEquals("2.66", lines.get(1).getSalesPrice());
        assertEquals("", lines.get(1).getVoidReason());
        assertEquals("FOOD", lines.get(1).getItemType());
        assertEquals(tax2.getName(), lines.get(1).getTaxName());
        assertEquals(String.format("%.2f",tax2.getRate()/10D) + "%", lines.get(1).getTaxRate());
        assertEquals("TABLE", lines.get(0).getGuestName());
    }

    @Test
    public void getRevenueReportLines() throws Exception {
        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<RevenueReportLine> lines = reportingService.getRevenueReportLines(constraints);
        assertEquals(2, lines.size());
        assertEquals("2017-06-29 00:00:01", lines.get(0).getStartTime());
        assertEquals("2017-06-29 12:59:59", lines.get(0).getEndTime());
        assertEquals("1", lines.get(0).getSessionId());
        assertEquals("SEATED", lines.get(0).getSessionType());
        assertEquals(table1.getName(), lines.get(0).getTableNumbers());
        assertEquals("3.66", lines.get(0).getSubTotal());
        assertEquals("0.32", lines.get(0).getVatTotal());
        assertEquals("0.00", lines.get(0).getTips());
        assertEquals("1.66", lines.get(0).getDiscounts());
        assertEquals("2.00", lines.get(0).getPayments());
        assertEquals("0.00", lines.get(0).getVoidedPayments());
        assertEquals("0.00", lines.get(0).getOverpayment());
        assertEquals("N", lines.get(0).getVoided());
        assertEquals("", lines.get(0).getVoidReason());
        assertEquals("4", lines.get(0).getNumberOfCovers());
        assertEquals(staff1.getUserName(), lines.get(0).getClosedBy());
    }

    @Test
    public void getRevenueReportLinesWithVoid1() throws Exception {
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        session.getAdjustments().clear();
        sessionArchiveRepository.save(archive);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<RevenueReportLine> lines = reportingService.getRevenueReportLines(constraints);
        assertEquals(2, lines.size());

        assertEquals("Y", lines.get(0).getVoided());
        assertEquals("FORCE CLOSED", lines.get(0).getVoidReason());
    }

    @Test
    public void getRevenueReportLinesWithVoidedPayments() throws Exception {
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        Adjustment payment = session.getAdjustments().stream().filter(a -> a.getAdjustmentType().getType() == AdjustmentTypeType.PAYMENT).findFirst().orElse(null);
        assertNotNull(payment);

        payment.setVoided(true);
        payment.setVoidedByStaffId(staff1.getId());
        sessionArchiveRepository.save(archive);

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<RevenueReportLine> lines = reportingService.getRevenueReportLines(constraints);
        assertEquals(2, lines.size());

        assertEquals("2.00", lines.get(0).getVoidedPayments());
    }


    @Test
    public void getRevenueReportLines_AdjustmentsWithChange() throws Exception {
        addCashOverpayment();

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<RevenueReportLine> lines = reportingService.getRevenueReportLines(constraints);
        assertEquals(2, lines.size());
        assertEquals("2.00", lines.get(0).getPayments());
    }

    @Test
    public void getReservationLines() throws Exception {
        setUpBookings();

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<ReservationLine> lines = reportingService.getReservationLines(constraints);
        assertEquals(3,lines.size());

        assertEquals("2017-06-29 00:00", lines.get(0).getTime());
        assertEquals(booking1.getName(), lines.get(0).getName());
        assertEquals("10", lines.get(0).getCovers());
        assertEquals(booking1.getTelephone(), lines.get(0).getTelephone());
        assertEquals(booking1.getEmail(), lines.get(0).getEmail());
        assertEquals(booking1.getNotes(), lines.get(0).getNotes());
        assertEquals("ACCEPTED", lines.get(0).getBookingState());
        assertEquals("ONLINE", lines.get(0).getOrigin());
    }

    private void setUpBookings() {
        setUpBooking(1498690801000L, booking1, customer1);
        setUpBooking(1498690801000L + (1000*60*5), booking2, customer1);
        setUpBooking(1498690801000L + (1000*60*10), booking3, customer2);

        booking1.setName("Foobar1");
        booking1.setNumberOfPeople(10);
        booking1.setTelephone("1234");
        booking1.setEmail("dsf@df.com");
        booking1.setNotes("asfddsf");
        booking1.setInstantiatedFrom(ActivityInstantiationConstant.BOOKING_WIDGET);
        bookingRepository.save(booking1);
    }

    @Test
    public void getCashupLines() throws Exception {
        setUpCashups();

        ReportingConstraints constraints = new ReportingConstraints();
        constraints.setStart(0);
        constraints.setEnd(20);
        constraints.setZoneId(ZoneId.of("Europe/London"));
        constraints.setRestaurantId(restaurant1.getId());

        List<CashUpReportLine> lines = reportingService.getCashupLines(constraints);
        assertEquals(2, lines.size());

        assertTrue(lines.stream().anyMatch(l -> l.getId().equals(cashUp1.getId())));
        assertTrue(lines.stream().anyMatch(l -> l.getId().equals(cashUp2.getId())));

        for(CashUpReportLine line : lines) {
            for(Field field : CashUpReportLine.class.getDeclaredFields()) {
                if(!(field.getName().equals("startDate")
                        || field.getName().equals("endDate")
                        || field.getName().equals("id")
                        || field.getName().equals("totalSalesAfterAdjustments")
                        || field.getName().equals("paymentTypes")
                        || field.getName().equals("adjustmentTypes")
                        || field.getName().equals("refunds"))) {
                    field.setAccessible(true);
                    if(field.getName().endsWith("Count")) {
                        assertEquals("13", field.get(line));
                    } else {
                        assertEquals("0.13", field.get(line));
                    }
                }
            }

            assertEquals("1970-01-01 01:00", line.getStartDate());
            assertEquals("1970-01-01 01:00", line.getEndDate());
            assertTrue(line.getId().equals(cashUp1.getId()) || line.getId().equals(cashUp2.getId()));
            assertEquals("0.00", line.getTotalSalesAfterAdjustments());

            if(line.getId().equals(cashUp3.getId())) {
                assertEquals("0.10", line.getColumnToValues().get("CARD"));
                assertEquals("0.20", line.getColumnToValues().get("CASH"));
            }
        }

    }

    private void setUpCashups() throws Exception {
        cashUp1.setStartTime(0);
        cashUp1.setEndTime(10);
        setUpCashupNumbers(cashUp1);
        cashUp1.setRestaurantId(restaurant1.getId());
        cashUp2.setStartTime(11);
        cashUp2.setEndTime(20);
        setUpCashupNumbers(cashUp2);
        cashUp2.getPaymentReport().put("CARD", 10);
        cashUp2.getPaymentReport().put("CASH", 20);
        cashUp2.setRestaurantId(restaurant1.getId());
        cashUp3.setStartTime(0);
        cashUp3.setEndTime(20);

        cashUp3.setRestaurantId(restaurant2.getId());

        cashUpRepository.save(cashUp1);
        cashUpRepository.save(cashUp2);
        cashUpRepository.save(cashUp3);
    }

    @Test
    public void testCreateCSVWrapper1() {
        setUpBookings();

        ReportingConstraints constraints = reportingService.createConstraints(token, "28-06-2017", "29-06-2017");
        List<ReservationLine> lines = reportingService.getReservationLines(constraints);
        CSVWrapper wrapper = reportingService.createCsvWrapper(lines, ReservationLine.class, "report.csv");
        assertEquals("report.csv", wrapper.getFileName());
        String[] split = wrapper.getContent().split("\\n");
        for(String line : split) {
            assertEquals(line.endsWith(",") ? 8 : 9, line.split(",").length);
        }
        assertEquals(4, split.length);
        assertEquals("Date/Time,Booking State,Booking Via,Name,Covers,Phone,Email,Time Created,Notes", split[0]);
        String[] bits = split[1].split(",");
        assertEquals("2017-06-29 00:00", bits[0]);
        assertEquals("ACCEPTED", bits[1]);
        assertEquals("ONLINE", bits[2]);
        assertEquals(booking1.getName(), bits[3]);
        assertEquals("10", bits[4]);
        assertEquals(booking1.getTelephone(), bits[5]);
        assertEquals(booking1.getEmail(), bits[6]);
        assertEquals(booking1.getNotes(), bits[8]);
    }

    @Test
    public void testCreateCSVWrapper2() throws Exception {
        setUpCashups();
        createCSVWrapperDynamic(false);
    }

    @Test
    public void testCreateCSVWrapper3() throws Exception {
        setUpCashups();
        createCSVWrapperDynamic(true);
    }

    private void createCSVWrapperDynamic(boolean withDynamicContent) {
        if(!withDynamicContent) {
            cashUp2.getPaymentReport().clear();
            cashUpRepository.save(cashUp2);
        }
        ReportingConstraints constraints = new ReportingConstraints();
        constraints.setStart(0);
        constraints.setEnd(20);
        constraints.setZoneId(ZoneId.of("Europe/London"));
        constraints.setRestaurantId(restaurant1.getId());

        List<CashUpReportLine> lines = reportingService.getCashupLines(constraints);
        // reverse the lines to maintain tests (currently will be desc order)
        lines = Lists.reverse(lines);
        CSVWrapper wrapper = reportingService.createCsvWrapper(lines, CashUpReportLine.class, "report.csv");
        assertEquals("report.csv", wrapper.getFileName());

        String[] split = wrapper.getContent().split("\n");
        assertEquals(3, split.length);
        int headerLength = split[0].split(",").length;

        for(int i = 1; i < split.length; i++) {
            // headers (WITHOUT dynamic) are currently:
            // "Start","End","On Premise Count","On Premise Value","Takeaway Count","Takeaway Value","Unpaid On Premise Count","Unpaid On Premise Value","Unpaid Takeaway Count",
            // "Unpaid Takeaway Value","Total Voids","Food Items Count","Food Items Value","Drink Items Count","Drink Items Value","Other Items Count","Other Items Value","Delivery Charges",
            // "Total Sales (before adjustments)","Total Bill Adjustments","Discounts","Total Sales (after adjustments)","Total VAT Charged","Net Sales","Tips (% on table)","Over-Payments","Total Payments (inc over-payments/tips)","PaymentTypes","Refunds","Unique ID"

            // headers (WITH dynamic) are currently:
            // "Start","End","On Premise Count","On Premise Value","Takeaway Count","Takeaway Value","Unpaid On Premise Count","Unpaid On Premise Value","Unpaid Takeaway Count",
            // "Unpaid Takeaway Value","Total Voids","Food Items Count","Food Items Value","Drink Items Count","Drink Items Value","Other Items Count","Other Items Value","Delivery Charges",
            // "Total Sales (before adjustments)","Total Bill Adjustments","Discounts","Total Sales (after adjustments)","Total VAT Charged","Net Sales","Tips (% on table)","Over-Payments","Total Payments (inc over-payments/tips)","CARD","CASH","Refunds""Unique ID"

            String[] bits = split[i].split(",");
            assertEquals(headerLength, bits.length);
            assertEquals(bits[0], "1970-01-01 01:00");
            assertEquals(bits[1], "1970-01-01 01:00");
            assertEquals(bits[2], "13");
            assertEquals(bits[3], "0.13");
            assertEquals(bits[4], "13");
            assertEquals(bits[5], "0.13");
            assertEquals(bits[6], "13");
            assertEquals(bits[7], "0.13");
            assertEquals(bits[8], "13");
            assertEquals(bits[9], "0.13");
            assertEquals(bits[10], "0.13");
            assertEquals(bits[11], "13");
            assertEquals(bits[12], "0.13");
            assertEquals(bits[13], "13");
            assertEquals(bits[14], "0.13");
            assertEquals(bits[15], "13");
            assertEquals(bits[16], "0.13");
            assertEquals(bits[17], "0.13");
            assertEquals(bits[18], "0.13");
            assertEquals(bits[19], "0.13");
            assertEquals(bits[20], "");
            assertEquals(bits[21], "0.00");
            assertEquals(bits[22], "0.13");
            assertEquals(bits[23], "0.13");
            assertEquals(bits[24], "0.13");
            assertEquals(bits[25], "0.13");
            assertEquals(bits[26], "0.13");
            if(withDynamicContent && i ==2) {
                assertEquals(bits[27], "0.10"); //card
                assertEquals(bits[28], "0.20"); //cash
                assertEquals(bits[29], ""); // refunds (none in this example)
                assertTrue(bits[30].equals(cashUp1.getId()) || bits[30].equals(cashUp2.getId()));
            } else if(!withDynamicContent) {
                assertEquals(bits[27], ""); //payment types
                assertEquals(bits[28], ""); // refunds (none in this example)
                assertTrue(bits[29].equals(cashUp1.getId()) || bits[29].equals(cashUp2.getId()));
            }
        }
    }

    private void setUpCashupNumbers(CashUp cashUp) throws Exception{
        for(Field field : CashUpKeys.class.getDeclaredFields()) {
            if(Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                cashUp.getReport().put((String)field.get(null), 13);
            }
        }
    }

    private void addCashOverpayment() {
        SessionArchive archive = sessionArchiveRepository.findBySessionId(session1.getId());
        Session session = archive.getSession();
        adjustmentType3.setType(AdjustmentTypeType.PAYMENT);
        adjustmentType3.setSupportsChange(true);
        adjustmentTypeRepository.save(adjustmentType3);
        Adjustment payment1 = new Adjustment(session.getId());
        payment1.setValue(200);
        payment1.setNumericalType(NumericalAdjustmentType.ABSOLUTE);
        payment1.setAdjustmentType(adjustmentType3);
        payment1.setStaffId("1");
        payment1.setCreated(bst1 + 3000);
        session.getAdjustments().add(payment1);
        sessionArchiveRepository.save(archive);
    }

}