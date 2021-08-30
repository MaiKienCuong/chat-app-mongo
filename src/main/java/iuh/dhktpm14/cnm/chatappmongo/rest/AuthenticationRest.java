package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.EmailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserSignupDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.payload.SiginRequest;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.eclipse.jetty.http.HttpCookie.SAME_SITE_STRICT_COMMENT;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("${spring.security.cross_origin}")
public class AuthenticationRest {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppUserDetailService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SiginRequest payload, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Đăng nhập không thành công. Tài khoản hoặc mật khẩu không đúng"));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var user = (User) authentication.getPrincipal();
        String jwtAccess = jwtUtils.generateJwtAccessTokenFromAuthentication(authentication);
        String jwtRefresh = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
        user.setRefreshToken(jwtRefresh);
        userRepository.save(user);

        var refreshTokenCookie = new Cookie("refresh_token", jwtRefresh);
        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setComment(SAME_SITE_STRICT_COMMENT);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(new UserSummaryDto(user, jwtAccess));
    }

    @GetMapping("/refreshtoken")
    public ResponseEntity<?> getRefreshToken(@CookieValue(value = "refresh_token") String requestRefreshToken, HttpServletResponse response) {
        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            Optional<User> user = userRepository.findById(jwtUtils.getUserIdFromJwtToken(requestRefreshToken));
            if (user.isPresent() && user.get().getRefreshToken().equals(requestRefreshToken)) {
                String token = jwtUtils.generateJwtAccessTokenFromUserId(user.get().getId());
                String newRefreshToken = jwtUtils.generateJwtRefreshTokenFromUserId(user.get().getId());
                var cookie = new Cookie("refresh_token", newRefreshToken);
                cookie.setHttpOnly(true);
                cookie.setComment(SAME_SITE_STRICT_COMMENT);
                cookie.setPath("/");
                response.addCookie(cookie);
                user.get().setRefreshToken(newRefreshToken);
                userRepository.save(user.get());
                return ResponseEntity.ok(token);
            }
        }
        return ResponseEntity.badRequest()
                .body(new MessageResponse("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"));
    }

    @PostMapping("signup/save_information")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupDto user) {
        if (userService.signup(user)) {
            Optional<User> optional = userRepository.findDistinctByPhoneNumberOrUsernameOrEmail(user.getPhoneNumber());
            if (optional.isPresent()) {
                user.setId(optional.get().getId());
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Số điện thoại đã tồn tại"));
    }

    @PutMapping("/signup/save_information")
    public ResponseEntity<?> updateSignup(@Valid @RequestBody UserSignupDto user) {
        if (userService.updateInformation(user))
            return ResponseEntity.ok(user);
        return ResponseEntity.badRequest().body(new MessageResponse("Số điện thoại đã tồn tại"));
    }

    @PostMapping("/signup/check_phone_number")
    public ResponseEntity<?> checkPhoneNumber(@RequestBody UserSignupDto dto) {
        if (! userService.checkPhoneNumber(dto.getPhoneNumber()))
            return ResponseEntity.ok(new MessageResponse("Số điện thoại hợp lệ"));
        return ResponseEntity.badRequest().body(new MessageResponse("Số điện thoại đã tồn tại"));
    }

    @PostMapping("/signup/check_email")
    public ResponseEntity<?> checkEmail(@RequestBody EmailDto dto) {
        if (! userService.checkEmail(dto.getEmail()))
            return ResponseEntity.ok(new MessageResponse("Email hợp lệ"));
        return ResponseEntity.badRequest().body(new MessageResponse("Email đã tồn tại"));
    }

    @PutMapping("/signup/send_vetification_code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody User user)
            throws UnsupportedEncodingException, MessagingException {
        if (! userService.regexEmail(user.getEmail()))
            return ResponseEntity.badRequest().body(new MessageResponse("Email không hợp lệ."));
        if (user.getId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Dữ liệu gửi lên không hợp lệ - thiếu id."));
        var userDB = userService.findById(user.getId());
        var userCheckEmail = userService.findByEmail(user.getEmail());
        if (userDB == null)
            return ResponseEntity.badRequest().body(new MessageResponse("User không tồn tại."));
        if (userCheckEmail != null && ! (userDB.getId().equals(userCheckEmail.getId())))
            return ResponseEntity.badRequest().body(new MessageResponse("Email đã tồn tại."));

        userDB.setEmail(user.getEmail());
        userService.sendVerificationEmail(userDB);
        return ResponseEntity.ok(new MessageResponse("Gửi mã xác thực thành công."));

    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verify(@RequestBody User user) {
        if (userService.verify(user))
            return ResponseEntity.ok(new MessageResponse("Xác thực thành công"));
        return ResponseEntity.badRequest().body(new MessageResponse("Mã xác nhận không chính xác"));
    }

    /*
     * @PostMapping("/signup/password") public ResponseEntity<?>
     * enterPassword(@RequestBody UserPasswordUpdateDTO user){
     * if(userService.updatePassword(user)) return ResponseEntity.ok(new
     * MessageResponse("update_password_susscess")); else return
     * ResponseEntity.ok(new MessageResponse("update_password__fail")); }
     */

}
