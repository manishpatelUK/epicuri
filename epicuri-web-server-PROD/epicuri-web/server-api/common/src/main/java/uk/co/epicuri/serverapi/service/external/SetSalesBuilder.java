package uk.co.epicuri.serverapi.service.external;

import uk.co.epicuri.serverapi.common.pojo.external.marketman.MarketManDish;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.MarketManModifier;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.MarketManTransaction;
import uk.co.epicuri.serverapi.common.pojo.external.marketman.SetSalesRequest;
import uk.co.epicuri.serverapi.common.pojo.model.TaxRate;
import uk.co.epicuri.serverapi.common.pojo.model.menu.MenuItem;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.DateTimeConstants;
import uk.co.epicuri.serverapi.service.SessionCalculationService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SetSalesBuilder {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private List<Order> allOrders;
    private long fromUTC;
    private long toUTC;
    private String token;
    private int gross;
    private int net;
    private String id;
    private Map<String, TaxRate> taxRateMap;

    public SetSalesBuilder withOrders(List<Order> allOrders) {
        this.allOrders = allOrders;
        return this;
    }

    public SetSalesBuilder withLimits(long fromUTC, long toUTC) {
        this.fromUTC = fromUTC;
        this.toUTC = toUTC;
        return this;
    }

    public SetSalesBuilder withToken(String token) {
        this.token = token;
        return this;
    }

    public SetSalesBuilder withTotalGross(int gross) {
        this.gross = gross;
        return this;
    }

    public SetSalesBuilder withTotalNet(int net) {
        this.net = net;
        return this;
    }

    public SetSalesBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public SetSalesBuilder withTaxRates(Map<String,TaxRate> taxRateMap) {
        this.taxRateMap = taxRateMap;
        return this;
    }


    public SetSalesRequest build() {
        SetSalesRequest request = new SetSalesRequest(token);
        request.setUniqueId(id);

        ZoneId utc = ZoneId.of("UTC");
        request.setFromDateUTC(DateTimeConstants.convertToDateTime(FORMATTER, utc, fromUTC));
        String toDateUTC = DateTimeConstants.convertToDateTime(FORMATTER, utc, toUTC);
        request.setToDateUTC(toDateUTC);

        request.setTotalPriceWithVAT(MoneyService.toMoneyRoundNearest(gross));
        request.setTotalPriceWithoutVAT(MoneyService.toMoneyRoundNearest(net));

        Set<MenuItem> menuItems = new HashSet<>();
        Set<Modifier> modifiers = new HashSet<>();
        for(Order order : allOrders) {
            menuItems.add(order.getMenuItem());
            if(order.getModifiers() != null) {
                modifiers.addAll(order.getModifiers());
            }
        }

        //dishes and modifiers
        request.setDishes(createDishesArray(menuItems, taxRateMap));
        request.setModifiers(createModifiersArray(modifiers));

        //update Transactions
        List<MarketManTransaction> transactionsArray = createTransactionsArray(allOrders);
        transactionsArray.forEach(t -> t.setDateUTC(toDateUTC));
        request.setTransactions(transactionsArray);

        return request;
    }

    private List<MarketManDish> createDishesArray(Collection<MenuItem> items, Map<String,TaxRate> taxRateMap) {
        List<MarketManDish> list = new ArrayList<>();
        for(MenuItem menuItem : items) {
            list.add(createDish(menuItem, taxRateMap.get(menuItem.getTaxTypeId())));
        }
        return list;
    }

    private MarketManDish createDish(MenuItem menuItem, TaxRate taxRate) {
        return new MarketManDish(menuItem, MoneyService.toMoneyRoundNearest(SessionCalculationService.calculateNet(menuItem.getPrice(), taxRate.getRateAsDouble())));
    }

    private List<MarketManModifier> createModifiersArray(Collection<Modifier> modifiers) {
        List<MarketManModifier> list = new ArrayList<>();
        for(Modifier modifier : modifiers) {
            list.add(createModifier(modifier, modifier.getTaxRate()));
        }

        return list;
    }

    private List<MarketManTransaction> createTransactionsArray(List<Order> allOrders) {
        List<MarketManTransaction> transactions = new ArrayList<>();

        Map<String,Integer> cumulativeGrossMenuItems = new HashMap<>();
        Map<String,Integer> cumulativeGrossModifiers = new HashMap<>();
        Map<String,Integer> cumulativeNetMenuItems = new HashMap<>();
        Map<String,Integer> cumulativeNetModifiers = new HashMap<>();

        Set<MenuItem> allItems = new HashSet<>();
        Set<Modifier> allModifiers = new HashSet<>();

        Map<String,Integer> itemAggregation = new HashMap<>();
        Map<String,Integer> modifierAggregation = new HashMap<>();

        for(Order order : allOrders) {
            int itemGross = SessionCalculationService.getOrderValueExcludingModifiers(order);

            cumulativeGrossMenuItems.put(order.getMenuItem().getPlu(), cumulativeGrossMenuItems.getOrDefault(order.getMenuItemId(), 0) + itemGross);
            cumulativeNetMenuItems.put(order.getMenuItem().getPlu(), cumulativeNetMenuItems.getOrDefault(order.getMenuItemId(), 0) + SessionCalculationService.calculateNet(itemGross, order.getTaxRate().getRateAsDouble()));
            itemAggregation.put(order.getMenuItem().getPlu(), itemAggregation.getOrDefault(order.getMenuItem().getPlu(),0) + order.getQuantity());

            allItems.add(order.getMenuItem());

            for(Modifier modifier : order.getModifiers()) {
                int modifierGross = SessionCalculationService.getActualPrice(modifier) * order.getQuantity();
                cumulativeGrossModifiers.put(modifier.getPlu(), cumulativeGrossModifiers.getOrDefault(order.getMenuItemId(), 0) + modifierGross);
                cumulativeNetModifiers.put(modifier.getPlu(), cumulativeNetModifiers.getOrDefault(order.getMenuItemId(), 0) + SessionCalculationService.calculateNet(modifierGross, modifier.getTaxRate().getRateAsDouble()));
                modifierAggregation.put(modifier.getPlu(), modifierAggregation.getOrDefault(modifier.getPlu(),0) + order.getQuantity());
            }
            allModifiers.addAll(order.getModifiers());
        }

        for(MenuItem item : allItems) {
            MarketManTransaction transaction = new MarketManTransaction();
            transaction.setCode(item.getPlu());
            transaction.setId(item.getId());
            transaction.setName(item.getName());
            transaction.setGrossTotal(MoneyService.toMoneyRoundNearest(cumulativeGrossMenuItems.getOrDefault(item.getPlu(), 0)));
            transaction.setNetTotal(MoneyService.toMoneyRoundNearest(cumulativeNetMenuItems.getOrDefault(item.getPlu(), 0)));
            transaction.setQuantity(itemAggregation.getOrDefault(item.getPlu(), 0));
            transactions.add(transaction);
        }

        for(Modifier modifier : allModifiers) {
            MarketManTransaction transaction = new MarketManTransaction();
            transaction.setCode(modifier.getPlu());
            transaction.setId(modifier.getId());
            transaction.setName(modifier.getModifierValue());
            transaction.setGrossTotal(MoneyService.toMoneyRoundNearest(cumulativeGrossModifiers.getOrDefault(modifier.getPlu(), 0)));
            transaction.setNetTotal(MoneyService.toMoneyRoundNearest(cumulativeNetModifiers.getOrDefault(modifier.getPlu(), 0)));
            transaction.setQuantity(modifierAggregation.getOrDefault(modifier.getPlu(), 0));
            transactions.add(transaction);
        }

        return transactions;
    }

    private MarketManModifier createModifier(Modifier modifier, TaxRate taxRate) {
        return new MarketManModifier(modifier, MoneyService.toMoneyRoundNearest(SessionCalculationService.calculateNet(modifier.getPrice(), taxRate.getRateAsDouble())));
    }
}
