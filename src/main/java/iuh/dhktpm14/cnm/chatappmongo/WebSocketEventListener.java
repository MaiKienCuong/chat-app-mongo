package iuh.dhktpm14.cnm.chatappmongo;

import iuh.dhktpm14.cnm.chatappmongo.chat.UserPrincipal;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * khi người dùng truy cập vào trang web thì sẽ dùng javascript để kết nối đến websocket để nhận tin nhắn realtime
 * class này lắng nghe sự kiện của người dùng đến websocket
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AppUserDetailService userDetailService;

    /**
     * khi cập nhật một thuộc tính, dùng mongoTemplate chắc nhanh hơn repository.save(),
     * vì có những thuộc tính khi thay đổi thì phải cập nhật lại index
     */

    private static final Logger logger = Logger.getLogger(WebSocketEventListener.class.getName());

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        logger.log(Level.INFO, "event connecting to websocket");
    }

    /**
     * kết nối thành công đến websocket
     * cập nhật trạng thái của user thành ONLINE
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        logger.log(Level.INFO, "event connected to websocket");
        var userPrincipal = (UserPrincipal) event.getUser();
        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            String accessToken = userPrincipal.getAccessToken();
            if (userId == null)
                logger.log(Level.INFO, "userId is null");
            if (accessToken == null)
                logger.log(Level.INFO, "access token is null");
            if (! jwtUtils.validateJwtToken(accessToken))
                logger.log(Level.INFO, "access token is expired");
            if (userId != null && accessToken != null &&
                    jwtUtils.validateJwtToken(accessToken) && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
                if (userDetailService.existsById(userId)) {
                    logger.log(Level.INFO, "userId = {0} is connected", userId);
                    logger.log(Level.INFO, "access_token = {0}", accessToken);
                    userDetailService.updateStatusOnline(userId);
                } else
                    logger.log(Level.INFO, "userId = {0} not found", userId);
            }
        } else {
            logger.log(Level.INFO, "user is null");
        }
    }

    /**
     * sự kiện khi người dùng đóng tab
     * có thể cập nhật trạng thái truy cập cuối cùng ở đây
     * cập nhật lastOnline cho user
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        logger.log(Level.INFO, "event disconnect to websocket");
        var userPrincipal = (UserPrincipal) event.getUser();
        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            String accessToken = userPrincipal.getAccessToken();
            if (userId != null && accessToken != null &&
                    jwtUtils.validateJwtToken(accessToken) && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
                if (userDetailService.existsById(userId)) {
                    userDetailService.updateStatusOffline(userId);
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
        Principal user = event.getUser();
        if (user != null) {
            logger.log(Level.INFO, "userId = {0} is unsubscribing", user.getName());
            logger.log(Level.INFO, "userId = {0} unsubscribed", user.getName());
        }
    }

}
