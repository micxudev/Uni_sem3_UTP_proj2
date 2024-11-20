package connectionImplementation;

public enum ConnectionStatus {
    CONNECTED(0, "connected"),
    DISCONNECTED(1, "disconnected"),
    PORT_NOT_INT(-1, "port is not an Integer"),
    PORT_OUT_OF_RANGE(-2, "port is out of range"),
    UNKNOWN_IP(-3, "unknown IP address"),
    CONNECTION_FAILED(-4, "could not connect to the server"),
    UNKNOWN_ERROR(-5, "unknown error occurred"),
    ALREADY_DISCONNECTED(-6, "already disconnected"),
    ERROR_CLOSING_CONNECTION(-7, "error closing connection"),
    THREAD_INTERRUPTED(-8, "connection closing was interrupted"),
    CONNECTION_TIMEOUT(-9, "connection attempt timed out"),
    USERNAME_INVALID (-10, "username is invalid"),
    USERNAME_TAKEN (-11, "username is taken"),
    IO_EXCEPTION(-12, "IO exception"),
    PROTOCOL_VIOLATION(-13, "protocol violation");

    private final int code;
    private final String message;

    ConnectionStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}