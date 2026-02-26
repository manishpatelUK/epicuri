package uk.co.epicuri.waiter.webservice;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.epicuri.waiter.contentprovider.EpicuriContent;
import uk.co.epicuri.waiter.interfaces.ValidWebServiceCall;
import uk.co.epicuri.waiter.model.EpicuriCustomer;
import uk.co.epicuri.waiter.model.EpicuriMenu;
import uk.co.epicuri.waiter.model.EpicuriOrderItem;

public class QuickOrderWebServiceCall implements WebServiceCall, ValidWebServiceCall {
    String body;
    boolean orderPrint;
    long orderTimeStamp;
    boolean isValid = true;

    public QuickOrderWebServiceCall(String name, int number, EpicuriCustomer customer, boolean adHoc, String[] tables, ArrayList<EpicuriOrderItem> orders, boolean orderPrint, String deliveryLocation, boolean isRefund) {
        this(name, number, customer, adHoc, tables, "-1", orders, orderPrint, deliveryLocation, isRefund);
    }
    public QuickOrderWebServiceCall(String name,
                                    int number,
                                    EpicuriCustomer customer,
                                    boolean adHoc,
                                    String[] tables,
                                    String serviceId,
                                    ArrayList<EpicuriOrderItem> orders,
                                    boolean orderPrint,
                                    String deliveryLocation,
                                    boolean isRefund) {
        this.orderPrint = orderPrint;
        this.orderTimeStamp = System.currentTimeMillis();

        JSONObject o = new JSONObject();
        try {
            JSONObject party = new JSONObject();
            party.put("Name", name);
            party.put("NumberOfPeople", number);
            party.put("CreateSession", true);
            party.put("IsAdHoc", adHoc);
            if(!adHoc){
                party.put("ServiceId", serviceId);
            }
            if(isRefund) {
                party.put("refund", true);
            }
            JSONArray tableArray = new JSONArray();
            if(null != tables) {
                for (String table : tables) {
                    tableArray.put(table);
                }
            }
            party.put("Tables", tableArray);
            if(null != customer){
                JSONObject leadCustomer = new JSONObject();
                leadCustomer.put("Id", customer.getId());
                party.put("LeadCustomer", leadCustomer);
            }
            o.put("Party", party);

            JSONArray orderArray = new JSONArray();
            for(EpicuriOrderItem order: orders){
                JSONObject orderJSON = new JSONObject();
                orderJSON.put("Quantity", order.getQuantity());
                orderJSON.put("MenuItemId", order.getItem().getId());
                orderJSON.put("InstantiatedFromId", 0); // hardcoded value for Waiter App

                JSONArray modifiers = new JSONArray();
                for(EpicuriMenu.ModifierValue value: order.getChosenModifiers()){
                    modifiers.put(value.getId());
                }
                orderJSON.put("Modifiers", modifiers);
                if(order.isPriceOverridden()){
                    orderJSON.put("Price", order.getPriceOverride().getAmount().toPlainString());
                }
                if(order.getNote() != null){
                    orderJSON.put("Note", order.getNote());
                }

                // these are both unknown
                orderJSON.put("DinerId", "-1");
                orderJSON.put("CourseId", "-1");
                orderArray.put(orderJSON);
            }
            o.put("Order", orderArray);
            if(deliveryLocation != null && deliveryLocation.length() > 0) {
                o.put("OrderLocation", deliveryLocation);
            }

        } catch (JSONException e){
            e.printStackTrace();
            throw new RuntimeException("cannot continue");
        }
        body = o.toString();

        if(WebServiceTask.getSet().contains(this)){
            this.isValid = false;
        } else {
            WebServiceTask.getSet().add(this);
        }

        removeExpired();
    }

    private void removeExpired() {
        Iterator<ValidWebServiceCall> i = WebServiceTask.getSet().iterator();
        while (i.hasNext()){
            if(i.next().expired()){
                i.remove();
            }
        }
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public boolean requiresToken() {
        return true;
    }

    @Override
    public String getPath() {
        return "/Waiting/PostWaitingWithOrder?willAttemptImmediatePrint=true&orderPrintsRequired=" + String.valueOf(orderPrint);
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[]{EpicuriContent.SESSION_URI};
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QuickOrderWebServiceCall that = (QuickOrderWebServiceCall) o;

        if (orderPrint != that.orderPrint) return false;
        if (Math.abs(orderTimeStamp - that.orderTimeStamp) > 2500) return false;
        return body != null ? body.equals(that.body) : that.body == null;
    }

    @Override public int hashCode() {
        int result = body != null ? body.hashCode() : 0;
        result = 31 * result + (orderPrint ? 1 : 0);
        return result;
    }

    public boolean expired() {
        long timePassed = System.currentTimeMillis() - orderTimeStamp;
        return timePassed > 2500;
    }

    public boolean isValid() {
        return isValid;
    }
}