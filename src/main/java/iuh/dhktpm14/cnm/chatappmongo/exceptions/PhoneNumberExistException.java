package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class PhoneNumberExistException extends RuntimeException {

    public PhoneNumberExistException() {
        super("Số điện thoại đã tồn tại");
    }

    public PhoneNumberExistException(String message) {
        super(message);
    }

}
