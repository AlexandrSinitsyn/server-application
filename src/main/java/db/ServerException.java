package db;

public class ServerException extends RuntimeException {

    public ServerException(final String message) {
        super(message);
    }

    public ServerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
