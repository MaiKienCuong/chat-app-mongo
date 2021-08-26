package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.EmailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserDetailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserSignupDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.RefreshToken;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.payload.SiginRequest;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import static org.eclipse.jetty.http.HttpCookie.SAME_SITE_STRICT_COMMENT;

@RestController
@RequestMapping("api/auth")
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

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SiginRequest payload, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Đăng nhập không thành công. Tài khoản hoặc mật khẩu không đúng"));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        String jwtRefresh = jwtUtils.generateJwtRefreshToken(authentication);
        var user = (User) authentication.getPrincipal();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), jwtRefresh);
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken.getToken());
        System.out.println("FIRST : " + refreshToken.getToken());

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setComment(SAME_SITE_STRICT_COMMENT);
        refreshTokenCookie.setPath("/");

        response.addCookie(refreshTokenCookie);
        user = userRepository.findDistinctByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("UsernameNotFoundException"));

        return ResponseEntity.ok(new UserDetailDto(user, jwt));
    }

    @GetMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(HttpServletRequest httpRequest) {
        Cookie[] cookies = httpRequest.getCookies();
        String requestRefreshToken = null;
        if (cookies != null) {
            Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> c.getName().equals("refresh_token"))
                    .findFirst();
            if (cookie.isPresent())
                requestRefreshToken = cookie.get().getValue();
        }

        if (jwtUtils.getExpirationFromJwtToken(requestRefreshToken).getTime() < (new Date().getTime() + 1))
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại"));
        String token = jwtUtils.generateTokenFromUsername(jwtUtils.getUserNameFromJwtToken(requestRefreshToken));
        return ResponseEntity.ok(token);

    }

    @PostMapping("/signup/save_information")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupDto user) {
        if (userService.signup(user)) {
            user.setId(userRepository.findByPhoneNumber(user.getPhoneNumber()).getId());
            return ResponseEntity.ok(user);
        } else
            return ResponseEntity.badRequest().body(new MessageResponse("số điện thoại đã tồn tại"));
    }

    @PutMapping("/signup/save_information")
    public ResponseEntity<?> updateSignup(@Valid @RequestBody UserSignupDto user) {
        if (userService.updateInformation(user))
            return ResponseEntity.ok(user);
        else
            return ResponseEntity.badRequest().body(new MessageResponse("số điện thoại đã tồn tại"));
    }

    @PostMapping("/signup/check_phone_number")
    public ResponseEntity<?> checkPhoneNumber(@RequestBody UserSignupDto dto) {
        if (! userService.checkPhoneNumber(dto.getPhoneNumber()))
            return ResponseEntity.ok(new MessageResponse("Số điện thoại hợp lệ"));
        else
            return ResponseEntity.badRequest().body(new MessageResponse("Số điện thoại đã tồn tại"));
    }

    @PostMapping("/signup/check_email")
    public ResponseEntity<?> checkEmail(@RequestBody EmailDto dto) {
        if (! userService.checkEmail(dto.getEmail()))
            return ResponseEntity.ok(new MessageResponse("Email hợp lệ"));
        else
            return ResponseEntity.badRequest().body(new MessageResponse("Email đã tồn tại"));
    }

    @PutMapping("/signup/send_vetification_code")
    public ResponseEntity<?> sendVetificationCode(@RequestBody User user)
            throws UnsupportedEncodingException, MessagingException {
        if (! userService.regexEmail(user.getEmail()))
            return ResponseEntity.badRequest().body(new MessageResponse("Email không hợp lệ."));
        if (user.getId() == null)
            return ResponseEntity.badRequest().body(new MessageResponse("Dữ liệu gửi lên không hợp lệ - thiếu id."));
        User userDB = userService.findById(user.getId());

        User userCheckEmail = userService.findByEmail(user.getEmail());
        if (userDB == null)
            return ResponseEntity.badRequest().body(new MessageResponse("User không tồn tại."));
        if (userCheckEmail != null)
            if (userDB.getId() != userCheckEmail.getId())
                return ResponseEntity.badRequest().body(new MessageResponse("Email đã tồn tại."));

        userDB.setEmail(user.getEmail());
        userService.sendVerificationEmail(userDB);
        return ResponseEntity.ok(new MessageResponse("Gửi mã xác thực thành công."));

    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> vetify(@RequestBody User user) {

        if (userService.vetify(user))
            return ResponseEntity.ok(new MessageResponse("Xác thực thành công"));
        else
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
