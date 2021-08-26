package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException() {
        super("MessageNotFoundException");
    }

    public MessageNotFoundException(String message) {
        super(message);
    }

    public MessageNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageNotFoundException(Throwable cause) {
        super(cause);
    }

}
