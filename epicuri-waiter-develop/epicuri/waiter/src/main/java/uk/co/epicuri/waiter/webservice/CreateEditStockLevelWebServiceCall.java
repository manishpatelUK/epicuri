package uk.co.epicuri.waiter.webservice;

import android.net.Uri;

import uk.co.epicuri.waiter.model.StockLevel;

public class CreateEditStockLevelWebServiceCall implements WebServiceCall {
    private final StockLevel stockLevel;

    public CreateEditStockLevelWebServiceCall(StockLevel stockLevel) {
        this.stockLevel = stockLevel;
    }

    @Override
    public String getMethod() {
        return this.stockLevel.getId() == null ? "POST" : "PUT";
    }

    @Override
    public String getPath() {
        return "/StockControl" + (this.stockLevel.getId() == null ? "" : "/"+this.stockLevel.getId());
    }

    @Override
    public String getBody() {
        return this.stockLevel.toJson();
    }

    @Override
    public Uri[] getUrisToRefresh() {
        return new Uri[0];
    }

    @Override
    public boolean requiresToken() {
        return true;
    }
}
