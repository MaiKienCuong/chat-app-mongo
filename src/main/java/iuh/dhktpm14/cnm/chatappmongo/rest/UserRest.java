package iuh.dhktpm14.cnm.chatappmongo.rest;

import io.swagger.annotations.ApiOperation;
import iuh.dhktpm14.cnm.chatappmongo.dto.ChangePasswordDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserProfileDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserUpdateDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.mapper.UserMapper;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.AmazonS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/user")
@CrossOrigin("${spring.security.cross_origin}")
public class UserRest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AmazonS3Service s3Service;

    @GetMapping("/email")
    public ResponseEntity<?> existEmail(@RequestBody String email, Locale locale) {
        if (userRepository.existsByEmail(email)) {
            String message = messageSource.getMessage("email_exists", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        return null;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Lấy thông tin về người dùng hiện tại")
    public ResponseEntity<?> getCurrentUser(@ApiIgnore @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userMapper.toUserDetailDto(user));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Cập nhật thông tin user")
    public ResponseEntity<?> updateInformationUser(@ApiIgnore @AuthenticationPrincipal User user,
                                                   @Valid @RequestBody UserUpdateDto userUpdate) {
        user.setEmail(userUpdate.getEmail());
        user.setDisplayName(userUpdate.getDisplayName());
        user.setGender(userUpdate.getGender());
        user.setDateOfBirth(userUpdate.getDateOfBirth());

        userRepository.save(user);

        return ResponseEntity.ok(userMapper.toUserDetailDto(user));
    }

    @PutMapping(value = "/me", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Cập nhật thông tin user")
    public ResponseEntity<?> updateInformationUserForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                            @Valid UserUpdateDto userUpdate, BindingResult result) {
    	if(result.hasErrors()) {
       		
       		return ResponseEntity.badRequest()
                       .body(new MessageResponse(messageSource.getMessage(result.getFieldError(), null)));
       	}
        return updateInformationUser(user, userUpdate);
    }

    @PutMapping("/me/changePassword")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi mật khẩu")
    public ResponseEntity<?> changePassword(@ApiIgnore @AuthenticationPrincipal User user,
                                            @Valid @RequestBody ChangePasswordDto passwordDto,
                                            Locale locale) {
        String message;
        Optional<User> userOptional = userRepository.findById(user.getId());
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (passwordEncoder.matches(passwordDto.getOldPass(), user.getPassword())) {
                user.setPassword(passwordEncoder.encode(passwordDto.getNewPass()));
                userRepository.save(user);
                message = messageSource.getMessage("changePass_success", null, locale);
                return ResponseEntity.ok(new MessageResponse(message));
            }
            message = messageSource.getMessage("oldPass_incorrect", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        message = messageSource.getMessage("changePass_failed", null, locale);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    @PutMapping(value = "/me/changePassword", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi mật khẩu")
    public ResponseEntity<?> changePasswordForMobile(@ApiIgnore @AuthenticationPrincipal User user,
                                                     @Valid ChangePasswordDto passwordDto,
                                                     Locale locale, BindingResult result) {
    	if(result.hasErrors()) {
       		
       		return ResponseEntity.badRequest()
                       .body(new MessageResponse(messageSource.getMessage(result.getFieldError(), null)));
       	}
    	return changePassword(user, passwordDto, locale);
    }

    @PutMapping(value = "/me/changeImage")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh đại diện")
    public ResponseEntity<?> changeImage(@ApiIgnore @AuthenticationPrincipal User user,
                                         MultipartFile file, Locale locale) {
        String message;
        if (file == null) {
            message = messageSource.getMessage("file_is_null", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        if (file.isEmpty()) {
            message = messageSource.getMessage("file_is_empty", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(message));
        }
        String newImageUrl = s3Service.uploadFile(file);
        user.setImageUrl(newImageUrl);
        userRepository.save(user);
        return ResponseEntity.ok(userMapper.toUserDetailDto(user));
    }

    @PutMapping(value = "/me/changeImage", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Đổi ảnh đại diện")
    public ResponseEntity<?> changeImageForMobile(@ApiIgnore @AuthenticationPrincipal User user, MultipartFile file, Locale locale) {
        return changeImage(user, file, locale);
    }

    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tìm kiếm user theo tên gần đúng")
    public ResponseEntity<?> searchUser(@ApiIgnore @AuthenticationPrincipal User user, @RequestParam String textToSearch) {
        List<User> result = userRepository.findAllByDisplayNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByDisplayNameAsc(textToSearch, textToSearch);
        if (result == null) {
            return ResponseEntity.ok(new ArrayList<>(0));
        }
        List<UserProfileDto> userProfiles = result.stream().filter(x -> ! x.getId().equals(user.getId()))
                .map(userMapper::toUserProfileDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userProfiles);
    }

    @PostMapping(value = "/search", consumes = "application/x-www-form-urlencoded")
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Tìm kiếm user theo tên gần đúng")
    public ResponseEntity<?> searchUserForMobile(@ApiIgnore @AuthenticationPrincipal User user, String textToSearch) {
        return searchUser(user, textToSearch);
    }

}
