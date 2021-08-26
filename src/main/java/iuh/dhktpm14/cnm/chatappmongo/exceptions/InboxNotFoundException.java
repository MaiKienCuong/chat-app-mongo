package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class InboxNotFoundException extends RuntimeException {

    public InboxNotFoundException() {
        super("InboxNotFoundException");
    }

    public InboxNotFoundException(String message) {
        super(message);
    }

    public InboxNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public InboxNotFoundException(Throwable cause) {
        super(cause);
    }

}
