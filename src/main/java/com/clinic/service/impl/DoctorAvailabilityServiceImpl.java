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

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Check for overlaps with existing active availabilities on the same day
        List<DoctorAvailability> existingList = availabilityRepository.findByDoctorIdAndActiveTrue(doctor.getId());
        for (DoctorAvailability existing : existingList) {
            if (existing.getDayOfWeek() == request.getDayOfWeek()) {
                boolean overlaps = request.getStartTime().isBefore(existing.getEndTime()) 
                        && request.getEndTime().isAfter(existing.getStartTime());
                if (overlaps) {
                    throw new ConflictException("Availability overlaps with an existing slot on " + request.getDayOfWeek());
                }
            }
        }

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

        if (request.getStartTime().isAfter(request.getEndTime()) || request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        // Check for overlaps with other active availabilities of the same doctor on the same day (excluding itself)
        List<DoctorAvailability> existingList = availabilityRepository.findByDoctorIdAndActiveTrue(doctor.getId());
        for (DoctorAvailability existing : existingList) {
            if (!existing.getId().equals(id) && existing.getDayOfWeek() == request.getDayOfWeek()) {
                boolean overlaps = request.getStartTime().isBefore(existing.getEndTime()) 
                        && request.getEndTime().isAfter(existing.getStartTime());
                if (overlaps) {
                    throw new ConflictException("Availability overlaps with an existing slot on " + request.getDayOfWeek());
                }
            }
        }

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
}
