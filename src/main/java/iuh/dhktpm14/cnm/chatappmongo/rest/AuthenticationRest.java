package iuh.dhktpm14.cnm.chatappmongo.rest;

import iuh.dhktpm14.cnm.chatappmongo.dto.EmailDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserSignupDto;
import iuh.dhktpm14.cnm.chatappmongo.dto.UserSummaryDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.EmailExistException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.PhoneNumberExistException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UnAuthenticateException;
import iuh.dhktpm14.cnm.chatappmongo.exceptions.UserNotFoundException;
import iuh.dhktpm14.cnm.chatappmongo.jwt.JwtUtils;
import iuh.dhktpm14.cnm.chatappmongo.payload.MessageResponse;
import iuh.dhktpm14.cnm.chatappmongo.payload.SiginRequest;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import iuh.dhktpm14.cnm.chatappmongo.service.AppUserDetailService;
import iuh.dhktpm14.cnm.chatappmongo.util.Utils;
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
import java.util.Locale;
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

    @Autowired
    private MessageSource messageSource;

    @PostMapping(path = "/signin", consumes = "application/json")
    public ResponseEntity<?> signin(@RequestBody SiginRequest payload, HttpServletResponse response, Locale locale) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword()));
        } catch (AuthenticationException e) {
            String loginFail = messageSource.getMessage("login_fail", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(loginFail));
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var user = (User) authentication.getPrincipal();
        String jwtAccess = jwtUtils.generateJwtAccessTokenFromAuthentication(authentication);
        String jwtRefresh = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
        userService.setRefreshToken(user.getId(), jwtRefresh);

        response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, jwtRefresh));

        return ResponseEntity.ok(new UserSummaryDto(user, jwtAccess));
    }

    @GetMapping("/refreshtoken")
    public ResponseEntity<?> getRefreshToken(@CookieValue(value = Utils.REFRESH_TOKEN) String requestRefreshToken, HttpServletResponse response) {
        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            String userId = jwtUtils.getUserIdFromJwtToken(requestRefreshToken);
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent() && requestRefreshToken.equals(userOptional.get().getRefreshToken())) {
                var user = userOptional.get();
                String token = jwtUtils.generateJwtAccessTokenFromUserId(user.getId());
                String newRefreshToken = jwtUtils.generateJwtRefreshTokenFromUserId(user.getId());
                response.addCookie(getHttpCookie(Utils.REFRESH_TOKEN, newRefreshToken));
                userService.setRefreshToken(userId, newRefreshToken);
                return ResponseEntity.ok(token);
            }
        }
        throw new UnAuthenticateException();
    }

    @PostMapping(path = "/signup/save_information", consumes = "application/json")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupDto user) {
        if (userService.signup(user)) {
            Optional<User> optional = userRepository.findDistinctByPhoneNumber(user.getPhoneNumber());
            if (optional.isPresent()) {
                user.setId(optional.get().getId());
                return ResponseEntity.ok(user);
            }
        }
        throw new PhoneNumberExistException();
    }

    @PutMapping("/signup/save_information")
    public ResponseEntity<?> updateSignup(@Valid @RequestBody UserSignupDto user) {
        if (userService.updateInformation(user))
            return ResponseEntity.ok(user);
        throw new PhoneNumberExistException();
    }

    @PostMapping("/signup/check_phone_number")
    public ResponseEntity<?> checkPhoneNumber(@RequestBody UserSignupDto dto, Locale locale) {
        if (! userService.checkPhoneNumber(dto.getPhoneNumber())) {
            String phoneValid = messageSource.getMessage("phone_valid", null, locale);
            return ResponseEntity.ok(new MessageResponse(phoneValid));
        }
        throw new PhoneNumberExistException();
    }

    @PostMapping("/signup/check_email")
    public ResponseEntity<?> checkEmail(@RequestBody EmailDto dto, Locale locale) {
        if (! userService.checkEmail(dto.getEmail())) {
            String emailValid = messageSource.getMessage("email_valid", null, locale);
            return ResponseEntity.ok(new MessageResponse(emailValid));
        }
        throw new EmailExistException();
    }

    @PutMapping("/signup/send_verification_code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody User user, Locale locale)
            throws UnsupportedEncodingException, MessagingException {
        if (! userService.regexEmail(user.getEmail())) {
            String emailInvalid = messageSource.getMessage("email_invalid", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(emailInvalid));
        }
        if (user.getId() == null) {
            String dataInvalid = messageSource.getMessage("data_invalid", null, locale);
            return ResponseEntity.badRequest().body(new MessageResponse(dataInvalid));
        }
        var userDB = userService.findById(user.getId());
        var userCheckEmail = userService.findByEmail(user.getEmail());
        if (userDB == null)
            throw new UserNotFoundException();
        if (userCheckEmail != null && ! (userDB.getId().equals(userCheckEmail.getId())))
            throw new EmailExistException();

        userDB.setEmail(user.getEmail());
        userService.sendVerificationEmail(userDB);

        String success = messageSource.getMessage("send_code_success", null, locale);
        return ResponseEntity.ok(new MessageResponse(success));
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<?> verify(@RequestBody User user, Locale locale) {
        if (userService.verify(user)) {
            String success = messageSource.getMessage("verify_success", null, locale);
            return ResponseEntity.ok(new MessageResponse(success));
        }
        String codeInvalid = messageSource.getMessage("verify_code_invalid", null, locale);
        return ResponseEntity.badRequest().body(new MessageResponse(codeInvalid));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> signout(@CookieValue(value = "refresh_token") String requestRefreshToken, HttpServletResponse response, Locale locale) {
        if (requestRefreshToken != null && jwtUtils.validateJwtToken(requestRefreshToken)) {
            String userId = jwtUtils.getUserIdFromJwtToken(requestRefreshToken);
            userService.setRefreshToken(userId, null);
            var cookie = getHttpCookie(Utils.REFRESH_TOKEN, "");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            String logoutSuccess = messageSource.getMessage("logout_success", null, locale);
            return ResponseEntity.ok(new MessageResponse(logoutSuccess));
        }
        String sessionExpired = messageSource.getMessage("session_expired", null, locale);
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
    public ResponseEntity<?> signupForMoblie(UserSignupDto user, BindingResult result, Locale locale) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(messageSource.getMessage(result.getFieldError(), locale)));
        }
        return signup(user);
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
