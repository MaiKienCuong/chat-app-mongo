package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.MemberDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.MyException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.RoomNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.MemberMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.RoomMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.MessageRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.ReadTrackingRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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

    /**
     * endpoint lấy số tin nhắn mới theo roomId, nếu cần
     */
    @GetMapping("/{roomId}/count-new-message")
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
    @GetMapping("/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chi tiết cuộc trò chuyện: Lấy danh sách thành viên")
    public ResponseEntity<?> getAllMembers(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String roomId) {
        if (user == null)
            throw new UnAuthenticateException();
        Optional<Room> optional = roomRepository.findById(roomId);
        if (optional.isPresent()) {
            var room = optional.get();
            Set<Member> members = room.getMembers();
            for (Member m : members) {
                // nếu là thành viên trong room mới xem được thông tin
                if (m.getUserId().equals(user.getId())) {
                    Set<MemberDto> dto = members.stream()
                            .map(x -> memberMapper.toMemberDto(x))
                            .collect(Collectors.toSet());
                    return ResponseEntity.ok(dto);
                }
            }
        }
        return ResponseEntity.badRequest().build();
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
            for (Member m : room.getMembers()) {
                // nếu là thành viên trong room mới xem được thông tin
                if (m.getUserId().equals(user.getId())) {
                    return ResponseEntity.ok(roomMapper.toRoomDetailDto(roomId));
                }
            }
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * endpoint lấy tin nhắn cuối theo roomId, nếu cần
     */
    @GetMapping("/{roomId}/last-message")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tin nhắn cuối, hiện tại chưa cần")
    public ResponseEntity<?> getLastMessage(@PathVariable String roomId) {
        return ResponseEntity.ok(messageRepository.getLastMessageOfRoom(roomId));
    }

    /**
     * tạo nhóm mới
     */
    @PostMapping("/group")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tạo nhóm chat mới")
    public ResponseEntity<?> createNewGroup(@RequestBody Room room, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        room.setCreateByUserId(user.getId());
        room.setType(RoomType.GROUP);
        if (room.getMembers() == null || room.getMembers().isEmpty())
            throw new MyException("Chưa có thành viên");
        for (Member m : room.getMembers()) {
            if (! m.getUserId().equals(user.getId())) {
                m.setAddByUserId(user.getId());
                m.setAddTime(new Date());
            }
        }
        // thêm người dùng hiện tại vào nhóm
        room.getMembers().add(Member.builder().userId(user.getId()).build());
        return ResponseEntity.ok(roomRepository.save(room));
    }

    /**
     * thêm thành viên cho nhóm
     */
    @PostMapping("/group/{roomId}")
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
        for (Member m : members) {
            if (! m.getUserId().equals(user.getId())) {
                m.setAddByUserId(user.getId());
                m.setAddTime(new Date());
            }
        }
        room.getMembers().addAll(members);
        return ResponseEntity.ok(roomRepository.save(room));
    }

    @GetMapping("/common-group/count/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy số lượng nhóm chung giữa hai người")
    public ResponseEntity<?> countCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(roomRepository.countCommonGroupBetween(user.getId(), anotherUserId));
    }

    @GetMapping("/common-group/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách nhóm chung giữa hai người")
    public ResponseEntity<?> getCommonGroup(@PathVariable String anotherUserId, @ApiIgnore @AuthenticationPrincipal User user) {
        if (user == null)
            throw new UnAuthenticateException();
        if (anotherUserId == null)
            return ResponseEntity.badRequest().build();
        List<Room> commonGroups = roomRepository.findCommonGroupBetween(user.getId(), anotherUserId);
        List<Object> roomSummaryList = commonGroups.stream().map(roomMapper::toRoomSummaryDto).collect(Collectors.toList());
        return ResponseEntity.ok(roomSummaryList);
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
