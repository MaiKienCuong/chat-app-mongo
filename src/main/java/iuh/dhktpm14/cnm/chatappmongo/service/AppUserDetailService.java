package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserSignupDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.OnlineStatus;
import iuh.dhktpm14.cnm.chatappmongo.enumvalue.RoleType;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Service
public class AppUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MessageSource messageSource;

    private static final Random random = new Random();

    private static final Logger logger = Logger.getLogger(AppUserDetailService.class.getName());

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.log(Level.INFO, "load user by user name = {0}", username);

        return userRepository.findDistinctByPhoneNumberOrUsernameOrEmail(username)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("username_not_found",
                            new Object[]{ username }, LocaleContextHolder.getLocale());
                    return new UsernameNotFoundException(message);
                });
    }

    public boolean signup(UserSignupDto userDto) {
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber()))
            return false;
        var user = User.builder()
                .displayName(userDto.getDisplayName())
                .password(encoder.encode(userDto.getPassword()))
                .phoneNumber(userDto.getPhoneNumber())
                .enable(false)
                .roles(RoleType.ROLE_USER.toString())
                .build();
        userRepository.save(user);
        return true;
    }

    public void sendVerificationEmail(User user) throws UnsupportedEncodingException, MessagingException {
        int verificationCode = random.nextInt((999999 - 100000) + 1) + 100000;
//        setVerificationCode(user.getId(), verificationCode + "");
        user.setVerificationCode(String.valueOf(verificationCode));
        userRepository.save(user);

        logger.log(Level.INFO, "email verification code = {0}", String.valueOf(verificationCode));

        String toAddress = user.getEmail();
        var fromAddress = "chat_app_email";
        var senderName = messageSource.getMessage("verification_sender_name_in_mail", null, LocaleContextHolder.getLocale());
        var subject = messageSource.getMessage("verification_subject_in_mail", null, LocaleContextHolder.getLocale());
        var content = messageSource.getMessage("verification_content_in_mail",
                new Object[]{ user.getDisplayName(), String.valueOf(verificationCode) }, LocaleContextHolder.getLocale());

        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true);

        logger.log(Level.INFO, "sending verification email to email address = {0}", toAddress);

        /*
        đưa vào thread khác để client không phải đợi
         */
        new Thread(() -> mailSender.send(message)).start();

    }

    public boolean verify(User user) {
        Optional<User> userOptional = userRepository.findDistinctByEmail(user.getEmail());
        if (userOptional.isEmpty())
            return false;
        var existsUser = userOptional.get();
        if (existsUser.getVerificationCode().equalsIgnoreCase(user.getVerificationCode())) {
            existsUser.setEnable(true);
            existsUser.setVerificationCode(null);
            userRepository.save(existsUser);
            return true;
        }
        return false;
    }

    public boolean updateInformation(UserSignupDto dto) {
        Optional<User> optional = userRepository.findById(dto.getId());
        if (optional.isEmpty())
            return false;
        var user = optional.get();
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber()) && ! user.getPhoneNumber().equals(dto.getPhoneNumber()))
            return false;
        user.setDisplayName(dto.getDisplayName());
        user.setPassword(dto.getPassword());
        user.setPhoneNumber(dto.getPhoneNumber());
        userRepository.save(user);
        return true;
    }

    public boolean regexEmail(String email) {
        var pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(email);
        return matcher.find();
    }

    /**
     * cập nhật refreshToken cho userId
     */
    public void setRefreshToken(String userId, String refreshToken) {
        logger.log(Level.INFO, "refresh token = {0}", refreshToken);
        var criteria = Criteria.where("_id").is(userId);
        var update = new Update();
        update.set("refreshToken", refreshToken);
        mongoTemplate.updateFirst(Query.query(criteria), update, User.class);
    }

    private void setVerificationCode(String userId, String verificationCode) {
        logger.log(Level.INFO, "save verification code = {0} for userId = {1} to database",
                new Object[]{ verificationCode, userId });
        var criteria = Criteria.where("_id").is(userId);
        var update = new Update();
        update.set("verificationCode", verificationCode);
        mongoTemplate.updateFirst(Query.query(criteria), update, User.class);
    }

    /*
    cập nhật trạng thái là đang online
     */
    public void updateStatusOnline(String userId) {
        logger.log(Level.INFO, "updating online status for userId = {0}", userId);
        var criteria = Criteria.where("_id").is(userId);
        var update = new Update();
        update.set("onlineStatus", OnlineStatus.ONLINE)
                .unset("lastOnline");
        mongoTemplate.updateFirst(Query.query(criteria), update, User.class);
    }

    /*
    cập nhật trạng thái là đã offline
     */
    public void updateStatusOffline(String userId) {
        logger.log(Level.INFO, "updating offline status and time for userId = {0}", userId);
        var criteria = Criteria.where("_id").is(userId);
        var update = new Update();
        update.set("onlineStatus", OnlineStatus.OFFLINE)
                .set("lastOnline", new Date());
        mongoTemplate.updateFirst(Query.query(criteria), update, User.class);
    }

    @Cacheable("user")
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findDistinctByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * tìm kiếm user theo tên gần đúng, k phân biệt hoa thường
     */
    public List<User> findAllByDisplayNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByDisplayNameAsc(String displayName, String phoneNumber) {
        return userRepository.findAllByDisplayNameContainingIgnoreCaseOrPhoneNumberContainingIgnoreCaseOrderByDisplayNameAsc(displayName, phoneNumber);
    }

    public List<User> findByIdIn(List<String> ids) {
        return userRepository.findByIdIn(ids);
    }

    public Optional<User> findDistinctByUsername(String userName) {
        return userRepository.findDistinctByUsername(userName);
    }

    public Optional<User> findDistinctByPhoneNumber(String phoneNumber) {
        return userRepository.findDistinctByPhoneNumber(phoneNumber);
    }

    public Optional<User> findDistinctByPhoneNumberOrUsernameOrEmail(String phoneNumber) {
        return userRepository.findDistinctByPhoneNumberOrUsernameOrEmail(phoneNumber);
    }

    public boolean existsById(String userId) {
        return userRepository.existsById(userId);
    }

    public boolean existsByUsername(String userName) {
        return userRepository.existsByUsername(userName);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
