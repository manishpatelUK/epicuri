package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import uk.co.epicuri.serverapi.common.pojo.management.MgmtDisplayField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;
import uk.co.epicuri.serverapi.common.pojo.model.IDAble;
import uk.co.epicuri.serverapi.common.pojo.model.session.NotificationConstant;
import uk.co.epicuri.serverapi.common.pojo.model.session.NotificationType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by manish
 */
@MgmtPojoModel
public class ScheduledItem extends IDAble{
    private long timeAfterStart;

    @MgmtDisplayField
    private String text;

    @MgmtIgnoreField
    private String target = NotificationConstant.TARGET_WAITER_ACTION.getConstant();
    private NotificationType notificationType;

    //recurring only
    private long recurring; //either this is set or timeAfterStart is set, never both
    private long initialDelay;

    public ScheduledItem(){}

    public ScheduledItem(Schedule parent) {
        this(parent,new ArrayList<>());
    }

    public ScheduledItem(Schedule parent, List<ScheduledItem> items) {
        setId(generateId(parent, items));
    }

    public long getTimeAfterStart() {
        return timeAfterStart;
    }

    public void setTimeAfterStart(long timeAfterStart) {
        this.timeAfterStart = timeAfterStart;
    }

    public long getRecurring() {
        return recurring;
    }

    public void setRecurring(long recurring) {
        this.recurring = recurring;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
}
