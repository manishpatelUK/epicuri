package uk.co.epicuri.serverapi.common.pojo.model;


import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtIgnoreField;
import uk.co.epicuri.serverapi.common.pojo.management.MgmtPojoModel;

@MgmtPojoModel(useAccessMethods = true)
public class LatLongPair {
    private double longitude;
    private double latitude;

    @MgmtIgnoreField
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2D)
    private double[] position = new double[2];

    public LatLongPair() {
    }

    public LatLongPair(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        position[1] = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        position[0] = longitude;
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
        longitude = position[0];
        latitude = position[1];
    }

    @Override
    public String toString() {
        return "{longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
