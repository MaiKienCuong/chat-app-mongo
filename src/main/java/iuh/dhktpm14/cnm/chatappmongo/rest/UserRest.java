package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.ChangePasswordDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserUpdateDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.MyMedia;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.entity.UserReport;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserReportMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.UserReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.Date;
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
    private UserReportService userReportService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserReportMapper userReportMapper;

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
                                                   @Valid @RequestBody UserUpdateDto userUpdate,
                                                   Locale locale) {
        log.info("userUpdate from client = {}", userUpdate);

        if (userUpdate.getUsername() != null) {
            Optional<User> userOptional = userDetailService.findDistinctByUsername(userUpdate.getUsername());
            if (userOptional.isPresent()) {
                var existsUser = userOptional.get();
                if (! existsUser.getId().equals(user.getId())) {
                    String message = messageSource.getMessage("username_is_exists",
                            new Object[]{ userUpdate.getUsername() }, locale);
                    log.error(message);
                    return ResponseEntity.badRequest().body(new MessageResponse(message));
                } else {
                    user.setUsername(userUpdate.getUsername());
                }
            } else {
                user.setUsername(userUpdate.getUsername());
            }
        }

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
                                                            Locale locale) {
        return updateInformationUser(user, userUpdate, locale);
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
            var response = new MessageResponse(message);
            response.setField("oldPass");
            return ResponseEntity.badRequest().body(response);
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
                                                     Locale locale) {
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
        MyMedia media = s3Service.uploadFile(files);
        user.setImageUrl(media.getUrl());
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
        Optional<User> userOptional = userDetailService.findDistinctByPhoneNumberOrEmail(textToSearch);
        if (userOptional.isPresent() && ! user.getId().equals(userOptional.get().getId())) {
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
            return ResponseEntity.ok(userMapper.toViewProfileDto(userOptional.get()));
        }
        String message = messageSource.getMessage("user_not_found", null, locale);
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    @PostMapping("/report")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Báo cáo người dùng")
    public ResponseEntity<?> reportUser(@ApiIgnore @AuthenticationPrincipal User user,
                                        @RequestBody UserReport userReport,
                                        Locale locale) {
        if (userReport == null)
            return ResponseEntity.badRequest().build();
        if (userReport.getToId() == null)
            return ResponseEntity.badRequest().build();
        log.info("userId = {} report userId = {}", user.getId(), userReport.getToId());
        if (user.getId().equals(userReport.getToId()))
            return ResponseEntity.badRequest().build();
        if (! userDetailService.existsById(userReport.getToId()))
            return ResponseEntity.badRequest().build();
        userReport.setSeen(false);
        userReport.setFromId(user.getId());
        userReport.setCreateAt(new Date());
        if (userReport.getMessageId() != null && messageService.findById(userReport.getMessageId()).isEmpty()) {
            userReport.setMessageId(null);
        }
        userReportService.save(userReport);
        return ResponseEntity.ok(userReportMapper.toUserReportDto(userReport));
    }

}
