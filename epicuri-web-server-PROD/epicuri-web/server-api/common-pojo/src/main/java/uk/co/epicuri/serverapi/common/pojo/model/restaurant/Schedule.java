package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@MgmtPojoModel
public class Schedule extends IDAble{
    @MgmtDisplayField
    private String name;
    private List<ScheduledItem> scheduledItems = new ArrayList<>();
    private List<ScheduledItem> recurringItems = new ArrayList<>();

    public Schedule(){}

    public Schedule(Service service) {
        this(service,new ArrayList<>());
    }

    public Schedule(Service service, List<Schedule> current) {
        setId(generateId(service, current));
    }

    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public void setScheduledItems(List<ScheduledItem> scheduledItems) {
        this.scheduledItems = scheduledItems;
    }

    public List<ScheduledItem> getRecurringItems() {
        return recurringItems;
    }

    public void setRecurringItems(List<ScheduledItem> recurringItems) {
        this.recurringItems = recurringItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
