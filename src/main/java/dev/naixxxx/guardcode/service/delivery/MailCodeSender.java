package dev.naixxxx.guardcode.service.delivery;

import dev.naixxxx.guardcode.config.AppSettings;
import dev.naixxxx.guardcode.domain.DeliveryChannel;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailCodeSender implements CodeSender {
    private static final Logger log = LoggerFactory.getLogger(MailCodeSender.class);
    private final String username;
    private final String password;
    private final String from;
    private final Session session;

    public MailCodeSender(AppSettings settings) {
        this.username = settings.get("email.username");
        this.password = settings.get("email.password");
        this.from = settings.get("email.from");
        this.session = Session.getInstance(settings.asProperties(), new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override public DeliveryChannel channel() { return DeliveryChannel.EMAIL; }

    @Override public void send(String destination, String code) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("OTP confirmation code");
            message.setText("Your confirmation code: " + code);
            Transport.send(message);
            log.info("OTP email sent to {}", destination);
        } catch (MessagingException e) {
            throw new DeliveryException("Email delivery failed", e);
        }
    }
}
