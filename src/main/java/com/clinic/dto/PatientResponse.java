package com.clinic.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private Long id;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String bloodGroup;
    private String emergencyContact;
    private UserResponse user;
}
