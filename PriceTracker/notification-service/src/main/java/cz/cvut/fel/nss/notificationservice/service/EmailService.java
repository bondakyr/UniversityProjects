package cz.cvut.fel.nss.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.from-address}")
    private String fromAddress;

    @Value("${notification.fail-soft:true}")
    private boolean failSoft;

    public boolean send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to {} (subject={})", to, subject);
            return true;
        } catch (Exception ex) {
            log.warn("Email send failed (to={}, subject={}): {}", to, subject, ex.getMessage());
            if (!failSoft) throw ex;
            return false;
        }
    }
}
