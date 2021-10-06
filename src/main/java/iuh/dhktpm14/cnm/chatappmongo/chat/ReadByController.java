package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReadByFromClient;
import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReadByToClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
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
    private RoomRepository roomRepository;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    @Autowired
    private ReadTrackingService readTrackingService;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final Logger logger = Logger.getLogger(ReadByController.class.getName());

    @MessageMapping("/read")
    public void processMessage(@Payload ReadByFromClient readByFromClient, UserPrincipal userPrincipal) {
        logger.log(Level.INFO, "user principal: {0}", userPrincipal);
        logger.log(Level.INFO, "read tracking from client: {0}", readByFromClient);

        var readByToClient = new ReadByToClient();
        readByToClient.setReadAt(dateFormat.format(readByFromClient.getReadAt()));
        readByToClient.setMessageId(readByFromClient.getMessageId());
        readByToClient.setRoomId(readByFromClient.getRoomId());
        readByToClient.setReadByUser(userMapper.toUserProfileDto(readByFromClient.getUserId()));

        var readTracking = readTrackingRepository.findByRoomIdAndUserId(readByFromClient.getRoomId(), userPrincipal.getName());
        if (readTracking != null) {
            readByToClient.setOldMessageId(readTracking.getMessageId());
        }

        Optional<Room> roomOptional = roomRepository.findById(readByFromClient.getRoomId());
        if (roomOptional.isPresent()) {
            var room = roomOptional.get();
            logger.log(Level.INFO, "updating read tracking to database");
            readTrackingService.updateReadTracking(userPrincipal.getName(), readByFromClient.getRoomId(), readByFromClient.getMessageId());

            for (Member member : room.getMembers()) {
                if (! member.getUserId().equals(userPrincipal.getName())) {
                    logger.log(Level.INFO, "sending read tracking of user id={0} to member id={1}", new Object[]{ userPrincipal.getName(), member.getUserId() });
                    messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/read", readByToClient);
                }
            }
        }
    }

}
