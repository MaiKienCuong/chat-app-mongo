package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("UserNotFoundException");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

}
