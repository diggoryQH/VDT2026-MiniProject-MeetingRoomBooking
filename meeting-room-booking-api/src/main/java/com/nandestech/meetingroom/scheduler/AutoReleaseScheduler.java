package com.nandestech.meetingroom.scheduler;

import com.nandestech.meetingroom.entity.Booking;
import com.nandestech.meetingroom.entity.BookingStatus;
import com.nandestech.meetingroom.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutoReleaseScheduler {

    @Autowired
    private BookingRepository bookingRepository;

    // Run every 5 minutes
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void releaseUnattendedBookings() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        
        // Find bookings that are APPROVED, have passed start time + 15 mins, but not CHECKED_IN
        // In our DB, status is APPROVED until they check-in.
        // We will fetch APPROVED bookings where startTime <= cutoffTime
        
        List<Booking> unattended = bookingRepository.findByStatusAndStartTimeLessThanEqual(
                BookingStatus.APPROVED, cutoffTime);
                
        for (Booking booking : unattended) {
            booking.setStatus(BookingStatus.NO_SHOW);
            booking.setUpdatedAt(LocalDateTime.now());
            // Optionally, we could send an email/notification here
        }
        
        if (!unattended.isEmpty()) {
            bookingRepository.saveAll(unattended);
            System.out.println("Auto-released " + unattended.size() + " unattended bookings (NO_SHOW)");
        }
    }
}
