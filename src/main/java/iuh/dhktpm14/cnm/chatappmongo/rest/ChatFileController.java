package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.InboxMessage;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxMessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("api/chat")
@CrossOrigin("${spring.security.cross_origin}")
public class ChatFileController {
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AmazonS3Service s3Service;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping(value = "/file", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("gui file")
    public ResponseEntity<?> sendFileMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                            @RequestParam List<MultipartFile> files,
                                            @RequestParam String roomId,
                                            Locale locale) {
        return sendFile(user, files, roomId, locale);
    }

    @PostMapping(value = "/file")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("gui file")
    public ResponseEntity<?> sendFile(@ApiIgnore @AuthenticationPrincipal User user,
                                      @RequestParam List<MultipartFile> files,
                                      @RequestParam String roomId,
                                      Locale locale) {
        List<String> urls = new ArrayList<>();
        List<Message> messages = new ArrayList<>();
        String message;
        if (files == null) {
            message = messageSource.getMessage("file_is_null", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        if (files.isEmpty()) {
            message = messageSource.getMessage("file_is_empty", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isPresent()) {
            var room = roomOptional.get();
            for (MultipartFile file : files) {
                String newImageUrl = s3Service.uploadFile(file);
                urls.add(newImageUrl);

                Message m = Message.builder()
                        .type(MessageType.valueOf(file.getContentType()))
                        .roomId(roomId)
                        .senderId(user.getId())
                        .content(newImageUrl)
                        .build();
//                messageRepository.save(m);
                m = mongoTemplate.insert(m);
                messages.add(m);
                sendMessageToAllMemberOfRoom(m, room);
            }
            for (Message m : messages) {
                saveMessageToDatabase(m, room);
                inboxService.updateLastTimeForAllInboxOfRoom(room);
                readTrackingService.incrementUnReadMessageForMembersOfRoomExcludeUserId(room, user.getId());
                readTrackingService.updateReadTracking(user.getId(), room.getId(), m.getId());
            }
            return ResponseEntity.ok(urls);
        }

        return ResponseEntity.badRequest().build();

    }

    /**
     * gửi message tới tất cả các thành viên qua websocket, chưa lưu xuông db
     */
    private void sendMessageToAllMemberOfRoom(Message message, Room room) {
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
    private void saveMessageToDatabase(Message message, Room room) {
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
