package com.clinic.controller;

import com.clinic.dto.PatientRequest;
import com.clinic.dto.PatientResponse;
import com.clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patient Management", description = "Endpoints for managing patient profiles")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "Create patient profile", description = "Creates a profile for the authenticated PATIENT, or by an ADMIN for a specific user ID")
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request, Principal principal) {
        PatientResponse response = patientService.createPatient(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    @Operation(summary = "Update patient profile", description = "Updates a patient profile by ID. Restricted to the profile owner or ADMIN.")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id, @Valid @RequestBody PatientRequest request, Principal principal) {
        PatientResponse response = patientService.updatePatient(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete patient profile", description = "Deletes a patient profile by ID. Admin only.")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'DOCTOR')")
    @Operation(summary = "Get patient profile by ID", description = "Retrieves patient details by profile ID. Restricted to the owner, DOCTOR, or ADMIN.")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN', 'DOCTOR')")
    @Operation(summary = "Get patient profile by User ID", description = "Retrieves patient details by User ID.")
    public ResponseEntity<PatientResponse> getPatientByUserId(@PathVariable Long userId) {
        PatientResponse response = patientService.getPatientByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get my patient profile", description = "Retrieves the patient profile of the currently logged-in user.")
    public ResponseEntity<PatientResponse> getMyProfile(Principal principal) {
        PatientResponse response = patientService.getPatientByEmail(principal.getName());
        return ResponseEntity.ok(response);
    }
}
