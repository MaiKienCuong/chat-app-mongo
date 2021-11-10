package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.Contact;
import iuh.dhktpm14.cnm.chatappmongo.dto.ContactSync;
import iuh.dhktpm14.cnm.chatappmongo.dto.FriendDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.Friend;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.mapper.FriendMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendRequestService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/friends")
@CrossOrigin("${spring.security.cross_origin}")
public class FriendRest {

    @Autowired
    private FriendService friendService;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendRequestService friendRequestService;

    @Autowired
    private MessageSource messageSource;

    /**
     * lấy danh sách bạn bè của người dùng hiện tại đã đăng nhập
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy danh sách bạn bè")
    public ResponseEntity<?> getAllFriendOfCurrentUser(@ApiIgnore @AuthenticationPrincipal User user,
                                                       Pageable pageable,
                                                       @RequestParam Optional<String> query) {
        log.info("get all friend of userId = {}, page = {}, size = {}", user.getId(), pageable.getPageNumber(), pageable.getPageSize());
        log.info("query = {}", query);
        if (query.isPresent() && ! query.get().trim().isEmpty()) {
            Page<Friend> friendDtoPage = friendService.findByUsernameOrPhoneOrDisplayNameRegex(user.getId(), query.get(), pageable);
            return ResponseEntity.ok(toFriendDto(friendDtoPage));
        }
        Page<Friend> friendPage = friendService.getAllFriendOfUser(user.getId(), pageable);

        return ResponseEntity.ok(toFriendDto(friendPage));
    }

    /**
     * xóa bạn bè
     */
    @DeleteMapping("/{deleteId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Xóa bạn bè")
    public ResponseEntity<?> deleteFriend(@ApiIgnore @AuthenticationPrincipal User user,
                                          @PathVariable String deleteId,
                                          Locale locale) {
        log.info("delete friend between userId = {} and userId = {}", user.getId(), deleteId);
        // xóa bạn bè với chính mình
        if (deleteId.equals(user.getId())) {
            log.error("can not delete friend with myself");
            return ResponseEntity.badRequest().build();
        }
        // deleteId không tồn tại trong database
        if (! userDetailService.existsById(deleteId)) {
            String message = messageSource.getMessage("user_not_found", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        // chỉ xóa khi hai người là bạn bè
        if (friendService.isFriend(user.getId(), deleteId)) {
            log.info("deleting friend in database");
            friendService.deleteFriend(user.getId(), deleteId);
            return ResponseEntity.ok().build();
        }
        log.error("userId = {} and userId = {} are not friend", user.getId(), deleteId);
        return ResponseEntity.badRequest().build();
    }

    @PostMapping(value = "/syncContact")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đồng bộ danh bạ")
    public ResponseEntity<?> syncContactForMobile(@ApiIgnore @AuthenticationPrincipal User user, @RequestBody List<Contact> contacts) {
        log.info("sync contact of userId = {}", user.getId());
        log.info("contacts = {}", contacts);
        List<ContactSync> contactSyncs = new ArrayList<>();
        if (contacts != null && ! contacts.isEmpty()) {
            for (Contact contact : contacts) {
                Optional<User> userOptional = userDetailService.findDistinctByPhoneNumber(contact.getPhone());
                if (userOptional.isPresent()) {
                    var u = userOptional.get();

                    var contactSync = ContactSync.builder()
                            .user(userMapper.toUserProfileDto(u))
                            .name(contact.getName())
                            .phone(contact.getPhone())
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
