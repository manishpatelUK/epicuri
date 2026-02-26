package uk.co.epicuri.serverapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.common.Tuple;
import uk.co.epicuri.serverapi.common.pojo.model.menu.ItemType;
import uk.co.epicuri.serverapi.common.pojo.model.menu.Modifier;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.session.*;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;
import uk.co.epicuri.serverapi.engines.FuseBoxAggregationProxy;
import uk.co.epicuri.serverapi.service.util.OrderSummary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.co.epicuri.serverapi.service.util.MapUtil.update;

@Service
public class SessionCalculationService {
    @Autowired
    private MasterDataService masterDataService;

    @Autowired
    private LiveDataService liveDataService;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    public boolean isPaid(Session session) {
        Map<CalculationKey,Number> map = calculateValues(session);

        return isPaid(map);
    }

    public static boolean isPaid(Map<CalculationKey, Number> map) {
        return remainingValueIsNotGreaterThanZero(map);
    }

    public static boolean totalIsNotZero(Map<CalculationKey, Number> map) {
        return map.getOrDefault(CalculationKey.TOTAL, 0).intValue() != 0;
    }

    public static boolean remainingValueIsNotGreaterThanZero(Map<CalculationKey, Number> map) {
        return !(map.getOrDefault(CalculationKey.REMAINING_TOTAL, 0).intValue() > 0);
    }

    public Map<CalculationKey,Number> calculateValues(Session session) {
        return calculateValues(session, liveDataService.getOrders(session.getId()));
    }

    public BillSplit calculateDinerSplits(Session session,
                                          List<Order> orders) {
        Map<CalculationKey,Number> calculations = calculateValues(session, orders);
        return calculateDinerSplits(session, orders, calculations);
    }

    public BillSplit calculateDinerSplits(Session session, List<Order> orders, Map<CalculationKey, Number> calculations) {
        BillSplit billSplit = new BillSplit();
        List<Diner> diners = session.getDiners();

        orders = orders.stream().filter(o -> o.getAdjustment() == null).collect(Collectors.toList());
        Map<String,List<Order>> ordersByDiner = orders.stream().collect(Collectors.groupingBy(Order::getDinerId));
        for(Map.Entry<String,List<Order>> entry : ordersByDiner.entrySet()) {
            OrderSummary summary = summarise(entry.getValue());
            billSplit.getVatSplits().put(entry.getKey(), summary.getSumVAT());
        }

        Diner defaultDiner = null;
        for(Diner diner : diners) {
            int dinerTotalPennies = 0;
            for (Order order : orders) {
                if(isOrderVoided(order)) {
                    continue;
                }

                if (order.getDinerId().equals(diner.getId())
                        && !order.isRemoveFromReports()) {
                    dinerTotalPennies += getOrderValue(order);
                    if(diner.isDefaultDiner()) {
                        defaultDiner = diner;
                    }
                }
            }
            billSplit.getItemSplits().put(diner.getId(), dinerTotalPennies);
        }

        int tableTotalPennies = defaultDiner == null ? 0 : billSplit.getItemSplits().get(defaultDiner.getId());
        List<Diner> actualDiners = diners.stream().filter(d -> !d.isDefaultDiner()).collect(Collectors.toList());
        updateSplitForDiner(billSplit.getTableItemSplits(), actualDiners, tableTotalPennies);
        updateSplitForDiner(billSplit.getTipSplits(), actualDiners, calculations.get(CalculationKey.TIP_TOTAL).intValue());
        updateSplitForDiner(billSplit.getDiscountSplits(), actualDiners, calculations.get(CalculationKey.DISCOUNT_TOTAL).intValue());
        updateSplitForDiner(billSplit.getEqualSplits(), actualDiners, calculations.get(CalculationKey.TOTAL).intValue());

        return billSplit;
    }

    private void updateSplitForDiner(Map<String,Integer> map, List<Diner> diners, int totalToSplit) {
        int nDiners = diners.size();
        if(nDiners == 0) {
            return;
        }

        int[] splits = MoneyService.split(totalToSplit, nDiners);
        int i = 0;
        for(Diner diner : diners) {
            map.put(diner.getId(), splits[i]);
            i++;
        }
    }

    /**
     * Calculates the revenues and totals for a single session
     * @param session the session
     * @param orders orders in this session
     * @return
     */
    public Map<CalculationKey,Number> calculateValues(Session session, List<Order> orders) {
        int runningTotal = 0;
        BigDecimal vatTotal;

        Map<CalculationKey, Number> calculatedValues = new HashMap<>();

        OrderSummary summary = summarise(orders);
        runningTotal = summary.getItemTypeTotal().values().stream().mapToInt(Integer::intValue).sum();
        vatTotal = BigDecimal.valueOf(summary.getItemTypeVatTotal().values().stream().mapToInt(Integer::intValue).sum());
        calculatedValues.put(CalculationKey.VAT_TOTAL_BEFORE_ADJUSTMENTS, MoneyService.toPenniesRoundNearest(vatTotal));

        calculatedValues.put(CalculationKey.SESSION_TOTAL, runningTotal);

        int subTotal = runningTotal;
        int discountTotal = 0;
        int tipTotal = 0;

        // calculate discounts before payments
        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.isVoided()) {
                continue;
            }
            AdjustmentTypeType type = adjustment.getAdjustmentType().getType();

            if(type == AdjustmentTypeType.DISCOUNT) {
                int adj = adjustment.getValue();
                if (adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
                    if (adj > runningTotal) {
                        adj = runningTotal;
                    }
                    discountTotal += adj;
                    runningTotal -= adj;
                }
                else if (adjustment.getNumericalType() == NumericalAdjustmentType.PERCENTAGE) {
                    if (runningTotal > 0) {
                        if (adj >= 1000) {
                            discountTotal += runningTotal;
                            runningTotal = 0;
                        }
                        else {
                            int thisDiscount = discountOperatingValueByPercentage(adjustment.getValue(), runningTotal);
                            discountTotal += thisDiscount;
                            runningTotal -= thisDiscount;
                        }
                    }
                }
            }
        }

        discountTotal = discountTotal > subTotal ? subTotal : discountTotal;
        calculatedValues.put(CalculationKey.DISCOUNT_TOTAL, discountTotal);

        //recalculate vat as a proportion of discount
        if(discountTotal > 0) {
            BigDecimal totalVatable = BigDecimal.valueOf(calculatedValues.get(CalculationKey.SESSION_TOTAL).intValue());
            BigDecimal discountFractionOfTotal = BigDecimal.ONE.subtract(BigDecimal.valueOf(discountTotal).setScale(5, BigDecimal.ROUND_HALF_EVEN).divide(totalVatable, BigDecimal.ROUND_HALF_EVEN));
            vatTotal = discountFractionOfTotal.multiply(vatTotal);
        }

        // tips always on whatever the customer is billed
        if(session.getTipPercentage() != null) {
            BigDecimal calculatedTipValue = BigDecimal.valueOf(session.getTipPercentage()).scaleByPowerOfTen(-2).multiply(BigDecimal.valueOf(subTotal-discountTotal));
            tipTotal = MoneyService.toPenniesRoundNearest(calculatedTipValue);
        }

        calculatedValues.put(CalculationKey.TIP_PERCENTAGE, session.getTipPercentage());
        calculatedValues.put(CalculationKey.TIP_TOTAL, tipTotal);

        int paymentNoChangeSupport = 0;
        int paymentChangeSupport = 0;
        int paymentsVoided = 0;
        //payments are handled after discounts
        for(Adjustment adjustment : session.getAdjustments()) {
            AdjustmentTypeType type = adjustment.getAdjustmentType().getType();

            if(type == AdjustmentTypeType.PAYMENT) {
                int adj = adjustment.getValue();
                if (adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
                    if(adjustment.isVoided()) {
                        paymentsVoided += adj;
                    } else {
                        runningTotal -= adj;
                        if (adjustment.getAdjustmentType().isSupportsChange()) {
                            paymentChangeSupport += adj;
                        } else {
                            paymentNoChangeSupport += adj;
                        }
                    }
                }
                else if (adjustment.getNumericalType() == NumericalAdjustmentType.PERCENTAGE) {
                    int thisPayment = discountOperatingValueByPercentage(adjustment.getValue(), runningTotal);
                    if(adjustment.isVoided()) {
                        paymentsVoided += thisPayment;
                    } else {
                        runningTotal -= thisPayment;
                        if (adjustment.getAdjustmentType().isSupportsChange()) {
                            paymentChangeSupport += thisPayment;
                        } else {
                            paymentNoChangeSupport += thisPayment;
                        }
                    }
                }
            }
        }
        int paymentTotal = paymentChangeSupport + paymentNoChangeSupport;
        calculatedValues.put(CalculationKey.TOTAL_PAYMENTS_CHANGEABLE, paymentChangeSupport);
        calculatedValues.put(CalculationKey.TOTAL_PAYMENTS_NOT_CHANGEABLE, paymentNoChangeSupport);
        calculatedValues.put(CalculationKey.TOTAL_PAYMENTS, paymentTotal);
        calculatedValues.put(CalculationKey.TOTAL_PAYMENTS_VOIDED, paymentsVoided);

        int remainingTotal = runningTotal;
        int calculatedDeliveryCost = 0;

        if(session.getOriginalBooking() != null
                && session.getOriginalBooking().getTakeawayType() == TakeawayType.DELIVERY
                && session.getCalculatedDeliveryCost() == null) {
            calculatedDeliveryCost = calculateDeliveryCost(session);
        }
        calculatedValues.put(CalculationKey.DELIVERY_TOTAL, calculatedDeliveryCost);

        int total = (subTotal + tipTotal + calculatedDeliveryCost) - discountTotal;
        calculatedValues.put(CalculationKey.TOTAL_BEFORE_TIP, total-tipTotal);

        if((subTotal - discountTotal) <= 0) {
            total = 0;
        }
        else {
            remainingTotal += tipTotal;
        }

        if(total < 0) {
            total = 0;
        }

        int overpayment = paymentTotal > total ? paymentTotal - total : 0;

        int change = 0;
        if (overpayment > 0) {
            //if non-changeable overpayment is > total, then the difference is the new overpayment and all changeable overpayment is change
            if(paymentNoChangeSupport >= total) {
                overpayment = paymentNoChangeSupport - total;
                change = paymentChangeSupport;
            } else if(paymentNoChangeSupport < total) {
                //otherwise there is no overpayment and the rest is change (after total is paid)
                overpayment = 0;
                change = paymentTotal - total;
            }
        }

        //new adjustment type - gratuities
        int gratuities = 0;
        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.isVoided()) {
                continue;
            }

            AdjustmentTypeType type = adjustment.getAdjustmentType().getType();

            if (type == AdjustmentTypeType.GRATUITY) {
                int adj = adjustment.getValue();
                if (adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
                    if(adjustment.getAdjustmentType().isSupportsChange()) {
                        change += adj;
                    } else {
                        overpayment += adj;
                        gratuities += adj;
                    }
                }
                else if (adjustment.getNumericalType() == NumericalAdjustmentType.PERCENTAGE) {
                    int thisGratuity = discountOperatingValueByPercentage(adjustment.getValue(), runningTotal);
                    if (adjustment.getAdjustmentType().isSupportsChange()) {
                        change += thisGratuity;
                    } else {
                        overpayment += thisGratuity;
                        gratuities += adj;
                    }
                }
            }
        }
        calculatedValues.put(CalculationKey.GRATUITIES, gratuities);

        calculatedValues.put(CalculationKey.TOTAL, total);

        calculatedValues.put(CalculationKey.TOTAL_BEFORE_ADJUSTMENTS, subTotal);
        calculatedValues.put(CalculationKey.SUB_TOTAL, subTotal);

        calculatedValues.put(CalculationKey.VAT_TOTAL, MoneyService.toPenniesRoundNearest(vatTotal));

        calculatedValues.put(CalculationKey.REMAINING_TOTAL, remainingTotal);
        calculatedValues.put(CalculationKey.OVER_PAYMENTS, overpayment);
        calculatedValues.put(CalculationKey.OVER_PAYMENTS_INCLUDING_TIP, overpayment + tipTotal);
        calculatedValues.put(CalculationKey.CHANGE_DUE, change);

        int numberOfGuests = 0;
        if(session.getDiners() != null) {
            if(session.getSessionType() == SessionType.ADHOC) {
                numberOfGuests++;
            } else {
                for (Diner diner : session.getDiners()) {
                    if (!diner.isDefaultDiner()) {
                        numberOfGuests++;
                    }
                }
            }
        }
        calculatedValues.put(CalculationKey.NUMBER_OF_GUESTS, numberOfGuests);

        return calculatedValues;
    }

    public int calculateDeliveryCost(Session session) {
        Restaurant restaurant = masterDataService.getRestaurant(session.getRestaurantId());
        if(restaurant == null) {
            return 0;
        }

        if(session.getSessionType() != SessionType.TAKEAWAY) {
            return 0;
        }

        Booking booking = session.getOriginalBooking();
        if(booking == null) {
            return 0;
        }

        if(booking.getTakeawayType() != TakeawayType.DELIVERY) {
            return 0;
        }

        if(session.getCalculatedDeliveryCost() != null) {
            return session.getCalculatedDeliveryCost();
        }

        FuseBoxAggregationProxy proxy = FuseBoxAggregationProxy.createTakeawayProxy(autowireCapableBeanFactory, booking, session, restaurant);
        return proxy.getDeliverySurcharge();
    }

    public static OrderSummary summarise(List<Order> orders) {
        OrderSummary summary = new OrderSummary();
        orders = orders
                .stream()
                .filter(o -> !o.isRemoveFromReports()
                        && o.getDeleted() == null)
                .collect(Collectors.toList());

        // group orders by tax group
        Map<String,List<Order>> ordersByTax = orders.stream().collect(Collectors.groupingBy(o -> o.getMenuItem().getTaxTypeId()));

        // group modifiers by tax group
        // add order/menu item total
        Map<String,List<ModifierTuple>> modifiersByTax = new HashMap<>();
        for(Order order : orders) {
            summary.updateMenuItemCount(order.getMenuItem(), order.getQuantity());
            summary.updateMenuItemTotal(order.getMenuItem(), getMenuItemActualPrice(order) * order.getQuantity());
            for(Modifier modifier : order.getModifiers()) {
                if(!modifiersByTax.containsKey(modifier.getTaxTypeId())) {
                    modifiersByTax.put(modifier.getTaxTypeId(), new ArrayList<>());
                }
                for(int i = 0; i < order.getQuantity(); i++) {
                    modifiersByTax.get(modifier.getTaxTypeId()).add(new ModifierTuple(modifier, order));
                }
            }
        }

        ordersByTax.forEach((k,v) -> calculateOrderVATOnTypesTaxGroup(v, summary));
        modifiersByTax.forEach((k,v) -> calculateModifierVATOnTaxGroup(v, summary));

        // add any remaining pennies to the first category for tax group and item type
        int totalVAT = summary.getSumVAT();

        int sumVATByItem = summary.getItemTypeVatTotal().values().stream().reduce(0, Integer::sum);
        int sumVATByVATType = summary.getVatTypeTotal().values().stream().reduce(0, Integer::sum);

        if(sumVATByItem < totalVAT) {
            addDifference(summary.getItemTypeVatTotal(), totalVAT - sumVATByItem);
        }

        if(sumVATByVATType < totalVAT) {
            addDifference(summary.getVatTypeTotal(), totalVAT - sumVATByVATType);
        }

        return summary;
    }

    private static <T> void addDifference(Map<T,Integer> destination, int difference) {
        for(Map.Entry<T,Integer> entry : destination.entrySet()) {
            if(entry.getValue() != 0) {
                destination.put(entry.getKey(), destination.get(entry.getKey()) + difference);
                break;
            }
        }
    }

    private static void calculateOrderVATOnTypesTaxGroup(List<Order> orders, OrderSummary summary) {
        Map<ItemType, List<Order>> ordersByTypeMap = orders.stream().collect(Collectors.groupingBy(o -> o.getMenuItem().getType()));
        Map<ItemType, Integer> totalCaptured = new HashMap<>();

        for(Map.Entry<ItemType, List<Order>> entry : ordersByTypeMap.entrySet()) {
            int totalByType = calculateTotalsAndSummariseByType(entry.getValue(), entry.getKey(), summary);
            totalCaptured.put(entry.getKey(), totalByType);
        }

        BigDecimal totalVAT = BigDecimal.valueOf(calculateVAT(orders, false));
        int total = totalCaptured.values().stream().mapToInt(Integer::intValue).sum();

        ItemType[] itemTypes = ordersByTypeMap.keySet().toArray(new ItemType[0]);
        allocateVATByType(summary, totalCaptured, totalVAT, total, itemTypes);

        // each order here will by of 1 VAT type
        int vatIntValue = totalVAT.intValue();
        summary.updateVatTypeTotal(orders.get(0).getTaxRate(), vatIntValue);
    }

    private static int calculateTotalsAndSummariseByType(List<Order> orders, ItemType type, OrderSummary summary) {
        int captured = 0;
        for(Order order : orders) {
            if(isOrderVoided(order)) {
                continue;
            }
            int actualPrice = getMenuItemActualPrice(order);

            actualPrice = actualPrice * order.getQuantity();
            captured += actualPrice;

            summary.updateItemTypeCount(type, order.getQuantity());
            if(order.getAdjustment() != null) {
                summary.updateAdjustment(order.getAdjustment().getAdjustmentType().getName(), actualPrice);
            }
            summary.updateItemTypeTotal(type, actualPrice);
        }

        return captured;
    }

    private static boolean isOrderVoided(Order order) {
        return order.getAdjustment() != null && order.getAdjustment().getAdjustmentType().getType() == AdjustmentTypeType.DISCOUNT;
    }

    public static int getMenuItemActualPrice(Order order) {
        int actualPrice = order.getPriceOverride();
        // zero the price if there is an adjustment
        if(isOrderVoided(order)) {
            actualPrice = 0;
        }
        return actualPrice;
    }

    public static int getActualPrice(Modifier modifier) {
        return modifier.getPriceOverride();
    }

    public static int getActualPrice(List<Modifier> modifiers) {
        int total = 0;
        for(Modifier modifier : modifiers) {
            total += getActualPrice(modifier);
        }
        return total;
    }

    public static int getOrderValue(Order order) {
        return order.getQuantity() * getUnitValue(order);
    }

    public static int getUnitValue(Order order) {
        return getMenuItemActualPrice(order) + getActualPrice(order.getModifiers());
    }

    public static int getOrderValue(List<Order> orders) {
        int total = 0;
        for(Order order : orders) {
            total += order.getQuantity() * getUnitValue(order);
        }
        return total;
    }

    public static int getOrderValueExcludingModifiers(List<Order> orders) {
        int total = 0;
        for(Order order : orders) {
            total += getOrderValueExcludingModifiers(order);
        }
        return total;
    }

    public static int getOrderValueExcludingModifiers(Order order) {
        return order.getQuantity() * getMenuItemActualPrice(order);
    }

    private static int calculateTotalsAndSummariseByType(List<ModifierTuple> modifiers, OrderSummary summary) {
        int captured = 0;
        for(ModifierTuple modifierTuple : modifiers) {
            if(isOrderVoided(modifierTuple.getOrder())) {
                continue;
            }
            Modifier modifier = modifierTuple.getModifier();

            int actualPrice = getActualPrice(modifier);
            captured += actualPrice;

            summary.updateItemTypeTotal(modifierTuple.getType(), actualPrice);
        }

        return captured;
    }

    private static int calculateVAT(List<Order> orders, boolean includeModifiers) {
        BigDecimal vatValue = BigDecimal.ZERO;
        for(Order order : orders) {
            if(isOrderVoided(order)) {
                continue;
            }
            int actualPrice = includeModifiers ? getOrderValue(order) : getMenuItemActualPrice(order) * order.getQuantity();
            BigDecimal bdActualPrice = BigDecimal.valueOf(actualPrice);
            double actualTaxRate = order.getTaxRate() == null ? 0D : order.getTaxRate().getRateAsDouble();
            vatValue = vatValue.add(bdActualPrice.subtract(bdActualPrice.divide(BigDecimal.valueOf(1 + actualTaxRate), BigDecimal.ROUND_HALF_UP)));
        }

        return MoneyService.toPenniesRoundNearest(vatValue);
    }

    private static int calculateModifierVATOnType(List<ModifierTuple> modifiers) {
        BigDecimal vatValue = BigDecimal.ZERO;
        for(ModifierTuple modifierTuple : modifiers) {
            if(isOrderVoided(modifierTuple.getOrder())) {
                continue;
            }
            Modifier modifier = modifierTuple.getModifier();
            int actualPrice = getActualPrice(modifier);
            BigDecimal bdActualPrice = BigDecimal.valueOf(actualPrice);
            double actualTaxRate = modifier.getTaxRate() == null ? 0D : modifier.getTaxRate().getRateAsDouble();
            vatValue = vatValue.add(bdActualPrice.subtract(bdActualPrice.divide(BigDecimal.valueOf(1 + actualTaxRate), BigDecimal.ROUND_HALF_UP)));
        }

        return MoneyService.toPenniesRoundNearest(vatValue);
    }

    public static int calculateNet(int actualPrice, double taxRateAsDouble) {
        BigDecimal bdActualPrice = BigDecimal.valueOf(actualPrice);
        BigDecimal netValue = bdActualPrice.divide(BigDecimal.valueOf(1 + taxRateAsDouble), BigDecimal.ROUND_HALF_UP);
        return MoneyService.toPenniesRoundNearest(netValue);
    }

    private static void calculateModifierVATOnTaxGroup(List<ModifierTuple> modifiers, OrderSummary summary) {
        Map<ItemType, List<ModifierTuple>> modifiersByTypeMap = modifiers.stream().collect(Collectors.groupingBy(ModifierTuple::getType));
        Map<ItemType, Integer> totalCaptured = new HashMap<>();

        for(Map.Entry<ItemType, List<ModifierTuple>> entry : modifiersByTypeMap.entrySet()) {
            int totalByType = calculateTotalsAndSummariseByType(entry.getValue(), summary);
            totalCaptured.put(entry.getKey(), totalByType);
        }

        BigDecimal totalVAT = BigDecimal.valueOf(calculateModifierVATOnType(modifiers));
        int total = totalCaptured.values().stream().mapToInt(Integer::intValue).sum();

        ItemType[] itemTypes = modifiersByTypeMap.keySet().toArray(new ItemType[0]);
        allocateVATByType(summary, totalCaptured, totalVAT, total, itemTypes);
    }

    private static void allocateVATByType(OrderSummary summary, Map<ItemType, Integer> totalCaptured, BigDecimal totalVAT, double total, ItemType[] itemTypes) {
        if(total == 0D) {
            return;
        }

        for (ItemType itemType : itemTypes) {
            BigDecimal fraction = BigDecimal.valueOf(totalCaptured.get(itemType) / total);
            BigDecimal vatBd = fraction.multiply(totalVAT);
            int vat = MoneyService.toPenniesRoundNearest(vatBd);
            summary.updateItemTypeVAT(itemType, vat);
        }
    }

    public static Tuple<List<Adjustment>,Map<Adjustment,Integer>> allAdjustmentBreakdown(Session session, int sessionTotal) {
        List<Adjustment> orderOfAdjustments = new ArrayList<>();
        Map<Adjustment,Integer> paymentsByAdjustment = new HashMap<>();

        for(Adjustment adjustment : session.getAdjustments()) {
            orderOfAdjustments.add(adjustment);
            if (adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
                update(paymentsByAdjustment, adjustment, adjustment.getValue());
            } else if (adjustment.getNumericalType() == NumericalAdjustmentType.PERCENTAGE) {
                int thisPayment = discountOperatingValueByPercentage(adjustment.getValue(), sessionTotal);
                update(paymentsByAdjustment, adjustment, thisPayment);
            }
        }

        return new Tuple<>(orderOfAdjustments, paymentsByAdjustment);
    }

    public static Tuple<Map<AdjustmentType,Integer>,Map<Adjustment,Integer>> paymentBreakdown(Session session, int sessionTotal) {
        Map<AdjustmentType,Integer> paymentsByType = new HashMap<>();
        Map<Adjustment,Integer> paymentsByAdjustment = new HashMap<>();

        int actualPaidTotal = 0;
        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.isVoided()) {
                continue;
            }

            if(adjustment.getAdjustmentType().getType() != AdjustmentTypeType.PAYMENT) {
                continue;
            }

            if (adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
                update(paymentsByType,adjustment.getAdjustmentType(),adjustment.getValue());
                update(paymentsByAdjustment, adjustment, adjustment.getValue());
                actualPaidTotal += adjustment.getValue();
            } else if (adjustment.getNumericalType() == NumericalAdjustmentType.PERCENTAGE) {
                int thisPayment = discountOperatingValueByPercentage(adjustment.getValue(), sessionTotal);
                update(paymentsByType,adjustment.getAdjustmentType(),thisPayment);
                update(paymentsByAdjustment, adjustment, thisPayment);
                actualPaidTotal += thisPayment;
            }
        }

        List<AdjustmentType> changeEnabled = paymentsByType.keySet().stream().filter(AdjustmentType::isSupportsChange).collect(Collectors.toList());
        if(actualPaidTotal > sessionTotal && changeEnabled.size() > 0) {
            int overpaid = actualPaidTotal - sessionTotal;
            for(AdjustmentType adjustmentType : changeEnabled) {
                int value = paymentsByType.get(adjustmentType);
                if(value >= overpaid) {
                    paymentsByType.put(adjustmentType,value - overpaid);
                    break;
                } else {
                    paymentsByType.remove(adjustmentType);
                    overpaid =- value;
                }
            }
        }

        return new Tuple<>(paymentsByType, paymentsByAdjustment);
    }

    public static Map<AdjustmentType,Integer> adjustmentBreakdown(Session session, int sessionTotal) {
        return calculateBreakdown(session, sessionTotal, AdjustmentTypeType.DISCOUNT);
    }

    private static Map<AdjustmentType,Integer> calculateBreakdown(Session session, int sessionTotal, AdjustmentTypeType filter) {
        Map<AdjustmentType,Integer> adjustments = new HashMap<>();

        for(Adjustment adjustment : session.getAdjustments()) {
            if(adjustment.isVoided()) {
                continue;
            }

            if (adjustment.getAdjustmentType().getType() != filter) {
                continue;
            }

            if (adjustment.getNumericalType() == NumericalAdjustmentType.ABSOLUTE) {
                update(adjustments,adjustment.getAdjustmentType(),adjustment.getValue());
            } else if (adjustment.getNumericalType() == NumericalAdjustmentType.PERCENTAGE) {
                int thisPayment = discountOperatingValueByPercentage(adjustment.getValue(), sessionTotal);
                update(adjustments,adjustment.getAdjustmentType(),thisPayment);
            }
        }

        return adjustments;
    }

    private static int discountOperatingValueByPercentage(int adjustmentValue, int operatingValue) {
        BigDecimal payment = BigDecimal.valueOf(adjustmentValue)
                .scaleByPowerOfTen(-3)
                .multiply(BigDecimal.valueOf(operatingValue));
        return MoneyService.toPenniesRoundNearest(payment);
    }

    private static class ModifierTuple {
        private final Modifier modifier;
        private final Order order;

        public ModifierTuple(Modifier modifier, Order order) {

            this.modifier = modifier;
            this.order = order;
        }

        public Modifier getModifier() {
            return modifier;
        }

        public Order getOrder() {
            return order;
        }

        public ItemType getType() {
            return order.getMenuItem().getType();
        }
    }
}
