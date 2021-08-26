package iuh.dhktpm14.cnm.chatappmongo.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailService {

    @Getter
    private String otp;

    @Autowired
    private JavaMailSender javaMailSender;

    private Random random = new Random();

    public void sendOtp(String to) {
        otp = String.valueOf(random.nextLong());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("otp");
        message.setText(otp);
        javaMailSender.send(message);
    }

}
