package uk.co.epicuri.serverapi.engines.reporting;

public class CustomerStat {
    private int reservations = 0;
    private int takeaways = 0;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = Long.MIN_VALUE;

    public void incrementReservations() {
        reservations++;
    }

    public void incrementTakeaways() {
        takeaways++;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public int getReservations() {
        return reservations;
    }

    public int getTakeaways() {
        return takeaways;
    }

    public long getMinTime() {
        return minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }
}
