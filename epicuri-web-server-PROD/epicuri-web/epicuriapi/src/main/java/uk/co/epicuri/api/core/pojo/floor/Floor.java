package uk.co.epicuri.api.core.pojo.floor;

/**
 * Created by Manish on 23/06/2015.
 */
public class Floor {
    private int Id;
    private String Name;
    private int Capacity;
    private String ImageURL;
    private int Layout;
    private int Scale;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getCapacity() {
        return Capacity;
    }

    public void setCapacity(int capacity) {
        Capacity = capacity;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
    }

    public int getLayout() {
        return Layout;
    }

    public void setLayout(int layout) {
        Layout = layout;
    }

    public int getScale() {
        return Scale;
    }

    public void setScale(int scale) {
        Scale = scale;
    }
}
