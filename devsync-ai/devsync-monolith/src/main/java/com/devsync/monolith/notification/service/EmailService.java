package com.devsync.monolith.notification.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendInvitationEmail(String toEmail, String orgName, String roleName) {
        log.info("[EMAIL SERVICE] Preparing invitation email for {} to join {} as {}", toEmail, orgName, roleName);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject("Invitation to join " + orgName + " on DevSync AI");
            
            String htmlContent = "<h3>You have been invited!</h3>"
                    + "<p>You have been invited to join the organization <strong>" + orgName + "</strong> as a <strong>" + roleName + "</strong> on DevSync AI.</p>"
                    + "<p>Please register a new account on DevSync using this email address: <strong>" + toEmail + "</strong> to accept this invitation.</p>"
                    + "<br/>"
                    + "<p>Best regards,<br/>The DevSync AI Team</p>";
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("[EMAIL SERVICE] Invitation email successfully sent to {}", toEmail);
        } catch (Exception e) {
            log.error("[EMAIL SERVICE] Failed to send invitation email to {}. Error: {}", toEmail, e.getMessage());
            // Safe fallback: do not throw exception to prevent breaking the transaction
        }
    }
}
