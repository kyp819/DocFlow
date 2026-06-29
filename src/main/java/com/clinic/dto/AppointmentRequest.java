package com.clinic.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO for booking an appointment.
 * 
 * Validation rules enforce:
 * - Valid doctor ID (positive integer)
 * - Valid appointment date (today or future)
 * - Valid appointment time
 * - Optional clinical notes (max 1000 characters)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    @Positive(message = "Doctor ID must be a positive number")
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Appointment date must be in the present or future")
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment time is required")
    private LocalTime appointmentTime;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}

