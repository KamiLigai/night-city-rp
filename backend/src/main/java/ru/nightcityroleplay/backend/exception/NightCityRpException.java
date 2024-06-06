package ru.nightcityroleplay.backend.exception;

public class NightCityRpException extends RuntimeException {

    public NightCityRpException(String message) {
        super(message);
    }

    public NightCityRpException(String message, Throwable cause) {
        super(message, cause);
    }
}
