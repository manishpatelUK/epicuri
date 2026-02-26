package uk.co.epicuri.waiter.service.interfaces;

import java.util.List;

public interface ILoggingListener {
    void onLoggingStart();
    void onLoggingComplete(List<String> log);
}
