package uk.co.epicuri.serverapi.common.pojo.external.textlocal;

public class MessageConfirmation {
    private String id;
    private String recipient;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
