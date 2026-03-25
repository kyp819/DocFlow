package com.clinic.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleRequest {
    @NotNull(message = "New appointment date is required")
    @FutureOrPresent(message = "New appointment date must be in the present or future")
    private LocalDate appointmentDate;

    @NotNull(message = "New appointment time is required")
    private LocalTime appointmentTime;
}
