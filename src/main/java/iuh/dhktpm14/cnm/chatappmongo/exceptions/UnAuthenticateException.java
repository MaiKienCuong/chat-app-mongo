package iuh.dhktpm14.cnm.chatappmongo.exceptions;

public class UnAuthenticateException extends RuntimeException {

    public UnAuthenticateException() {
        super("Vui lòng đăng nhập");
    }

    public UnAuthenticateException(String message) {
        super(message);
    }

    public UnAuthenticateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnAuthenticateException(Throwable cause) {
        super(cause);
    }

}
