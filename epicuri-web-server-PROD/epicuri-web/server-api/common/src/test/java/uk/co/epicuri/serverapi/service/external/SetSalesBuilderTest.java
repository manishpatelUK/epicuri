package uk.co.epicuri.serverapi.service.external;

import org.junit.Before;
import org.junit.Test;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.MarketManDish;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.MarketManModifier;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.MarketManTransaction;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.SetSalesRequest;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.service.SessionCalculationService;
import uk.co.epicuri.serverapi.service.SessionSetupBaseIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class SetSalesBuilderTest extends SessionSetupBaseIT {

    @Before
    public void setUp() throws Exception {
        super.setUpSession();

        order1.setMenuItem(menuItem1);
        order2.setMenuItem(menuItem2);
        order3.setMenuItem(menuItem3);

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(order3);
    }

    @Test
    public void testBuild() {
        List<Order> orders = new ArrayList<>();
        orders.add(order1);
        orders.add(order2);
        orders.add(order3);

        long from = 1530863350468L; //Fri Jul 06 2018 07:49:10
        long to = 1530863377132L; //Fri Jul 06 2018 07:49:37

        Map<String,TaxRate> taxRateMap = masterDataService.getTaxRate().stream().collect(Collectors.toMap(TaxRate::getId, Function.identity()));

        SetSalesBuilder builder = new SetSalesBuilder()
                .withId("123")
                .withLimits(from,to)
                .withOrders(orders)
                .withTaxRates(taxRateMap)
                .withToken("abc")
                .withTotalGross(10)
                .withTotalNet(5);

        SetSalesRequest request = builder.build();
        assertEquals("123", request.getUniqueId());
        assertEquals("abc", request.getToken());
        assertEquals("2018/07/06 07:49:10", request.getFromDateUTC());
        assertEquals("2018/07/06 07:49:37", request.getToDateUTC());
        assertEquals(0.1, request.getTotalPriceWithVAT(), 0.001);
        assertEquals(0.05, request.getTotalPriceWithoutVAT(), 0.001);
        assertEquals(3, request.getDishes().size());
        test(menuItem1, request.getDishes().stream().filter(d -> d.getId().equals(menuItem1.getId())).findFirst().orElse(null), taxRateMap);
        test(menuItem2, request.getDishes().stream().filter(d -> d.getId().equals(menuItem2.getId())).findFirst().orElse(null), taxRateMap);
        test(menuItem3, request.getDishes().stream().filter(d -> d.getId().equals(menuItem3.getId())).findFirst().orElse(null), taxRateMap);

        assertEquals(1, request.getModifiers().size());
        test(modifier1, request.getModifiers().get(0), taxRateMap);

        assertEquals(4, request.getTransactions().size());
        test(order1, request.getTransactions().stream().filter(s -> s.getId().equals(menuItem1.getId())).findFirst().orElse(null), 10, 10);
        test(order2, request.getTransactions().stream().filter(s -> s.getId().equals(menuItem2.getId())).findFirst().orElse(null), 40, 33);
        test(order3, request.getTransactions().stream().filter(s -> s.getId().equals(menuItem3.getId())).findFirst().orElse(null), 90, 77);
        testModifier(order2, request.getTransactions().stream().filter(s -> s.getId().equals(modifier1.getId())).findFirst().orElse(null), 226, 188);

        for(MarketManTransaction transaction : request.getTransactions()) {
            assertEquals("2018/07/06 07:49:37", transaction.getDateUTC());
        }
    }

    private void test(Order order, MarketManTransaction transaction, int gross, int net) {
        assertNotNull(transaction);
        assertEquals(order.getMenuItem().getId(), transaction.getId());
        assertEquals(order.getMenuItem().getName(), transaction.getName());
        assertEquals(order.getMenuItem().getPlu(), transaction.getCode());
        assertEquals(MoneyService.toMoneyRoundNearest(gross), transaction.getGrossTotal(), 0.001);
        assertEquals(MoneyService.toMoneyRoundNearest(net), transaction.getNetTotal(), 0.001);
        assertEquals(order.getQuantity(), transaction.getQuantity(), 0.001);
        assertNotNull(transaction.getDateUTC());
    }

    private void testModifier(Order order, MarketManTransaction transaction, int gross, int net) {
        assertNotNull(transaction);
        assertEquals(order.getModifiers().get(0).getId(), transaction.getId());
        assertEquals(order.getModifiers().get(0).getModifierValue(), transaction.getName());
        assertEquals(order.getModifiers().get(0).getPlu(), transaction.getCode());
        assertEquals(MoneyService.toMoneyRoundNearest(gross), transaction.getGrossTotal(), 0.001);
        assertEquals(MoneyService.toMoneyRoundNearest(net), transaction.getNetTotal(), 0.001);
        assertEquals(order.getQuantity(), transaction.getQuantity(), 0.001);
        assertNotNull(transaction.getDateUTC());
    }

    private void test(MenuItem item, MarketManDish dish, Map<String, TaxRate> taxRateMap) {
        if(item == null || dish == null) {
            fail();
        }
        assertEquals(item.getId(), dish.getId());
        assertEquals(item.getName(), dish.getName());
        assertEquals(MoneyService.toMoneyRoundNearest(item.getPrice()), dish.getGrossPrice(), 0.001);
        assertEquals(MoneyService.toMoneyRoundNearest(SessionCalculationService.calculateNet(item.getPrice(), taxRateMap.get(item.getTaxTypeId()).getRateAsDouble())), dish.getNetPrice(), 0.001);
        assertEquals(item.getPlu(), dish.getCode());
        assertEquals(item.getType().getName(), dish.getCategory());
    }

    private void test(Modifier modifier, MarketManModifier mmModifier, Map<String, TaxRate> taxRateMap) {
        if(modifier == null || mmModifier == null) {
            fail();
        }
        assertEquals(modifier.getId(), mmModifier.getId());
        assertEquals(modifier.getModifierValue(), mmModifier.getName());
        assertEquals(MoneyService.toMoneyRoundNearest(modifier.getPrice()), mmModifier.getGrossPrice(), 0.001);
        assertEquals(MoneyService.toMoneyRoundNearest(SessionCalculationService.calculateNet(modifier.getPrice(), taxRateMap.get(modifier.getTaxTypeId()).getRateAsDouble())), mmModifier.getNetPrice(), 0.001);
        assertEquals(modifier.getPlu(), mmModifier.getCode());
        assertEquals(modifier.getType().getName(), mmModifier.getCategory());
    }
}