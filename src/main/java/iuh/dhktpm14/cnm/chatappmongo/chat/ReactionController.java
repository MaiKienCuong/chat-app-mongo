package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReactionFromClient;
import iuh.dhktpm14.cnm.chatappmongo.dto.chat.ReactionToClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Reaction;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class ReactionController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AppUserDetailService userDetailService;

    private static final Logger logger = Logger.getLogger(ReactionController.class.getName());

    @MessageMapping("/reaction")
    public void processMessage(@Payload ReactionFromClient reaction, UserPrincipal userPrincipal) {
        logger.log(Level.INFO, "reaction from client = {0}", reaction);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();

        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(reaction.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                var room = roomOptional.get();
                var reactionToClient = ReactionToClient.builder()
                        .messageId(reaction.getMessageId())
                        .roomId(reaction.getRoomId())
                        .type(reaction.getType())
                        .reactByUser(userMapper.toUserProfileDto(reaction.getUserId()))
                        .build();

                var react = Reaction.builder()
                        .type(reaction.getType())
                        .reactByUserId(reaction.getUserId())
                        .build();

                logger.log(Level.INFO, "adding reaction = {0}, to messageId = {1}, to database",
                        new Object[]{ react, reaction.getMessageId() });
                messageService.addReactToMessage(reaction.getMessageId(), react);

                for (Member member : room.getMembers()) {
                    logger.log(Level.INFO, "send reaction type = {0}, from userId = {1}, to memberId = {2}",
                            new Object[]{ reactionToClient.getType(), userId, member.getUserId() });
                    messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/reaction", reactionToClient);
                }
            }
        }

    }

}
