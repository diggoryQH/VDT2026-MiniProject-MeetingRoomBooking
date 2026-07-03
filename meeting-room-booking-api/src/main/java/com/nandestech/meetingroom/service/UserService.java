package com.nandestech.meetingroom.service;

import com.nandestech.meetingroom.dto.BookingResponse;
import com.nandestech.meetingroom.dto.UserRequest;
import com.nandestech.meetingroom.dto.UserResponse;
import com.nandestech.meetingroom.entity.Booking;
import com.nandestech.meetingroom.entity.User;
import com.nandestech.meetingroom.repository.BookingRepository;
import com.nandestech.meetingroom.repository.DepartmentRepository;
import com.nandestech.meetingroom.repository.RoomRepository;
import com.nandestech.meetingroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Set<String> VALID_ROLES = Set.of("ADMIN", "APPROVER", "EMPLOYEE");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private RoomRepository roomRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserResponse createUser(UserRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        String role = request.getRole() != null ? request.getRole().toUpperCase() : "EMPLOYEE";
        if (!VALID_ROLES.contains(role)) {
            throw new RuntimeException("Invalid role. Valid roles: ADMIN, APPROVER, EMPLOYEE");
        }

        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setDepartmentId(request.getDepartmentId());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return toManagementResponse(userRepository.save(user));
    }

    public Page<UserResponse> getAllUsers(int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return userRepository.findAll(pageable)
                .map(this::toManagementResponse);
    }

    public java.util.List<UserResponse> getUserDirectory() {
        return userRepository.findAll().stream()
                .map(this::toManagementResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toManagementResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            String role = request.getRole().toUpperCase();
            if (!VALID_ROLES.contains(role)) {
                throw new RuntimeException("Invalid role. Valid roles: ADMIN, APPROVER, EMPLOYEE");
            }
            user.setRole(role);
        }
        if (request.getDepartmentId() != null) {
            departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            user.setDepartmentId(request.getDepartmentId());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return toManagementResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    private UserResponse toManagementResponse(User user) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .departmentId(user.getDepartmentId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (user.getDepartmentId() != null) {
            departmentRepository.findById(user.getDepartmentId())
                    .ifPresent(dept -> builder.departmentName(dept.getName()));
        }

        return builder.build();
    }

    private UserResponse toResponse(User user) {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        List<BookingResponse> bookings = bookingRepository.findByUserIdAndStartTimeBetween(user.getId(), startOfToday, endOfToday)
                .stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());

        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .departmentId(user.getDepartmentId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .bookings(bookings);

        if (user.getDepartmentId() != null) {
            departmentRepository.findById(user.getDepartmentId())
                    .ifPresent(dept -> builder.departmentName(dept.getName()));
        }

        return builder.build();
    }

    private BookingResponse toBookingResponse(Booking booking) {
        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .roomId(booking.getRoomId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .description(booking.getDescription())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt());

        // Use JPA relationship if loaded, otherwise fall back to query
        if (booking.getRoom() != null) {
            builder.roomName(booking.getRoom().getName());
        } else {
            roomRepository.findById(booking.getRoomId())
                    .ifPresent(room -> builder.roomName(room.getName()));
        }

        return builder.build();
    }
}
