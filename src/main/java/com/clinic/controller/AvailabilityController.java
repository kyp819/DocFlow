package com.clinic.controller;

import com.clinic.dto.AvailabilityRequest;
import com.clinic.dto.AvailabilityResponse;
import com.clinic.service.DoctorAvailabilityService;
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
@RequestMapping("/api/availabilities")
@Tag(name = "Doctor Availability Management", description = "Endpoints for managing doctor availability slots")
public class AvailabilityController {

    private final DoctorAvailabilityService availabilityService;

    public AvailabilityController(DoctorAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create doctor availability", description = "Creates a new availability time slot for the logged-in doctor")
    public ResponseEntity<AvailabilityResponse> createAvailability(
            @Valid @RequestBody AvailabilityRequest request, Principal principal) {
        AvailabilityResponse response = availabilityService.createAvailability(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update doctor availability", description = "Updates an existing availability slot by ID")
    public ResponseEntity<AvailabilityResponse> updateAvailability(
            @PathVariable Long id, @Valid @RequestBody AvailabilityRequest request, Principal principal) {
        AvailabilityResponse response = availabilityService.updateAvailability(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Delete doctor availability", description = "Deletes an availability slot by ID")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id, Principal principal) {
        availabilityService.deleteAvailability(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "View doctor availability slots", description = "Retrieves all availability slots (active and inactive) for a doctor")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilities(@PathVariable Long doctorId) {
        List<AvailabilityResponse> response = availabilityService.getAvailabilitiesByDoctorId(doctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}/active")
    @Operation(summary = "View active availability slots", description = "Retrieves only active availability slots for a doctor")
    public ResponseEntity<List<AvailabilityResponse>> getActiveAvailabilities(@PathVariable Long doctorId) {
        List<AvailabilityResponse> response = availabilityService.getActiveAvailabilitiesByDoctorId(doctorId);
        return ResponseEntity.ok(response);
    }
}
