package uk.co.epicuri.serverapi.common.pojo.model.restaurant;

public class HourSpan implements Comparable<HourSpan>{
    private int hourOpen = 0;
    private int minuteOpen = 0;

    private int hourClose = 0;
    private int minuteClose = 0;

    public HourSpan(){}
    public HourSpan(int hourOpen, int minuteOpen, int hourClose, int minuteClose) {
        this.hourOpen = hourOpen;
        this.minuteOpen = minuteOpen;
        this.hourClose = hourClose;
        this.minuteClose = minuteClose;
    }

    public int getHourOpen() {
        return hourOpen;
    }

    public void setHourOpen(int hourOpen) {
        this.hourOpen = hourOpen;
    }

    public int getMinuteOpen() {
        return minuteOpen;
    }

    public void setMinuteOpen(int minuteOpen) {
        this.minuteOpen = minuteOpen;
    }

    public int getHourClose() {
        return hourClose;
    }

    public void setHourClose(int hourClose) {
        this.hourClose = hourClose;
    }

    public int getMinuteClose() {
        return minuteClose;
    }

    public void setMinuteClose(int minuteClose) {
        this.minuteClose = minuteClose;
    }

    @Override
    public int compareTo(HourSpan o) {
        int thisMinutesOpen = (hourOpen * 60) + minuteOpen;
        int thatMinutesOpen = (o.hourOpen * 60) + o.minuteOpen;

        return Integer.compare(thatMinutesOpen, thatMinutesOpen);
    }
}
