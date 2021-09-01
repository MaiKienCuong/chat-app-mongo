package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException() {
        super("Không tìm thấy phòng chat này");
    }

    public RoomNotFoundException(String message) {
        super(message);
    }

}
