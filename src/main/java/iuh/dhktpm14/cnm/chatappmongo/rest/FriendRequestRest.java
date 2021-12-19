package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.entity.Member;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.Room;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoomType;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendRequestService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendRequestSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendService;
import iuh.dhktpm14.cnm.chatappmongo.service.RoomService;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    private FriendRequestSocketService friendRequestSocketService;

    @Autowired
    private UserMapper userMapper;

    /**
     * lấy tất cả lời mời kết bạn đã nhận được
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tất cả lời mời kết bạn đã nhận được")
    public ResponseEntity<?> getAllFriendRequestReceived(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        log.info("get all friend request received of userId = {}, page = {}, size = {}", user.getId(),
                pageable.getPageNumber(), pageable.getPageSize());
        Page<FriendRequest> friendRequestPage = friendRequestService.getAllFriendRequestReceived(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestDto(friendRequestPage));
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đếm số lời mời kết bạn đã nhận được")
    public ResponseEntity<?> countFriendRequestReceived(@ApiIgnore @AuthenticationPrincipal User user) {
        int count = friendRequestService.countFriendRequestReceived(user.getId());
        log.info("count friend request received of userId = {}, count = {}", user.getId(), count);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/sent")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đếm số lời mời kết bạn đã gửi")
    public ResponseEntity<?> countFriendRequestSent(@ApiIgnore @AuthenticationPrincipal User user) {
        int count = friendRequestService.countFriendRequestSent(user.getId());
        log.info("count friend request sent of userId = {}, count = {}", user.getId(), count);
        return ResponseEntity.ok(count);
    }

    /**
     * lấy tất cả lời mời kết bạn đã gửi đi
     */
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy tất cả lời mời kết bạn đã gửi đi")
    public ResponseEntity<?> getAllFriendRequestSent(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        log.info("get all friend request sent of userId = {}, page = {}, size = {}", user.getId(),
                pageable.getPageNumber(), pageable.getPageSize());
        Page<FriendRequest> friendRequestPage = friendRequestService.getAllFriendRequestSent(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestDto(friendRequestPage));
    }

    /**
     * gửi lời kết bạn đến một người khác
     */
    @PostMapping("/{toId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Gửi lời mời kết bạn đến người khác")
    public ResponseEntity<?> addNewFriendRequest(@ApiIgnore @AuthenticationPrincipal User user,
                                                 @PathVariable String toId,
                                                 Locale locale) {
        log.info("add new friend request from userId = {}, to userId = {}", user.getId(), toId);
        // gửi đến chính mình
        if (toId.equals(user.getId())) {
            log.error("can not send to myself");
            return ResponseEntity.badRequest().build();
        }
        // gửi đến người không tồn tại trong database
        if (! userDetailService.existsById(toId)) {
            String message = messageSource.getMessage("user_not_found", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        // hai người đã là bạn bè
        if (friendService.isFriend(user.getId(), toId)) {
            log.error("userId = {} and userId = {} are already friends", user.getId(), toId);
            return ResponseEntity.badRequest().build();
        }
        // nếu chưa có lời mời kết bạn nào trong database
        if (! friendRequestService.isSent(user.getId(), toId) && ! friendRequestService.isReceived(user.getId(), toId)) {
            var friendRequest = FriendRequest.builder()
                    .fromId(user.getId())
                    .createAt(new Date())
                    .toId(toId)
                    .build();
            log.info("saving friend request to database");
            friendRequestService.save(friendRequest);
            boolean received = friendRequestSocketService.sendFriendRequestReceived(friendRequest);
            if (received)
                log.info("send friend request received via socket successfully");
            else
                log.error("send friend request received via socket fail");
            return ResponseEntity.ok().build();
        }
        log.error("friend request is already in database");
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
        log.info("accepting friend request of userId = {} to me", idToAccept);
        // hai người đã là bạn bè
        if (friendService.isFriend(user.getId(), idToAccept)) {
            log.error("userId = {} and userId = {} are already friends", user.getId(), idToAccept);
            return ResponseEntity.badRequest().build();
        }
        // người không tồn tại trong database
        if (! userDetailService.existsById(idToAccept)) {
            String message = messageSource.getMessage("user_not_found", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        // có lời mời từ người đó trong database
        if (friendRequestService.isReceived(user.getId(), idToAccept)) {
            log.info("accepted friend request of userId = {} to me, deleting friend request in database",
                    idToAccept);
            Optional<FriendRequest> friendRequestOptional = friendRequestService.findByFromIdAndToId(idToAccept, user.getId());
            if (friendRequestOptional.isPresent()) {
                boolean accept = friendRequestSocketService.sendFriendRequestAccept(friendRequestOptional.get());
                if (accept)
                    log.info("send friend request accept via socket successfully");
                else
                    log.error("send friend request accept via socket fail");
                friendRequestService.deleteFriendRequest(idToAccept, user.getId());
            }

            // lưu 2 record trong database
            log.info("save friend to database");
            friendService.save(Friend.builder().userId(user.getId()).friendId(idToAccept).createAt(new Date()).build());
            friendService.save(Friend.builder().userId(idToAccept).friendId(user.getId()).createAt(new Date()).build());

            /*
            gửi tin nhắn hệ thống thông báo sau khi kết bạn
             */
            String content = messageSource.getMessage("message_after_accept_friend", null, locale);
            sendMessageAfterCreateFriend(user, idToAccept, content);
            return ResponseEntity.ok().body(userMapper.toUserProfileDto(idToAccept));
        }
        String message = messageSource.getMessage("no_friend_request_in_database", null, locale);
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    private void sendMessageAfterCreateFriend(User user, String idToAccept, String content) {
        Set<Member> members = new HashSet<>();
        members.add(Member.builder().userId(user.getId()).build());
        members.add(Member.builder().userId(idToAccept).build());
        var room = roomService.findCommonRoomBetween(user.getId(), idToAccept);
        if (room == null) {
            room = Room.builder()
                    .type(RoomType.ONE)
                    .members(members)
                    .build();
            roomService.save(room);
        }
        var message = Message.builder()
                .type(MessageType.SYSTEM)
                .content(content)
                .createAt(new Date())
                .roomId(room.getId())
                .build();
        chatSocketService.sendSystemMessage(message, room);
    }

    /**
     * hủy lời mời kết bạn
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Thu hồi lại lời mời kết bạn đã gửi hoặc xóa lời mời đã nhận")
    public ResponseEntity<?> deleteFriendRequest(@ApiIgnore @AuthenticationPrincipal User user,
                                                 @PathVariable String deleteId,
                                                 Locale locale) {
        log.info("recall friend request");
        // hủy kết bạn với chính mình
        if (deleteId.equals(user.getId())) {
            log.error("can not recall friend request with myself");
            return ResponseEntity.badRequest().build();
        }
        // người dùng không tồn tại trong database
        if (! userDetailService.existsById(deleteId)) {
            String message = messageSource.getMessage("user_not_found", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        // chỉ xóa khi đã gửi lời mời đến người này
        if (friendRequestService.isSent(user.getId(), deleteId)) {
            log.info("deleting sent request");
            Optional<FriendRequest> friendRequestOptional = friendRequestService.findByFromIdAndToId(user.getId(), deleteId);
            if (friendRequestOptional.isPresent()) {
                friendRequestService.deleteFriendRequest(user.getId(), deleteId);
                boolean recall = friendRequestSocketService.sendFriendRequestRecall(friendRequestOptional.get());
                if (recall)
                    log.info("recall friend request via socket successfully");
                else
                    log.error("recall friend request via socket fail");
            }
        }
        // xóa lời mời đã nhận được
        if (friendRequestService.isReceived(user.getId(), deleteId)) {
            log.info("deleting received request");
            Optional<FriendRequest> friendRequestOptional = friendRequestService.findByFromIdAndToId(deleteId, user.getId());
            if (friendRequestOptional.isPresent()) {
                friendRequestService.deleteFriendRequest(deleteId, user.getId());
                boolean delete = friendRequestSocketService.sendFriendRequestDelete(friendRequestOptional.get());
                if (delete)
                    log.info("delete friend request via socket successfully");
                else
                    log.error("delete friend request via socket fail");
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     *
     */
    private Page<?> toFriendRequestDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestDto> dto = content.stream()
                .map(x -> friendMapper.toFriendRequestDto(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

}
