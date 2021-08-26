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
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
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
		return userRepository.findDistinctByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("UsernameNotFoundException"));
	}

	public boolean signup(UserSignupDto user) {
		/*
		 * if(userRepository.existsByPhoneNumber(user.getPhoneNumber())) return false;
		 */
		if (userRepository.existsByPhoneNumber(user.getPhoneNumber()))
			return false;
		User user2 = new User();
		user2.setDisplayName(user.getDisplayName());
		user2.setPassword(encoder.encode(user.getPassword()));
		user2.setPhoneNumber(user.getPhoneNumber());
		user2.setRoles("ROLE_USER");
		userRepository.save(user2);
		return true;
	}

	public boolean checkPhoneNumber(String phoneNumber) {
		if (userRepository.existsByPhoneNumber(phoneNumber))
			return true;
		else
			return false;
	}

	public boolean checkEmail(String email) {
		if (userRepository.existsByEmail(email))
			return true;
		else
			return false;
	}

	public void sendVerificationEmail(User user) throws UnsupportedEncodingException, MessagingException {
		Random random = new Random();
		int vetificationCode = random.nextInt((999999 - 100000) + 1) + 100000;
		user.setVerificationCode(vetificationCode + "");
		userRepository.save(user);
		String toAddress = user.getEmail();
		String fromAddress = "chat_app_email";
		String senderName = "chat_app_admin";
		String subject = "Please verify your registration";
		String content = "Hello " + user.getDisplayName() + ",<br>" + "This is verification code :<br>" + "Code :"
				+ user.getVerificationCode() + "<br>" + "Welcome to our social network,<br>" + "Chat App -->>>>.";

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(fromAddress, senderName);
		helper.setTo(toAddress);
		helper.setSubject(subject);
		helper.setText(content, true);

		mailSender.send(message);
	}

	public boolean vetify(User user) {
		User userDB = userRepository.findByEmail(user.getEmail());
		if (userDB == null)
			return false;
		if (userDB.getVerificationCode().equalsIgnoreCase(user.getVerificationCode())) {
			userDB.setEnable(true);
			userDB.setVerificationCode(null);
			userRepository.save(userDB);
			return true;
		} else
			return false;
	}

	

	public boolean updateInformation(UserSignupDto dto) {
		if(userRepository.existsByPhoneNumber(dto.getPhoneNumber()) && !userRepository.findById(dto.getId()).get().getPhoneNumber().equals(dto.getPhoneNumber()))
			return false;
		Optional<User> optional = userRepository.findById(dto.getId());
		User user = optional.get();
		user.setDisplayName(dto.getDisplayName());
		user.setPassword(dto.getPassword());
		user.setPhoneNumber(dto.getPhoneNumber());
		userRepository.save(user);
		return true;
	}
	
	public User findById(String id) {
		User user = null;
		Optional<User> optional = userRepository.findById(id);
		if(optional.isPresent())
			user = optional.get();
		return user;
	}
	
	public User findByEmail(String email) {
		User user = userRepository.findByEmail(email);
		return user;
	}
	
	public boolean regexEmail(String email) {
		Pattern pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(email);
		return matcher.find();
	}

}
