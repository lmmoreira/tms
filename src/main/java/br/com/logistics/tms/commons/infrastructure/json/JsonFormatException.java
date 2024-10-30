package br.com.logistics.tms.commons.infrastructure.json;

public class JsonFormatException extends RuntimeException {

    public JsonFormatException(String message) {
        super(message, null, true, false);
    }

    public JsonFormatException(String message, Throwable cause) {
        super(message, cause, true, false);
    }

}
