package uk.co.epicuri.api.core.pojo;

/**
 * 28/08/2014
 */
public class TaxRate {
    private int Id;
    private double Rate;
    private String Name;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public double getRate() {
        return Rate;
    }

    public void setRate(double rate) {
        this.Rate = rate;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
