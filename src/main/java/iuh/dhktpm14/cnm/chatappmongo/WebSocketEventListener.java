package iuh.dhktpm14.cnm.chatappmongo;

import iuh.dhktpm14.cnm.chatappmongo.chat.UserPrincipal;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;

/**
 * khi người dùng truy cập vào trang web thì sẽ dùng javascript để kết nối đến websocket để nhận tin nhắn realtime
 * class này lắng nghe sự kiện của người dùng đến websocket
 */
@Slf4j
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

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        log.info("event connecting to websocket");
    }

    /**
     * kết nối thành công đến websocket
     * cập nhật trạng thái của user thành ONLINE
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        log.info("event connected to websocket");
        var userPrincipal = (UserPrincipal) event.getUser();
        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            String accessToken = userPrincipal.getAccessToken();
            if (userId == null) {
                log.error("userId is null");
                return;
            }
            if (accessToken == null) {
                log.error("access token is null");
                return;
            }
            if (! jwtUtils.validateJwtToken(accessToken)) {
                log.error("access token invalid");
                return;
            }
            if (jwtUtils.validateJwtToken(accessToken) && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
                if (userDetailService.existsById(userId)) {
                    log.info("userId = {} is connected", userId);
                    log.info("update online status for userId = {}", userId);
                    userDetailService.updateStatusOnline(userId);
                } else
                    log.error("userId = {} not found", userId);
            }
        } else
            log.error("user is null");
    }

    /**
     * sự kiện khi người dùng đóng tab
     * có thể cập nhật trạng thái truy cập cuối cùng ở đây
     * cập nhật lastOnline cho user
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        log.info("event disconnect to websocket");
        var userPrincipal = (UserPrincipal) event.getUser();
        if (userPrincipal != null) {
            String userId = userPrincipal.getName();
            String accessToken = userPrincipal.getAccessToken();
            if (userId != null && accessToken != null &&
                    jwtUtils.validateJwtToken(accessToken) && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
                if (userDetailService.existsById(userId)) {
                    log.info("userId = {} is disconnect", userId);
                    log.info("update offline status for userId = {}", userId);
                    userDetailService.updateStatusOffline(userId);
                } else
                    log.error("userId = {} not found", userId);
            } else
                log.error("userId or access token is null");
        } else
            log.error("user is null");
    }

    /**
     * sự kiện user subcribe để lắng nghe tin nhắn
     */
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            log.info("userId = {} is subscribing", user.getName());
        } else
            log.error("user is null");
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            log.info("userId = {} is unsubscribing", user.getName());
        } else
            log.error("user is null");
    }

}
