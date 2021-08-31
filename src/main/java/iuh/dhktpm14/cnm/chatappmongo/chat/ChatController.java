package iuh.dhktpm14.cnm.chatappmongo.chat;

import iuh.dhktpm14.cnm.chatappmongo.dto.chat.MessageFromClient;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Controller
public class ChatController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @MessageMapping("/chat")
    public void processMessage(@Payload MessageFromClient messageDto, UserPrincipal userPrincipal) {

        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();
        // kiểm tra userId và accessToken gửi lên từ header stomClient
        // nếu accessToken hợp lệ và accessToken thuộc về userId
        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomRepository.findById(messageDto.getRoomId());
            Optional<User> userOptional = userRepository.findById(userId);
            List<Inbox> inboxes = new ArrayList<>();
            Message message = null;

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                var room = roomOptional.get();
                Set<Member> members = room.getMembers();
                message = Message
                        .builder()
                        .roomId(room.getId())
                        .senderId(userId)
                        .createAt(new Date())
                        .type(messageDto.getType())
                        .content(messageDto.getContent())
                        .build();
                for (Member m : members) {
                    // gửi tin nhắn đến các user
                    // if (! m.getUserId().equals(user.getId())) {
                    messagingTemplate.convertAndSendToUser(m.getUserId(), "/queue/messages",
                            messageMapper.toMessageToClient(message));
                    // }
                    // tìm danh sách inbox của tất cả các thành viên
                    // nếu có thì thêm vào danh sách
                    if (inboxRepository.existsByOfUserIdAndRoomId(m.getUserId(), room.getId())) {
                        var inbox = inboxRepository.findByOfUserIdAndRoomId(m.getUserId(), room.getId());
                        inboxes.add(inbox);
                    } else {
                        // nếu member chưa có inbox thì tạo mới rồi thêm vào danh sách
                        var inbox = Inbox.builder().ofUserId(m.getUserId())
                                .roomId(room.getId()).empty(false).build();
                        inboxRepository.save(inbox);
                        inboxes.add(inbox);
                    }
                }
            }
            if (message != null && ! inboxes.isEmpty()) {
                messageRepository.save(message);
                for (Inbox inbox : inboxes) {
                    var inboxMessage = InboxMessage.builder()
                            .inboxId(inbox.getId())
                            .messageId(message.getId())
                            .messageCreateAt(message.getCreateAt())
                            .build();
                    inboxMessageRepository.save(inboxMessage);
                }
            }
        }
    }

}
