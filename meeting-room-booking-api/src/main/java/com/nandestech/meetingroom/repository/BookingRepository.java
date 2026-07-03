package com.nandestech.meetingroom.repository;

import com.nandestech.meetingroom.entity.Booking;
import com.nandestech.meetingroom.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    // ── Overlap checks ──────────────────────────────────────────────────

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.roomId = :roomId " +
           "AND b.status IN (com.nandestech.meetingroom.entity.BookingStatus.PENDING, com.nandestech.meetingroom.entity.BookingStatus.APPROVED) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.roomId = :roomId " +
           "AND b.id <> :excludeId " +
           "AND b.status IN (com.nandestech.meetingroom.entity.BookingStatus.PENDING, com.nandestech.meetingroom.entity.BookingStatus.APPROVED) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    boolean existsOverlappingBookingExcluding(
            @Param("roomId") Long roomId,
            @Param("excludeId") Long excludeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Pessimistic lock on overlapping bookings for a room.
     * Used to prevent race conditions: acquires row-level locks so concurrent
     * transactions must wait before checking/inserting for the same room + time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId " +
           "AND b.status IN (com.nandestech.meetingroom.entity.BookingStatus.PENDING, com.nandestech.meetingroom.entity.BookingStatus.APPROVED) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookingsForUpdate(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ── Basic finders ───────────────────────────────────────────────────

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(Long userId, BookingStatus status, Pageable pageable);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByUserIdAndStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.roomId = :roomId " +
           "AND b.status IN (com.nandestech.meetingroom.entity.BookingStatus.PENDING, com.nandestech.meetingroom.entity.BookingStatus.APPROVED) " +
           "AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ── Calendar & scheduler queries ────────────────────────────────────

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :start AND :end " +
           "AND b.status IN (com.nandestech.meetingroom.entity.BookingStatus.PENDING, com.nandestech.meetingroom.entity.BookingStatus.APPROVED)")
    @EntityGraph(attributePaths = {"user", "room"})
    List<Booking> findByStartTimeBetweenAndActiveStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.status = com.nandestech.meetingroom.entity.BookingStatus.APPROVED " +
           "AND b.startTime BETWEEN :start AND :end")
    List<Booking> findApprovedBookingsStartingBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<Booking> findByStatusAndStartTimeLessThanEqual(BookingStatus status, LocalDateTime startTime);
}
