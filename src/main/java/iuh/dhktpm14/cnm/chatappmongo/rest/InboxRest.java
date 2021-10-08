package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.InboxMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxMessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inboxs")
@CrossOrigin("${spring.security.cross_origin}")
public class InboxRest {

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxMessageService inboxMessageService;

    @Autowired
    private InboxMapper inboxMapper;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AppUserDetailService userDetailService;

    private static final Logger logger = Logger.getLogger(InboxRest.class.getName());

    /**
     * lấy tất cả inbox của người dùng hiện tại
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách cuộc trò chuyện")
    public ResponseEntity<?> getAllInboxOfCurrentUser(@RequestParam Optional<RoomType> type, @ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        /*
        không phân trang khi lấy chat group
         */
        logger.log(Level.INFO, "get all inbox of userId = {0}", user.getId());
        logger.log(Level.INFO, "get all inbox page = {0}, size = {1}", new Object[]{ pageable.getPageNumber(), pageable.getPageSize() });
        if (type.isPresent() && type.get().equals(RoomType.GROUP)) {
            Page<Inbox> inboxPage = inboxService.getAllInboxOfUser(user.getId(), Pageable.unpaged());
            return ResponseEntity.ok(toInboxGroupDto(inboxPage));
        } else {
            Page<Inbox> inboxPage = inboxService.getAllInboxOfUser(user.getId(), pageable);
            return ResponseEntity.ok(toInboxDto(inboxPage));
        }
    }

    @GetMapping("/ofRoomId/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy inbox theo userId và roomId")
    public ResponseEntity<?> getInboxByUserIdAndRoomId(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Inbox> inboxOptional = inboxService.findByOfUserIdAndRoomId(user.getId(), roomId);
        if (inboxOptional.isPresent())
            return ResponseEntity.ok(inboxMapper.toInboxDto(inboxOptional.get()));
        return ResponseEntity.badRequest().build();
    }

    /**
     * xóa inbox
     */
    @DeleteMapping("/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Xóa cuộc trò chuyện")
    public ResponseEntity<?> deleteInbox(@PathVariable String inboxId,
                                         @ApiIgnore @AuthenticationPrincipal User user,
                                         Locale locale) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Inbox> inboxOptional = inboxService.findById(inboxId);
        if (inboxOptional.isEmpty()) {
            String message = messageSource.getMessage("delete_inbox_failed_try_again", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        var inbox = inboxOptional.get();
        if (user.getId().equals(inbox.getOfUserId())) {
            inboxService.updateEmptyStatusInbox(inboxId, true);

            // xóa tất cả message liên kết với inbox này, không xóa trong collection message
            inboxMessageService.deleteAllMessageOfInbox(inboxId);

            // reset số tin nhắn chưa đọc thành 0
            readTrackingService.resetUnreadMessageToZero(inbox.getRoomId(), user.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/with/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy inbox với người này nếu có")
    public ResponseEntity<?> getAllInboxOfCurrentUser(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String anotherUserId) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        if (anotherUserId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        if (! userDetailService.existsById(anotherUserId))
            return ResponseEntity.badRequest().build();
        var room = roomService.findCommonRoomBetween(user.getId(), anotherUserId);
        /*
        nếu 2 người này chưa có room chung thì trả về room và inbox với id là null
        nhưng vẫn set thuộc tính "to" cho room để client hiện tên và ảnh của người kia
         */
        if (room == null) {
            var r = new RoomSummaryDto();
            r.setId(null);
            r.setType(RoomType.ONE);
            r.setTo(userMapper.toUserProfileDto(anotherUserId));

            var dto = new InboxSummaryDto();
            dto.setId(null);
            dto.setRoom(r);
            return ResponseEntity.ok(dto);
        }
        /*
        nếu có room chung rồi thì trả về inbox của người dùng hiện tại trong room đó
         */
        Optional<Inbox> inbox = inboxService.findByOfUserIdAndRoomId(user.getId(), room.getId());
        if (inbox.isPresent())
            return ResponseEntity.ok(inboxMapper.toInboxSummaryDto(inbox.get()));
        /*
        nếu room đã tồn tại mà người dùng hiện tại chưa có inbox thì trả về inbox với id là null
         */
        var dto = new InboxSummaryDto();
        dto.setId(null);
        dto.setRoom(room);
        return ResponseEntity.ok(dto);
    }

    /*
    nếu hàm phía trên trả về inbox với id là null thì client sẽ gọi hàm này để tạo
    room và inbox trước khi gửi tin nhắn đầu tiên
     */
    @PostMapping("/with/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tạo room và inbox mới cho anotherUserId")
    public ResponseEntity<?> createNewRoomAndNewInbox(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String anotherUserId) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        if (anotherUserId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        var room = roomService.findCommonRoomBetween(user.getId(), anotherUserId);
        /*
        nếu room == null thì tạo room và inbox cho 2 người rồi trả về cho client  inbox của người dùng hiện tại
         */
        if (room == null) {
            Set<Member> members = new HashSet<>();
            members.add(Member.builder().userId(user.getId()).build());
            members.add(Member.builder().userId(anotherUserId).build());
            var newRoom = Room.builder()
                    .members(members)
                    .type(RoomType.ONE)
                    .build();
            roomService.save(newRoom);
            logger.log(Level.INFO, "room between userId = {0} and userId = {1} is null, creating new room",
                    new Object[]{ user.getId(), anotherUserId });
            var myInbox = createAndSaveInboxForUserInRoom(user.getId(), newRoom.getId());
//            createAndSaveInboxForUserInRoom(anotherUserId, newRoom.getId());
            return ResponseEntity.ok(inboxMapper.toInboxSummaryDto(myInbox));
        } else {
            /*
            nếu 2 người đã có room chung, kiểm tra xem người dùng hiện tại có inbox chưa
            nếu có rồi thì kiểm tra tiếp xem người dùng thứ 2 kia đã có inbox của room hay chưa
            nếu chưa có thì tạo inbox cho người dùng thứ 2
             */
            logger.log(Level.INFO, "room between userId = {0} and userId = {1} is not null, find exists inbox",
                    new Object[]{ user.getId(), anotherUserId });
            Optional<Inbox> myInbox = inboxService.findByOfUserIdAndRoomId(user.getId(), room.getId());
            if (myInbox.isPresent()) {
                logger.log(Level.INFO, "inbox of userId = {0} in roomId = {1} is not run, returning...",
                        new Object[]{ user.getId(), room.getId() });
//                Optional<Inbox> inboxOfAnotherUser = inboxRepository.findByOfUserIdAndRoomId(anotherUserId, room.getId());
//                if (inboxOfAnotherUser.isEmpty()) {
//                    System.out.println("inbox of two user empty, create new inbox for second user");
//                    createAndSaveInboxForUserInRoom(anotherUserId, room.getId());
//                }
                return ResponseEntity.ok(inboxMapper.toInboxSummaryDto(myInbox.get()));
            }
            /*
            hai người mới chỉ có room chung, chưa ai có inbox nên tạo inbox cho 2 người
             */
            logger.log(Level.INFO, "inbox of userId = {0} and userId = {1} is null, creating new 2 inbox",
                    new Object[]{ user.getId(), anotherUserId });
            var firstInbox = createAndSaveInboxForUserInRoom(user.getId(), room.getId());
//            createAndSaveInboxForUserInRoom(anotherUserId, room.getId());
            return ResponseEntity.ok(inboxMapper.toInboxSummaryDto(firstInbox));
        }
    }

    private Inbox createAndSaveInboxForUserInRoom(String userId, String roomId) {
        logger.log(Level.INFO, "creating new inbox of userId = {0} in roomId = {1}",
                new Object[]{ userId, roomId });
        var inbox = Inbox.builder()
                .roomId(roomId)
                .ofUserId(userId)
                .build();
        return inboxService.save(inbox);
    }

    /**
     * chuyển từ Page inbox sang page inboxDto
     */
    private Page<?> toInboxDto(Page<Inbox> inboxPage) {
        List<Inbox> content = inboxPage.getContent();
        List<InboxDto> dto = content.stream()
                .map(x -> inboxMapper.toInboxDto(x))
                .sorted()
                .collect(Collectors.toList());
        return new PageImpl<>(dto, inboxPage.getPageable(), inboxPage.getTotalElements());
    }

    private Page<?> toInboxGroupDto(Page<Inbox> inboxPage) {
        List<Inbox> content = inboxPage.getContent();
        List<InboxDto> dto = content.stream()
                .map(x -> inboxMapper.toInboxDto(x))
                .sorted()
                .filter(x -> x.getRoom().getType().equals(RoomType.GROUP))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, inboxPage.getPageable(), inboxPage.getTotalElements());
    }

}
