package iuh.dhktpm14.cnm.chatappmongo.chat;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.ArrayList;
import java.util.Map;

import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

/**
 * tạm thời khi connect lên websocket bằng stomclient thì truyền vào header
 * chứa id cửa người dùng hiện tại và accessToken
 * <p>
 * id: khi user có tin nhắn mới, server sẽ gửi tin nhắn về theo id này
 * accessToken: tạm thời dùng để xác thực
 */
public class UserInterceptor implements ChannelInterceptor {

    /**
     * đọc header để lấy id và accessToken trước khi tin nhắn được gửi lên server
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            Object raw = message.getHeaders().get(NATIVE_HEADERS);

            if (raw instanceof Map) {
                Object userId = ((Map) raw).get("userId");
                Object accessToken = ((Map) raw).get("access_token");

                if (userId instanceof ArrayList) {
                    var principal = new UserPrincipal(((ArrayList<String>) userId).get(0));
                    principal.setAccessToken(((ArrayList<String>) accessToken).get(0));
                    // set người dùng hiện tại có 2 thuộc tính id và accessToken
                    accessor.setUser(principal);
                }
            }
        }
        return message;
    }
}