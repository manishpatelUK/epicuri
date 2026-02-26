package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.OrderUtil;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Restaurant;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.CalculationKey;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.pojo.model.session.Session;
import uk.co.epicuri.serverapi.common.pojo.model.session.SessionType;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerSessionView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("SelfServiceMenuId")
    private String selfServiceMenuId;

    @JsonProperty("Restaurant")
    private CustomerRestaurantView restaurant;

    @JsonProperty("Time")
    private long time;

    @JsonProperty("MenuId")
    private String menuId;

    @JsonProperty("ClosedTime")
    private long closedTime;

    @JsonProperty("RequestedBill")
    private boolean requestedBill;

    @JsonProperty("totalTip")
    private double totalTip;

    @JsonProperty("totalDiscount")
    private double totalDiscount;

    @JsonProperty("total")
    private double total;

    @JsonProperty("subTotal")
    private double subTotal;

    @JsonProperty("remainingTotal")
    private double remainingTotal;

    @JsonProperty("Tables")
    private List<String> tables = new ArrayList<>();

    @JsonProperty("ClosedMessage")
    private String closedMessage;

    @JsonProperty("SocialMessage")
    private String socialMessage;

    @JsonProperty("Orders")
    private List<CustomerOrderView> orders;

    @JsonProperty("orderAggregates")
    private List<OrderAggregation> orderAggregates;

    @JsonProperty("Diners")
    private List<CustomerDinerView> diners;

    @JsonProperty("tipPercentage")
    private Double tipPercentage;

    @JsonProperty("sessionType")
    private SessionType sessionType;

    public CustomerSessionView(){}
    public CustomerSessionView(Session session,
                               Service service,
                               Restaurant restaurant,
                               double tipTotal,
                               Map<CalculationKey,Number> calculations,
                               String closedMessage,
                               String socialMessage,
                               List<Order> orders,
                               List<Course> courses,
                               Map<String,Customer> idToCustomer,
                               BillSplit billSplit) {
        this.id = session.getId();
        this.selfServiceMenuId = service.getSelfServiceMenuId();
        this.restaurant = new CustomerRestaurantView(restaurant);
        this.time = session.getStartTime() / 1000;
        this.menuId = service.getDefaultMenuId();
        this.closedTime = session.getClosedTime() == null ? 0 : session.getClosedTime() / 1000;
        this.requestedBill = session.isBillRequested();
        this.tables = session.getTables();
        this.closedMessage = closedMessage;
        this.socialMessage = socialMessage;
        this.orders = orders.stream()
                .filter(o -> !o.isRemoveFromReports() && o.getAdjustment() == null)
                .map(o -> new CustomerOrderView(o,
                courses.stream().filter(c -> c.getId().equals(o.getCourseId())).findFirst().orElse(null), service))
                .collect(Collectors.toList());

        orders.sort(Comparator.comparingLong(Order::getTime));

        //identify identical orders for convenience
        this.orderAggregates = OrderUtil.aggregateIdenticalOrders(orders.stream().filter(o -> !o.isRemoveFromReports() && o.getAdjustment() == null).collect(Collectors.toList()));

        Map<String,List<CustomerOrderView>> ordersByDiner = this.orders.stream().collect(Collectors.groupingBy(CustomerOrderView::getDinerId));

        // one of the diners will be the table itself
        this.diners = session.getDiners().stream().map(d ->
                new CustomerDinerView(
                        d,
                        d.getCustomerId() == null ? null : idToCustomer.get(d.getCustomerId()),
                        ordersByDiner.getOrDefault(d.getId(), new ArrayList<>()),
                        billSplit))
                .collect(Collectors.toList());

        this.tipPercentage = session.getTipPercentage() == null ? 0 : session.getTipPercentage();
        this.totalTip = MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.TIP_TOTAL).intValue());
        this.total = MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.TOTAL).intValue());
        this.totalDiscount = MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.DISCOUNT_TOTAL).intValue());
        this.subTotal = MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.SUB_TOTAL).intValue());
        this.remainingTotal = MoneyService.toMoneyRoundNearest(calculations.get(CalculationKey.REMAINING_TOTAL).intValue());
        this.remainingTotal = Math.max(0D, remainingTotal);

        this.sessionType = session.getSessionType();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSelfServiceMenuId() {
        return selfServiceMenuId;
    }

    public void setSelfServiceMenuId(String selfServiceMenuId) {
        this.selfServiceMenuId = selfServiceMenuId;
    }

    public CustomerRestaurantView getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(CustomerRestaurantView restaurant) {
        this.restaurant = restaurant;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public long getClosedTime() {
        return closedTime;
    }

    public void setClosedTime(long closedTime) {
        this.closedTime = closedTime;
    }

    public boolean isRequestedBill() {
        return requestedBill;
    }

    public void setRequestedBill(boolean requestedBill) {
        this.requestedBill = requestedBill;
    }

    public double getTotalTip() {
        return totalTip;
    }

    public void setTotalTip(double totalTip) {
        this.totalTip = totalTip;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public String getClosedMessage() {
        return closedMessage;
    }

    public void setClosedMessage(String closedMessage) {
        this.closedMessage = closedMessage;
    }

    public String getSocialMessage() {
        return socialMessage;
    }

    public void setSocialMessage(String socialMessage) {
        this.socialMessage = socialMessage;
    }

    public List<CustomerOrderView> getOrders() {
        return orders;
    }

    public void setOrders(List<CustomerOrderView> orders) {
        this.orders = orders;
    }

    public List<CustomerDinerView> getDiners() {
        return diners;
    }

    public void setDiners(List<CustomerDinerView> diners) {
        this.diners = diners;
    }

    public List<OrderAggregation> getOrderAggregates() {
        return orderAggregates;
    }

    public void setOrderAggregates(List<OrderAggregation> orderAggregates) {
        this.orderAggregates = orderAggregates;
    }

    public Double getTipPercentage() {
        return tipPercentage;
    }

    public void setTipPercentage(Double tipPercentage) {
        this.tipPercentage = tipPercentage;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getRemainingTotal() {
        return remainingTotal;
    }

    public void setRemainingTotal(double remainingTotal) {
        this.remainingTotal = remainingTotal;
    }
}
