package uk.co.epicuri.serverapi.service;

import static org.easymock.EasyMock.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import uk.co.epicuri.serverapi.common.pojo.external.ExternalIntegration;
import uk.co.epicuri.serverapi.common.pojo.external.KVData;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.Payment;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.PaymentSenseConstants;
import uk.co.epicuri.serverapi.common.pojo.external.paymentsense.TableResponse;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Default;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.FixedDefaults;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.repository.BaseIT;
import uk.co.epicuri.serverapi.service.external.PaymentSenseRestService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by manish on 01/08/2017.
 */
public class AsyncOrderHandlerServiceTest extends BaseIT{

    @Before
    public void setUp() throws Exception {
        super.setUp();

        menuItem1.setRestaurantId(restaurant1.getId());
        menuItem1.setType(ItemType.FOOD);
        menuItem1.setPrice(10);
        menuItem1.setTaxTypeId(tax1.getId());
        menuItemRepository.save(menuItem1);

        KVData value = new KVData();
        value.setHost("");
        value.setKey("");
        restaurant1.getIntegrations().put(ExternalIntegration.PAYMENT_SENSE, value);
        restaurant1.getTables().clear();
        table1.setName("t1");
        restaurant1.getTables().add(table1);
        restaurantRepository.save(restaurant1);

        session1.getTables().add(table1.getId());
        session1.setSessionType(SessionType.SEATED);
        session1.setRestaurantId(restaurant1.getId());
        sessionRepository.save(session1);

        Order order = new Order();
        order.setSessionId(session1.getId());
        order.setPriceOverride(menuItem1.getPrice());
        order.setMenuItemId(menuItem1.getId());
        order.setMenuItem(menuItem1);
        orderRepository.save(order);

        AdjustmentType psAdjustmentType1 = new AdjustmentType();
        psAdjustmentType1.setType(AdjustmentTypeType.PAYMENT);
        psAdjustmentType1.setName(PaymentSenseConstants.PS_ADJUSTMENT_TYPE);
        psAdjustmentType1.setSupportsChange(false);
        adjustmentTypeRepository.insert(psAdjustmentType1);

        AdjustmentType psAdjustmentType2 = new AdjustmentType();
        psAdjustmentType2.setType(AdjustmentTypeType.PAYMENT);
        psAdjustmentType2.setName(PaymentSenseConstants.PS_ADJUSTMENT_OTHER_TYPE);
        psAdjustmentType2.setSupportsChange(false);
        adjustmentTypeRepository.insert(psAdjustmentType2);

        AdjustmentType psAdjustmentType3 = new AdjustmentType();
        psAdjustmentType3.setType(AdjustmentTypeType.GRATUITY);
        psAdjustmentType3.setName(PaymentSenseConstants.PS_ADJUSTMENT_GRATUITY_TYPE);
        psAdjustmentType3.setSupportsChange(false);
        adjustmentTypeRepository.insert(psAdjustmentType3);
    }

    @Test
    public void onOrders() throws Exception {
        List<Order> orders = liveDataService.getOrders(session1.getId());

        AsyncOrderHandlerService asyncOrderHandlerService = getAsycHandlerService();
        PaymentSenseRestService paymentSenseRestService = mock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService, "paymentSenseRestService", paymentSenseRestService);
        expect(paymentSenseRestService.postTable("1", restaurant1, "t1", 10)).andReturn(true);
        expect(paymentSenseRestService.updateReceipt("1", restaurant1, "t1", 10, 0, orders)).andReturn(true);
        replay(paymentSenseRestService);

        asyncOrderHandlerService.onOrders("", restaurant1, session1, orders, orders);

        verify(paymentSenseRestService);

        reset(paymentSenseRestService);
        expect(paymentSenseRestService.postTable("1", restaurant1, "t1", 20)).andReturn(false);
        expect(paymentSenseRestService.updateReceipt("1", restaurant1, "t1", 10, 0, orders)).andReturn(true);
        expect(paymentSenseRestService.updateAmount("1", restaurant1, "t1", 10, 0, 20)).andReturn(true);
        replay(paymentSenseRestService);

        orders.forEach(o -> o.setId(null));
        orderRepository.insert(orders);
        asyncOrderHandlerService.onOrders("", restaurant1, session1, orders,orders);

        verify(paymentSenseRestService);
    }

    @Test
    public void onOrdersStockControl() throws Exception {
        List<Order> orders = liveDataService.getOrders(session1.getId());
        restaurant1.getIntegrations().clear();
        restaurantRepository.save(restaurant1);

        AsyncOrderHandlerService asyncOrderHandlerService = getAsycHandlerService();

        LiveDataService liveDataServiceMock = mock(LiveDataService.class);
        Whitebox.setInternalState(asyncOrderHandlerService, "liveDataService", liveDataServiceMock);
        replay(liveDataServiceMock);
        asyncOrderHandlerService.onOrders("", restaurant1, session1, orders,orders);
        verify(liveDataServiceMock);

        reset(liveDataServiceMock);
        liveDataServiceMock.updateStockControl(anyObject(), anyObject(), anyBoolean(), anyBoolean());
        expectLastCall();
        RestaurantDefault restaurantDefault = new RestaurantDefault(new Default(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true, "", "If true, will decrement stock number on every order", 0));
        restaurant1.getRestaurantDefaults().add(restaurantDefault);
        restaurantRepository.save(restaurant1);
        replay(liveDataServiceMock);
        asyncOrderHandlerService.onOrders("", restaurant1, session1, orders,orders);
        verify(liveDataServiceMock);
    }

    @Test
    public void onOrderRemoved() throws Exception {
        AsyncOrderHandlerService asyncOrderHandlerService = getAsycHandlerService();
        PaymentSenseRestService paymentSenseRestService = mock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService, "paymentSenseRestService", paymentSenseRestService);

        List<Order> orders = liveDataService.getOrders(session1.getId());
        expect(paymentSenseRestService.updateAmount("1", restaurant1, "t1", 10, 0, 0)).andReturn(true);
        expect(paymentSenseRestService.updateReceipt("1", restaurant1, "t1", 10, 0, orders)).andReturn(true);
        replay(paymentSenseRestService);

        Order order = orders.get(0);
        order.setAdjustment(adjustment1); //adjustment1 is a discount
        adjustment1.setValue(10);
        orderRepository.save(order);
        asyncOrderHandlerService.onOrderRemoved("", restaurant1, session1, order, orders);

        verify(paymentSenseRestService);
    }

    @Test
    public void onAllOrdersRemoved() throws Exception {
        //todo - only applicable to takeaways at the moment
    }

    @Test
    public void onSessionClose() throws Exception {
        AsyncOrderHandlerService asyncOrderHandlerService = getAsycHandlerService();
        PaymentSenseRestService paymentSenseRestService = mock(PaymentSenseRestService.class);
        Whitebox.setInternalState(asyncOrderHandlerService, "paymentSenseRestService", paymentSenseRestService);

        TableResponse tableResponse = new TableResponse();
        tableResponse.setAmount(10);
        tableResponse.setAmountPaid(0);
        tableResponse.setTableName("t1");
        tableResponse.setPayments(new ArrayList<>());
        expect(paymentSenseRestService.getTable(restaurant1, "t1")).andReturn(tableResponse);
        expect(paymentSenseRestService.getTable(restaurant1, "t1")).andReturn(tableResponse);
        expect(paymentSenseRestService.deleteTable("1", restaurant1,"t1", 10, 0)).andReturn(true);
        replay(paymentSenseRestService);

        asyncOrderHandlerService.onSessionClose("", restaurant1, session1, false);

        verify(paymentSenseRestService);
    }

    @Test
    public void onSessionCloseUpdateStock() throws Exception {
        restaurant1.getRestaurantDefaults().add(new RestaurantDefault(new Default(FixedDefaults.ENABLE_STOCK_COUNTDOWN, true, "", "If true, will decrement stock number on every order", 0)));
        restaurant1.getRestaurantDefaults().add(new RestaurantDefault(new Default(FixedDefaults.AUTO_STOCK_UNAVAILABLE, true, "", "If true and ENABLE_STOCK_COUNTDOWN is true, will automatically set any item with the PLU to ", 0)));
        restaurant1.getIntegrations().clear();
        restaurantRepository.save(restaurant1);
        session1.setSessionType(SessionType.ADHOC);
        sessionRepository.save(session1);

        AsyncOrderHandlerService asyncOrderHandlerService = getAsycHandlerService();
        LiveDataService mockLiveDataService = mock(LiveDataService.class);
        Whitebox.setInternalState(asyncOrderHandlerService,"liveDataService",mockLiveDataService);
        List<Order> orders = liveDataService.getOrders(session1.getId());
        expect(mockLiveDataService.getOrders(session1.getId())).andReturn(orders);
        mockLiveDataService.updateStockControl(restaurant1, orders, true, true);
        expectLastCall();
        replay(mockLiveDataService);

        asyncOrderHandlerService.onSessionClose("", restaurant1, session1, true);
        verify(mockLiveDataService);
    }

    @Test
    public void onReconciliationRequest() throws Exception {
        Adjustment payment1 = new Adjustment(session1.getId());
        payment1.setValue(1);
        payment1.setAdjustmentType(adjustmentType1);
        session1.getAdjustments().add(payment1);

        AsyncOrderHandlerService asyncOrderHandlerService = getAsycHandlerService();
        PaymentSenseRestService paymentSenseRestService = mock(PaymentSenseRestService.class);
        LiveDataService liveDataService = mock(LiveDataService.class);
        Whitebox.setInternalState(asyncOrderHandlerService, "paymentSenseRestService", paymentSenseRestService);
        Whitebox.setInternalState(asyncOrderHandlerService, "liveDataService", liveDataService);

        TableResponse tableResponse = new TableResponse();
        Payment psPayment1 = createPayment(1, "123");
        tableResponse.getPayments().add(psPayment1);
        expect(paymentSenseRestService.getTable(restaurant1,"t1")).andReturn(tableResponse);
        expect(liveDataService.getOrders(session1.getId())).andReturn(new ArrayList<>());
        expect(paymentSenseRestService.updateReceipt(anyString(), anyObject(), anyString(), anyInt(), anyInt(), anyObject())).andReturn(true);
        replay(paymentSenseRestService);

        asyncOrderHandlerService.onReconciliationRequest("", restaurant1.getId(), session1.getId());

        verify(paymentSenseRestService);

        Session session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertEquals(1, session.getAdjustments().get(0).getValue());
        assertEquals(psPayment1, session.getAdjustments().get(0).getSpecialAdjustmentData().get(PaymentSenseConstants.PAYMENT_KEY));

        //ensure it doesn't get added again
        reset(paymentSenseRestService);
        reset(liveDataService);
        expect(paymentSenseRestService.getTable(restaurant1,"t1")).andReturn(tableResponse);
        replay(paymentSenseRestService);
        asyncOrderHandlerService.onReconciliationRequest("", restaurant1.getId(), session1.getId());
        verify(paymentSenseRestService);
        session = sessionRepository.findOne(session1.getId());
        assertEquals(1, session.getAdjustments().size());
        assertEquals(1, session.getAdjustments().get(0).getValue());
        assertEquals(psPayment1, session.getAdjustments().get(0).getSpecialAdjustmentData().get(PaymentSenseConstants.PAYMENT_KEY));

        reset(paymentSenseRestService);
        reset(liveDataService);
        Payment psPayment2 = createPayment(2, "abc");
        tableResponse.getPayments().add(psPayment2);
        expect(paymentSenseRestService.getTable(restaurant1,"t1")).andReturn(tableResponse);
        expect(liveDataService.getOrders(session1.getId())).andReturn(new ArrayList<>());
        expect(paymentSenseRestService.updateReceipt(anyString(), anyObject(), anyString(), anyInt(), anyInt(), anyObject())).andReturn(true);
        replay(paymentSenseRestService);
        asyncOrderHandlerService.onReconciliationRequest("", restaurant1.getId(), session1.getId());
        verify(paymentSenseRestService);
        session = sessionRepository.findOne(session1.getId());
        assertEquals(2, session.getAdjustments().size());
        assertEquals(1, session.getAdjustments().get(0).getValue());
        assertEquals(2, session.getAdjustments().get(1).getValue());
        assertEquals(psPayment1, session.getAdjustments().get(0).getSpecialAdjustmentData().get(PaymentSenseConstants.PAYMENT_KEY));
        assertEquals(psPayment2, session.getAdjustments().get(1).getSpecialAdjustmentData().get(PaymentSenseConstants.PAYMENT_KEY));
    }

    private Payment createPayment(int amount, String authCode) {
        Payment psPayment = new Payment();
        psPayment.setCardSchemeName("aName");
        psPayment.setAmountPaid(amount);
        psPayment.setAuthCode(authCode);
        psPayment.setTime("atime");
        psPayment.setPaymentId(RandomStringUtils.randomAlphanumeric(4));
        return psPayment;
    }
}