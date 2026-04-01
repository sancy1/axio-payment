package com.axioquan.payment_service.modules.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

/**
 * Email service implementation using Spring Mail.
 * Sends transactional emails via configured SMTP server (Gmail SMTP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${SMTP_FROM:AxioQuan <diolux.inc@gmail.com>}")
    private String fromEmail;

    @Override
    public void send(String to, String subject, String body) {
        log.info("Sending email to: {} with subject: {}", to, subject);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Email send failed", e);
        }
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        log.info("Sending HTML email to: {} with subject: {}", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("HTML email send failed", e);
        }
    }
}

