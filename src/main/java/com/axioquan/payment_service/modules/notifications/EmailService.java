package com.axioquan.payment_service.modules.notifications;

/**
 * Service for sending emails.
 * Can be implemented with various email providers (SendGrid, AWS SES, etc.)
 */
public interface EmailService {

    /**
     * Send an email.
     * @param to The recipient email address
     * @param subject The email subject
     * @param body The email body (supports HTML)
     */
    void send(String to, String subject, String body);

    /**
     * Send an email with HTML content.
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The email body in HTML format
     */
    void sendHtml(String to, String subject, String htmlBody);
}
