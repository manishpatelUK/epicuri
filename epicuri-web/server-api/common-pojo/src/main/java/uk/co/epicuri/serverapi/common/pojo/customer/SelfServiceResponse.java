package uk.co.epicuri.serverapi.common.pojo.customer;

import uk.co.epicuri.serverapi.common.pojo.model.Course;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Service;
import uk.co.epicuri.serverapi.common.pojo.model.session.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SelfServiceResponse {
    private String publicOrderId;
    private List<String> orderIds = new ArrayList<>();
    private List<CustomerOrderView> orders = new ArrayList<>();
    private int amountPaid;

    public SelfServiceResponse(){}
    public SelfServiceResponse(List<Order> actualOrders, Map<String,Course> courses, Service service, int amountPaid, String publicOrderId){
        this.orderIds = actualOrders.stream().map(Order::getId).collect(Collectors.toList());
        this.amountPaid = amountPaid;
        this.publicOrderId = publicOrderId;
        actualOrders.forEach(o -> orders.add(new CustomerOrderView(o, courses.get(o.getCourseId()), service)));
    }

    public String getPublicOrderId() {
        return publicOrderId;
    }

    public void setPublicOrderId(String publicOrderId) {
        this.publicOrderId = publicOrderId;
    }

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public int getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(int amountPaid) {
        this.amountPaid = amountPaid;
    }

    public List<CustomerOrderView> getOrders() {
        return orders;
    }

    public void setOrders(List<CustomerOrderView> orders) {
        this.orders = orders;
    }
}
