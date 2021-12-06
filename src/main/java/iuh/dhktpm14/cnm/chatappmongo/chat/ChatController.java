package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.MessageFromClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.MyMedia;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MediaType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.BlockService;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private BlockService blockService;

    @Autowired
    private MessageSource messageSource;

    /*
    nhận tin nhắn từ client và xử lý, sau đó gửi cho các thành viên trong cuộc trò chuyện
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload MessageFromClient messageDto, UserPrincipal userPrincipal) {
        log.info("message from client = {}", messageDto);

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();

        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomService.findById(messageDto.getRoomId());
            Optional<User> userOptional = userDetailService.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                var currentUser = userOptional.get();
                setAuthentication(currentUser);
                var room = roomOptional.get();
                if (! hasBlock(currentUser, room)) {
                    var member = Member.builder().userId(userId).build();

                    if (room.getMembers().contains(member)) {
                        var messageExcludeFile = createMessageExcludeFile(messageDto, room, currentUser);
                        var messageIncludeFile = createMessageIncludeFile(messageDto, room, currentUser);
                        log.info("messageExcludeFile = {}", messageExcludeFile);
                        log.info("messageIncludeFile = {}", messageIncludeFile);
                        if (messageDto.getMedia() != null && messageIncludeFile != null && messageIncludeFile.getMedia() != null) {
                            if (messageIncludeFile.getMedia().size() != messageDto.getMedia().size()) {
                                messageExcludeFile.setContent(null);
                                log.info("sending message exclude and include file file to websocket");
                                chatSocketService.sendMessage(messageExcludeFile, room, userId);
                            }
                            chatSocketService.sendMessage(messageIncludeFile, room, userId);
                        } else {
                            log.info("sending only message include file to websocket");
                            chatSocketService.sendMessage(messageExcludeFile, room, userId);
                        }
                    } else
                        log.error("userId = {} is not member of roomId = {}", userId, room.getId());
                } else {
                    log.info("userId = {} is block sending message to roomId  = {}", userId, room.getId());
                    sendBusyMessage(userId, room);
                }
            } else
                log.error("roomId = {} not exists", messageDto.getRoomId());
        } else
            log.error("userId or access token is null");
    }

    /*
    tách tin nhắn từ client thành 1 tin nhắn riêng chỉ chứa hình ảnh hoặc video, k chứa file
     */
    private Message createMessageExcludeFile(MessageFromClient messageDto, Room room, User sender) {
        var messageExcludeFile = Message.builder()
                .roomId(room.getId())
                .senderId(sender.getId())
                .createAt(new Date())
                .type(messageDto.getType())
                .content(messageDto.getContent())
                .replyId(messageDto.getReplyId())
                .build();
        if (messageDto.getMedia() != null) {
            List<MyMedia> mediaExcludeFile = messageDto.getMedia().stream().filter(x -> ! x.getType().equals(MediaType.FILE))
                    .collect(Collectors.toList());
            if (! mediaExcludeFile.isEmpty())
                messageExcludeFile.setMedia(mediaExcludeFile);
        }

        return messageExcludeFile;
    }

    /*
    tách tin nhắn từ client thành một tin nhắn chỉ chứa file, không chứa hình ảnh hoặc video
     */
    private Message createMessageIncludeFile(MessageFromClient messageDto, Room room, User sender) {
        Message messageIncludeFile = null;
        if (messageDto.getMedia() != null) {
            List<MyMedia> fileList = messageDto.getMedia().stream().filter(x -> x.getType().equals(MediaType.FILE))
                    .collect(Collectors.toList());
            if (! fileList.isEmpty()) {
                messageIncludeFile = Message.builder()
                        .roomId(room.getId())
                        .senderId(sender.getId())
                        .createAt(new Date())
                        .type(messageDto.getType())
                        .content(messageDto.getContent())
                        .replyId(messageDto.getReplyId())
                        .media(fileList)
                        .build();
            }
        }

        return messageIncludeFile;
    }

    /*
    gửi tin nhắn đang bận lại khi người gửi bị chặn
     */
    private void sendBusyMessage(String currentUserId, Room room) {
        log.error("currentUserId = {} cannot send message to roomId = {} because has been block",
                currentUserId, room.getId());
        Optional<Member> m = room.getMembers().stream().filter(x -> ! x.getUserId().equals(currentUserId)).findFirst();
        if (m.isPresent()) {
            var busyMessage = Message.builder()
                    .roomId(room.getId())
                    .senderId(m.get().getUserId())
                    .createAt(new Date())
                    .type(MessageType.TEXT)
                    .content(messageSource.getMessage("busy_message", null, LocaleContextHolder.getLocale()))
                    .build();
            chatSocketService.sendBusyMessage(busyMessage, room);
        }
    }

    private void setAuthentication(User user) {
        var authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    /*
    kiểm tra xem người gửi tin nhắn có bị chặn trong room này hay không
     */
    private boolean hasBlock(User currentUser, Room room) {
        RoomType type = room.getType();
        if (type.equals(RoomType.ONE)) {
            Set<Member> members = room.getMembers();
            if (members != null) {
                List<Member> newMembers = new ArrayList<>(members);
                newMembers.removeIf(x -> x.getUserId().equals(currentUser.getId()));
                if (newMembers.size() == 1 && newMembers.get(0) != null) {
                    var member = newMembers.get(0);
                    return member.getUserId() != null &&
                            blockService.checkThisUserBlockMe(currentUser.getId(), member.getUserId());
                }
            }
        }
        return false;
    }

}
