package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

import org.apache.commons.lang3.builder.EqualsBuilder;
import uk.co.epicuri.serverapi.common.pojo.host.HostTablePositionView;

/**
 * Created by Manish on 17/07/2015.
 */
public class Position {
    private double x;
    private double y;
    private double rotation;
    private double scaleX;
    private double scaleY;

    public Position(){}
    public Position(HostTablePositionView positionView) {
        this.x = positionView.getX();
        this.y = positionView.getY();
        this.rotation = positionView.getRotation();
        this.scaleX = positionView.getScaleX();
        this.scaleY = positionView.getScaleY();
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
    public boolean equals(Object object) {
        return EqualsBuilder.reflectionEquals(this, object);
    }
}
