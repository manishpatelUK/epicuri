package uk.co.epicuri.serverapi.common.pojo.host;

public class HostPartyChangeRequest {
    private int numberOfDiners;
    private String name;

    public int getNumberOfDiners() {
        return numberOfDiners;
    }

    public void setNumberOfDiners(int numberOfDiners) {
        this.numberOfDiners = numberOfDiners;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
