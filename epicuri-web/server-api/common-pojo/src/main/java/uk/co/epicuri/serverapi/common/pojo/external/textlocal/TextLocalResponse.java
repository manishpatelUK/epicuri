package uk.co.epicuri.serverapi.common.pojo.external.textlocal;

import java.util.List;

public class TextLocalResponse {
    private long balance;
    private long batch_id;
    private int cost;
    private InnerMessage message;
    private List<MessageConfirmation> messages;
    private String status;

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getBatch_id() {
        return batch_id;
    }

    public void setBatch_id(long batch_id) {
        this.batch_id = batch_id;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public InnerMessage getMessage() {
        return message;
    }

    public void setMessage(InnerMessage message) {
        this.message = message;
    }

    public List<MessageConfirmation> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageConfirmation> messages) {
        this.messages = messages;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TextLocalResponse{" +
                "balance=" + balance +
                ", batch_id=" + batch_id +
                ", cost=" + cost +
                ", message=" + message +
                ", messages=" + messages +
                ", status='" + status + '\'' +
                '}';
    }
}
