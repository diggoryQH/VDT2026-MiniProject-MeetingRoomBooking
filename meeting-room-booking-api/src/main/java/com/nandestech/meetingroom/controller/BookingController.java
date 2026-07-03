package com.nandestech.meetingroom.controller;

import com.nandestech.meetingroom.dto.ApiResponse;
import com.nandestech.meetingroom.dto.BookingRequest;
import com.nandestech.meetingroom.dto.BookingResponse;
import com.nandestech.meetingroom.service.BookingService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management", description = "CRUD operations and workflow for room bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking", description = "Book a meeting room. Status starts as PENDING.")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username) {
        BookingResponse data = bookingService.createBooking(request, username);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping
    @Operation(summary = "Get all bookings", description = "Admin sees all bookings with filters. Users see only their own.")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @Parameter(description = "Filter by room ID") Long roomId,
            @RequestParam(required = false) @Parameter(description = "Filter by user ID (admin only)") Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "Start date filter") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "End date filter") LocalDateTime endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Page<BookingResponse> data = bookingService.getAllBookings(role, username, page, limit,
                status, roomId, userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/calendar")
    @Operation(summary = "Get bookings for calendar view", description = "Returns active bookings within a date range for calendar display.")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCalendarBookings(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "Range start") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            @Parameter(description = "Range end") LocalDateTime end) {
        List<BookingResponse> data = bookingService.getCalendarBookings(start, end);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role) {
        BookingResponse data = bookingService.getBookingById(id, role, username);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a booking", description = "Admin only. Update room, time, or description.")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role) {
        BookingResponse data = bookingService.updateBooking(id, request, username, role);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a booking", description = "Admin can delete any. Users can delete their own.")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role) {
        bookingService.deleteBooking(id, username, role);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .message("Booking deleted successfully")
                .build());
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "Approve a booking", description = "ADMIN or APPROVER (department-scoped) can approve PENDING bookings.")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute(value = "X-DepartmentId", required = false) Long departmentId) {
        BookingResponse data = bookingService.approveBooking(id, role, username, departmentId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "Reject a booking", description = "ADMIN or APPROVER (department-scoped) can reject PENDING bookings.")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute(value = "X-DepartmentId", required = false) Long departmentId) {
        String reason = body != null ? body.get("reason") : null;
        BookingResponse data = bookingService.rejectBooking(id, role, username, departmentId, reason);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking", description = "Users can cancel their own PENDING/APPROVED bookings. Admins can cancel any.")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username,
            @Parameter(hidden = true) @RequestAttribute("X-Role") String role) {
        String reason = body != null ? body.get("reason") : null;
        BookingResponse data = bookingService.cancelBooking(id, username, role, reason);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/checkin")
    @Operation(summary = "Check in to a booking", description = "Check into an approved booking.")
    public ResponseEntity<ApiResponse<BookingResponse>> checkInBooking(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username) {
        BookingResponse data = bookingService.checkInBooking(id, username);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/checkout")
    @Operation(summary = "Check out from a booking", description = "Check out of a checked-in booking.")
    public ResponseEntity<ApiResponse<BookingResponse>> checkOutBooking(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("X-Username") String username) {
        BookingResponse data = bookingService.checkOutBooking(id, username);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
