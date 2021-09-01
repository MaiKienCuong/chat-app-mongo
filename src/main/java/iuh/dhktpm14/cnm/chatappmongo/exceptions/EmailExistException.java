package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class EmailExistException extends RuntimeException {
    public EmailExistException() {
        super("Email đã tồn tại");
    }

    public EmailExistException(String message) {
        super(message);
    }
}
