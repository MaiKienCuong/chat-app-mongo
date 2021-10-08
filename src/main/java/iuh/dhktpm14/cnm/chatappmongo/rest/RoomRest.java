package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.MemberDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.RoomNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UserNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.InboxMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MemberMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.RoomMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/rooms")
@CrossOrigin(value = "${spring.security.cross_origin}")
public class RoomRest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ReadTrackingService readTrackingService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private InboxMapper inboxMapper;

    @Autowired
    private AmazonS3Service s3Service;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ChatSocketService chatSocketService;

    @Autowired
    private static final Logger logger = Logger.getLogger(RoomRest.class.getName());

    /**
     * endpoint lấy số tin nhắn mới theo roomId, nếu cần
     */
    @GetMapping("/unReadMessage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy số tin nhắn mới, hiện tại chưa cần")
    public ResponseEntity<?> countNewMessage(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        if (user == null)
            throw new UnAuthenticateException();
        var readTracking = readTrackingService.findByRoomIdAndUserId(roomId, user.getId());
        if (readTracking != null) {
            return ResponseEntity.ok(readTracking.getUnReadMessage());
        }
        return ResponseEntity.ok(0);
    }

    /**
     * lấy tất cả thành viên trong room
     */
    @GetMapping("/members/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi tiết cuộc trò chuyện: Lấy danh sách thành viên")
    public ResponseEntity<?> getAllMembers(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            Set<Member> members = room.getMembers();
            // nếu là thành viên trong room mới được xem
            if (members != null && members.contains(Member.builder().userId(user.getId()).build())) {
                Set<MemberDto> dto = members.stream()
                        .map(x -> memberMapper.toMemberDto(x))
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                return ResponseEntity.ok(dto);
            }
        }
        throw new RoomNotFoundException();
    }

    /**
     * lấy thông tin chi tiết về room
     */
    @GetMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thông tin chi tiết cuộc trò chuyện")
    public ResponseEntity<?> getById(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            Set<Member> members = room.getMembers();
            // nếu là thành viên trong room mới được xem
            if (members != null && members.contains(Member.builder().userId(user.getId()).build())) {
                return ResponseEntity.ok(roomMapper.toRoomDetailDto(room));
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * đổi tên nhóm cho web
     */
    @PostMapping("/rename/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi tên nhóm")
    public ResponseEntity<?> renameRoom(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId, @RequestBody RoomSummaryDto room) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            roomService.renameRoom(roomId, room.getName());
            return ResponseEntity.ok(room.getName());
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * đổi tên nhóm cho mobile
     */
    @PostMapping(value = "/rename/{roomId}", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi tên nhóm")
    public ResponseEntity<?> renameRoomForMobile(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId, RoomSummaryDto room) {
        return renameRoom(user, roomId, room);
    }

    /**
     * đổi ảnh đại diện nhóm web
     */
    @PostMapping("/changeImage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh nhóm")
    public ResponseEntity<?> changeImage(@ApiIgnore @AuthenticationPrincipal User user,
                                         @PathVariable String roomId,
                                         @RequestParam List<MultipartFile> files,
                                         Locale locale) {
        if (user == null)
            throw new UnAuthenticateException();
        String message;
        if (files == null) {
            message = messageSource.getMessage("file_is_null", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        if (files.isEmpty()) {
            message = messageSource.getMessage("file_is_empty", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            var url = s3Service.uploadFile(files.get(0));
            room.setImageUrl(url);
            roomService.save(room);
            return ResponseEntity.ok(List.of(url));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * đổi ảnh đại diện nhóm mobile
     */
    @PostMapping(value = "/changeImage/{roomId}", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh nhóm")
    public ResponseEntity<?> changeImageForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                  @PathVariable String roomId,
                                                  @RequestParam List<MultipartFile> file,
                                                  Locale locale) {
        return changeImage(user, roomId, file, locale);
    }

    /**
     * endpoint lấy tin nhắn cuối theo roomId, nếu cần
     */
    @GetMapping("/lastMessage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tin nhắn cuối, hiện tại chưa cần")
    public ResponseEntity<?> getLastMessage(@PathVariable String roomId, @AuthenticationPrincipal User user) {
        var lastMessage = messageService.getLastMessageOfRoom(user.getId(), roomId);
        if (lastMessage.isPresent() && messageService.checkPermissionToSeeMessage(lastMessage.get().getId(), user.getId())) {
            return ResponseEntity.ok(messageMapper.toMessageDto(lastMessage.get()));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * tạo room mới
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tạo room chat mới")
    public ResponseEntity<?> createNewRoom(@RequestBody Room room,
                                           @ApiIgnore @AuthenticationPrincipal User user,
                                           Locale locale) {
        logger.log(Level.INFO, "new room to create from client = {0}", room);
        if (user == null)
            throw new UnAuthenticateException();
        if (room.getMembers() == null || room.getMembers().isEmpty()) {
            String message = messageSource.getMessage("no_member", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        room.setId(null);
        // xóa thành viên không tồn tại và user hiện tại nếu có
        room.getMembers().removeIf(x -> x.getUserId().equals(user.getId()) || ! userDetailService.existsById(x.getUserId()));
        if (room.getType().equals(RoomType.ONE)) {
            room.setName(null);
            room.setImageUrl(null);
            if (room.getMembers().size() != 1) {
                String message = messageSource.getMessage("list_member_invalid", null, locale);
                return ResponseEntity.badRequest().body(new MessageResponse(message));
            }
            List<String> memberIds = room.getMembers().stream().map(Member::getUserId)
                    .collect(Collectors.toList());
            var existRoom = roomService.findCommonRoomBetween(user.getId(), memberIds.get(0));
            if (existRoom != null) {
                Optional<Inbox> inboxOptional = inboxService.findByOfUserIdAndRoomId(user.getId(), existRoom.getId());
                if (inboxOptional.isPresent())
                    return ResponseEntity.ok(inboxMapper.toInboxDto(inboxOptional.get()));
                var inbox = Inbox.builder().ofUserId(user.getId()).roomId(existRoom.getId()).build();
                inboxService.save(inbox);
                return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
            }
            room.getMembers().add(Member.builder().userId(user.getId()).build());
            roomService.save(room);
            var inbox = Inbox.builder().ofUserId(user.getId()).roomId(room.getId()).build();
            inboxService.save(inbox);
            return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
        }
        if (room.getType().equals(RoomType.GROUP)) {
            for (Member m : room.getMembers()) {
                m.setAddByUserId(user.getId());
                m.setAddTime(new Date());
                m.setAdmin(false);
            }
            room.setCreateByUserId(user.getId());
            // thêm người dùng hiện tại vào nhóm
            room.getMembers().add(Member.builder().userId(user.getId()).isAdmin(true).build());
            roomService.save(room);
            var inbox = Inbox.builder().ofUserId(user.getId()).roomId(room.getId()).build();
            inboxService.save(inbox);
            sendMessageAfterCreateRoom(room, user, locale);
            return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(value = "/", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tạo room chat mới")
    public ResponseEntity<?> createNewRoomForMobile(@RequestBody Room room,
                                                    @ApiIgnore @AuthenticationPrincipal User user,
                                                    Locale locale) {
        return createNewRoom(room, user, locale);
    }

    private void sendMessageAfterCreateRoom(Room room, User user, Locale locale) {
        String content = messageSource.getMessage("message_after_create_room",
                new Object[]{ user.getDisplayName() }, locale);
        logger.log(Level.INFO, "sending message after create room with content = {0}", content);
        var message = Message.builder()
                .roomId(room.getId())
                .type(MessageType.SYSTEM)
                .content(content)
                .build();
        chatSocketService.sendSystemMessage(message, room);
    }

    /**
     * set thành viên làm admin của room
     */
    @PostMapping("/admin/{roomId}/{memberId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("set thành viên làm admin nhóm")
    public ResponseEntity<?> setAdminForMember(@PathVariable String roomId, @PathVariable String memberId, @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (! userDetailService.existsById(memberId))
            throw new UserNotFoundException();
        if (roomService.setAdmin(memberId, roomId, user.getId()))
            return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().build();
    }

    /**
     * Xóa thành viên khỏi nhóm
     */
    @DeleteMapping("/{roomId}/{memberId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("xóa thành viên")
    public ResponseEntity<?> deleteMember(@PathVariable String roomId, @PathVariable String memberId,
                                          @AuthenticationPrincipal User user, Locale locale) {
        logger.log(Level.INFO, "delete member userId = {0} in roomId = {1}",
                new Object[]{ memberId, roomId });
        Optional<User> memberOptional = userDetailService.findById(memberId);
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (memberOptional.isPresent() && roomOptional.isPresent()) {
            User memberToDelete = memberOptional.get();
            String content = messageSource.getMessage("message_after_delete_member",
                    new Object[]{ user.getDisplayName(), memberToDelete.getDisplayName() }, locale);
            logger.log(Level.INFO, "sending message after delete member with content = {0}",
                    new Object[]{ content });

            var message = Message.builder()
                    .type(MessageType.SYSTEM)
                    .content(content)
                    .roomId(roomId)
                    .build();

            logger.log(Level.INFO, "deleting member userId = {0} in roomId = {1}",
                    new Object[]{ memberId, roomId });
            /*
            phải gửi tin nhắn trước khi xóa vì khi xóa người đó không còn trong room nên không gửi được
             */
            chatSocketService.sendSystemMessage(message, roomOptional.get());
            roomService.deleteMember(memberId, roomId, user.getId());
            return ResponseEntity.ok().build();
        }
        logger.log(Level.WARNING, "error delete member userId = {0} in roomId = {1}",
                new Object[]{ memberId, roomId });
        return ResponseEntity.badRequest().build();
    }

    /**
     * thêm thành viên cho nhóm
     */
    @PostMapping("/members/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thêm thành viên vào nhóm chat")
    public ResponseEntity<?> addMemberToRoom(@PathVariable String roomId,
                                             @RequestBody List<Member> members,
                                             @ApiIgnore @AuthenticationPrincipal User user,
                                             Locale locale) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (roomOptional.isEmpty())
            throw new RoomNotFoundException();
        if (! roomService.isMemberOfRoom(user.getId(), roomId))
            return ResponseEntity.badRequest().build();
        var room = roomOptional.get();
        if (room.getType().equals(RoomType.ONE)) {
            String message = messageSource.getMessage("cannot_add_member_please_create_new_group", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        var finalRoom = room;
        members.removeIf(x -> x.getUserId().equals(user.getId())
                || ! userDetailService.existsById(x.getUserId())
                || finalRoom.getMembers().contains(x));
        for (Member m : members) {
            m.setAddByUserId(user.getId());
            m.setAddTime(new Date());
            m.setAdmin(false);
        }
        roomService.addMembersToRoom(members, roomId);
        room = roomService.findById(roomId).get();
        /*
        gửi tin nhắn hệ thống thông báo thêm thành viên
         */
        for (Member m : members) {
            Optional<User> userOptional = userDetailService.findById(m.getUserId());
            if (userOptional.isPresent()) {
                String content = messageSource.getMessage("message_after_add_member",
                        new Object[]{ userOptional.get().getDisplayName(), user.getDisplayName() }, locale);
                logger.log(Level.INFO, "sending message after  add member with content = {0}", content);

                var message = Message.builder()
                        .type(MessageType.SYSTEM)
                        .content(content)
                        .roomId(roomId)
                        .build();
                chatSocketService.sendSystemMessage(message, room);
            }
        }
        return ResponseEntity.ok(room.getMembers() != null ? room.getMembers() : new ArrayList<>(0));
    }

    /**
     * thêm thành viên cho nhóm cho mobile
     */
    @PostMapping(value = "/members/{roomId}", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thêm thành viên vào nhóm chat")
    public ResponseEntity<?> addMemberToRoomForMobile(@PathVariable String roomId,
                                                      @RequestBody List<Member> members,
                                                      @ApiIgnore @AuthenticationPrincipal User user,
                                                      Locale locale) {
        return addMemberToRoom(roomId, members, user, locale);
    }

    @GetMapping("/commonGroup/count/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy số lượng nhóm chung giữa hai người")
    public ResponseEntity<?> countCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(roomService.countCommonGroupBetween(user.getId(), anotherUserId));
    }

    @GetMapping("/commonGroup/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách nhóm chung giữa hai người")
    public ResponseEntity<?> getCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        List<Room> commonGroups = roomService.findCommonGroupBetween(user.getId(), anotherUserId);
        List<Optional<Inbox>> inboxs = commonGroups.stream()
                .map(x -> inboxService.findByOfUserIdAndRoomId(user.getId(), x.getId()))
                .collect(Collectors.toList());
        List<InboxSummaryDto> inboxDto = inboxs.stream()
                .filter(Optional::isPresent)
                .map(x -> inboxMapper.toInboxSummaryDto(x.get()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(inboxDto);
    }

    /**
     * chuyển từ Page<Room> qua Page<RoomDto>
     */
    private Page<?> toRoomDto(Page<Room> roomPage) {
        List<Room> content = roomPage.getContent();
        List<Object> dto = content.stream().map(x -> roomMapper.toRoomSummaryDto(x.getId())).collect(Collectors.toList());
        return new PageImpl<>(dto, roomPage.getPageable(), roomPage.getTotalElements());
    }

}
