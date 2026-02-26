package uk.co.epicuri.serverapi.management.event;

/**
 * Created by manish
 */
public interface ParameterisedSimpleAction<T> {
    void onAction(T t);
}
