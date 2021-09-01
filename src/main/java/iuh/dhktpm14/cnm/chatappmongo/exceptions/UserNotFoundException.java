package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException() {
        super("Không tìm thấy user này");
    }

    public UserNotFoundException(String message) {
        super(message);
    }

}
