package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.ChangePasswordDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserUpdateDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ViewProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.FriendStatus;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendRequestService;
import iuh.dhktpm14.cnm.chatappmongo.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/user")
@CrossOrigin("${spring.security.cross_origin}")
public class UserRest {

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AmazonS3Service s3Service;

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestService friendRequestService;

    @GetMapping("/email")
    public ResponseEntity<?> existEmail(@RequestBody String email, Locale locale) {
        log.info("checking email = {}", email);
        if (userDetailService.existsByEmail(email)) {
            String message = messageSource.getMessage("email_exists", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        return null;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy thông tin về người dùng hiện tại")
    public ResponseEntity<?> getCurrentUser(@ApiIgnore @AuthenticationPrincipal User user) {
        log.info("getting current user info");
        return ResponseEntity.ok(userMapper.toUserDetailDto(user));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Cập nhật thông tin user")
    public ResponseEntity<?> updateInformationUser(@ApiIgnore @AuthenticationPrincipal User user,
                                                   @Valid @RequestBody UserUpdateDto userUpdate) {
        log.info("userUpdate from client = {}", userUpdate);

        user.setEmail(userUpdate.getEmail());
        user.setDisplayName(userUpdate.getDisplayName());
        user.setGender(userUpdate.getGender());
        user.setDateOfBirth(userUpdate.getDateOfBirth());

        userDetailService.save(user);
        log.info("updated user");

        return ResponseEntity.ok(userMapper.toUserDetailDto(user));
    }

    @PutMapping(value = "/me", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Cập nhật thông tin user")
    public ResponseEntity<?> updateInformationUserForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                            @Valid UserUpdateDto userUpdate,
                                                            BindingResult result,
                                                            Locale locale) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(messageSource.getMessage(result.getFieldError(), locale)));
        }
        return updateInformationUser(user, userUpdate);
    }

    @PutMapping("/me/changePassword")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi mật khẩu")
    public ResponseEntity<?> changePassword(@ApiIgnore @AuthenticationPrincipal User user,
                                            @Valid @RequestBody ChangePasswordDto passwordDto,
                                            Locale locale) {
        log.info("change password for userId = {}", user.getId());
        String message;
        Optional<User> userOptional = userDetailService.findById(user.getId());
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (passwordEncoder.matches(passwordDto.getOldPass(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(passwordDto.getNewPass()));
                userDetailService.save(user);
                message = messageSource.getMessage("changePass_success", null, locale);
                return ResponseEntity.ok(new MessageResponse(message));
            }
            message = messageSource.getMessage("oldPass_incorrect", null, locale);
            log.error(message);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        message = messageSource.getMessage("changePass_failed", null, locale);
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    @PutMapping(value = "/me/changePassword", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi mật khẩu")
    public ResponseEntity<?> changePasswordForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                     @Valid ChangePasswordDto passwordDto,
                                                     Locale locale,
                                                     BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(messageSource.getMessage(result.getFieldError(), locale)));
        }
        return changePassword(user, passwordDto, locale);
    }

    @PutMapping(value = "/me/changeImage")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh đại diện")
    public ResponseEntity<?> changeImage(@ApiIgnore @AuthenticationPrincipal User user,
                                         MultipartFile files,
                                         Locale locale) {
        log.info("change image for userId = {}", user.getId());
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
        String newImageUrl = s3Service.uploadFile(files);
        user.setImageUrl(newImageUrl);
        userDetailService.save(user);
        log.info("change image success");
        return ResponseEntity.ok(userMapper.toUserDetailDto(user));
    }

    @PutMapping(value = "/me/changeImage", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh đại diện")
    public ResponseEntity<?> changeImageForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                  MultipartFile files,
                                                  Locale locale) {
        return changeImage(user, files, locale);
    }

    @PostMapping("/searchPhone")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tìm kiếm user theo tên gần đúng")
    public ResponseEntity<?> searchUser(@ApiIgnore @AuthenticationPrincipal User user, @RequestParam String textToSearch) {
        Optional<User> userOptional = userDetailService.findDistinctByPhoneNumber(textToSearch);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(List.of(userOptional.map(userMapper::toUserProfileDto).get()));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping(value = "/searchPhone", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tìm kiếm user theo tên gần đúng")
    public ResponseEntity<?> searchUserForMobile(@ApiIgnore @AuthenticationPrincipal User user, String textToSearch) {
        return searchUser(user, textToSearch);
    }

    @GetMapping("/viewProfile/{anotherUserId}")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Xem trang cá nhân của người khác")
    public ResponseEntity<?> viewProfile(@ApiIgnore @AuthenticationPrincipal User user,
                                         @PathVariable String anotherUserId,
                                         Locale locale) {
        log.info("userId = {} viewing profile of userId = {}", user.getId(), anotherUserId);
        Optional<User> userOptional = userDetailService.findById(anotherUserId);
        if (userOptional.isPresent()) {
            var friendStatus = FriendStatus.NONE;
            if (friendService.isFriend(user.getId(), anotherUserId))
                friendStatus = FriendStatus.FRIEND;
            else if (friendRequestService.isSent(user.getId(), anotherUserId))
                friendStatus = FriendStatus.SENT;
            else if (friendRequestService.isReceived(user.getId(), anotherUserId))
                friendStatus = FriendStatus.RECEIVED;
            log.info("friend status = {}", friendStatus);
            ViewProfileDto viewProfile = ViewProfileDto.builder()
                    .user(userMapper.toUserProfileDto(userOptional.get()))
                    .friendStatus(friendStatus)
                    .build();
            return ResponseEntity.ok(viewProfile);
        }
        String message = messageSource.getMessage("user_not_found", null, locale);
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

}
