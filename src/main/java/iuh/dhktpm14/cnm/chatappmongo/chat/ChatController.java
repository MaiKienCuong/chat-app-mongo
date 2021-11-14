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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Optional;

@Slf4j
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

    @MessageMapping("/chat")
    public void processMessage(@Payload MessageFromClient messageDto, UserPrincipal userPrincipal) {
        log.info("message from client = {}", messageDto);

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
                setAuthentication(userOptional.get());
                var room = roomOptional.get();
                var member = Member.builder().userId(userId).build();
                if (room.getMembers().contains(member)) {
                    var message = Message.builder()
                            .roomId(room.getId())
                            .senderId(userId)
                            .createAt(new Date())
                            .type(messageDto.getType())
                            .content(messageDto.getContent())
                            .replyId(messageDto.getReplyId())
                            .media(messageDto.getMedia())
                            .build();
                    log.info("sending message = {} to websocket", message);
                    chatSocketService.sendMessage(message, room, userId);
                } else
                    log.error("userId = {} is not member of roomId = {}", userId, room.getId());
            } else
                log.error("roomId = {} not exists", messageDto.getRoomId());
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
