package com.nandestech.meetingroom.service;

import com.nandestech.meetingroom.dto.RoomRequest;
import com.nandestech.meetingroom.dto.RoomResponse;
import com.nandestech.meetingroom.entity.Department;
import com.nandestech.meetingroom.entity.Room;
import com.nandestech.meetingroom.repository.DepartmentRepository;
import com.nandestech.meetingroom.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public RoomResponse createRoom(RoomRequest req) {
        Room room = Room.builder()
                .name(req.getName())
                .capacity(req.getCapacity())
                .location(req.getLocation())
                .equipment(req.getEquipment())
                .isAvailable(req.getIsAvailable())
                .departmentId(req.getDepartmentId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        room = roomRepository.save(room);
        return toResponse(room);
    }

    public Page<RoomResponse> getAllRooms(int page, int limit, String name, String location,
                                          Integer minCapacity, Long departmentId) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        boolean hasFilters = (name != null && !name.isBlank())
                || (location != null && !location.isBlank())
                || minCapacity != null
                || departmentId != null;

        if (hasFilters) {
            return roomRepository.searchRooms(
                    name != null && !name.isBlank() ? name : null,
                    location != null && !location.isBlank() ? location : null,
                    minCapacity,
                    departmentId,
                    pageable
            ).map(this::toResponse);
        }

        return roomRepository.findAll(pageable).map(this::toResponse);
    }

    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return toResponse(room);
    }

    public RoomResponse updateRoom(Long id, RoomRequest req) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setName(req.getName());
        room.setCapacity(req.getCapacity());
        room.setLocation(req.getLocation());
        room.setEquipment(req.getEquipment());
        room.setIsAvailable(req.getIsAvailable());
        room.setDepartmentId(req.getDepartmentId());
        room.setUpdatedAt(LocalDateTime.now());

        room = roomRepository.save(room);
        return toResponse(room);
    }

    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        roomRepository.delete(room);
    }

    private RoomResponse toResponse(Room room) {
        RoomResponse.RoomResponseBuilder builder = RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .equipment(room.getEquipment())
                .isAvailable(room.getIsAvailable())
                .departmentId(room.getDepartmentId())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt());

        if (room.getDepartmentId() != null) {
            departmentRepository.findById(room.getDepartmentId())
                    .ifPresent(dept -> builder.departmentName(dept.getName()));
        }

        return builder.build();
    }
}
