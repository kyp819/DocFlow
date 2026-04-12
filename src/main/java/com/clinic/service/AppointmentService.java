package com.clinic.service;

import com.clinic.dto.AppointmentRequest;
import com.clinic.dto.AppointmentResponse;
import com.clinic.dto.RescheduleRequest;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentRequest request, String patientEmail);
    AppointmentResponse cancelAppointment(Long id, String currentUserEmail);
    AppointmentResponse rescheduleAppointment(Long id, RescheduleRequest request, String currentUserEmail);
    AppointmentResponse getAppointmentById(Long id, String currentUserEmail);
    List<AppointmentResponse> getMyAppointments(String currentUserEmail);
    List<AppointmentResponse> getDoctorAppointments(Long doctorId, String currentUserEmail);
    List<AppointmentResponse> getTodayAppointments(String currentUserEmail);
}
