package uk.co.epicuri.serverapi.common.pojo.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.co.epicuri.serverapi.common.pojo.OrderUtil;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.session.Diner;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDinerView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("IsTable")
    private boolean isTable;

    @JsonProperty("EpicuriUser")
    private EpicuriUserView epicuriUser;

    @JsonProperty("Orders")
    private List<CustomerOrderView> orders = new ArrayList<>();

    @JsonProperty("orderAggregates")
    private List<OrderAggregation> orderAggregates;

    @JsonProperty("SubTotal")
    private double subTotal;

    @JsonProperty("SharedTotal")
    private double sharedTotal;

    @JsonProperty("tip")
    private double tip;

    @JsonProperty("discounts")
    private double discounts;

    @JsonProperty("total")
    private double total;

    @JsonProperty("equalSplits")
    private double equalSplits;

    @JsonProperty("name")
    private String name;

    public CustomerDinerView(){}

    public CustomerDinerView(Diner diner, Customer customer, List<CustomerOrderView> orders, BillSplit billSplit) {
        id = diner.getId();
        isTable = diner.isDefaultDiner();
        if(orders != null) {
            this.orders = orders;
            this.orderAggregates = OrderUtil.aggregateIdenticalOrderViews(orders);
        }

        if(customer != null) {
            epicuriUser = new EpicuriUserView(customer);
        }

        int subTotalInt = billSplit.getItemSplits().getOrDefault(id, 0);
        this.subTotal = MoneyService.toMoneyRoundNearest(subTotalInt);
        int sharedTotalInt = billSplit.getTableItemSplits().getOrDefault(id, 0);
        this.sharedTotal = MoneyService.toMoneyRoundNearest(sharedTotalInt);
        int tipTotalInt = billSplit.getTipSplits().getOrDefault(id, 0);
        this.tip = MoneyService.toMoneyRoundNearest(tipTotalInt);
        int discountsTotalInt = billSplit.getDiscountSplits().getOrDefault(id, 0);
        this.discounts = MoneyService.toMoneyRoundNearest(discountsTotalInt);
        this.equalSplits = MoneyService.toMoneyRoundNearest(billSplit.getEqualSplits().getOrDefault(id, 0));
        this.total = MoneyService.toMoneyRoundNearest((sharedTotalInt + subTotalInt + tipTotalInt)-discountsTotalInt);
        this.name = customer == null ? diner.getName() : (epicuriUser.getName().getFirstName() + " " + epicuriUser.getName().getLastName()).trim();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isTable() {
        return isTable;
    }

    public void setIsTable(boolean isTable) {
        this.isTable = isTable;
    }

    public EpicuriUserView getEpicuriUser() {
        return epicuriUser;
    }

    public void setEpicuriUser(EpicuriUserView epicuriUser) {
        this.epicuriUser = epicuriUser;
    }

    public List<CustomerOrderView> getOrders() {
        return orders;
    }

    public void setOrders(List<CustomerOrderView> orders) {
        this.orders = orders;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getSharedTotal() {
        return sharedTotal;
    }

    public void setSharedTotal(double sharedTotal) {
        this.sharedTotal = sharedTotal;
    }

    public double getTip() {
        return tip;
    }

    public void setTip(double tip) {
        this.tip = tip;
    }

    public double getDiscounts() {
        return discounts;
    }

    public void setDiscounts(double discounts) {
        this.discounts = discounts;
    }

    public double getEqualSplits() {
        return equalSplits;
    }

    public void setEqualSplits(double equalSplits) {
        this.equalSplits = equalSplits;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<OrderAggregation> getOrderAggregates() {
        return orderAggregates;
    }

    public void setOrderAggregates(List<OrderAggregation> orderAggregates) {
        this.orderAggregates = orderAggregates;
    }
}
