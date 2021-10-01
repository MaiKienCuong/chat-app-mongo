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
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
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

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private InboxService inboxService;

    @MessageMapping("/chat")
    public void processMessage(@Payload MessageFromClient messageDto, UserPrincipal userPrincipal) {
        System.out.println(messageDto);
        String userId = userPrincipal.getName();
        String accessToken = userPrincipal.getAccessToken();
        // kiểm tra userId và accessToken gửi lên từ header stomClient
        // nếu accessToken hợp lệ và accessToken thuộc về userId
        if (userId != null && accessToken != null && jwtUtils.validateJwtToken(accessToken)
                && userId.equals(jwtUtils.getUserIdFromJwtToken(accessToken))) {
            Optional<Room> roomOptional = roomRepository.findById(messageDto.getRoomId());
            Optional<User> userOptional = userRepository.findById(userId);

            if (roomOptional.isPresent() && userOptional.isPresent()) {
                var room = roomOptional.get();
                System.out.println("room = " + room);
                var message = Message.builder().roomId(room.getId()).senderId(userId)
                        .type(messageDto.getType())
                        .content(messageDto.getContent())
                        .build();
                messageRepository.save(message);
                sendMessageToAllMemberOfRoom(message, room);
                saveMessageToDatabase(message, room);
                updateLastTimeForAllInboxOfRoom(room);
                incrementUnReadMessageForMembersOfRoomExcludeUserId(room, userId);
                updateReadTracking(userId, room.getId(), message.getId());
            }
        }
    }

    public void updateLastTimeForAllInboxOfRoom(Room room) {
        inboxService.updateLastTimeForAllInboxOfRoom(room);
    }

    public void incrementUnReadMessageForMembersOfRoomExcludeUserId(Room room, String userId) {
        readTrackingService.incrementUnReadMessageForMembersOfRoomExcludeUserId(room, userId);
    }

    public void updateReadTracking(String userId, String roomId, String messageId) {
        readTrackingService.updateReadTracking(userId, roomId, messageId);
    }

    /**
     * gửi message tới tất cả các thành viên qua websocket, chưa lưu xuông db
     */
    public void sendMessageToAllMemberOfRoom(Message message, Room room) {
        Set<Member> members = room.getMembers();
        System.out.println("message = " + message);
        if (members != null && ! members.isEmpty()) {
            for (Member m : members) {
                // if (! m.getUserId().equals(user.getId())) {
                System.out.println("send to  = " + m.getUserId());
                messagingTemplate.convertAndSendToUser(m.getUserId(), "/queue/messages",
                        messageMapper.toMessageToClient(message));
                // }
            }
        }
    }

    /**
     * lưu tin nhắn và liên kết danh sách inbox của tất cả thành viên với message này
     */
    public void saveMessageToDatabase(Message message, Room room) {
        List<Inbox> inboxes = getAllInboxOfRoomToSaveMessage(room);
        if (! inboxes.isEmpty()) {
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

    /**
     * lấy danh sách inbox của tất cả thành viên trong room,
     * nếu thành viên nào trong room chưa có inbox thì tạo inbox cho thành viên đó
     */
    private List<Inbox> getAllInboxOfRoomToSaveMessage(Room room) {
        List<Inbox> inboxes = new ArrayList<>();
        Set<Member> members = room.getMembers();
        if (members != null && ! members.isEmpty())
            for (Member m : members) {
                // tìm danh sách inbox của tất cả các thành viên
                // nếu có thì thêm vào danh sách
                var inboxOptional = inboxRepository.findByOfUserIdAndRoomId(m.getUserId(), room.getId());
                Inbox inbox;
                if (inboxOptional.isPresent()) {
                    inbox = inboxOptional.get();
                    if (inbox.isEmpty())
                        inboxService.updateEmptyStatusInbox(inbox.getId(), false);
                } else {
                    // nếu member chưa có inbox thì tạo mới rồi thêm vào danh sách
                    inbox = Inbox.builder().ofUserId(m.getUserId())
                            .roomId(room.getId()).empty(false).build();
                    inboxRepository.save(inbox);
                }
                inboxes.add(inbox);
            }
        return inboxes;
    }

}
