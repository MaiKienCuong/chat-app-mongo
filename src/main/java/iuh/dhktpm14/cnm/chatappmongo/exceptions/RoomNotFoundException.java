package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException() {
        super("RoomNotFoundException");
    }

    public RoomNotFoundException(String message) {
        super(message);
    }

    public RoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoomNotFoundException(Throwable cause) {
        super(cause);
    }

}
