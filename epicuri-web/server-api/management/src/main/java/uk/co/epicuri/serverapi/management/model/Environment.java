package uk.co.epicuri.serverapi.management.model;

/**
 * Created by manish
 */
public enum Environment {
    NONE(null),
    PROD(BaseURL.PROD_BASE_URL),
    DEV(BaseURL.DEV_BASE_URL);



    private final String url;
    Environment(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

}
