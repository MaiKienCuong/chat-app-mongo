package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.Contact;
import iuh.dhktpm14.cnm.chatappmongo.dto.ContactSync;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.repository.FriendRepository;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin("${spring.security.cross_origin}")
public class FriendRest {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * lấy danh sách bạn bè của người dùng hiện tại đã đăng nhập
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách bạn bè")
    public ResponseEntity<?> getAllFriendOfCurrentUser(@ApiIgnore @AuthenticationPrincipal User user, Pageable pageable) {
        if (user == null)
            throw new UnAuthenticateException();
        Page<Friend> friendPage = friendRepository.getAllFriendOfUser(user.getId(), pageable);

        return ResponseEntity.ok(toFriendDto(friendPage));
    }

    /**
     * xóa bạn bè
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Xóa bạn bè")
    public ResponseEntity<?> deleteFriend(@ApiIgnore @AuthenticationPrincipal User user, @PathVariable String deleteId) {
        if (user == null)
            throw new UnAuthenticateException();
        // xóa bạn bè với chính mình
        if (deleteId.equals(user.getId()))
            return ResponseEntity.badRequest().build();
        // deleteId không tồn tại trong database
        if (! userRepository.existsById(deleteId))
            return ResponseEntity.badRequest().build();
        // chỉ xóa khi hai người là bạn bè
        if (friendRepository.isFriend(user.getId(), deleteId)) {
            friendRepository.deleteFriend(user.getId(), deleteId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(value = "/syncContact")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đồng bộ danh bạ")
    public ResponseEntity<?> syncContactForMobile(@ApiIgnore @AuthenticationPrincipal User user, @RequestBody List<Contact> contacts) {
        if (user == null)
            throw new UnAuthenticateException();
        List<ContactSync> contactSyncs = new ArrayList<>();
        if (contacts != null && ! contacts.isEmpty()) {
            for (Contact contact : contacts) {
                Optional<User> userOptional = userRepository.findDistinctByPhoneNumber(contact.getPhone());
                if (userOptional.isPresent()) {
                    var u = userOptional.get();
                    var contactSync = ContactSync.builder()
                            .user(userMapper.toUserProfileDto(u))
                            .name(contact.getName())
                            .phone(contact.getPhone())
                            .isFriend(friendRepository.isFriend(user.getId(), u.getId()))
                            .build();
                    contactSyncs.add(contactSync);
                }
            }
            contactSyncs.removeIf(x -> x.getUser().getId().equals(user.getId()));
        }
        return ResponseEntity.ok(contactSyncs);
    }

    /**
     *
     */
    private Page<?> toFriendDto(Page<Friend> friendPage) {
        List<Friend> content = friendPage.getContent();
        List<FriendDto> dto = content.stream()
                .map(x -> friendMapper.toFriendDto(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, friendPage.getPageable(), friendPage.getTotalElements());
    }

}
