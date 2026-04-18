package com.clinic.controller;

import com.clinic.dto.DoctorRequest;
import com.clinic.dto.DoctorResponse;
import com.clinic.service.DoctorService;
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
@RequestMapping("/api/doctors")
@Tag(name = "Doctor Management", description = "Endpoints for managing doctor profiles")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Create a doctor profile", description = "Creates a detailed profile for the authenticated DOCTOR or by an ADMIN for a specific user ID")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest request, Principal principal) {
        DoctorResponse response = doctorService.createDoctor(request, principal.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @Operation(summary = "Update a doctor profile", description = "Updates a doctor profile by profile ID. Restricted to the profile owner or ADMIN.")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id, @Valid @RequestBody DoctorRequest request, Principal principal) {
        DoctorResponse response = doctorService.updateDoctor(id, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a doctor profile", description = "Deletes a doctor profile by ID. Admin only.")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor profile by ID", description = "Retrieves doctor profile details by profile ID")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get doctor profile by User ID", description = "Retrieves doctor profile details by associated User ID")
    public ResponseEntity<DoctorResponse> getDoctorByUserId(@PathVariable Long userId) {
        DoctorResponse response = doctorService.getDoctorByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List and search doctors", description = "Lists doctors. Supports filtering by specialization and searching by name.")
    public ResponseEntity<List<DoctorResponse>> searchDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String name) {
        List<DoctorResponse> response = doctorService.listAndSearchDoctors(specialization, name);
        return ResponseEntity.ok(response);
    }
}
