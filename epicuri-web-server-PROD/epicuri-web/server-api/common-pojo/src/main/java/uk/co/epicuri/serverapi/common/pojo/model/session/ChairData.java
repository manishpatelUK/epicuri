package uk.co.epicuri.serverapi.common.pojo.model.session;

/**
 * Created by manish
 */
public class ChairData {
    private String type = "table";
    private double x;
    private double y;
    private String dinerId;
    private double width;
    private double breadth;
    private double rotation;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDinerId() {
        return dinerId;
    }

    public void setDinerId(String dinerId) {
        this.dinerId = dinerId;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getBreadth() {
        return breadth;
    }

    public void setBreadth(double breadth) {
        this.breadth = breadth;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChairData chairData = (ChairData) o;

        if (Double.compare(chairData.x, x) != 0) return false;
        if (Double.compare(chairData.y, y) != 0) return false;
        if (Double.compare(chairData.width, width) != 0) return false;
        if (Double.compare(chairData.breadth, breadth) != 0) return false;
        if (Double.compare(chairData.rotation, rotation) != 0) return false;
        if (!type.equals(chairData.type)) return false;
        return dinerId.equals(chairData.dinerId);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = type.hashCode();
        temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + dinerId.hashCode();
        temp = Double.doubleToLongBits(width);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(breadth);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(rotation);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
