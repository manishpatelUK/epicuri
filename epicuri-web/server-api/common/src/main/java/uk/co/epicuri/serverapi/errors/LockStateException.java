package uk.co.epicuri.serverapi.errors;

public class LockStateException extends Exception {
    public LockStateException() {
        super();
    }

    public LockStateException(String ex) {
        super(ex);
    }

    public LockStateException(Exception ex) {
        super(ex);
    }
}
