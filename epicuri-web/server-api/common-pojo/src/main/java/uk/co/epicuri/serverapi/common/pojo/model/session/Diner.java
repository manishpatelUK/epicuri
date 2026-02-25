package uk.co.epicuri.serverapi.common.pojo.model.session;


import org.springframework.data.annotation.Transient;
import uk.co.epicuri.serverapi.common.pojo.model.Deletable;
import uk.co.epicuri.serverapi.common.pojo.model.Customer;

public class Diner extends Deletable {
    private String customerId;

    // for convenience
    @Transient
    private transient Customer customer;

    private boolean defaultDiner = false;
    private String name;

    public Diner(){}
    public Diner(Session parent){
        setId(generateId(parent, parent.getDiners()));
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public boolean isDefaultDiner() {
        return defaultDiner;
    }

    public void setDefaultDiner(boolean defaultDiner) {
        this.defaultDiner = defaultDiner;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
