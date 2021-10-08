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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class ReadByController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private RoomService roomService;

//    @Autowired
//    private ReadTrackingRepository readTrackingRepository;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private JwtUtils jwtUtils;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = Logger.getLogger(ReadByController.class.getName());

    @MessageMapping("/read")
    public void processMessage(@Payload ReadByFromClient readByFromClient, UserPrincipal userPrincipal) {
        logger.log(Level.INFO, "read tracking from client: {0}", readByFromClient);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();

        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(readByFromClient.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                var room = roomOptional.get();
                var readByToClient = ReadByToClient.builder()
                        .readAt(dateFormat.format(readByFromClient.getReadAt()))
                        .messageId(readByFromClient.getMessageId())
                        .roomId(readByFromClient.getRoomId())
                        .readByUser(userMapper.toUserProfileDto(readByFromClient.getUserId()))
                        .build();

                /*var readTracking = readTrackingRepository.findByRoomIdAndUserId(readByFromClient.getRoomId(), userPrincipal.getName());
                if (readTracking != null) {
                    readByToClient.setOldMessageId(readTracking.getMessageId());
                }*/

                logger.log(Level.INFO, "updating read tracking to database");
                readTrackingService.updateReadTracking(userId, room.getId(), readByFromClient.getMessageId());

                for (Member member : room.getMembers()) {
                    if (! member.getUserId().equals(userId)) {
                        logger.log(Level.INFO, "sending read tracking of user id={0} to member id={1}",
                                new Object[]{ userId, member.getUserId() });
                        messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/read", readByToClient);
                    }
                }
            }
        }

    }

}
