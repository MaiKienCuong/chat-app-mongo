package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestReceivedDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestSentDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendRequestService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend-request")
@CrossOrigin("${spring.security.cross_origin}")
public class FriendRequestRest {

    @Autowired
    private FriendRequestService friendRequestService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private ChatSocketService chatSocketService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RoomService roomService;

    private static final Logger logger = Logger.getLogger(FriendRequestRest.class.getName());

    /**
     * lấy tất cả lời mời kết bạn đã nhận được
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tất cả lời mời kết bạn đã nhận được")
    public ResponseEntity<?> getAllFriendRequestReceived(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        logger.log(Level.INFO, "get all friend request: page = {0}, size = {1}",
                new Object[]{ pageable.getPageNumber(), pageable.getPageSize() });
        Page<FriendRequest> friendRequestPage = friendRequestService.getAllFriendRequestReceived(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestReceivedDto(friendRequestPage));
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đếm số lời mời kết bạn đã nhận được")
    public ResponseEntity<?> countFriendRequestReceived(@ApiIgnore @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(friendRequestService.countFriendRequestReceived(user.getId()));
    }

    @GetMapping("/count/sent")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đếm số lời mời kết bạn đã gửi")
    public ResponseEntity<?> countFriendRequestSent(@ApiIgnore @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(friendRequestService.countFriendRequestSent(user.getId()));
    }

    /**
     * lấy tất cả lời mời kết bạn đã gửi đi
     */
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tất cả lời mời kết bạn đã gửi đi")
    public ResponseEntity<?> getAllFriendRequestSent(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        logger.log(Level.INFO, "get all friend request sent: page = {0}, size = {1}",
                new Object[]{ pageable.getPageNumber(), pageable.getPageSize() });
        Page<FriendRequest> friendRequestPage = friendRequestService.getAllFriendRequestSent(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestSentDto(friendRequestPage));
    }

    /**
     * gửi lời kết bạn đến một người khác
     */
    @PostMapping("/{toId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Gửi lời mời kết bạn đến người khác")
    public ResponseEntity<?> addNewFriendRequest(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String toId) {
        if (user == null)
            throw new UnAuthenticateException();
        // gửi đến chính mình
        if (toId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // gửi đến người không tồn tại trong database
        if (! userDetailService.existsById(toId))
            return ResponseEntity.badRequest().build();
        // hai người đã là bạn bè
        if (friendService.isFriend(user.getId(), toId))
            return ResponseEntity.badRequest().build();
        // chưa có lời mời kết bạn nào trong database
        if (! friendRequestService.isSent(user.getId(), toId) && ! friendRequestService.isReceived(user.getId(), toId)) {
            var friendRequest = FriendRequest.builder()
                    .fromId(user.getId())
                    .toId(toId)
                    .build();
            friendRequestService.save(friendRequest);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();

    }

    /**
     * chấp nhận lời mời kết bạn
     */
    @PutMapping("/{idToAccept}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Chấp nhận lời mời kết bạn")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable String idToAccept,
                                                 @ApiIgnore @AuthenticationPrincipal User user,
                                                 Locale locale) {
        if (user == null)
            throw new UnAuthenticateException();
        // hai người đã là bạn bè
        if (friendService.isFriend(user.getId(), idToAccept))
            return ResponseEntity.badRequest().build();
        // người không tồn tại trong database
        if (! userDetailService.existsById(idToAccept))
            return ResponseEntity.badRequest().build();
        // có lời mời từ người đó trong database
        if (friendRequestService.isReceived(user.getId(), idToAccept)) {
            friendRequestService.deleteFriendRequest(idToAccept, user.getId());
            // lưu 2 record trong database
            friendService.save(Friend.builder().userId(user.getId()).friendId(idToAccept).build());
            friendService.save(Friend.builder().userId(idToAccept).friendId(user.getId()).build());

            /*
            gửi tin nhắn hệ thống thông báo sau khi kết bạn
             */
            new Thread(() -> {
                Set<Member> members = new HashSet<>();
                members.add(Member.builder().userId(user.getId()).build());
                members.add(Member.builder().userId(idToAccept).build());
                var room = Room.builder()
                        .type(RoomType.ONE)
                        .members(members)
                        .build();
                roomService.save(room);
                String content = messageSource.getMessage("message_after_accept_friend", null, locale);
                var message = Message.builder()
                        .type(MessageType.SYSTEM)
                        .content(content)
                        .roomId(room.getId())
                        .createAt(new Date())
                        .build();
                chatSocketService.sendSystemMessage(message, room);
            }).start();
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * hủy lời mời kết bạn
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thu hồi lại lời mời kết bạn đã gửi hoặc xóa lời mời đã nhận")
    public ResponseEntity<?> deleteFriendRequest(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String deleteId) {
        if (user == null)
            throw new UnAuthenticateException();
        // hủy kết bạn với chính mình
        if (deleteId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // người dùng không tồn tại trong database
        if (! userDetailService.existsById(deleteId))
            return ResponseEntity.badRequest().build();
        // chỉ xóa khi đã gửi lời mời đến người này
        if (friendRequestService.isSent(user.getId(), deleteId))
            friendRequestService.deleteFriendRequest(user.getId(), deleteId);
        // xóa lời mời đã nhận được
        if (friendRequestService.isReceived(user.getId(), deleteId))
            friendRequestService.deleteFriendRequest(deleteId, user.getId());
        return ResponseEntity.ok().build();
    }

    /**
     *
     */
    private Page<?> toFriendRequestReceivedDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestReceivedDto> dto = content.stream()
                .map(x -> friendMapper.toFriendRequestReceived(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

    /**
     *
     */
    private Page<?> toFriendRequestSentDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestSentDto> dto = content.stream()
                .map(x -> friendMapper.toFriendRequestSent(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

}
