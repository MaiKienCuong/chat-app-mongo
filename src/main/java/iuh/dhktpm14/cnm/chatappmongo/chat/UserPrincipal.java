package iuh.dhktpm14.cnm.chatappmongo.chat;

import lombok.Getter;
import lombok.Setter;

import java.security.Principal;

/**
 * class mở rộng Principal, dùng để lưu thông tin người dùng hiện tại trong websocket
 * khi server gửi tin nhắn cho client sẽ gửi dựa vào giá trị trong hàm getName()
 * getName() ở đây sẽ là id của người dùng hiện tại được truyền thông qua header
 * accessToken cũng sẽ được đọc từ header dùng để xác thực
 */
public class UserPrincipal implements Principal {

    private final String userId;
    @Setter
    @Getter
    private String accessToken;

    public UserPrincipal(String userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId;
    }
}
