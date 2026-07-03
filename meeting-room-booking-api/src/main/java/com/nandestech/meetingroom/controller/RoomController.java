package com.nandestech.meetingroom.controller;

import com.nandestech.meetingroom.dto.ApiResponse;
import com.nandestech.meetingroom.dto.RoomRequest;
import com.nandestech.meetingroom.dto.RoomResponse;
import com.nandestech.meetingroom.service.RoomService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@Tag(name = "Room Management", description = "CRUD operations for meeting rooms. Read is public; write is admin-only.")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a room", description = "Admin only. Create a new meeting room.")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody RoomRequest request) {
        RoomResponse data = roomService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping
    @Operation(summary = "Get all rooms", description = "List rooms with optional search and filter.")
    public ResponseEntity<ApiResponse<Page<RoomResponse>>> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) @Parameter(description = "Search by name") String name,
            @RequestParam(required = false) @Parameter(description = "Search by location") String location,
            @RequestParam(required = false) @Parameter(description = "Minimum capacity") Integer minCapacity,
            @RequestParam(required = false) @Parameter(description = "Filter by department") Long departmentId) {
        Page<RoomResponse> data = roomService.getAllRooms(page, limit, name, location, minCapacity, departmentId);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse data = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a room", description = "Admin only.")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        RoomResponse data = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a room", description = "Admin only.")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status("success")
                .message("Room deleted successfully")
                .build());
    }
}
