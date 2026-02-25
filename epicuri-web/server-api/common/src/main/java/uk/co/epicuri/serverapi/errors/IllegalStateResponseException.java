package uk.co.epicuri.serverapi.errors;

import org.springframework.http.HttpStatus;

public class IllegalStateResponseException extends Exception {
    private final String responseMessage;
    private final HttpStatus status;

    public IllegalStateResponseException(String responseMessage, HttpStatus status) {
        super(responseMessage);
        this.responseMessage = responseMessage;
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
