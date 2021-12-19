package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserSignupDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.AdminLog;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.payload.SiginRequest;
import iuh.dhktpm14.cnm.chatappmongo.service.AdminLogService;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.eclipse.jetty.http.HttpCookie.SAME_SITE_NONE_COMMENT;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin("${spring.security.cross_origin}")
public class AuthenticationRest {

    @Autowired
    private AdminLogService adminLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AppUserDetailService userDetailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MessageSource messageSource;

    @PostMapping(path = "/admin/signin")
    public ResponseEntity<?> signInForAdmin(@RequestBody SiginRequest payload, HttpServletResponse response, Locale locale) {
        log.info("authenticating for username = {}", payload.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
            log.info("login ok with username = {}", payload.getUsername());
        } catch (AuthenticationException e) {
            String loginFail = messageSource.getMessage("login_fail", null, locale);
            log.error(loginFail);
            return ResponseEntity.badRequest().body(new MessageResponse(loginFail));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var user = (User) authentication.getPrincipal();

        if (user.getRoles().equals("ROLE_USER"))
            return ResponseEntity.badRequest().body(new MessageResponse("Access denied"));

        String jwtAccess = jwtUtils.generateJwtAccessTokenFromAuthentication(authentication);
        String jwtRefresh = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
        userDetailService.setRefreshToken(user.getId(), jwtRefresh);
//        response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, jwtRefresh));
        response.setHeader("Set-Cookie", Utils.REFRESH_TOKEN + "=" + jwtRefresh + "; Path=/; HttpOnly; SameSite=None; Secure");

        AdminLog adminLog = AdminLog.builder()
                .handlerObjectId(user.getId())
                .content("is sign in")
                .time(new Date())
                .relatedObjectId(user.getId())
                .build();
        adminLogService.writeLog(adminLog);
        return ResponseEntity.ok(new UserSummaryDto(user, jwtAccess));
    }

    @PostMapping(path = "/signin", consumes = "application/json")
    public ResponseEntity<?> signin(@RequestBody SiginRequest payload, HttpServletResponse response, Locale locale) {
        log.info("authenticating for username = {}", payload.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
            log.info("login ok with username = {}", payload.getUsername());
        } catch (AuthenticationException e) {
            String loginFail = messageSource.getMessage("login_fail", null, locale);
            log.error(loginFail);
            return ResponseEntity.badRequest().body(new MessageResponse(loginFail));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var user = (User) authentication.getPrincipal();
        String jwtAccess = jwtUtils.generateJwtAccessTokenFromAuthentication(authentication);
        String jwtRefresh = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
        userDetailService.setRefreshToken(user.getId(), jwtRefresh);

//        response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, jwtRefresh));
        response.setHeader("Set-Cookie", Utils.REFRESH_TOKEN + "=" + jwtRefresh + ";  Path=/; HttpOnly; SameSite=None; Secure");

        return ResponseEntity.ok(new UserSummaryDto(user, jwtAccess));
    }

    @GetMapping("/refreshtoken")
    public ResponseEntity<?> getRefreshToken(@CookieValue(value = Utils.REFRESH_TOKEN) String requestRefreshToken, HttpServletResponse response, Locale locale) {
        log.info("refreshing token = {}", requestRefreshToken);
        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            String userId = jwtUtils.getUserIdFromJwtToken(requestRefreshToken);
            Optional<User> userOptional = userDetailService.findById(userId);

            if (userOptional.isPresent() && requestRefreshToken.equals(userOptional.get().getRefreshToken())) {
                var user = userOptional.get();
                String token = jwtUtils.generateJwtAccessTokenFromUserId(user.getId());
                String newRefreshToken = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
//                response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, newRefreshToken));
                response.setHeader("Set-Cookie", Utils.REFRESH_TOKEN + "=" + newRefreshToken + ";  Path=/; HttpOnly; SameSite=None; Secure");

                userDetailService.setRefreshToken(userId, newRefreshToken);
                return ResponseEntity.ok(token);
            } else {
                if (userOptional.isEmpty()) {
                    log.error("userId = {} not exists", userId);
                    String message = messageSource.getMessage("user_not_found", null, locale);
                    return ResponseEntity.badRequest().body(new MessageResponse(message));
                }
            }
        }
        String message = messageSource.getMessage("refresh_token_invalid", null, locale);
        log.error(message);
        return ResponseEntity.badRequest().body(new MessageResponse(message));
    }

    /*
    kiểm tra tính hợp lệ của dữ liệu trước khi đăng ký
     */
    @PostMapping("/signup/phone/valid")
    public ResponseEntity<?> validSignupByPhonePhone(@Valid @RequestBody UserSignupDto dto, Locale locale) {
        String phoneNumber = dto.getPhoneNumber();
        log.info("valid for userSignup dto displayName = {}, email = {}, phoneNumber= {}",
                dto.getDisplayName(), dto.getEmail(), phoneNumber);
        if (phoneNumber != null && regexPhone(phoneNumber)) {
            if (userDetailService.existsByPhoneNumber(phoneNumber)) {
                Optional<User> userOptional = userDetailService.findDistinctByPhoneNumber(phoneNumber);
                User user = userOptional.get();
                if (user.isEnable()) {
                    String phoneExists = messageSource.getMessage("phone_exists", null, locale);
                    log.error(phoneExists);
                    return ResponseEntity.badRequest().body(new MessageResponse("phoneNumber", phoneExists));
                } else {
                    dto.setId(user.getId());
                }
            }
            return ResponseEntity.ok(dto);
        }
        String phoneInvalid = messageSource.getMessage("phone_invalid", null, locale);
        log.error(phoneInvalid);
        return ResponseEntity.badRequest().body(new MessageResponse("phoneNumber", phoneInvalid));
    }

    @PostMapping("/signup/email/valid")
    public ResponseEntity<?> validEmail(@Valid @RequestBody UserSignupDto dto, Locale locale) {
        String email = dto.getEmail();
        log.info("valid for userSignup dto displayName = {}, email = {}, email= {}",
                dto.getDisplayName(), email, email);
        if (email != null && regexEmail(email)) {
            if (userDetailService.existsByEmail(email)) {
                Optional<User> userOptional = userDetailService.findByEmail(email);
                User user = userOptional.get();
                if (user.isEnable()) {
                    String emailExists = messageSource.getMessage("email_exists", null, locale);
                    log.error(emailExists);
                    return ResponseEntity.badRequest().body(new MessageResponse("email", emailExists));
                } else {
                    dto.setId(user.getId());
                }
            }
            return ResponseEntity.ok(dto);
        }
        String emailInvalid = messageSource.getMessage("email_invalid", null, locale);
        log.error(emailInvalid);
        return ResponseEntity.badRequest().body(new MessageResponse("email", emailInvalid));
    }

    public boolean regexPhone(String phone) {
        var pattern = Pattern.compile("^\\+{0,1}[0-9]{9,12}$", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(phone);
        return matcher.find();
    }

    public boolean regexEmail(String email) {
        var pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(email);
        return matcher.find();
    }

    @PostMapping("/signup/phone")
    public ResponseEntity<?> phoneSave(@Valid @RequestBody UserSignupDto dto, Locale locale) {
        log.info("phone signup for userSignup dto displayName = {}, email = {}, phoneNumber= {}",
                dto.getDisplayName(), dto.getEmail(), dto.getPhoneNumber());
        if (dto.getPhoneNumber() != null && userDetailService.existsByPhoneNumber(dto.getPhoneNumber())) {
            Optional<User> userOptional = userDetailService.findDistinctByPhoneNumber(dto.getPhoneNumber());
            User user = userOptional.get();
            if (user.isEnable()) {
                String phoneInvalid = messageSource.getMessage("phone_exists", null, locale);
                log.error(phoneInvalid);
                return ResponseEntity.badRequest().body(new MessageResponse("phoneNumber", phoneInvalid));
            } else {
                dto.setId(user.getId());
                userDetailService.signupPhone(dto);
                return ResponseEntity.ok(dto);
            }
        }
        if (dto.getEmail() != null && userDetailService.existsByEmail(dto.getEmail())) {
            Optional<User> userOptional = userDetailService.findByEmail(dto.getEmail());
            User user = userOptional.get();
            if (user.isEnable()) {
                String emailExists = messageSource.getMessage("email_exists", null, locale);
                log.error(emailExists);
                return ResponseEntity.badRequest().body(new MessageResponse("email", emailExists));
            } else {
                dto.setId(user.getId());
                userDetailService.signupEmail(dto);
                return ResponseEntity.ok(dto);
            }
        }
        userDetailService.signupPhone(dto);
        return ResponseEntity.ok(dto);
    }


    /*
    luu thong tin dang ky bang email
     */
    @PostMapping(path = "/signup/email", consumes = "application/json")
    public ResponseEntity<?> signupEmail(@Valid @RequestBody UserSignupDto dto, Locale locale) throws UnsupportedEncodingException, MessagingException {
        log.info("email signup for userSignup dto displayName = {}, email = {}, phoneNumber= {}",
                dto.getDisplayName(), dto.getEmail(), dto.getPhoneNumber());
        if (dto.getPhoneNumber() != null && userDetailService.existsByPhoneNumber(dto.getPhoneNumber())) {
            Optional<User> userOptional = userDetailService.findDistinctByPhoneNumber(dto.getPhoneNumber());
            User user = userOptional.get();
            if (user.isEnable()) {
                String phoneInvalid = messageSource.getMessage("phone_exists", null, locale);
                log.error(phoneInvalid);
                return ResponseEntity.badRequest().body(new MessageResponse("phoneNumber", phoneInvalid));
            } else {
                dto.setId(user.getId());
                userDetailService.sendVerificationEmail(userDetailService.signupEmail(dto));
                return ResponseEntity.ok(dto);
            }
        }
        if (dto.getEmail() != null && userDetailService.existsByEmail(dto.getEmail())) {
            Optional<User> userOptional = userDetailService.findByEmail(dto.getEmail());
            User user = userOptional.get();
            if (user.isEnable()) {
                String emailExists = messageSource.getMessage("email_exists", null, locale);
                log.error(emailExists);
                return ResponseEntity.badRequest().body(new MessageResponse("email", emailExists));
            } else {
                dto.setId(user.getId());
                userDetailService.sendVerificationEmail(userDetailService.signupEmail(dto));
                return ResponseEntity.ok(dto);
            }
        }
        var savedUser = userDetailService.signupEmail(dto);
        userDetailService.sendVerificationEmail(savedUser);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping(value = "/signup/email/verify")
    public ResponseEntity<?> verify(@RequestBody User user, Locale locale) {
        log.info("verify for user id = {}", user.getId());
        if (user.getId() != null) {
            Optional<User> userOptional = userDetailService.findById(user.getId());
            if (userOptional.isPresent()) {
                var userDb = userOptional.get();
                if (userDb.getVerificationCode().equalsIgnoreCase(user.getVerificationCode())) {
                    userDb.setEnable(true);
                    userDetailService.save(userDb);
                    String success = messageSource.getMessage("verify_success", null, locale);
                    log.info(success);
                    return ResponseEntity.ok(new MessageResponse(success));
                } else {
                    String codeInvalid = messageSource.getMessage("verify_code_invalid", null, locale);
                    log.error(codeInvalid);
                    return ResponseEntity.badRequest().body(new MessageResponse(codeInvalid));
                }
            }
        }
        String userNotFound = messageSource.getMessage("user_not_found", null, locale);
        log.error(userNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(userNotFound));
    }

    @PostMapping(value = "/signup/email/reSendVerificationCode")
    public ResponseEntity<?> reSendVerificationCode(@RequestBody User user, Locale locale) throws UnsupportedEncodingException, MessagingException {
        log.info("re send verification code for user id = {}", user.getDisplayName());
        if (user.getId() != null) {
            Optional<User> userOptional = userDetailService.findById(user.getId());
            if (userOptional.isPresent()) {
                var userDb = userOptional.get();
                userDetailService.sendVerificationEmail(userDb);
                String success = messageSource.getMessage("re_send_verification_success", null, locale);
                log.info(success);
                return ResponseEntity.ok().body(new MessageResponse(success));
            }
        }
        String userNotFound = messageSource.getMessage("user_not_found", null, locale);
        log.error(userNotFound);
        return ResponseEntity.badRequest().body(new MessageResponse(userNotFound));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(HttpServletResponse response, Locale locale) {
        log.info("signout");
//        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
//            String userId = jwtUtils.getUserIdFromJwtToken(requestRefreshToken);
//            userDetailService.setRefreshToken(userId, null);
//        var cookie = getHttpCookie(Utils.REFRESH_TOKEN, "");
//        cookie.setMaxAge(0);
//        response.addCookie(cookie);

        response.setHeader("Set-Cookie", Utils.REFRESH_TOKEN + "=" + "" + ";  Path=/; HttpOnly; SameSite=None; Secure; MaxAge=0");

        String logoutSuccess = messageSource.getMessage("logout_success", null, locale);
        log.info(logoutSuccess);
        return ResponseEntity.ok(new MessageResponse(logoutSuccess));
//        }
//        String sessionExpired = messageSource.getMessage("session_expired", null, locale);
//        log.error(sessionExpired);
//        return ResponseEntity.badRequest().body(new MessageResponse(sessionExpired));

    }

    private Cookie getHttpCookie(String name, String value) {
        var cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setComment(SAME_SITE_NONE_COMMENT);
        cookie.setPath("/");
        return cookie;
    }

    @PostMapping(path = "/signin", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> signinForMobile(SiginRequest payload, HttpServletResponse response, Locale locale) {
        return signin(payload, response, locale);
    }

}
