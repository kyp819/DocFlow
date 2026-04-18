package com.clinic.controller;

import com.clinic.dto.AppointmentRequest;
import com.clinic.dto.AppointmentResponse;
import com.clinic.dto.RescheduleRequest;
import com.clinic.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointment Management", description = "Endpoints for booking, rescheduling, and cancelling clinic appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book a new appointment", description = "Books an appointment slot. Restricted to patients.")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody AppointmentRequest request, Principal principal) {
        AppointmentResponse response = appointmentService.bookAppointment(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an appointment", description = "Cancels an appointment by ID. Allowed for the booked patient, assigned doctor, or admin.")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id, Principal principal) {
        AppointmentResponse response = appointmentService.cancelAppointment(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule an appointment", description = "Reschedules an appointment by ID. Allowed for the booked patient, assigned doctor, or admin.")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable Long id, @Valid @RequestBody RescheduleRequest request, Principal principal) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment details by ID", description = "Retrieves details of a specific appointment. Restrict to owner patient, assigned doctor, or admin.")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id, Principal principal) {
        AppointmentResponse response = appointmentService.getAppointmentById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get my appointments", description = "Retrieves all appointments for the currently logged-in user (patient or doctor)")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(Principal principal) {
        List<AppointmentResponse> response = appointmentService.getMyAppointments(principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Get appointments for a specific doctor", description = "Retrieves appointments assigned to a doctor. Restricted to that doctor or admin.")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @PathVariable Long doctorId, Principal principal) {
        List<AppointmentResponse> response = appointmentService.getDoctorAppointments(doctorId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Get today's appointments", description = "Retrieves all appointments scheduled for today. Restricted to doctor (own queue) or admin.")
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments(Principal principal) {
        List<AppointmentResponse> response = appointmentService.getTodayAppointments(principal.getName());
        return ResponseEntity.ok(response);
    }
}
