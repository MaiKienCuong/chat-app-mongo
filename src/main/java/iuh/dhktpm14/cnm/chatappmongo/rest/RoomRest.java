package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.InboxSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.MemberDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.RoomGroupSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Inbox;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.RoomNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UserNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.InboxMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MemberMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MessageMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.RoomMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.InboxRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/rooms")
@CrossOrigin(value = "${spring.security.cross_origin}")
public class RoomRest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ReadTrackingRepository readTrackingRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InboxRepository inboxRepository;

    @Autowired
    private InboxMapper inboxMapper;

    @Autowired
    private AmazonS3Service s3Service;

    /**
     * endpoint lấy số tin nhắn mới theo roomId, nếu cần
     */
    @GetMapping("/unReadMessage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy số tin nhắn mới, hiện tại chưa cần")
    public ResponseEntity<?> countNewMessage(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        if (user == null)
            throw new UnAuthenticateException();
        var readTracking = readTrackingRepository.findByRoomIdAndUserId(roomId, user.getId());
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
        Optional<Room> optional = roomRepository.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            Set<Member> members = room.getMembers();
            // nếu là thành viên trong room mới được xem
            if (members != null && members.contains(Member.builder().userId(user.getId()).build())) {
                Set<MemberDto> dto = members.stream()
                        .map(x -> memberMapper.toMemberDto(x))
                        .collect(Collectors.toSet());
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
        Optional<Room> optional = roomRepository.findById(roomId);
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
    public ResponseEntity<?> renameRoom(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId, @RequestBody RoomGroupSummaryDto room) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> optional = roomRepository.findById(roomId);
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
    public ResponseEntity<?> renameRoomForMobile(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId, RoomGroupSummaryDto room) {
        return renameRoom(user, roomId, room);
    }

    /**
     * đổi ảnh đại diện nhóm web
     */
    @PostMapping("/changeImage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh nhóm")
    public ResponseEntity<?> changeImage(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId, @RequestParam List<MultipartFile> files) {
        if (user == null)
            throw new UnAuthenticateException();
        System.out.println("user = " + user);
        System.out.println("files = " + files);
        for (MultipartFile file : files) {
            System.out.println("original file nam = " + file.getOriginalFilename());
            System.out.println("file name = " + file.getName());
            System.out.println("content type = " + file.getContentType());
            System.out.println("size = " + file.getSize());
            try {
                System.out.println("bytes = " + Arrays.toString(file.getBytes()));

            } catch (IOException ignored) {

            }
            System.out.println("-----------------");
        }
        List<String> urls = new ArrayList<>();
        Optional<Room> optional = roomRepository.findById(roomId);
        if (optional.isPresent()) {
            if (! files.isEmpty()) {
                String url = s3Service.uploadFile(files.get(0));
                urls.add(url);
            }
            return ResponseEntity.ok(urls);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * đổi ảnh đại diện nhóm mobile
     */
    @PostMapping(value = "/changeImage/{roomId}", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh nhóm")
    public ResponseEntity<?> changeImageForMobile(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId, @RequestParam List<MultipartFile> file) {
        return changeImage(user, roomId, file);
    }

    /**
     * endpoint lấy tin nhắn cuối theo roomId, nếu cần
     */
    @GetMapping("/lastMessage/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tin nhắn cuối, hiện tại chưa cần")
    public ResponseEntity<?> getLastMessage(@PathVariable String roomId, @AuthenticationPrincipal User user) {
        var lastMessage = messageRepository.getLastMessageOfRoom(roomId);
        if (lastMessage != null && messageService.checkPermissionToSeeMessage(lastMessage.getId(), user.getId())) {
            return ResponseEntity.ok(messageMapper.toMessageDto(lastMessage));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * tạo room mới
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tạo room chat mới")
    public ResponseEntity<?> createNewRoom(@RequestBody Room room, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (room.getMembers() == null || room.getMembers().isEmpty())
            throw new MyException("Chưa có thành viên");
        room.setId(null);
        // xóa thành viên không tồn tại và user hiện tại nếu có
        room.getMembers().removeIf(x -> x.getUserId().equals(user.getId()) || ! userRepository.existsById(x.getUserId()));
        if (room.getType().equals(RoomType.ONE)) {
            room.setName(null);
            room.setImageUrl(null);
            if (room.getMembers().size() != 1) {
                throw new MyException("Danh sách thành viên không hợp lệ");
            }
            List<String> memberIds = room.getMembers().stream().map(Member::getUserId)
                    .collect(Collectors.toList());
            var existRoom = roomRepository.findCommonRoomBetween(user.getId(), memberIds.get(0));
            if (existRoom != null) {
                Optional<Inbox> inboxOptional = inboxRepository.findByOfUserIdAndRoomId(user.getId(), existRoom.getId());
                if (inboxOptional.isPresent())
                    return ResponseEntity.ok(inboxMapper.toInboxDto(inboxOptional.get()));
                var inbox = Inbox.builder().ofUserId(user.getId()).roomId(existRoom.getId()).build();
                inboxRepository.save(inbox);
                return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
            }
            room.getMembers().add(Member.builder().userId(user.getId()).build());
            roomRepository.save(room);
            var inbox = Inbox.builder().ofUserId(user.getId()).roomId(room.getId()).build();
            inboxRepository.save(inbox);
            return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
        }
        if (room.getType().equals(RoomType.GROUP)) {
            for (Member m : room.getMembers()) {
                m.setAddByUserId(user.getId());
                m.setAddTime(new Date());
                m.setIsAdmin(false);
            }
            room.setCreateByUserId(user.getId());
            // thêm người dùng hiện tại vào nhóm
            room.getMembers().add(Member.builder().userId(user.getId()).isAdmin(true).build());
            roomRepository.save(room);
            var inbox = Inbox.builder().ofUserId(user.getId()).roomId(room.getId()).build();
            inboxRepository.save(inbox);
            return ResponseEntity.ok(inboxMapper.toInboxDto(inbox));
        }
        return ResponseEntity.badRequest().build();
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
        if (! userRepository.existsById(memberId))
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
    public ResponseEntity<?> deleteMember(@PathVariable String roomId, @PathVariable String memberId, @AuthenticationPrincipal User user) {
        if (roomService.deleteMember(memberId, roomId, user.getId()))
            return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().build();
    }

    /**
     * thêm thành viên cho nhóm
     */
    @PostMapping("/members/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thêm thành viên vào nhóm chat")
    public ResponseEntity<?> addMemberToRoom(@PathVariable String roomId, @RequestBody List<Member> members, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> roomOptional = roomRepository.findById(roomId);
        if (roomOptional.isEmpty())
            throw new RoomNotFoundException();
        var room = roomOptional.get();
        if (room.getType().equals(RoomType.ONE))
            throw new MyException("Không thể thêm thành viên. Vui lòng tạo nhóm mới");
        members.removeIf(x -> x.getUserId().equals(user.getId()) || ! userRepository.existsById(x.getUserId()));
        for (Member m : members) {
            m.setAddByUserId(user.getId());
            m.setAddTime(new Date());
            m.setIsAdmin(false);
        }
        roomService.addMembersToRoom(members, roomId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/commonGroup/count/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy số lượng nhóm chung giữa hai người")
    public ResponseEntity<?> countCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(roomRepository.countCommonGroupBetween(user.getId(), anotherUserId));
    }

    @GetMapping("/commonGroup/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách nhóm chung giữa hai người")
    public ResponseEntity<?> getCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        List<Room> commonGroups = roomRepository.findCommonGroupBetween(user.getId(), anotherUserId);
        List<Optional<Inbox>> inboxs = commonGroups.stream()
                .map(x -> inboxRepository.findByOfUserIdAndRoomId(user.getId(), x.getId()))
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
