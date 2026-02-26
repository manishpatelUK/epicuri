package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.TimeUtil;
import uk.co.epicuri.serverapi.common.pojo.common.BillSplit;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;
import uk.co.epicuri.serverapi.common.pojo.model.Preference;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.RestaurantDefault;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.Diner;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;
import uk.co.epicuri.serverapi.common.service.money.MoneyService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostDinerView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("IsTable")
    private boolean table;

    @JsonProperty("EpicuriUser")
    private HostCustomerView customer;

    @JsonProperty("Orders")
    private List<String> orderIds = new ArrayList<>();

    @JsonProperty("IsBirthday")
    private boolean birthday = false;

    @JsonProperty("subTotal")
    private double subTotal;

    @JsonProperty("total")
    private double total;

    @JsonProperty("tip")
    private double tip;

    @JsonProperty("discounts")
    private double discounts;

    @JsonProperty("vat")
    private double vat;

    @JsonProperty("orders")
    private List<HostOrderView> orders = new ArrayList<>();

    @JsonProperty("name")
    private String name;

    public HostDinerView(){}
    public HostDinerView(Diner diner,
                         Customer customer,
                         Map<String,Preference> allPreferences,
                         Collection<Order> allOrders,
                         RestaurantDefault birthdaySpan,
                         Service service,
                         BillSplit billSplit) {
        this.id = diner.getId();
        this.table = diner.isDefaultDiner();
        if(customer != null && customer.isRegisteredViaApp()) {
            this.customer = new HostCustomerView(customer, allPreferences);
            this.birthday = TimeUtil.isBirthday(customer.getBirthday(),((Number) birthdaySpan.getValue()).intValue());
            this.name = (this.customer.getName().getFirstName() + " " + this.customer.getName().getLastName()).trim();
        } else {
            this.name = diner.getName();
        }
        this.orderIds = allOrders.stream()
                .filter(o -> StringUtils.isNotBlank(o.getDinerId()) && o.getDinerId().equals(diner.getId()))
                .map(Order::getId)
                .collect(Collectors.toList());
        this.orders = allOrders.stream()
                .filter(o -> StringUtils.isNotBlank(o.getDinerId()) && o.getDinerId().equals(diner.getId()))
                .map(o -> new HostOrderView(o, service)).collect(Collectors.toList());

        this.subTotal = MoneyService.toMoneyRoundNearest(billSplit.getItemSplits().getOrDefault(id, 0));
        this.tip = MoneyService.toMoneyRoundNearest(billSplit.getTipSplits().getOrDefault(id, 0));
        this.discounts = MoneyService.toMoneyRoundNearest(billSplit.getDiscountSplits().getOrDefault(id, 0));
        this.total = (subTotal + tip) - discounts;
        this.vat = MoneyService.toMoneyRoundNearest(billSplit.getVatSplits().getOrDefault(id, 0));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isTable() {
        return table;
    }

    public void setTable(boolean table) {
        this.table = table;
    }

    public HostCustomerView getCustomer() {
        return customer;
    }

    public void setCustomer(HostCustomerView customer) {
        this.customer = customer;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public boolean isBirthday() {
        return birthday;
    }

    public void setBirthday(boolean birthday) {
        this.birthday = birthday;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
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

    public List<HostOrderView> getOrders() {
        return orders;
    }

    public void setOrders(List<HostOrderView> orders) {
        this.orders = orders;
    }

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
