package ru.nightcityroleplay.tests.exception;

public class AppContextException extends RuntimeException {
    public AppContextException(String message) {
        super(message);
    }

    public AppContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
