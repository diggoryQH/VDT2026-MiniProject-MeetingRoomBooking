package com.nandestech.meetingroom.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Externalized business rules for booking validation.
 * All values are configurable via application.properties under the "booking." prefix.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "booking")
public class BookingConfig {

    /** Minimum booking duration in minutes (default: 15) */
    private int minDurationMinutes = 15;

    /** Maximum booking duration in hours (default: 4) */
    private int maxDurationHours = 4;

    /** Earliest hour a booking can start (default: 7 = 07:00) */
    private int workdayStartHour = 7;

    /** Latest hour a booking can end (default: 22 = 22:00) */
    private int workdayEndHour = 22;

    /** Maximum days in advance a booking can be made (default: 30) */
    private int maxAdvanceBookingDays = 30;
}
