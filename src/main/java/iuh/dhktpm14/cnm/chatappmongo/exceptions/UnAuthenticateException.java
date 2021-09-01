package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class UnAuthenticateException extends RuntimeException {

    public UnAuthenticateException() {
        super("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");
    }

    public UnAuthenticateException(String message) {
        super(message);
    }

}
