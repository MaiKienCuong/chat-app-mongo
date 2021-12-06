package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReadByFromClient;
import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReadByToClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Optional;

@Slf4j
@Controller
public class ReadByController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private JwtUtils jwtUtils;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /*
    xử lý khi client đã đọc tin nhắn
     */
    @MessageMapping("/read")
    public void processReadTracking(@Payload ReadByFromClient readByFromClient, UserPrincipal userPrincipal) {
        log.info("read tracking from client: {}", readByFromClient);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();

        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(readByFromClient.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                setAuthentication(userOptional.get());
                var room = roomOptional.get();
                if (room.isMemBerOfRoom(userId)) {
                    var readByToClient = ReadByToClient.builder()
                            .readAt(dateFormat.format(readByFromClient.getReadAt()))
                            .messageId(readByFromClient.getMessageId())
                            .roomId(readByFromClient.getRoomId())
                            .readByUser(userMapper.toUserProfileDto(readByFromClient.getUserId()))
                            .build();

                    log.info("updating read tracking to database");
                    readTrackingService.updateReadTracking(userId, room.getId(), readByFromClient.getMessageId());

                    for (Member member : room.getMembers()) {
//                        if (! member.getUserId().equals(userId)) {
                        log.info("sending read tracking of user id={} to member id={}",
                                userId, member.getUserId());
                        messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/read", readByToClient);
//                        }
                    }
                } else
                    log.error("userId = {} is not member of roomId = {}", userId, room.getId());
            } else
                log.error("roomId = {} is not exists", readByFromClient.getRoomId());
        } else
            log.error("userId or access token is null");
    }

    /*
    xử lý khi client đã đọc tin nhắn, nhưng là tin nhắn có type=SYSTEM, chỉ set số tin nhắn
    chưa đọc về 0 chứ không cập nhật tin nhắn cuối là tin nhắn SYSTEM
     */
    @MessageMapping("/read/resetUnreadMessage")
    public void resetUnreadMessage(@Payload ReadByFromClient readByFromClient, UserPrincipal userPrincipal) {
        log.info("resetUnreadMessage. read tracking from client: {}", readByFromClient);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();

        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(readByFromClient.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                setAuthentication(userOptional.get());
                var room = roomOptional.get();
                if (room.isMemBerOfRoom(userId)) {
                    readTrackingService.resetUnReadMessage(userId, room.getId());
                } else
                    log.error("userId = {} is not member of roomId = {}", userId, room.getId());
            } else
                log.error("roomId = {} is not exists", readByFromClient.getRoomId());
        } else
            log.error("userId or access token is null");
    }

    private void setAuthentication(User user) {
        var authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

}
