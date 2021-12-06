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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Slf4j
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

    /*
    xử lý khi client bày tỏ cảm xúc về tin nhắn
     */
    @MessageMapping("/reaction")
    public void processReaction(@Payload ReactionFromClient reaction, UserPrincipal userPrincipal) {
        log.info("reaction from client = {}", reaction);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();

        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(reaction.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                setAuthentication(userOptional.get());
                var room = roomOptional.get();
                if (room.isMemBerOfRoom(userId)) {
                    var reactionToClient = ReactionToClient.builder()
                            .messageId(reaction.getMessageId())
                            .roomId(reaction.getRoomId())
                            .type(reaction.getType())
                            .reactByUser(userMapper.toUserProfileDto(reaction.getUserId()))
                            .build();

                    var reactToDatabase = Reaction.builder()
                            .type(reaction.getType())
                            .reactByUserId(reaction.getUserId())
                            .build();

                    log.info("adding reaction = {}, to messageId = {}, to database",
                            reactToDatabase, reaction.getMessageId());
                    messageService.addReactToMessage(reaction.getMessageId(), reactToDatabase);

                    for (Member member : room.getMembers()) {
                        log.info("send reaction type = {}, from userId = {}, to memberId = {}",
                                reactionToClient.getType(), userId, member.getUserId());
                        messagingTemplate.convertAndSendToUser(member.getUserId(), "/queue/reaction", reactionToClient);
                    }
                } else
                    log.error("userId = {} is not member of roomId = {}", userId, room.getId());
            } else
                log.error("roomId = {} is not exists", reaction.getRoomId());
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
