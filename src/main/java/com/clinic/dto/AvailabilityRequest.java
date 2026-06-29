package com.clinic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request DTO for creating or updating doctor availability slots.
 * 
 * Validation rules enforce:
 * - Valid day of week (required)
 * - Valid time range with start time before end time
 * - Time precision to minutes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityRequest {
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @Builder.Default
    private boolean active = true;
}

