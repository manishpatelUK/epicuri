package uk.co.epicuri.serverapi.common.pojo.external.mews;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MewsProductOrders {
    @JsonProperty("Count")
    private int count = 1;

    @JsonProperty("ProductId")
    private String productId;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
