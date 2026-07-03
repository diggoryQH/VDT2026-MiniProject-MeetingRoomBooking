package com.nandestech.meetingroom.service;

import com.nandestech.meetingroom.entity.Booking;
import com.nandestech.meetingroom.entity.BookingStatus;
import com.nandestech.meetingroom.entity.Room;
import com.nandestech.meetingroom.entity.User;
import com.nandestech.meetingroom.repository.BookingRepository;
import com.nandestech.meetingroom.repository.RoomRepository;
import com.nandestech.meetingroom.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public byte[] exportBookingsToExcel(String status, Long roomId, Long userId,
                                         LocalDateTime startDate, LocalDateTime endDate,
                                         String username, String role) throws IOException {
        Specification<Booking> spec = (root, query, cb) -> cb.conjunction();

        boolean isAdmin = "ADMIN".equals(role) || "SUPERADMIN".equals(role);
        if (!isAdmin) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if ("APPROVER".equals(role)) {
                List<Long> deptRoomIds = roomRepository.findByDepartmentId(user.getDepartmentId())
                        .stream().map(Room::getId).collect(Collectors.toList());
                        
                spec = spec.and((root, query, cb) -> cb.or(
                        cb.equal(root.get("userId"), user.getId()),
                        deptRoomIds.isEmpty() ? cb.disjunction() : root.get("roomId").in(deptRoomIds)
                ));
            } else {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), user.getId()));
            }
        }

        if (status != null && !status.isBlank()) {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), bookingStatus));
        }
        if (roomId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("roomId"), roomId));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), startDate));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), endDate));
        }

        List<Booking> bookings = bookingRepository.findAll(spec);

        // Prefetch users and rooms for efficiency
        Map<Long, String> userNames = userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getId, u -> u.getName() != null ? u.getName() : "Unknown", (a, b) -> a));
        Map<Long, String> roomNames = roomRepository.findAll().stream()
                .collect(Collectors.toMap(Room::getId, r -> r.getName() != null ? r.getName() : "Unknown", (a, b) -> a));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Bookings");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Headers
            String[] headers = {"ID", "User", "Room", "Start Time", "End Time", "Status", "Description", "Created At"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIndex = 1;
            for (Booking booking : bookings) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(booking.getId());
                row.createCell(1).setCellValue(userNames.getOrDefault(booking.getUserId(), "Unknown"));
                row.createCell(2).setCellValue(roomNames.getOrDefault(booking.getRoomId(), "Unknown"));
                row.createCell(3).setCellValue(booking.getStartTime() != null ? booking.getStartTime().format(DATE_FORMAT) : "");
                row.createCell(4).setCellValue(booking.getEndTime() != null ? booking.getEndTime().format(DATE_FORMAT) : "");
                row.createCell(5).setCellValue(booking.getStatus() != null ? booking.getStatus().name() : "");
                row.createCell(6).setCellValue(booking.getDescription() != null ? booking.getDescription() : "");
                row.createCell(7).setCellValue(booking.getCreatedAt() != null ? booking.getCreatedAt().format(DATE_FORMAT) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
