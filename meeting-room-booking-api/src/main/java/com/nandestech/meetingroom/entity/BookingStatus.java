package com.nandestech.meetingroom.entity;

import java.util.Map;
import java.util.Set;

/**
 * Enum representing the lifecycle states of a booking.
 * Enforces valid state transitions via a state machine pattern.
 *
 * <pre>
 * State Diagram:
 *   PENDING  → APPROVED | REJECTED | CANCELLED
 *   APPROVED → CANCELLED
 *   REJECTED → (terminal)
 *   CANCELLED → (terminal)
 * </pre>
 */
public enum BookingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    CHECKED_IN,
    COMPLETED,
    NO_SHOW;

    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = Map.of(
            PENDING, Set.of(APPROVED, REJECTED, CANCELLED),
            APPROVED, Set.of(CANCELLED, CHECKED_IN, NO_SHOW),
            CHECKED_IN, Set.of(COMPLETED),
            REJECTED, Set.of(),
            CANCELLED, Set.of(),
            COMPLETED, Set.of(),
            NO_SHOW, Set.of()
    );

    /**
     * Checks whether transitioning from the current status to the target status is allowed.
     *
     * @param target the desired new status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(BookingStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Validates the transition and throws an exception if it is invalid.
     *
     * @param target the desired new status
     * @throws IllegalStateException if the transition is not allowed
     */
    public void validateTransitionTo(BookingStatus target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException(
                    String.format("Cannot transition booking from %s to %s", this.name(), target.name())
            );
        }
    }
}
