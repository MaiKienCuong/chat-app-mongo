package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.MonthDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.ResetPasswordDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserReportDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.AdminLog;
import iuh.dhktpm14.cnm.chatappmongo.entity.Message;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.entity.UserReport;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.MessageType;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoleType;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserReportMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.service.AdminLogService;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.ChatSocketService;
import iuh.dhktpm14.cnm.chatappmongo.service.MessageService;
import iuh.dhktpm14.cnm.chatappmongo.service.UserReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@CrossOrigin("${spring.security.cross_origin}")
public class AdminRest {

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserReportService userReportService;

    @Autowired
    private UserReportMapper userReportMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ChatSocketService chatSocketService;

    @GetMapping(value = "/list")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Lấy danh sách tất cả user")
    public ResponseEntity<?> getAllUser(@ApiIgnore @AuthenticationPrincipal User admin, Pageable pageable, @RequestParam String role) {
        log.info("admin with username = {} getting all user", admin.getUsername());
        if (role.equalsIgnoreCase("admin"))
            role = "ROLE_ADMIN";
        else if (role.equalsIgnoreCase("user"))
            role = "ROLE_USER";
        else
            return ResponseEntity.badRequest().body("Role does not exist");
        writeLogToDatabase(admin, admin, "getting all user with " + role);
        Page<User> findAll = userDetailService.findAllByRoles(role, pageable);
        return ResponseEntity.ok(toUserDto(findAll));
    }


    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Cập nhật thông tin người dùng")
    public ResponseEntity<?> updateInformationUser(@ApiIgnore @AuthenticationPrincipal User admin, @RequestBody User update) {
        log.info("admin = {} update information for user = {}", admin.getDisplayName(), update.getId());
        User user = userDetailService.findById(update.getId()).get();
        user.setImageUrl(update.getImageUrl());
        user.setEmail(update.getEmail());
        user.setDisplayName(update.getDisplayName());
        user.setPhoneNumber(update.getPhoneNumber());
        user.setGender(update.getGender());
        user.setDateOfBirth(update.getDateOfBirth());
        writeLogToDatabase(admin, user, "update information for user");
        return ResponseEntity.ok(userDetailService.save(user));
    }


    @PostMapping("/change_password")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Thay đổi mật khẩu cho người dùng")
    public ResponseEntity<?> ChangePasswordUser(@ApiIgnore @AuthenticationPrincipal User admin, @Valid @RequestBody ResetPasswordDto dto) {
        User user = userDetailService.findById(dto.getUserId()).get();
        if (user == null)
            return ResponseEntity.badRequest().body(new MessageResponse("User does not exist"));

        log.info("admin = {} change password for user = {}", admin.getDisplayName(), user.getId());
        writeLogToDatabase(admin, user, "change password for user");
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        return ResponseEntity.ok(userDetailService.save(user));
    }


    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Cấp tài khoản quyền admin mới")
    public ResponseEntity<?> createNewAdminAccount(@ApiIgnore @AuthenticationPrincipal User admin,
                                                   @RequestBody User user,
                                                   Locale locale) {
        log.info("licensor = {} , licensee = {}", admin.getUsername(), user.toString());
        if (user.getPassword() != null) {
            user.setId(null);
            user.setEnable(true);
            user.setBlock(false);
            user.setRoles(RoleType.ROLE_ADMIN.toString());
            writeLogToDatabase(admin, user, "update information for user");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return ResponseEntity.ok(userDetailService.save(user));
        }
        String passwordNotEmpty = messageSource.getMessage("password_not_empty", null, locale);
        log.error(passwordNotEmpty);
        return ResponseEntity.badRequest().body(new MessageResponse(passwordNotEmpty));
    }


    @PostMapping("/lock_account")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Khóa tài khoản user")
    public ResponseEntity<?> lockAccount(@ApiIgnore @AuthenticationPrincipal User admin, @RequestParam String userId) {
        User user = userDetailService.findById(userId).get();
        log.info("admin = {} , locked account = {}", admin.getDisplayName(), user.toString());
        user.setBlock(true);
        writeLogToDatabase(admin, user, "locked account user");
        return ResponseEntity.ok(userDetailService.save(user));

    }

    @PostMapping("/unlock_account")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Mở khóa tài khoản user")
    public ResponseEntity<?> unlockAccount(@ApiIgnore @AuthenticationPrincipal User admin, @RequestParam String userId) {
        User user = userDetailService.findById(userId).get();
        log.info("admin = {} , locked account = {}", admin.getDisplayName(), user.toString());
        user.setBlock(false);
        writeLogToDatabase(admin, user, "unlock account user");
        return ResponseEntity.ok(userDetailService.save(user));

    }


    @PostMapping("/users/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @ApiOperation("Tìm kiếm user theo tên hoặc số điện thoại gần đúng")
    public ResponseEntity<?> searchUser(@ApiIgnore @AuthenticationPrincipal User user, @RequestParam String textToSearch, Pageable pageable) {
        if (user.getRoles().equalsIgnoreCase("ROLE_USER")) {
            List<User> result = userDetailService.findAllByDisplayNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByDisplayNameAsc(textToSearch, textToSearch);

            if (result == null) {
                return ResponseEntity.ok(new ArrayList<>(0));
            }
            List<UserProfileDto> userProfiles = result.stream().filter(x -> ! x.getId().equals(user.getId()))
                    .map(userMapper::toUserProfileDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(userProfiles);
        }

        Page<User> result = userDetailService.findAllByDisplayNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByDisplayNameAsc(textToSearch, textToSearch, pageable);

        log.info("admin with username = {} finding user with key = {}", user.getUsername(), textToSearch);
        writeLogToDatabase(user, user, "finding user with key : " + textToSearch);
        return ResponseEntity.ok(toUserProfileDto(result));
    }


    @PostMapping("/total_sign_up")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Lấy số lượng đăng ký trong một khoảng thời gian")
    public Integer getNumberSignUpInMonth(@ApiIgnore @AuthenticationPrincipal User admin, @Valid @RequestBody MonthDto dto) {

        return userDetailService.findByCreateAtBetween(dto.getFrom(), dto.getTo()).size();
    }

    @PostMapping("/total_message")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Lấy số lượng tin nhắn đã được gửi trong một khoảng thời gian")
    public Integer getTotalMessageInMonth(@ApiIgnore @AuthenticationPrincipal User admin, @Valid @RequestBody MonthDto dto) {

        return messageService.findByCreateAtBetween(dto.getFrom(), dto.getTo()).size();
    }

    @GetMapping("/statistic_message")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Thống kê số lượng tin nhắn trong 1 năm bất kỳ")
    public ResponseEntity<?> getTotalMessageInMonth(@ApiIgnore @AuthenticationPrincipal User admin,
                                                    @RequestParam int year) {
        return ResponseEntity.ok(messageService.statisticsByMonths(year));
    }

    @GetMapping("/statistic_sign_up")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Thống kê số lượng đăng ký trong 1 năm bất kỳ")
    public ResponseEntity<?> statisticsSignUpByGender(@ApiIgnore @AuthenticationPrincipal User admin,
                                                      @RequestParam int year) {
        return ResponseEntity.ok(userDetailService.statisticsSignUpByGender(year));
    }

    @GetMapping("/read_log")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Đọc nhật ký hoạt động của quản trị")
    public ResponseEntity<?> readLog(@ApiIgnore @AuthenticationPrincipal User admin, Pageable pageable) {
        Page<AdminLog> logs = adminLogService.findAll(pageable);
        writeLogToDatabase(admin, admin, "truy cập nhật ký hoạt động của quản trị");

        return ResponseEntity.ok(logs);
    }


    @GetMapping("/userReport")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Lấy danh sách báo cáo về user")
    public ResponseEntity<?> getAll(@ApiIgnore @AuthenticationPrincipal User admin,
                                    Pageable pageable,
                                    @RequestParam Optional<Boolean> seen) {
        log.info("admin id = {} getting all user report list, page = {}, size = {}", admin.getId(), pageable.getPageNumber(), pageable.getPageSize());
        Page<UserReport> userReportPage;
        if (seen.isEmpty())
            userReportPage = userReportService.findAll(pageable);
        else
            userReportPage = userReportService.findAll(seen.get(), pageable);
        return ResponseEntity.ok(toPageUserReportDto(userReportPage));
    }

    @GetMapping("/userReport/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Xem chi tiết một báo cáo")
    public ResponseEntity<?> getUserReportDetail(@ApiIgnore @AuthenticationPrincipal User admin,
                                                 @PathVariable String id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        Optional<UserReport> userReportOptional = userReportService.findById(id);
        if (userReportOptional.isPresent()) {
            return ResponseEntity.ok(userReportMapper.toUserReportDto(id));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/userReport/seen/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Đánh dấu báo cáo này là đã xem")
    public ResponseEntity<?> markSeenReport(@ApiIgnore @AuthenticationPrincipal User admin,
                                            @PathVariable String id) {
        if (id == null)
            return ResponseEntity.badRequest().build();
        Optional<UserReport> userReportOptional = userReportService.findById(id);
        if (userReportOptional.isPresent()) {
            var userReport = userReportOptional.get();
            userReport.setSeen(true);
            userReportService.save(userReport);
            return ResponseEntity.ok(userReportMapper.toUserReportDto(id));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/blockList")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Danh sách những người bị block hoặc không")
    public ResponseEntity<?> markSeenReport(@ApiIgnore @AuthenticationPrincipal User admin,
                                            @RequestParam Optional<Boolean> isBlock,
                                            Pageable pageable) {
        if (isBlock.isPresent()) {
            Page<User> userPage = userDetailService.findByBlock(isBlock.get(), pageable);
            return ResponseEntity.ok(toUserProfileDto(userPage));
        }
        Page<User> userPage = userDetailService.findByBlock(true, pageable);
        return ResponseEntity.ok(toUserProfileDto(userPage));
    }

    @DeleteMapping("/message/{messageId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @ApiOperation("Admin gỡ một tin nhắn")
    public ResponseEntity<?> deleteMessageById(@PathVariable String messageId,
                                               @ApiIgnore @AuthenticationPrincipal User admin,
                                               Locale locale) {
        Optional<Message> messageOptional = messageService.findById(messageId);
        if (messageOptional.isEmpty()) {
            String messageNotFound = messageSource.getMessage("message_not_found", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(messageNotFound));
        }
        var message = messageOptional.get();
        String contentOfMessageDeleted = messageSource.getMessage("content_of_message_be_deleted_by_admin", null, locale);
        message.setDeleted(true);
        message.setContent(contentOfMessageDeleted);
        message.setType(MessageType.TEXT);
        message.setMedia(null);
        messageService.deleteMessage(messageId, contentOfMessageDeleted);
        chatSocketService.sendDeletedMessage(message, message.getRoomId());
        return ResponseEntity.ok().build();
    }

    /*

     */
    private Page<?> toPageUserReportDto(Page<UserReport> userReportPage) {
        List<UserReport> content = userReportPage.getContent();
        List<UserReportDto> dto = content.stream()
                .map(x -> userReportMapper.toUserReportDto(x))
                .collect(Collectors.toList());
        return new PageImpl<>(dto, userReportPage.getPageable(), userReportPage.getTotalElements());
    }

    /**
     *
     */
    private Page<?> toUserDto(Page<User> userPage) {
        List<User> content = userPage.getContent();
//        List<UserProfileDto> dto = content.stream()
//                .map(x -> userMapper.toUserProfileDto(x))
//                .collect(Collectors.toList());
        return new PageImpl<>(content, userPage.getPageable(), userPage.getTotalElements());
    }

    private Page<?> toUserProfileDto(Page<User> userPage) {
        List<User> content = userPage.getContent();
        List<UserProfileDto> dto = content.stream()
                .map(x -> userMapper.toUserProfileDto(x))
                .collect(Collectors.toList());
        return new PageImpl<>(content, userPage.getPageable(), userPage.getTotalElements());
    }


    private void writeLogToDatabase(User admin, User user, String content) {
        AdminLog adminLog = AdminLog.builder()
                .handlerObjectId(admin.getId())
                .content(content)
                .time(new Date())
                .relatedObjectId(user.getId())
                .build();
        adminLogService.writeLog(adminLog);
    }

}
