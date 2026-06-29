package com.clinic.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorRequest {
    private Long userId; // Optional: Only required if Admin is creating the doctor profile

    @NotBlank(message = "Specialization is required")
    @Size(min = 3, max = 100, message = "Specialization must be between 3 and 100 characters")
    private String specialization;

    @NotBlank(message = "Qualification is required")
    @Size(min = 3, max = 100, message = "Qualification must be between 3 and 100 characters")
    private String qualification;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experience;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee cannot be negative")
    @Positive(message = "Consultation fee must be greater than zero")
    private BigDecimal consultationFee;

    @NotBlank(message = "Hospital name is required")
    @Size(min = 2, max = 150, message = "Hospital name must be between 2 and 150 characters")
    private String hospitalName;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;
}

