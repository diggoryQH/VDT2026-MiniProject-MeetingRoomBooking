package com.nandestech.meetingroom.scheduler;

import com.nandestech.meetingroom.entity.Booking;
import com.nandestech.meetingroom.entity.Room;
import com.nandestech.meetingroom.repository.BookingRepository;
import com.nandestech.meetingroom.repository.RoomRepository;
import com.nandestech.meetingroom.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.nandestech.meetingroom.repository.UserRepository;
import com.nandestech.meetingroom.service.EmailService;

@Component
public class ReminderScheduler {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Runs every 5 minutes.
     * Finds approved bookings starting within the next 15 minutes
     * and sends a reminder notification to the booking owner.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindow = now.plusMinutes(15);

        List<Booking> upcomingBookings = bookingRepository.findApprovedBookingsStartingBetween(now, reminderWindow);

        for (Booking booking : upcomingBookings) {
            // Skip if reminder already sent for this booking
            if (notificationService.hasReminderBeenSent(booking.getId())) {
                continue;
            }

            String roomName = roomRepository.findById(booking.getRoomId())
                    .map(Room::getName)
                    .orElse("Unknown Room");

            String startTime = booking.getStartTime().format(TIME_FORMAT);

            notificationService.createNotification(
                    booking.getUserId(),
                    "REMINDER",
                    "Meeting Reminder",
                    "Your meeting in " + roomName + " starts at " + startTime + ". Please prepare.",
                    booking.getId()
            );

            // Send Email Reminder
            userRepository.findById(booking.getUserId()).ifPresent(requester -> {
                String htmlBody = emailService.buildReminderTemplate(
                        requester.getName(), 
                        roomName, 
                        startTime
                );
                emailService.sendEmailAsync(requester.getEmail(), "Nhắc nhở: Cuộc họp sắp diễn ra", htmlBody);
            });
        }
    }
}
