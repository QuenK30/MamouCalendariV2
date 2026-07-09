package fr.qmn.mamoucalendari.config;

public class RemoteApiException extends RuntimeException {

    private final int statusCode;

    public RemoteApiException(int statusCode, String message) {
        super("HTTP " + statusCode + " — " + message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
