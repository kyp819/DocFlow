package com.clinic.service.impl;

import com.clinic.dto.AvailabilityRequest;
import com.clinic.dto.AvailabilityResponse;
import com.clinic.entity.Doctor;
import com.clinic.entity.DoctorAvailability;
import com.clinic.entity.User;
import com.clinic.entity.Role;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DtoMapper;
import com.clinic.repository.DoctorAvailabilityRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.UserRepository;
import com.clinic.service.DoctorAvailabilityService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    public DoctorAvailabilityServiceImpl(DoctorAvailabilityRepository availabilityRepository,
                                         DoctorRepository doctorRepository,
                                         UserRepository userRepository,
                                         DtoMapper dtoMapper) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    private Doctor getDoctorOrThrow(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        
        return doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + email));
    }

    @Override
    @Transactional
    public AvailabilityResponse createAvailability(AvailabilityRequest request, String doctorEmail) {
        Doctor doctor = getDoctorOrThrow(doctorEmail);

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateNoOverlapWithExisting(doctor.getId(), request, null);

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .active(request.isActive())
                .build();

        DoctorAvailability saved = availabilityRepository.save(availability);
        return dtoMapper.toAvailabilityResponse(saved);
    }

    @Override
    @Transactional
    public AvailabilityResponse updateAvailability(Long id, AvailabilityRequest request, String doctorEmail) {
        Doctor doctor = getDoctorOrThrow(doctorEmail);
        DoctorAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Check ownership: Doctor can only update their own availabilities
        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("You are not authorized to update this availability");
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateNoOverlapWithExisting(doctor.getId(), request, id);

        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setActive(request.isActive());

        DoctorAvailability updated = availabilityRepository.save(availability);
        return dtoMapper.toAvailabilityResponse(updated);
    }

    @Override
    @Transactional
    public void deleteAvailability(Long id, String doctorEmail) {
        Doctor doctor = getDoctorOrThrow(doctorEmail);
        DoctorAvailability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability not found with id: " + id));

        // Check ownership
        if (!availability.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this availability");
        }

        availabilityRepository.delete(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilitiesByDoctorId(Long doctorId) {
        List<DoctorAvailability> list = availabilityRepository.findByDoctorId(doctorId);
        return list.stream()
                .map(dtoMapper::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getActiveAvailabilitiesByDoctorId(Long doctorId) {
        List<DoctorAvailability> list = availabilityRepository.findByDoctorIdAndActiveTrue(doctorId);
        return list.stream()
                .map(dtoMapper::toAvailabilityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Validates that start time is before end time.
     *
     * @param startTime the availability start time
     * @param endTime the availability end time
     * @throws BadRequestException if start time is not before end time
     */
    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new BadRequestException("Start time must be before end time");
        }
    }

    /**
     * Validates that a new availability slot does not overlap with existing slots.
     *
     * <p>Checks the doctor's active availabilities for the same day of week.
     * When updating (excludeId is not null), the existing slot with that ID is excluded from the check.</p>
     *
     * @param doctorId the doctor's ID
     * @param request the availability request containing day and time range
     * @param excludeId the availability ID to exclude from overlap check (null for create operations)
     * @throws ConflictException if an overlapping slot exists on the same day
     */
    private void validateNoOverlapWithExisting(Long doctorId, AvailabilityRequest request, Long excludeId) {
        List<DoctorAvailability> existingList = availabilityRepository.findByDoctorIdAndActiveTrue(doctorId);

        boolean hasOverlap = existingList.stream()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .filter(existing -> existing.getDayOfWeek() == request.getDayOfWeek())
                .anyMatch(existing -> hasTimeOverlap(request.getStartTime(), request.getEndTime(), existing.getStartTime(), existing.getEndTime()));

        if (hasOverlap) {
            throw new ConflictException("Availability overlaps with an existing slot on " + request.getDayOfWeek());
        }
    }

    /**
     * Checks if two time ranges overlap.
     *
     * @param newStart the start time of new slot
     * @param newEnd the end time of new slot
     * @param existingStart the start time of existing slot
     * @param existingEnd the end time of existing slot
     * @return true if the slots overlap, false otherwise
     */
    private boolean hasTimeOverlap(java.time.LocalTime newStart, java.time.LocalTime newEnd,
                                    java.time.LocalTime existingStart, java.time.LocalTime existingEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}
