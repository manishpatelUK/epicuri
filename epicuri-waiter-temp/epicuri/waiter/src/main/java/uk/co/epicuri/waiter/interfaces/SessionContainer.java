package uk.co.epicuri.waiter.interfaces;

public interface SessionContainer {
    void registerSessionListener(OnSessionChangeListener listener);
    void deRegisterSessionListener(OnSessionChangeListener listener);
}
