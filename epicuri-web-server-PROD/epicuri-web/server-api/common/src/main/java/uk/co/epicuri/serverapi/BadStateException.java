package uk.co.epicuri.serverapi;

/**
 * Created by manish
 */
public class BadStateException extends RuntimeException {
    public BadStateException() {
        super();
    }

    public BadStateException(String m) {
        super(m);
    }

    public BadStateException(Throwable t) {
        super(t);
    }

    public BadStateException(String m, Throwable t) {
        super(m, t);
    }

    public BadStateException(String message, Throwable cause,
                             boolean enableSuppression,
                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
