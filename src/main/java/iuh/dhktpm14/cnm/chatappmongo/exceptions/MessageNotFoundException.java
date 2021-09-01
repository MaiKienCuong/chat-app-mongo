package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException() {
        super("Không tìm thấy tin nhắn này");
    }

    public MessageNotFoundException(String message) {
        super(message);
    }

}
