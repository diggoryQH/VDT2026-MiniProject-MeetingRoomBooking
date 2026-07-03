package com.nandestech.meetingroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.nandestech.meetingroom.entity.BookingStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponse {

    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("room_name")
    private String roomName;

    @JsonProperty("room_department_id")
    private Long roomDepartmentId;

    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private BookingStatus status;

    private String description;

    @JsonProperty("approved_by")
    private Long approvedBy;

    @JsonProperty("approved_by_name")
    private String approvedByName;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String title;

    @JsonProperty("attendee_count")
    private Integer attendeeCount;

    @JsonProperty("cancel_reason")
    private String cancelReason;

    @JsonProperty("rejected_reason")
    private String rejectedReason;

    @JsonProperty("checked_in_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkedInAt;

    @JsonProperty("checked_out_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkedOutAt;

    @JsonProperty("recurring_group_id")
    private String recurringGroupId;

    @JsonProperty("attendee_ids")
    private java.util.List<Long> attendeeIds;
}
