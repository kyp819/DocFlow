package com.clinic.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private String specialization;
    private String qualification;
    private Integer experience;
    private BigDecimal consultationFee;
    private String hospitalName;
    private String bio;
    private UserResponse user;
}
