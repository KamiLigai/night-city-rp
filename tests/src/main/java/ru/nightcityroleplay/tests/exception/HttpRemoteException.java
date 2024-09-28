package ru.nightcityroleplay.tests.exception;

public class HttpRemoteException extends RuntimeException {
    public HttpRemoteException(String message) {
        super(message);
    }

    public HttpRemoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
