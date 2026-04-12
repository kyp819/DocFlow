package com.clinic.service;

import com.clinic.dto.AvailabilityRequest;
import com.clinic.dto.AvailabilityResponse;
import java.util.List;

public interface DoctorAvailabilityService {
    AvailabilityResponse createAvailability(AvailabilityRequest request, String doctorEmail);
    AvailabilityResponse updateAvailability(Long id, AvailabilityRequest request, String doctorEmail);
    void deleteAvailability(Long id, String doctorEmail);
    List<AvailabilityResponse> getAvailabilitiesByDoctorId(Long doctorId);
    List<AvailabilityResponse> getActiveAvailabilitiesByDoctorId(Long doctorId);
}
