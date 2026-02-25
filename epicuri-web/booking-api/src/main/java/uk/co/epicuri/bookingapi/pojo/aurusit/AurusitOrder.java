package uk.co.epicuri.bookingapi.pojo.aurusit;

/**
 * Created by Manish on 22/06/2015.
 */
public class AurusitOrder {
    private int itemId;
    private int quantity;
    private int price;
    private String note;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "AurusitOrder{" +
                "itemId=" + itemId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", note='" + note + '\'' +
                '}';
    }
}
