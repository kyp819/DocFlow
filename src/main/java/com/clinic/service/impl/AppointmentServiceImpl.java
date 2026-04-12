package com.clinic.service.impl;

import com.clinic.dto.AppointmentRequest;
import com.clinic.dto.AppointmentResponse;
import com.clinic.dto.RescheduleRequest;
import com.clinic.entity.*;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DtoMapper;
import com.clinic.repository.*;
import com.clinic.service.AppointmentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final int APPOINTMENT_DURATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  DoctorRepository doctorRepository,
                                  PatientRepository patientRepository,
                                  DoctorAvailabilityRepository availabilityRepository,
                                  UserRepository userRepository,
                                  DtoMapper dtoMapper) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
        this.dtoMapper = dtoMapper;
    }

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request, String patientEmail) {
        // Find patient
        User patientUser = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient user not found"));
        Patient patient = patientRepository.findByUserId(patientUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not setup. Please create patient profile first."));

        // Find doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found with id: " + request.getDoctorId()));

        // Validate date and time constraints
        validateAppointmentTime(doctor.getId(), patient.getId(), request.getAppointmentDate(), request.getAppointmentTime(), null);

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(request.getAppointmentDate())
                .appointmentTime(request.getAppointmentTime())
                .status(AppointmentStatus.BOOKED)
                .notes(request.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return dtoMapper.toAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelAppointment(Long id, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security check: only Admin, Doctor assigned, or Patient who booked can cancel
        if (currentUser.getRole() != Role.ADMIN 
                && !appointment.getPatient().getUser().getEmail().equals(currentUserEmail)
                && !appointment.getDoctor().getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You are not authorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);
        return dtoMapper.toAppointmentResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse rescheduleAppointment(Long id, RescheduleRequest request, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security check: only Admin, Doctor assigned, or Patient who booked can reschedule
        if (currentUser.getRole() != Role.ADMIN 
                && !appointment.getPatient().getUser().getEmail().equals(currentUserEmail)
                && !appointment.getDoctor().getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You are not authorized to reschedule this appointment");
        }

        // Validate date and time constraints for the new time (excluding this appointment from overlap check)
        validateAppointmentTime(appointment.getDoctor().getId(), appointment.getPatient().getId(), 
                request.getAppointmentDate(), request.getAppointmentTime(), id);

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(AppointmentStatus.RESCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        return dtoMapper.toAppointmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security check: only Admin, Doctor assigned, or Patient who booked can view
        if (currentUser.getRole() != Role.ADMIN 
                && !appointment.getPatient().getUser().getEmail().equals(currentUserEmail)
                && !appointment.getDoctor().getUser().getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You are not authorized to view this appointment");
        }

        return dtoMapper.toAppointmentResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Appointment> list;
        if (currentUser.getRole() == Role.PATIENT) {
            list = appointmentRepository.findByPatientUserEmail(currentUserEmail);
        } else if (currentUser.getRole() == Role.DOCTOR) {
            list = appointmentRepository.findByDoctorUserEmail(currentUserEmail);
        } else {
            // Admin gets all
            list = appointmentRepository.findAll();
        }

        return list.stream()
                .map(dtoMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security check: Admin or the Doctor themselves
        if (currentUser.getRole() != Role.ADMIN) {
            Doctor doctor = doctorRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Access Denied"));
            if (!doctor.getId().equals(doctorId)) {
                throw new AccessDeniedException("You can only view your own doctor appointments");
            }
        }

        List<Appointment> list = appointmentRepository.findByDoctorId(doctorId);
        return list.stream()
                .map(dtoMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTodayAppointments(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate today = LocalDate.now();
        List<Appointment> list;

        if (currentUser.getRole() == Role.DOCTOR) {
            list = appointmentRepository.findByAppointmentDateAndDoctorUserEmail(today, currentUserEmail);
        } else if (currentUser.getRole() == Role.ADMIN) {
            list = appointmentRepository.findByAppointmentDate(today);
        } else {
            throw new AccessDeniedException("Patients are not authorized to view today's complete queue");
        }

        return list.stream()
                .map(dtoMapper::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    private void validateAppointmentTime(Long doctorId, Long patientId, LocalDate date, LocalTime time, Long excludeAppointmentId) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointments cannot be booked in the past");
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        LocalTime endTime = time.plusMinutes(APPOINTMENT_DURATION_MINUTES);

        // Check if inside Doctor availability
        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorIdAndActiveTrue(doctorId);
        boolean isAvailable = false;
        for (DoctorAvailability availability : availabilities) {
            if (availability.getDayOfWeek() == dayOfWeek &&
                (time.isAfter(availability.getStartTime()) || time.equals(availability.getStartTime())) &&
                (endTime.isBefore(availability.getEndTime()) || endTime.equals(availability.getEndTime()))) {
                isAvailable = true;
                break;
            }
        }

        if (!isAvailable) {
            throw new ConflictException("The selected time slot is outside the doctor's available time slots");
        }

        List<AppointmentStatus> activeStatuses = List.of(AppointmentStatus.BOOKED, AppointmentStatus.RESCHEDULED);

        // Check Doctor overlap
        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
                doctorId, date, activeStatuses);
        for (Appointment app : doctorAppointments) {
            if (excludeAppointmentId != null && app.getId().equals(excludeAppointmentId)) {
                continue;
            }
            LocalTime appStart = app.getAppointmentTime();
            LocalTime appEnd = appStart.plusMinutes(APPOINTMENT_DURATION_MINUTES);
            if (time.isBefore(appEnd) && endTime.isAfter(appStart)) {
                throw new ConflictException("Doctor has an overlapping appointment at this time");
            }
        }

        // Check Patient overlap
        List<Appointment> patientAppointments = appointmentRepository.findByPatientIdAndAppointmentDateAndStatusIn(
                patientId, date, activeStatuses);
        for (Appointment app : patientAppointments) {
            if (excludeAppointmentId != null && app.getId().equals(excludeAppointmentId)) {
                continue;
            }
            LocalTime appStart = app.getAppointmentTime();
            LocalTime appEnd = appStart.plusMinutes(APPOINTMENT_DURATION_MINUTES);
            if (time.isBefore(appEnd) && endTime.isAfter(appStart)) {
                throw new ConflictException("Patient has an overlapping appointment booked at this time");
            }
        }
    }
}
