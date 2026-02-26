package uk.co.epicuri.waiter.interfaces;

public interface LoginEditListener {
    void createLogin(CharSequence name, CharSequence username, CharSequence password, CharSequence pin, CharSequence role);
    void editLogin(String id, CharSequence name, CharSequence username, CharSequence password, CharSequence pin, String permission);
    void deleteLogin(String id);
}
