package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class InboxNotFoundException extends RuntimeException {

    public InboxNotFoundException() {
        super("Không tìm thấy cuộc trò chuyện này");
    }

    public InboxNotFoundException(String message) {
        super(message);
    }

}
