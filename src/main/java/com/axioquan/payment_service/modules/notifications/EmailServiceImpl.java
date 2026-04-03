package com.axioquan.payment_service.modules.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

/**
 * Email service implementation using Spring Mail.
 * Sends transactional emails via configured SMTP server (Gmail SMTP).
 * 
 * Features:
 * - Synchronous send (sendHtml) - waits for completion
 * - Asynchronous send (sendHtmlAsync) - returns immediately, non-blocking
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

    /**
     * Send HTML email ASYNCHRONOUSLY (non-blocking).
     * 
     * IMPORTANT: If email fails, error is LOGGED ONLY.
     * Does NOT affect the calling code or process.
     * Perfect for non-critical notifications like payment confirmations.
     * 
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The email body in HTML format
     */
    @Override
    @Async
    public void sendHtmlAsync(String to, String subject, String htmlBody) {
        log.info("[ASYNC] Sending HTML email to: {} with subject: {}", to, subject);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("[ASYNC] HTML email sent successfully to: {}", to);

        } catch (Exception e) {
            // ✅ IMPORTANT: Log error but DO NOT THROW
            // This ensures email failure doesn't crash the payment process
            log.warn("[ASYNC] Email sending FAILED (non-critical) for {}: {}", 
                to, e.getMessage());
        }
    }
}

