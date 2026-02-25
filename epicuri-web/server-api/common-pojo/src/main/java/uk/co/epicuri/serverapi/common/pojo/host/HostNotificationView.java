package uk.co.epicuri.serverapi.common.pojo.host;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.ScheduledItem;
import uk.co.epicuri.serverapi.common.pojo.model.session.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HostNotificationView {
    @JsonProperty("Id")
    private String id;

    @JsonProperty("Text")
    private String text;

    @JsonProperty("Target")
    private String target;

    @JsonProperty("Acknowledgements")
    private List<HostAcknowledgementView> acknowledgements = new ArrayList<>();

    @JsonProperty("Created")
    private Long created;

    public HostNotificationView(){}

    public HostNotificationView(Notification notification) {
        this.id = notification.getId();
        this.text = notification.getText();
        this.target = notification.getTarget();

        if(notification.getAcknowledged() != null) {
            acknowledgements.add(new HostAcknowledgementView(notification));
        }

        this.created = notification.getTime() / 1000;
    }

    public HostNotificationView(ScheduledItem item, List<Notification> notifications) {
        this.id = item.getId();
        this.text = item.getText();
        this.target = item.getTarget();

        acknowledgements = notifications.stream()
                .filter(n -> n.getScheduledItemId() != null && n.getScheduledItemId().equals(item.getId()) && n.getAcknowledged() != null )
                .map(HostAcknowledgementView::new).collect(Collectors.toList());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<HostAcknowledgementView> getAcknowledgements() {
        return acknowledgements;
    }

    public void setAcknowledgements(List<HostAcknowledgementView> acknowledgements) {
        this.acknowledgements = acknowledgements;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
