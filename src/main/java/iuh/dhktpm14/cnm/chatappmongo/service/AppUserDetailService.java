package iuh.dhktpm14.cnm.chatappmongo.service;

import iuh.dhktpm14.cnm.chatappmongo.dto.UserSignupDto;
import iuh.dhktpm14.cnm.chatappmongo.entity.User;
import iuh.dhktpm14.cnm.chatappmongo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;
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
    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findDistinctByPhoneNumberOrUsernameOrEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("UsernameNotFoundException"));
    }

    public boolean signup(UserSignupDto userDto) {
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber()))
            return false;
        var user = new User();
        user.setDisplayName(userDto.getDisplayName());
        user.setPassword(encoder.encode(userDto.getPassword()));
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setEnable(false);
        user.setRoles("ROLE_USER");
        userRepository.save(user);
        return true;
    }

    public boolean checkPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void sendVerificationEmail(User user) throws UnsupportedEncodingException, MessagingException {
        var random = new Random();
        int verificationCode = random.nextInt((999999 - 100000) + 1) + 100000;
        user.setVerificationCode(verificationCode + "");
        userRepository.save(user);
        String toAddress = user.getEmail();
        String fromAddress = "chat_app_email";
        String senderName = "chat_app_admin";
        String subject = "Please verify your registration";
        String content = "Hello " + user.getDisplayName() + ",<br>" + "This is verification code :<br>" + "Code :"
                + user.getVerificationCode() + "<br>" + "Welcome to our social network,<br>" + "Chat App -->>>>.";

        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(content, true);

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

    public User findById(String id) {
        Optional<User> optional = userRepository.findById(id);
        return optional.orElse(null);
    }

    public User findByEmail(String email) {
        Optional<User> optional = userRepository.findDistinctByEmail(email);
        return optional.orElse(null);
    }

    public boolean regexEmail(String email) {
        var pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(email);
        return matcher.find();
    }

}
