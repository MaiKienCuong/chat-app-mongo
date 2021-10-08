package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.MessageFromClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class ChatController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RoomService roomService;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private ChatSocketService chatSocketService;

    private static final Logger logger = Logger.getLogger(ChatController.class.getName());

    @MessageMapping("/chat")
    public void processMessage(@Payload MessageFromClient messageDto, UserPrincipal userPrincipal) {
        logger.log(Level.INFO, "message from client = {0}", messageDto);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();
        /*
        kiểm tra userId và accessToken gửi lên từ header stomClient
        nếu accessToken hợp lệ và accessToken thuộc về userId
         */
        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(messageDto.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                var room = roomOptional.get();
                var member = Member.builder().userId(userId).build();
                if (room.getMembers().contains(member)) {
                    var message = Message.builder()
                            .roomId(room.getId())
                            .senderId(userId)
                            .type(messageDto.getType())
                            .content(messageDto.getContent())
                            .build();
                    logger.log(Level.INFO, "sending message = {0} to websocket", message);
                    chatSocketService.sendMessage(message, room, userId);
                }
            }
        }
    }

}
