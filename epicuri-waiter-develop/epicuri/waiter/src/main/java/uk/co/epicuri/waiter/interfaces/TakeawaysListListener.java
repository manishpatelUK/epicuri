package uk.co.epicuri.waiter.interfaces;

import java.util.Calendar;

public interface TakeawaysListListener {
    void addTakeaway(Calendar date);
    void showTakeaway(String sessionId, boolean autoShowItems);
}