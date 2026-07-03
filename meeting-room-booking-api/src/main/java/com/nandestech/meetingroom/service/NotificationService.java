package com.nandestech.meetingroom.service;

import com.nandestech.meetingroom.dto.NotificationResponse;
import com.nandestech.meetingroom.entity.Notification;
import com.nandestech.meetingroom.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(Long userId, String type, String title, String message, Long bookingId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .bookingId(bookingId)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> getNotifications(Long userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId);
    }

    public boolean hasReminderBeenSent(Long bookingId) {
        return notificationRepository.existsByBookingIdAndType(bookingId, "REMINDER");
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .bookingId(notification.getBookingId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
