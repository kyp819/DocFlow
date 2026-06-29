package com.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequest {
    private Long userId; // Optional: Only required if Admin is creating the patient profile

    @NotBlank(message = "Gender is required")
    @Size(min = 1, max = 20, message = "Gender must be between 1 and 20 characters")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @NotBlank(message = "Blood group is required")
    @Size(min = 1, max = 10, message = "Blood group must be between 1 and 10 characters")
    private String bloodGroup;

    @NotBlank(message = "Emergency contact is required")
    @Size(min = 10, max = 20, message = "Emergency contact must be between 10 and 20 characters")
    private String emergencyContact;
}

    private String emergencyContact;
}
