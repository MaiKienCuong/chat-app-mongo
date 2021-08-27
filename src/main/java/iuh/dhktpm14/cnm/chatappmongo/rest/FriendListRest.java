package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.FriendDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendListRest {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * lấy danh sách bạn bè
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFriendOfCurrentUser(@AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<Friend> friendPage = friendRepository.findAllByUserIdOrderByCreateAtDesc(user.getId(), pageable);

        return ResponseEntity.ok(toFriendDto(friendPage));
    }

    /**
     * thêm bạn bè
     */
    @PostMapping("/{toId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addNewFriend(@AuthenticationPrincipal User user, @PathVariable String toId) {
        if (user == null)
            throw new UnAuthenticateException();
        if (toId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        if (! userRepository.existsById(toId))
            return ResponseEntity.badRequest().build();
        if (! friendRepository.existsByUserIdAndFriendId(user.getId(), toId)) {
            var friend = Friend.builder()
                    .userId(user.getId())
                    .friendId(toId)
                    .build();
            friendRepository.save(friend);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * xóa bạn bè
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFriend(@AuthenticationPrincipal User user, @PathVariable String deleteId) {
        if (user == null)
            throw new UnAuthenticateException();
        friendRepository.deleteByUserIdAndFriendId(user.getId(), deleteId);
        return ResponseEntity.ok().build();
    }

    private Page<?> toFriendDto(Page<Friend> friendPage) {
        List<Friend> content = friendPage.getContent();
        List<FriendDto> dto = content.stream().map(x -> friendMapper.toFriendDto(x)).collect(Collectors.toList());
        return new PageImpl<>(dto, friendPage.getPageable(), friendPage.getTotalElements());
    }

}
