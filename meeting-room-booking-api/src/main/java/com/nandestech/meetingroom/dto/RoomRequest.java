package com.nandestech.meetingroom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {

    @NotBlank(message = "Room name is required")
    private String name;

    @NotNull(message = "Room capacity is required")
    private Integer capacity;

    @NotBlank(message = "Room location is required")
    private String location;

    private String equipment;

    @NotNull(message = "Availability status is required")
    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("department_id")
    private Long departmentId;
}
