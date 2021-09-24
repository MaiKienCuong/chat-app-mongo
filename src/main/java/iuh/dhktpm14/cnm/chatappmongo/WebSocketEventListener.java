package iuh.dhktpm14.cnm.chatappmongo;

import iuh.dhktpm14.cnm.chatappmongo.chat.UserPrincipal;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.OnlineStatus;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Date;

/**
 * khi người dùng truy cập vào trang web thì sẽ dùng javascript để kết nối đến websocket để nhận tin nhắn realtime
 * class này lắng nghe sự kiện của người dùng đến websocket
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    /**
     * khi cập nhật một thuộc tính, dùng mongoTemplate chắc nhanh hơn repository.save()
     */
    @Autowired
    private MongoTemplate mongoTemplate;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        System.out.println("-------connecting");
    }

    /**
     * kết nối thành công đến websocket
     * cập nhật trạng thái của user thành ONLINE
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        System.out.println("-----------------connected to websocket");
        var userPrincipal = (UserPrincipal) event.getUser();
        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            String accessToken = userPrincipal.getAccessToken();
            if (userId == null)
                System.out.println("-------------userId is null");
            if (accessToken == null)
                System.out.println("-------------access token is null");
            if (! jwtUtils.validateJwtToken(accessToken))
                System.out.println("-------------access token is expired");
            if (userId != null && accessToken != null) {
                if (jwtUtils.validateJwtToken(accessToken) && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
                    if (userRepository.existsById(userId)) {
                        System.out.println("userId = " + userId + " is connected");
                        System.out.println("access_token: " + accessToken);
                        var criteria = Criteria.where("_id").is(userId);
                        var update = new Update();
                        update.set("onlineStatus", OnlineStatus.ONLINE)
                                .unset("lastOnline");
                        mongoTemplate.updateFirst(Query.query(criteria), update, User.class);
                    } else
                        System.out.println("userId = " + userId + " not found");
                }
            }
        } else {
            System.out.println("---------------user is null");
        }
    }

    /**
     * sự kiện khi người dùng đóng tab
     * có thể cập nhật trạng thái truy cập cuối cùng ở đây
     * cập nhật lastOnline cho user
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println("-----------------disconnect to websocket");
        var userPrincipal = (UserPrincipal) event.getUser();
        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            String accessToken = userPrincipal.getAccessToken();
            if (userId != null && accessToken != null) {
                if (jwtUtils.validateJwtToken(accessToken) && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
                    if (userRepository.existsById(userId)) {
                        var criteria = Criteria.where("_id").is(userId);
                        var update = new Update();
                        update.set("onlineStatus", OnlineStatus.OFFLINE)
                                .set("lastOnline", new Date());
                        mongoTemplate.updateFirst(Query.query(criteria), update, User.class);
                    }
                }
            }
        }
    }

    /**
     * sự kiện user subcribe để lắng nghe tin nhắn
     */
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        System.out.println(event.getUser().getName());
        System.out.println("---------------unsubscribe");
    }

}
