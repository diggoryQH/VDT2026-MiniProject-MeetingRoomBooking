package com.nandestech.meetingroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private String type;

    private String title;

    private String message;

    @JsonProperty("is_read")
    private Boolean isRead;

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
