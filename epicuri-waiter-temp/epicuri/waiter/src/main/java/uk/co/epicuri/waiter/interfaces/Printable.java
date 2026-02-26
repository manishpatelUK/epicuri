package uk.co.epicuri.waiter.interfaces;

/**
 * Created by manish on 05/03/2018.
 */

public interface Printable {
    byte[] getPrintOutput();
    byte[] getPrintOutput(boolean doubleHeight, boolean doubleWidth);
}
