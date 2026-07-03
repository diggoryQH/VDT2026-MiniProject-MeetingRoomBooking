package com.nandestech.meetingroom.repository;

import com.nandestech.meetingroom.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByDepartmentId(Long departmentId, Pageable pageable);
    
    List<Room> findByDepartmentId(Long departmentId);

    Page<Room> findByIsAvailable(Boolean isAvailable, Pageable pageable);

    @Query("SELECT r FROM Room r WHERE " +
           "(cast(:name as string) IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', cast(:name as string), '%'))) " +
           "AND (cast(:location as string) IS NULL OR LOWER(r.location) LIKE LOWER(CONCAT('%', cast(:location as string), '%'))) " +
           "AND (:minCapacity IS NULL OR r.capacity >= :minCapacity) " +
           "AND (:departmentId IS NULL OR r.departmentId = :departmentId)")
    Page<Room> searchRooms(
            @Param("name") String name,
            @Param("location") String location,
            @Param("minCapacity") Integer minCapacity,
            @Param("departmentId") Long departmentId,
            Pageable pageable);
}
