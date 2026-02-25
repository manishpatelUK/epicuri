package uk.co.epicuri.serverapi.common.pojo.external.getaddress;

import java.util.ArrayList;
import java.util.List;

public class GetAddressResponse {
    /*
     "latitude": 52.24593734741211,
    "longitude": -0.891636312007904,
    "addresses":
    ["10 Watkin Terrace, , , , , Northampton, Northamptonshire",
    "12 Watkin Terrace, , , , , Northampton, Northamptonshire",
    */

    private long latitude;
    private long longitude;
    private List<String> addresses = new ArrayList<>();

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }
}
