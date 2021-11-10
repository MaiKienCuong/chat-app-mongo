package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.EmailDto;
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
import org.springframework.validation.BindingResult;
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
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static org.eclipse.jetty.http.HttpCookie.SAME_SITE_STRICT_COMMENT;

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
        String jwtAccess = jwtUtils.generateJwtAccessTokenFromAuthentication(authentication);
        String jwtRefresh = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
        userDetailService.setRefreshToken(user.getId(), jwtRefresh);

        response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, jwtRefresh));
        if (user.getRoles().equals("ROLE_USER"))
            return ResponseEntity.badRequest().body("Access denied");
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

        response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, jwtRefresh));

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
                response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, newRefreshToken));

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

    @PostMapping(path = "/signup/save_information", consumes = "application/json")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupDto user, Locale locale) {
        log.info("save signup information for userId = {}", user.getId());
        if (userDetailService.signup(user)) {
            Optional<User> optional = userDetailService.findDistinctByPhoneNumber(user.getPhoneNumber());
            if (optional.isPresent()) {
                user.setId(optional.get().getId());
                return ResponseEntity.ok(user);
            } else {
                log.error("userId = {} not exists", user.getId());
                String message = messageSource.getMessage("user_not_found", null, locale);
                return ResponseEntity.badRequest().body(new MessageResponse(message));
            }
        }
        String phoneInvalid = messageSource.getMessage("phone_invalid", null, locale);
        log.error(phoneInvalid);
        return ResponseEntity.badRequest().body(new MessageResponse(phoneInvalid));
    }

    @PutMapping("/signup/save_information")
    public ResponseEntity<?> updateSignup(@Valid @RequestBody UserSignupDto user, Locale locale) {
        log.info("update signup for user display name = {}", user.getDisplayName());
        if (userDetailService.updateInformation(user))
            return ResponseEntity.ok(user);
        String phoneInvalid = messageSource.getMessage("phone_invalid", null, locale);
        log.error(phoneInvalid);
        return ResponseEntity.badRequest().body(new MessageResponse(phoneInvalid));
    }

    @PostMapping("/signup/check_phone_number")
    public ResponseEntity<?> checkPhoneNumber(@RequestBody UserSignupDto dto, Locale locale) {
        log.info("checking phone number for phone = {}", dto.getPhoneNumber());
        if (! userDetailService.existsByPhoneNumber(dto.getPhoneNumber())) {
            String phoneValid = messageSource.getMessage("phone_valid", null, locale);
            log.info(phoneValid);
            return ResponseEntity.ok(new MessageResponse(phoneValid));
        }
        String phoneInvalid = messageSource.getMessage("phone_invalid", null, locale);
        log.error(phoneInvalid);
        return ResponseEntity.badRequest().body(new MessageResponse(phoneInvalid));
    }

    @PostMapping("/signup/check_email")
    public ResponseEntity<?> checkEmail(@RequestBody EmailDto dto, Locale locale) {
        log.info("checking email for email = {}", dto.getEmail());
        if (! userDetailService.existsByEmail(dto.getEmail())) {
            String emailValid = messageSource.getMessage("email_valid", null, locale);
            log.info(emailValid);
            return ResponseEntity.ok(new MessageResponse(emailValid));
        }
        String emailExists = messageSource.getMessage("email_exists", null, locale);
        log.error(emailExists);
        return ResponseEntity.badRequest().body(new MessageResponse(emailExists));
    }

    @PutMapping("/signup/send_verification_code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody User user, Locale locale)
            throws UnsupportedEncodingException, MessagingException {
        log.info("sending verification code to email = {}", user.getEmail());
        if (! userDetailService.regexEmail(user.getEmail())) {
            String emailInvalid = messageSource.getMessage("email_invalid", null, locale);
            log.error(emailInvalid);
            return ResponseEntity.badRequest().body(new MessageResponse(emailInvalid));
        }
        if (user.getId() == null) {
            String dataInvalid = messageSource.getMessage("data_invalid", null, locale);
            log.error(dataInvalid);
            return ResponseEntity.badRequest().body(new MessageResponse(dataInvalid));
        }
        var userDBOptional = userDetailService.findById(user.getId());
        var userCheckEmail = userDetailService.findByEmail(user.getEmail());
        if (userDBOptional.isEmpty()) {
            String userNotFound = messageSource.getMessage("user_not_found", null, locale);
            log.error(userNotFound);
            return ResponseEntity.badRequest().body(new MessageResponse(userNotFound));
        }
        var userDB = userDBOptional.get();
        if (userCheckEmail.isPresent() && ! (userDB.getId().equals(userCheckEmail.get().getId()))) {
            String emailExists = messageSource.getMessage("email_exists", null, locale);
            log.error(emailExists);
            return ResponseEntity.badRequest().body(new MessageResponse(emailExists));
        }
        userDB.setEmail(user.getEmail());
        userDetailService.sendVerificationEmail(userDB);

        String success = messageSource.getMessage("send_code_success", null, locale);
        log.info(success);
        return ResponseEntity.ok(new MessageResponse(success));
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verify(@RequestBody User user, Locale locale) {
        log.info("verify user display name = {}", user.getDisplayName());
        if (userDetailService.verify(user)) {
            String success = messageSource.getMessage("verify_success", null, locale);
            log.info(success);
            return ResponseEntity.ok(new MessageResponse(success));
        }
        String codeInvalid = messageSource.getMessage("verify_code_invalid", null, locale);
        log.error(codeInvalid);
        return ResponseEntity.badRequest().body(new MessageResponse(codeInvalid));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(@CookieValue(value = "refresh_token") String requestRefreshToken, HttpServletResponse response, Locale locale) {
        log.info("signout");
        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            String userId = jwtUtils.getUserIdFromJwtToken(requestRefreshToken);
            userDetailService.setRefreshToken(userId, null);
            var cookie = getHttpCookie(Utils.REFRESH_TOKEN, "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            String logoutSuccess = messageSource.getMessage("logout_success", null, locale);
            log.info(logoutSuccess);
            return ResponseEntity.ok(new MessageResponse(logoutSuccess));
        }
        String sessionExpired = messageSource.getMessage("session_expired", null, locale);
        log.error(sessionExpired);
        return ResponseEntity.badRequest().body(new MessageResponse(sessionExpired));

    }

    private Cookie getHttpCookie(String name, String value) {
        var cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        // cookie.setSecure(true);
        cookie.setComment(SAME_SITE_STRICT_COMMENT);
        cookie.setPath("/");
        return cookie;
    }

    //// for mobile

    @PostMapping(path = "/signin", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> signinForMobile(SiginRequest payload, HttpServletResponse response, Locale locale) {
        return signin(payload, response, locale);
    }

    @PostMapping(path = "signup/save_information", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> signupForMobile(UserSignupDto user, BindingResult result, Locale locale) {
        if (result.hasErrors()) {
            log.error("sign up for mobile has an error");
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(messageSource.getMessage(result.getFieldError(), locale)));
        }
        return signup(user, locale);
    }

    @PutMapping(path = "/signup/send_vetification_code", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> sendVerificationCodeForMobile(User user, Locale locale)
            throws UnsupportedEncodingException, MessagingException {
        return sendVerificationCode(user, locale);

    }

    @PostMapping(path = "/signup/verify", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<?> verifyForMobile(User user, Locale locale) {
        return verify(user, locale);
    }

}
