package uk.co.epicuri.serverapi.common.pojo.host;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.co.epicuri.serverapi.common.pojo.model.restaurant.Position;

/**
 * Created by manish
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostTablePositionView {
    @JsonProperty("X")
    private double x;

    @JsonProperty("Y")
    private double y;

    @JsonProperty("Rotation")
    private double rotation;

    @JsonProperty("ScaleX")
    private double scaleX;

    @JsonProperty("ScaleY")
    private double scaleY;

    public HostTablePositionView(){}
    public HostTablePositionView(Position position){
        this.x = position.getX();
        this.y = position.getY();
        this.rotation = position.getRotation();
        this.scaleX = position.getScaleX();
        this.scaleY = position.getScaleY();
    }

    public HostTablePositionView(double x, double y, double rotation, double scaleX, double scaleY) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
