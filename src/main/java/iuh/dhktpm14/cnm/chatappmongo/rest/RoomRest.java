package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.MemberDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.mapper.InboxMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MemberMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.RoomMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxMessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.InboxService;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.ReadTrackingService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomRestSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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
    private InboxMessageService inboxMessageService;

    @Autowired
    private RoomRestSocketService roomRestSocketService;

    /**
     * endpoint lấy số tin nhắn mới theo roomId, nếu cần
     */
    @GetMapping("/unReadMessage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy số tin nhắn mới, hiện tại chưa cần")
    public ResponseEntity<?> countNewMessage(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        log.info("count new message of userId = {}", user.getId());
        var readTracking = readTrackingService.findByRoomIdAndUserId(roomId, user.getId());
        if (readTracking != null) {
            long count = readTracking.getUnReadMessage();
            log.info("count new message = {}", count);
            return ResponseEntity.ok(count);
        }
        log.error("read tracking of userId = {} in roomId = {} is null, return count new message = 0",
                user.getId(), roomId);
        return ResponseEntity.ok(0);
    }

    /**
     * lấy tất cả thành viên trong room
     */
    @GetMapping("/members/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi tiết cuộc trò chuyện: Lấy danh sách thành viên")
    public ResponseEntity<?> getAllMembers(@ApiIgnore @AuthenticationPrincipal User user,
                                           @PathVariable String roomId,
                                           Locale locale) {
        log.info("userId = {} is getting list member in roomId = {}", user.getId(), roomId);
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            Set<Member> members = room.getMembers();
            // nếu là thành viên trong room mới được xem
            if (room.isMemBerOfRoom(user.getId())) {
                Set<MemberDto> dto = members.stream()
                        .map(x -> memberMapper.toMemberDto(x))
                        .sorted()
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                log.info("member = {}", dto);
                return ResponseEntity.ok(dto);
            }
        }
        String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
        log.error(roomNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
    }

    /**
     * lấy thông tin chi tiết về room
     */
    @GetMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thông tin chi tiết cuộc trò chuyện")
    public ResponseEntity<?> getById(@ApiIgnore @AuthenticationPrincipal User user,
                                     @PathVariable String roomId,
                                     Locale locale) {
        log.info("userId = {} is getting information detail of roomId = {}", user.getId(), roomId);
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            Set<Member> members = room.getMembers();
            // nếu là thành viên trong room mới được xem
            if (members != null && members.contains(Member.builder().userId(user.getId()).build())) {
                return ResponseEntity.ok(roomMapper.toRoomDetailDto(room));
            }
        }
        String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
        log.error(roomNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
    }

    /**
     * đổi tên nhóm cho web
     */
    @PostMapping("/rename/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi tên nhóm")
    public ResponseEntity<?> renameRoom(@ApiIgnore @AuthenticationPrincipal User user,
                                        @PathVariable String roomId,
                                        @RequestBody RoomSummaryDto room,
                                        Locale locale) {
        log.info("userId = {} is renaming roomId = {}", user.getId(), roomId);
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            roomService.renameRoom(roomId, room.getName());
            optional = roomService.findById(roomId);
            String content = messageSource.getMessage("message_after_rename_room",
                    new Object[]{ user.getDisplayName(), room.getName() }, locale);
            sendSystemMessage(content, optional.get());
            roomRestSocketService.sendAfterRename(optional.get());
            return ResponseEntity.ok(room.getName());
        }
        String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
        log.error(roomNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
    }

    /**
     * đổi tên nhóm cho mobile
     */
    @PostMapping(value = "/rename/{roomId}", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi tên nhóm")
    public ResponseEntity<?> renameRoomForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                 @PathVariable String roomId,
                                                 RoomSummaryDto room,
                                                 Locale locale) {
        return renameRoom(user, roomId, room, locale);
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
        log.info("userId = {} is changing image of roomId = {}", user.getId(), roomId);
        String message;
        if (files == null) {
            message = messageSource.getMessage("file_is_null", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        if (files.isEmpty()) {
            message = messageSource.getMessage("file_is_empty", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        Optional<Room> optional = roomService.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            var url = s3Service.uploadFile(files.get(0));
            log.info("setting new image for room with url = {}", url);
            room.setImageUrl(url.getUrl());
            roomService.save(room);
            String content = messageSource.getMessage("message_after_change_image_room",
                    new Object[]{ user.getDisplayName() }, locale);
            sendSystemMessage(content, optional.get());
            roomRestSocketService.sendAfterChangeImage(room);
            return ResponseEntity.ok(url);
        }
        String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
        log.error(roomNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
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
        log.info("userId = {} is getting last message of roomId = {}", user.getId(), roomId);
        var lastMessage = messageService.getLastMessageOfRoom(user.getId(), roomId);
        if (lastMessage.isPresent() && messageService.checkPermissionToSeeMessage(lastMessage.get().getId(), user.getId())) {
            return ResponseEntity.ok(messageMapper.toMessageDto(lastMessage.get()));
        }
        log.error("not found last message for userId = {} in roomId = {}", user.getId(), roomId);
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
        log.info("create room from client = {}", room);
        if (room.getMembers() == null || room.getMembers().isEmpty()) {
            String message = messageSource.getMessage("no_member", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }

        room.setId(null);
        room.setCreateAt(new Date());
        // xóa thành viên không tồn tại và user hiện tại nếu có
        room.getMembers().removeIf(x -> x.getUserId().equals(user.getId()) || ! userDetailService.existsById(x.getUserId()));

        if (room.getType().equals(RoomType.ONE)) {
            room.setName(null);
            room.setImageUrl(null);
            if (room.getMembers().size() != 1) {
                String message = messageSource.getMessage("list_member_invalid", null, locale);
                log.error(message);
                return ResponseEntity.badRequest().body(new MessageResponse(message));
            }
            List<String> memberIds = room.getMembers().stream().map(Member::getUserId)
                    .collect(Collectors.toList());
            var existRoom = roomService.findCommonRoomBetween(user.getId(), memberIds.get(0));
            if (existRoom != null) {
                log.info("exists room id = {}", existRoom.getId());
                Optional<Inbox> inboxOptional = inboxService.findByOfUserIdAndRoomId(user.getId(), existRoom.getId());
                if (inboxOptional.isPresent()) {
                    log.info("return exist inbox with id = {}", inboxOptional.get().getId());
                    return ResponseEntity.ok(inboxMapper.toInboxDto(inboxOptional.get()));
                }
                var inbox = Inbox.builder().ofUserId(user.getId()).roomId(existRoom.getId()).build();
                inboxService.save(inbox);
                log.info("return inbox created with id = {}", inbox.getId());
                return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
            }
            log.info("exists room is null, creating new room and inbox");
            room.getMembers().add(Member.builder().userId(user.getId()).build());
            roomService.save(room);
            var inbox = Inbox.builder().ofUserId(user.getId()).roomId(room.getId()).build();
            inboxService.save(inbox);
            log.info("new room one created = {}", room);
            log.info("new inbox for current user created= {}", inbox);
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
            String content = messageSource.getMessage("message_after_create_room",
                    new Object[]{ user.getDisplayName() }, locale);
            log.info("new room group created = {}", room);
            log.info("new inbox for current user created= {}", inbox);
            log.info("sending message after create room with content = {}", content);
            sendSystemMessage(content, room);
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

    private void sendSystemMessage(String content, Room room) {
        var message = Message.builder()
                .roomId(room.getId())
                .createAt(new Date())
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
    public ResponseEntity<?> setAdminForMember(@PathVariable String roomId, @PathVariable String memberId,
                                               @AuthenticationPrincipal User user,
                                               Locale locale) {
        log.info("userId = {} is setting admin for memberId = {}", user.getId(), memberId);
        if (! userDetailService.existsById(memberId)) {
            String userNotFound = messageSource.getMessage("user_not_found", null, locale);
            log.error(userNotFound);
            return ResponseEntity.badRequest().body(new MessageResponse(userNotFound));
        }
        if (roomService.setAdmin(memberId, roomId, user.getId())) {
            log.info("setting admin success");
            var roomOptional = roomService.findById(roomId);
            roomOptional.ifPresent(room -> roomRestSocketService.sendAfterSetAdmin(room));
            return ResponseEntity.ok().build();
        }
        log.error("setting admin error");
        return ResponseEntity.badRequest().build();
    }

    /**
     * thu hồi quyền admin của thành viên, chỉ có người tạo nhóm mới thực hiện được
     */
    @DeleteMapping("/admin/{roomId}/{memberId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thu hồi quyền admin của thành viên")
    public ResponseEntity<?> recallAdminForMember(@PathVariable String roomId, @PathVariable String memberId,
                                                  @AuthenticationPrincipal User user,
                                                  Locale locale) {
        log.info("userId = {} is recalling admin for memberId = {}", user.getId(), memberId);
        if (! userDetailService.existsById(memberId)) {
            String userNotFound = messageSource.getMessage("user_not_found", null, locale);
            log.error(userNotFound);
            return ResponseEntity.badRequest().body(new MessageResponse(userNotFound));
        }
        if (roomService.recallAdmin(memberId, roomId, user.getId())) {
            log.info("recall admin success");
            var roomOptional = roomService.findById(roomId);
            roomOptional.ifPresent(room -> roomRestSocketService.sendAfterRecallAdmin(room));
            return ResponseEntity.ok().build();
        }
        log.error("recall admin error");
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
        log.info("userId = {} is deleting member memberId = {} in roomId = {}", user.getId(), memberId, roomId);
        Optional<User> memberOptional = userDetailService.findById(memberId);
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (memberOptional.isPresent() && roomOptional.isPresent()) {
            User memberToDelete = memberOptional.get();
            String content = messageSource.getMessage("message_after_delete_member",
                    new Object[]{ user.getDisplayName(), memberToDelete.getDisplayName() }, locale);
            log.info("sending message after delete member with content = {}", content);
            /*
            phải gửi tin nhắn trước khi xóa vì khi xóa người đó không còn trong room nên không gửi được
             */
            sendSystemMessage(content, roomOptional.get());
            log.info("deleting member userId = {} in roomId = {}", memberId, roomId);
            boolean deleted = roomService.deleteMember(memberId, roomId, user.getId());
            if (deleted) {
                roomOptional = roomService.findById(roomId);
                roomRestSocketService.sendAfterDeleteMember(roomOptional.get());
                return ResponseEntity.ok(roomOptional.get().getMembers().stream().map(memberMapper::toMemberDto).collect(Collectors.toList()));
            } else
                return ResponseEntity.badRequest().build();
        }
        log.error("error delete member userId = {} in roomId = {}", memberId, roomId);
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
        log.info("userId = {} is adding list  members = {} to roomId = {}", user.getId(), members, roomId);
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (roomOptional.isEmpty()) {
            String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
            log.error(roomNotFound);
            return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
        }
        if (! roomService.isMemberOfRoom(user.getId(), roomId)) {
            log.error("currentUserId = {} is not member of roomId = {}", user.getId(), roomId);
            return ResponseEntity.badRequest().build();
        }
        var room = roomOptional.get();
        if (room.getType().equals(RoomType.ONE)) {
            String message = messageSource.getMessage("cannot_add_member_please_create_new_group", null, locale);
            log.error(message);
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
        roomRestSocketService.sendAfterAddMember(room);
        /*
        gửi tin nhắn hệ thống thông báo thêm thành viên
         */
        for (Member m : members) {
            Optional<User> userOptional = userDetailService.findById(m.getUserId());
            if (userOptional.isPresent()) {
                String content = messageSource.getMessage("message_after_add_member",
                        new Object[]{ userOptional.get().getDisplayName(), user.getDisplayName() }, locale);
                log.info("sending message after add member with content = {}", content);
                sendSystemMessage(content, room);
            }
        }
        return ResponseEntity.ok(room.getMembers().stream().map(memberMapper::toMemberDto).collect(Collectors.toList()));
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
        log.info("userId = {} is counting common group with userId = {}", user.getId(), anotherUserId);
        if (anotherUserId == null) {
            log.error("anotherUserId is null");
            return ResponseEntity.badRequest().build();
        }
        long count = roomService.countCommonGroupBetween(user.getId(), anotherUserId);
        log.info("count common group = {}", count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/commonGroup/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách nhóm chung giữa hai người")
    public ResponseEntity<?> getCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        log.info("userId = {} is getting common group with userId = {}", user.getId(), anotherUserId);
        if (anotherUserId == null) {
            log.error("anotherUserId is null");
            return ResponseEntity.badRequest().build();
        }
        List<Room> commonGroups = roomService.findCommonGroupBetween(user.getId(), anotherUserId);
        List<Optional<Inbox>> inboxs = commonGroups.stream()
                .map(x -> inboxService.findByOfUserIdAndRoomId(user.getId(), x.getId()))
                .collect(Collectors.toList());
        List<InboxDto> inboxDto = inboxs.stream()
                .filter(Optional::isPresent)
                .map(x -> inboxMapper.toInboxDto(x.get()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(inboxDto);
    }

    @PostMapping("/leave/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Rời khỏi nhóm")
    public ResponseEntity<?> leaveGroup(@PathVariable String roomId,
                                        @ApiIgnore @AuthenticationPrincipal User user,
                                        Locale locale) {
        log.info("userId = {} is leaving roomId = {}", user.getId(), roomId);
        if (roomId == null) {
            log.error("roomId = null");
            return ResponseEntity.badRequest().build();
        }
        Optional<Room> roomOptional = roomService.findById(roomId);
        if (roomOptional.isPresent()) {
            var room = roomOptional.get();
            if (room.getType().equals(RoomType.GROUP) && room.isMemBerOfRoom(user.getId())) {
                String content = messageSource.getMessage("message_leave_room",
                        new Object[]{ user.getDisplayName() }, locale);
                log.info("sending message leave room with content = {}", content);
                room.getMembers().removeIf(x -> x.getUserId().equals(user.getId()));
                sendSystemMessage(content, room);
                roomService.leaveGroup(user.getId(), roomId);
                inboxMessageService.deleteAllMessageOfUserInRoom(user.getId(), roomId);
                inboxService.deleteInbox(user.getId(), roomId);
                return ResponseEntity.ok().build();
            }
        }
        String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
        log.error(roomNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
    }

    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Xóa nhóm vĩnh viễn, chỉ dành cho người tạo nhóm")
    public ResponseEntity<?> deletePermanentlyRoom(@PathVariable String roomId,
                                                   @ApiIgnore @AuthenticationPrincipal User user,
                                                   Locale locale) {
        log.info("userId = {} is deleting permanently roomId = {}", user.getId(), roomId);
        if (roomId != null) {
            Optional<Room> roomOptional = roomService.findById(roomId);
            if (roomOptional.isPresent()) {
                var room = roomOptional.get();
                if (user.getId().equals(room.getCreateByUserId())) {
                    boolean delete = roomService.deletePermanentlyRoom(user.getId(), roomId);
                    if (delete) {
                        String success = messageSource.getMessage("delete_room_permanently_success",
                                new Object[]{ room.getName() }, locale);
                        log.info(success);
                        return ResponseEntity.ok().body(new MessageResponse(success));
                    }
                    String fail = messageSource.getMessage("delete_room_permanently_fail",
                            new Object[]{ room.getName() }, locale);
                    log.error(fail);
                    return ResponseEntity.badRequest().body(new MessageResponse(fail));
                }
                String accessDenied = messageSource.getMessage("delete_room_permanently_access_denied",
                        new Object[]{ room.getName() }, locale);
                log.error(accessDenied);
                return ResponseEntity.badRequest().body(new MessageResponse(accessDenied));
            }
        }
        String roomNotFound = messageSource.getMessage("room_not_found", null, locale);
        log.error(roomNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(roomNotFound));
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
