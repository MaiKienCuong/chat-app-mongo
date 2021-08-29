package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestReceivedDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendRequestSentDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.FriendRequest;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRequestRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friend-request")
@CrossOrigin("${spring.security.cross_origin}")
public class FriendRequestRest {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendMapper friendMapper;

    /**
     * lấy tất cả lời mời kết bạn đã nhận được
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFriendRequestReceived(@AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<FriendRequest> friendRequestPage = friendRequestRepository.getAllFriendRequestReceived(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestReceivedDto(friendRequestPage));
    }

    /**
     * lấy tất cả lời mời kết bạn đã gửi đi
     */
    @GetMapping("/sent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFriendRequestSent(@AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<FriendRequest> friendRequestPage = friendRequestRepository.getAllFriendRequestSent(user.getId(), pageable);

        return ResponseEntity.ok(toFriendRequestSentDto(friendRequestPage));
    }

    /**
     * gửi lời kết bạn đến một người khác
     */
    @PostMapping("/{toId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addNewFriendRequest(@AuthenticationPrincipal User user, @PathVariable String toId) {
        if (user == null)
            throw new UnAuthenticateException();
        // gửi đến chính mình
        if (toId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // gửi đến người không tồn tại trong database
        if (! userRepository.existsById(toId))
            return ResponseEntity.badRequest().build();
        // hai người đã là bạn bè
        if (friendRepository.isFriend(user.getId(), toId))
            return ResponseEntity.badRequest().build();
        // chưa có lời mời kết bạn nào trong database
        if (! friendRequestRepository.isSent(user.getId(), toId) && ! friendRequestRepository.isReceived(user.getId(), toId)) {
            var friendRequest = FriendRequest.builder()
                    .fromId(user.getId())
                    .toId(toId)
                    .build();
            friendRequestRepository.save(friendRequest);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();

    }

    /**
     * chấp nhận lời mời kết bạn
     */
    @PutMapping("/{idToAccept}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable String idToAccept, @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        // hai người đã là bạn bè
        if (friendRepository.isFriend(user.getId(), idToAccept))
            return ResponseEntity.badRequest().build();
        // người không tồn tại trong database
        if (! userRepository.existsById(idToAccept))
            return ResponseEntity.badRequest().build();
        // có lời mời từ người đó trong database
        if (friendRequestRepository.isReceived(user.getId(), idToAccept)) {
            friendRequestRepository.deleteFriendRequest(idToAccept, user.getId());
            // lưu 2 record trong database
            friendRepository.save(Friend.builder().userId(user.getId()).friendId(idToAccept).build());
            friendRepository.save(Friend.builder().userId(idToAccept).friendId(user.getId()).build());

            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * hủy lời mời kết bạn
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFriendRequest(@AuthenticationPrincipal User user, @PathVariable String deleteId) {
        if (user == null)
            throw new UnAuthenticateException();
        // hủy kết bạn với chính mình
        if (deleteId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // người dùng không tồn tại trong database
        if (! userRepository.existsById(deleteId))
            return ResponseEntity.badRequest().build();
        // chỉ xóa khi đã gửi lời mời đến người này
        if (friendRequestRepository.isSent(user.getId(), deleteId)) {
            friendRequestRepository.deleteFriendRequest(user.getId(), deleteId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     *
     */
    private Page<?> toFriendRequestReceivedDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestReceivedDto> dto = content.stream().map(x -> friendMapper.toFriendRequestReceived(x)).collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

    /**
     *
     */
    private Page<?> toFriendRequestSentDto(Page<FriendRequest> friendRequestPage) {
        List<FriendRequest> content = friendRequestPage.getContent();
        List<FriendRequestSentDto> dto = content.stream().map(x -> friendMapper.toFriendRequestSent(x)).collect(Collectors.toList());
        return new PageImpl<>(dto, friendRequestPage.getPageable(), friendRequestPage.getTotalElements());
    }

}
