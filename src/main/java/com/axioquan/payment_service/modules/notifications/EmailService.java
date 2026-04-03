package com.axioquan.payment_service.modules.notifications;

/**
 * Service for sending emails.
 * Can be implemented with various email providers (SendGrid, AWS SES, etc.)
 */
public interface EmailService {

    /**
     * Send an email (blocking - waits for completion).
     * @param to The recipient email address
     * @param subject The email subject
     * @param body The email body (supports HTML)
     */
    void send(String to, String subject, String body);

    /**
     * Send an email with HTML content (blocking - waits for completion).
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The email body in HTML format
     */
    void sendHtml(String to, String subject, String htmlBody);

    /**
     * Send an email asynchronously (non-blocking - returns immediately).
     * Failures are logged but do NOT affect calling code.
     * @param to The recipient email address
     * @param subject The email subject
     * @param htmlBody The email body in HTML format
     */
    void sendHtmlAsync(String to, String subject, String htmlBody);
}
