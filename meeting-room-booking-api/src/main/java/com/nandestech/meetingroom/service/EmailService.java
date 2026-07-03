package com.nandestech.meetingroom.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@viettel.com.vn}")
    private String fromEmail;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private EmailService self;

    @org.springframework.retry.annotation.Retryable(
        retryFor = { Exception.class }, 
        maxAttempts = 3, 
        backoff = @org.springframework.retry.annotation.Backoff(delay = 2000)
    )
    public void sendEmailSync(String to, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail.isEmpty() ? "no-reply@viettel.com.vn" : fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML

        mailSender.send(message);
        System.out.println("Email sent successfully to " + to);
    }

    public void sendEmailAsync(String to, String subject, String htmlBody) {
        CompletableFuture.runAsync(() -> {
            try {
                self.sendEmailSync(to, subject, htmlBody);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + to + " after retries: " + e.getMessage());
            }
        });
    }

    public String buildBookingStatusTemplate(String recipientName, String roomName, String startTime, String status, String reason) {
        String color = "#ef4444";
        String statusText = "Bị từ chối";
        if (status.equals("APPROVED")) {
            color = "#22c55e";
            statusText = "Đã được duyệt";
        } else if (status.equals("CANCELLED")) {
            color = "#f97316";
            statusText = "Đã bị hủy";
        }
        
        String reasonHtml = "";
        if (reason != null && !reason.isBlank()) {
            reasonHtml = "<p style='margin-bottom: 10px;'><strong>Lý do:</strong> " + reason + "</p>";
        }

        return "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;'>" +
               "<div style='background-color: #EE0033; padding: 20px; text-align: center; color: white;'>" +
               "<h2 style='margin: 0;'>Thông báo Lịch đặt phòng</h2>" +
               "</div>" +
               "<div style='padding: 20px;'>" +
               "<p>Chào <strong>" + recipientName + "</strong>,</p>" +
               "<p>Lịch đặt phòng của bạn đã có cập nhật mới.</p>" +
               "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 6px; margin: 20px 0;'>" +
               "<p style='margin-top: 0; margin-bottom: 10px;'><strong>Phòng:</strong> " + roomName + "</p>" +
               "<p style='margin-bottom: 10px;'><strong>Thời gian:</strong> " + startTime + "</p>" +
               "<p style='margin-bottom: 10px;'><strong>Trạng thái:</strong> <span style='color: " + color + "; font-weight: bold;'>" + statusText + "</span></p>" +
               reasonHtml +
               "</div>" +
               "<p>Trân trọng,<br>Hệ thống Quản lý Phòng họp</p>" +
               "</div>" +
               "</div>";
    }

    public String buildReminderTemplate(String recipientName, String roomName, String startTime) {
        return "<div style='font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 8px; overflow: hidden;'>" +
               "<div style='background-color: #EE0033; padding: 20px; text-align: center; color: white;'>" +
               "<h2 style='margin: 0;'>Nhắc nhở Cuộc họp</h2>" +
               "</div>" +
               "<div style='padding: 20px;'>" +
               "<p>Chào <strong>" + recipientName + "</strong>,</p>" +
               "<p>Bạn có một cuộc họp sắp diễn ra.</p>" +
               "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 4px solid #EE0033;'>" +
               "<p style='margin-top: 0; margin-bottom: 10px;'><strong>Phòng:</strong> " + roomName + "</p>" +
               "<p style='margin-bottom: 0;'><strong>Thời gian bắt đầu:</strong> " + startTime + "</p>" +
               "</div>" +
               "<p>Vui lòng chuẩn bị và có mặt đúng giờ nhé.</p>" +
               "<p>Trân trọng,<br>Hệ thống Quản lý Phòng họp</p>" +
               "</div>" +
               "</div>";
    }
}
