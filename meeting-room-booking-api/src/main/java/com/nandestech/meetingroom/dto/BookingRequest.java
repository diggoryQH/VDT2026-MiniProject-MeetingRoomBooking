package com.nandestech.meetingroom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingRequest {

    @NotNull(message = "Room ID is required to create a booking")
    @JsonProperty("room_id")
    private Long roomId;

    @NotNull(message = "Start time is required")
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "Please provide a description for the meeting")
    private String description;

    @NotBlank(message = "Please provide a title for the meeting")
    private String title;

    @NotNull(message = "Attendee count is required")
    @JsonProperty("attendee_count")
    private Integer attendeeCount;

    @JsonProperty("recurrence_type")
    private String recurrenceType; // NONE, DAILY, WEEKLY, MONTHLY

    @JsonProperty("attendee_ids")
    private java.util.List<Long> attendeeIds;

    @JsonProperty("recurrence_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recurrenceEndDate;
}
