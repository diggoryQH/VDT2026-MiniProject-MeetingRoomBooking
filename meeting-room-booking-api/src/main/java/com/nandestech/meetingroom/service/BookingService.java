package com.nandestech.meetingroom.service;

import com.nandestech.meetingroom.config.BookingConfig;
import com.nandestech.meetingroom.dto.BookingRequest;
import com.nandestech.meetingroom.dto.BookingResponse;
import com.nandestech.meetingroom.entity.Booking;
import com.nandestech.meetingroom.entity.BookingStatus;
import com.nandestech.meetingroom.entity.Room;
import com.nandestech.meetingroom.entity.User;
import com.nandestech.meetingroom.repository.BookingRepository;
import com.nandestech.meetingroom.repository.RoomRepository;
import com.nandestech.meetingroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "SUPERADMIN");
    private static final Set<String> APPROVER_ROLES = Set.of("ADMIN", "SUPERADMIN", "APPROVER");

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BookingConfig bookingConfig;

    private static final java.time.format.DateTimeFormatter DATE_FORMAT = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional
    public BookingResponse createBooking(BookingRequest req, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getIsAvailable()) {
            throw new RuntimeException("Room is not available for booking");
        }

        if (req.getAttendeeCount() != null && req.getAttendeeCount() > room.getCapacity()) {
            throw new RuntimeException("Attendee count exceeds room capacity of " + room.getCapacity());
        }

        java.util.List<Booking> bookingsToSave = new java.util.ArrayList<>();
        String recurringGroupId = null;
        
        LocalDateTime currentStartTime = req.getStartTime();
        LocalDateTime currentEndTime = req.getEndTime();
        LocalDateTime recurrenceEnd = req.getRecurrenceEndDate();
        String rType = req.getRecurrenceType();
        
        if (rType != null && !"NONE".equalsIgnoreCase(rType) && recurrenceEnd != null) {
            recurringGroupId = java.util.UUID.randomUUID().toString();
            if (recurrenceEnd.isBefore(currentStartTime)) {
                throw new RuntimeException("Recurrence end date must be after start date");
            }
        } else {
            recurrenceEnd = currentStartTime;
            rType = "NONE";
        }
        
        while (!currentStartTime.isAfter(recurrenceEnd)) {
            validateBookingTime(currentStartTime, currentEndTime);
            
            // Pessimistic lock: acquire row-level locks on existing overlapping bookings
            List<Booking> overlapping = bookingRepository.findOverlappingBookingsForUpdate(
                    req.getRoomId(), currentStartTime, currentEndTime);
            if (!overlapping.isEmpty()) {
                throw new RuntimeException("The room is already booked for " + currentStartTime.format(DATE_FORMAT));
            }
            
            Booking booking = Booking.builder()
                    .userId(user.getId())
                    .roomId(req.getRoomId())
                    .startTime(currentStartTime)
                    .endTime(currentEndTime)
                    .status(BookingStatus.PENDING)
                    .description(req.getDescription())
                    .title(req.getTitle())
                    .attendeeCount(req.getAttendeeCount())
                    .attendeeIds(req.getAttendeeIds() != null ? new java.util.ArrayList<>(req.getAttendeeIds()) : new java.util.ArrayList<>())
                    .recurringGroupId(recurringGroupId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            bookingsToSave.add(booking);
            
            if ("DAILY".equalsIgnoreCase(rType)) {
                currentStartTime = currentStartTime.plusDays(1);
                currentEndTime = currentEndTime.plusDays(1);
            } else if ("WEEKLY".equalsIgnoreCase(rType)) {
                currentStartTime = currentStartTime.plusWeeks(1);
                currentEndTime = currentEndTime.plusWeeks(1);
            } else if ("MONTHLY".equalsIgnoreCase(rType)) {
                currentStartTime = currentStartTime.plusMonths(1);
                currentEndTime = currentEndTime.plusMonths(1);
            } else {
                break;
            }
        }

        java.util.List<Booking> savedBookings;
        try {
            savedBookings = bookingRepository.saveAll(bookingsToSave);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("The room is already booked for one of the selected time slots");
        }

        if (!savedBookings.isEmpty()) {
            notifyApproversOfNewBooking(savedBookings.get(0), user, room);
        }

        return toResponse(savedBookings.get(0));
    }

    public Page<BookingResponse> getAllBookings(String role, String username, int page, int limit,
                                                String status, Long roomId, Long userId,
                                                LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean isAdmin = ADMIN_ROLES.contains(role);

        Specification<Booking> spec = (root, query, cb) -> {
            query.distinct(true);
            return cb.conjunction();
        };

        // Non-admin users can only see their own bookings, bookings where they are attendees 
        // (and APPROVERs also see bookings in their department)
        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if ("APPROVER".equals(role)) {
                List<Long> deptRoomIds = roomRepository.findByDepartmentId(user.getDepartmentId())
                        .stream().map(Room::getId).collect(Collectors.toList());
                        
                spec = spec.and((root, query, cb) -> {
                    jakarta.persistence.criteria.Join<Booking, Long> attendeeJoin = root.join("attendeeIds", jakarta.persistence.criteria.JoinType.LEFT);
                    return cb.or(
                            cb.equal(root.get("userId"), user.getId()),
                            cb.equal(attendeeJoin, user.getId()),
                            deptRoomIds.isEmpty() ? cb.disjunction() : root.get("roomId").in(deptRoomIds)
                    );
                });
            } else {
                spec = spec.and((root, query, cb) -> {
                    jakarta.persistence.criteria.Join<Booking, Long> attendeeJoin = root.join("attendeeIds", jakarta.persistence.criteria.JoinType.LEFT);
                    return cb.or(
                            cb.equal(root.get("userId"), user.getId()),
                            cb.equal(attendeeJoin, user.getId())
                    );
                });
            }
        } else if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }

        if (status != null && !status.isBlank()) {
            try {
                BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), bookingStatus));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid booking status: " + status);
            }
        }

        if (roomId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("roomId"), roomId));
        }

        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        }

        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), endDate));
        }

        return bookingRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public List<BookingResponse> getCalendarBookings(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByStartTimeBetweenAndActiveStatus(start, end)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id, String role, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!ADMIN_ROLES.contains(role)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            boolean isOwner = booking.getUserId().equals(user.getId());
            boolean isDeptApprover = false;
            if ("APPROVER".equals(role) && user.getDepartmentId() != null) {
                Room room = roomRepository.findById(booking.getRoomId()).orElse(null);
                isDeptApprover = room != null && user.getDepartmentId().equals(room.getDepartmentId());
            }
            if (!isOwner && !isDeptApprover) {
                throw new RuntimeException("Access denied: You can only view your own bookings");
            }
        }

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest req, String username, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!ADMIN_ROLES.contains(role)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!booking.getUserId().equals(user.getId()) || booking.getStatus() != BookingStatus.PENDING) {
                throw new RuntimeException("Access denied: You can only edit your own PENDING bookings");
            }
        }

        validateBookingTime(req.getStartTime(), req.getEndTime());

        // Pessimistic lock check for the new time slot (excluding current booking)
        List<Booking> overlapping = bookingRepository.findOverlappingBookingsForUpdate(
                req.getRoomId(), req.getStartTime(), req.getEndTime());
        overlapping.removeIf(b -> b.getId().equals(id));
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("The room is already booked for the selected time slot");
        }

        booking.setRoomId(req.getRoomId());
        booking.setStartTime(req.getStartTime());
        booking.setEndTime(req.getEndTime());
        booking.setDescription(req.getDescription());
        booking.setTitle(req.getTitle());
        if (req.getAttendeeCount() != null) {
            Room room = roomRepository.findById(req.getRoomId()).orElse(null);
            if (room != null && req.getAttendeeCount() > room.getCapacity()) {
                throw new RuntimeException("Attendee count exceeds room capacity");
            }
            booking.setAttendeeCount(req.getAttendeeCount());
        }
        booking.setUpdatedAt(LocalDateTime.now());

        try {
            booking = bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("The room is already booked for the selected time slot");
        }

        return toResponse(booking);
    }

    public void deleteBooking(Long id, String username, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!ADMIN_ROLES.contains(role)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!booking.getUserId().equals(user.getId()) || booking.getStatus() != BookingStatus.PENDING) {
                throw new RuntimeException("Access denied: You can only delete your own PENDING bookings");
            }
        }

        booking.setIsDeleted(true);
        bookingRepository.save(booking);
    }

    @Transactional
    public BookingResponse approveBooking(Long id, String role, String username, Long approverDepartmentId) {
        if (!APPROVER_ROLES.contains(role)) {
            throw new RuntimeException("Access denied: Only approvers can approve bookings");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // State machine validation
        booking.getStatus().validateTransitionTo(BookingStatus.APPROVED);

        // APPROVER can only approve bookings for rooms in their department
        if ("APPROVER".equals(role)) {
            Room room = roomRepository.findById(booking.getRoomId()).orElse(null);
            if (room != null && room.getDepartmentId() != null
                    && !room.getDepartmentId().equals(approverDepartmentId)) {
                throw new RuntimeException("Access denied: You can only approve bookings for rooms in your department");
            }
        }

        User approver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver.getId());
        booking.setUpdatedAt(LocalDateTime.now());

        final Booking savedBooking = bookingRepository.save(booking);

        // Notify the requester
        notificationService.createNotification(
                savedBooking.getUserId(),
                "BOOKING_APPROVED",
                "Booking Approved",
                "Your booking for room has been approved by " + approver.getName(),
                savedBooking.getId()
        );

        // Send Email & Notify attendees
        userRepository.findById(savedBooking.getUserId()).ifPresent(requester -> {
            String roomName = roomRepository.findById(savedBooking.getRoomId()).map(Room::getName).orElse("Unknown Room");
            String htmlBody = emailService.buildBookingStatusTemplate(
                    requester.getName(), 
                    roomName, 
                    savedBooking.getStartTime().format(DATE_FORMAT), 
                    "APPROVED", 
                    null
            );
            emailService.sendEmailAsync(requester.getEmail(), "Thông báo: Lịch đặt phòng đã được duyệt", htmlBody);

            // Notify attendees
            if (savedBooking.getAttendeeIds() != null && !savedBooking.getAttendeeIds().isEmpty()) {
                for (Long attendeeId : savedBooking.getAttendeeIds()) {
                    userRepository.findById(attendeeId).ifPresent(attendee -> {
                        notificationService.createNotification(
                                attendeeId,
                                "BOOKING_INVITATION",
                                "Meeting Invitation",
                                "You have been invited to a meeting by " + requester.getName() + " on " + savedBooking.getStartTime().format(DATE_FORMAT),
                                savedBooking.getId()
                        );
                        String htmlBodyAttendee = emailService.buildBookingStatusTemplate(
                                attendee.getName(), 
                                roomName, 
                                savedBooking.getStartTime().format(DATE_FORMAT), 
                                "APPROVED", 
                                null
                        );
                        emailService.sendEmailAsync(attendee.getEmail(), "Thông báo: Mời tham gia cuộc họp", htmlBodyAttendee);
                    });
                }
            }
        });

        return toResponse(savedBooking);
    }

    @Transactional
    public BookingResponse rejectBooking(Long id, String role, String username, Long approverDepartmentId, String reason) {
        if (!APPROVER_ROLES.contains(role)) {
            throw new RuntimeException("Access denied: Only approvers can reject bookings");
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // State machine validation
        booking.getStatus().validateTransitionTo(BookingStatus.REJECTED);

        // APPROVER can only reject bookings for rooms in their department
        if ("APPROVER".equals(role)) {
            Room room = roomRepository.findById(booking.getRoomId()).orElse(null);
            if (room != null && room.getDepartmentId() != null
                    && !room.getDepartmentId().equals(approverDepartmentId)) {
                throw new RuntimeException("Access denied: You can only reject bookings for rooms in your department");
            }
        }

        User approver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        booking.setStatus(BookingStatus.REJECTED);
        booking.setApprovedBy(approver.getId());
        booking.setRejectedReason(reason);
        booking.setUpdatedAt(LocalDateTime.now());

        final Booking savedBooking = bookingRepository.save(booking);

        String message = "Your booking has been rejected by " + approver.getName();
        if (reason != null && !reason.isBlank()) {
            message += ". Reason: " + reason;
        }

        notificationService.createNotification(
                savedBooking.getUserId(),
                "BOOKING_REJECTED",
                "Booking Rejected",
                message,
                savedBooking.getId()
        );

        // Send Email
        userRepository.findById(savedBooking.getUserId()).ifPresent(requester -> {
            String roomName = roomRepository.findById(savedBooking.getRoomId()).map(Room::getName).orElse("Unknown Room");
            String htmlBody = emailService.buildBookingStatusTemplate(
                    requester.getName(), 
                    roomName, 
                    savedBooking.getStartTime().format(DATE_FORMAT), 
                    "REJECTED", 
                    reason
            );
            emailService.sendEmailAsync(requester.getEmail(), "Thông báo: Lịch đặt phòng bị từ chối", htmlBody);
        });

        return toResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long id, String username, String role, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!ADMIN_ROLES.contains(role)) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!booking.getUserId().equals(user.getId())) {
                throw new RuntimeException("Access denied: You can only cancel your own bookings");
            }
        }

        // State machine validation
        booking.getStatus().validateTransitionTo(BookingStatus.CANCELLED);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(reason);
        booking.setUpdatedAt(LocalDateTime.now());
        final Booking savedBooking = bookingRepository.save(booking);

        userRepository.findById(savedBooking.getUserId()).ifPresent(requester -> {
            String roomName = roomRepository.findById(savedBooking.getRoomId()).map(Room::getName).orElse("Unknown Room");
            
            notificationService.createNotification(
                    requester.getId(),
                    "BOOKING_CANCELLED",
                    "Booking Cancelled",
                    "Your booking for " + roomName + " has been cancelled. Reason: " + reason,
                    savedBooking.getId()
            );
            
            String htmlBodyRequester = emailService.buildBookingStatusTemplate(
                    requester.getName(), 
                    roomName, 
                    savedBooking.getStartTime().format(DATE_FORMAT), 
                    "CANCELLED", 
                    reason
            );
            emailService.sendEmailAsync(requester.getEmail(), "Thông báo: Lịch đặt phòng đã bị hủy", htmlBodyRequester);

            if (savedBooking.getAttendeeIds() != null && !savedBooking.getAttendeeIds().isEmpty()) {
                for (Long attendeeId : savedBooking.getAttendeeIds()) {
                    userRepository.findById(attendeeId).ifPresent(attendee -> {
                        notificationService.createNotification(
                                attendeeId,
                                "BOOKING_CANCELLED",
                                "Meeting Cancelled",
                                "The meeting scheduled on " + savedBooking.getStartTime().format(DATE_FORMAT) + " has been cancelled.",
                                savedBooking.getId()
                        );
                        String htmlBodyAttendee = emailService.buildBookingStatusTemplate(
                                attendee.getName(), 
                                roomName, 
                                savedBooking.getStartTime().format(DATE_FORMAT), 
                                "CANCELLED", 
                                reason
                        );
                        emailService.sendEmailAsync(attendee.getEmail(), "Thông báo: Cuộc họp đã bị hủy", htmlBodyAttendee);
                    });
                }
            }
        });

        return toResponse(savedBooking);
    }

    @Transactional
    public BookingResponse checkInBooking(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!booking.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You can only check-in your own bookings");
        }

        booking.getStatus().validateTransitionTo(BookingStatus.CHECKED_IN);

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        return toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse checkOutBooking(Long id, String username) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!booking.getUserId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You can only check-out your own bookings");
        }

        booking.getStatus().validateTransitionTo(BookingStatus.COMPLETED);

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCheckedOutAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        return toResponse(bookingRepository.save(booking));
    }

    // ── Business validation ─────────────────────────────────────────────

    private void validateBookingTime(LocalDateTime start, LocalDateTime end) {
        if (start.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Booking cannot be made for past dates");
        }
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new RuntimeException("End time must be after start time");
        }

        // Working hours validation
        int startHour = start.getHour();
        int endHour = end.getHour();
        int endMinute = end.getMinute();
        // If end is exactly at the boundary (e.g. 22:00), that's fine.
        // If end goes beyond (e.g. 22:01), reject.
        if (startHour < bookingConfig.getWorkdayStartHour()) {
            throw new RuntimeException(
                    String.format("Booking cannot start before %02d:00", bookingConfig.getWorkdayStartHour()));
        }
        if (endHour > bookingConfig.getWorkdayEndHour() ||
                (endHour == bookingConfig.getWorkdayEndHour() && endMinute > 0)) {
            throw new RuntimeException(
                    String.format("Booking cannot end after %02d:00", bookingConfig.getWorkdayEndHour()));
        }

        // Duration validation
        Duration duration = Duration.between(start, end);
        if (duration.toMinutes() < bookingConfig.getMinDurationMinutes()) {
            throw new RuntimeException(
                    String.format("Minimum booking duration is %d minutes", bookingConfig.getMinDurationMinutes()));
        }
        if (duration.toHours() >= bookingConfig.getMaxDurationHours() &&
                duration.toMinutes() > (long) bookingConfig.getMaxDurationHours() * 60) {
            throw new RuntimeException(
                    String.format("Maximum booking duration is %d hours", bookingConfig.getMaxDurationHours()));
        }

        // Advance booking limit
        if (start.isAfter(LocalDateTime.now().plusDays(bookingConfig.getMaxAdvanceBookingDays()))) {
            throw new RuntimeException(
                    String.format("Bookings can only be made up to %d days in advance", bookingConfig.getMaxAdvanceBookingDays()));
        }
    }

    // ── Notification helpers ────────────────────────────────────────────

    private void notifyApproversOfNewBooking(Booking booking, User requester, Room room) {
        try {
            Long departmentId = room.getDepartmentId();
            List<User> approvers;
            if (departmentId != null) {
                // Find approvers in the room's department + all admins
                approvers = userRepository.findByDepartmentIdAndRole(departmentId, "APPROVER");
                approvers.addAll(userRepository.findByRole("ADMIN"));
                approvers.addAll(userRepository.findByRole("SUPERADMIN"));
            } else {
                // Shared room - notify all admins and approvers
                approvers = userRepository.findByRole("ADMIN");
                approvers.addAll(userRepository.findByRole("SUPERADMIN"));
                approvers.addAll(userRepository.findByRole("APPROVER"));
            }

            // Deduplicate and exclude the requester
            approvers.stream()
                    .map(User::getId)
                    .distinct()
                    .filter(id -> !id.equals(requester.getId()))
                    .forEach(approverId -> notificationService.createNotification(
                            approverId,
                            "NEW_BOOKING",
                            "New Booking Request",
                            requester.getName() + " requested to book " + room.getName(),
                            booking.getId()
                    ));
        } catch (Exception e) {
            // Don't fail the booking creation if notification fails
            System.err.println("Failed to send notifications: " + e.getMessage());
        }
    }

    // ── Response mapping ────────────────────────────────────────────────

    private BookingResponse toResponse(Booking booking) {
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .roomId(booking.getRoomId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .description(booking.getDescription())
                .title(booking.getTitle())
                .attendeeCount(booking.getAttendeeCount())
                .cancelReason(booking.getCancelReason())
                .rejectedReason(booking.getRejectedReason())
                .checkedInAt(booking.getCheckedInAt())
                .checkedOutAt(booking.getCheckedOutAt())
                .recurringGroupId(booking.getRecurringGroupId())
                .approvedBy(booking.getApprovedBy())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .attendeeIds(booking.getAttendeeIds() != null ? new java.util.ArrayList<>(booking.getAttendeeIds()) : new java.util.ArrayList<>());

        // Use JPA relationships if loaded (avoids N+1 queries when fetched via
        // @EntityGraph or JOIN FETCH). Fall back to individual lookups otherwise.
        if (booking.getUser() != null) {
            builder.userName(booking.getUser().getName());
        } else {
            try {
                userRepository.findById(booking.getUserId())
                        .ifPresent(user -> builder.userName(user.getName()));
            } catch (Exception ignored) {}
        }

        if (booking.getRoom() != null) {
            builder.roomName(booking.getRoom().getName());
            builder.roomDepartmentId(booking.getRoom().getDepartmentId());
        } else {
            try {
                roomRepository.findById(booking.getRoomId())
                        .ifPresent(room -> {
                            builder.roomName(room.getName());
                            builder.roomDepartmentId(room.getDepartmentId());
                        });
            } catch (Exception ignored) {}
        }

        if (booking.getApprovedBy() != null) {
            if (booking.getApprover() != null) {
                builder.approvedByName(booking.getApprover().getName());
            } else {
                try {
                    userRepository.findById(booking.getApprovedBy())
                            .ifPresent(approver -> builder.approvedByName(approver.getName()));
                } catch (Exception ignored) {}
            }
        }

        return builder.build();
    }
}
