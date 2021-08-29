package iuh.dhktpm14.cnm.chatappmongo;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * khi người dùng truy cập vào trang web thì sẽ dùng javascript để kết nối đến websocket để nhận tin nhắn realtime
 * class này lắng nghe sự kiện của người dùng đến websocket
 */
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
    }

    /**
     * kết nối thành công đến websocket
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        System.out.println("event.getUser() = " + event.getUser());
        System.out.println("-----connected");
    }

    /**
     * sự kiện khi người dùng đóng tab
     * có thể cập nhật trạng thái truy cập cuối cùng ở đây
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println("event.getUser() = " + event.getUser());
        System.out.println("-----disconnect");
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
    }

}
