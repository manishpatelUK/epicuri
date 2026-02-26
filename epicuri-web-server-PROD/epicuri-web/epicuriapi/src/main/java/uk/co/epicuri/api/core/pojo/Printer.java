package uk.co.epicuri.api.core.pojo;

/**
 * 28/08/2014
 */
public class Printer {
    private int Id;
    private String Name, IP;

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

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }
}
