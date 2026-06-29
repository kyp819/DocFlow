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

    /**
     * Validates all aspects of an appointment booking time slot.
     * 
     * @param doctorId the doctor's ID
     * @param patientId the patient's ID
     * @param date the requested appointment date
     * @param time the requested appointment time
     * @param excludeAppointmentId appointment ID to exclude from overlap checks (for reschedules)
     */
    private void validateAppointmentTime(Long doctorId, Long patientId, LocalDate date, LocalTime time, Long excludeAppointmentId) {
        validateAppointmentIsNotInPast(date, time);
        validateTimeIsWithinDoctorAvailability(doctorId, date, time);
        validateNoDoctorOverlap(doctorId, date, time, excludeAppointmentId);
        validateNoPatientOverlap(patientId, date, time, excludeAppointmentId);
    }

    /**
     * Validates that the appointment date and time are not in the past.
     *
     * @param date the appointment date
     * @param time the appointment time
     * @throws BadRequestException if the appointment is in the past
     */
    private void validateAppointmentIsNotInPast(LocalDate date, LocalTime time) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointments cannot be booked in the past");
        }
    }

    /**
     * Validates that the appointment time falls within the doctor's available time slots.
     *
     * @param doctorId the doctor's ID
     * @param date the appointment date
     * @param time the appointment time
     * @throws ConflictException if the time slot is outside doctor's availability
     */
    private void validateTimeIsWithinDoctorAvailability(Long doctorId, LocalDate date, LocalTime time) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        LocalTime endTime = time.plusMinutes(APPOINTMENT_DURATION_MINUTES);

        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorIdAndActiveTrue(doctorId);
        boolean isAvailable = availabilities.stream()
                .anyMatch(availability -> isTimeSlotWithinAvailability(availability, dayOfWeek, time, endTime));

        if (!isAvailable) {
            throw new ConflictException("The selected time slot is outside the doctor's available time slots");
        }
    }

    /**
     * Checks if a time slot falls within a doctor availability window.
     *
     * @param availability the doctor availability window
     * @param dayOfWeek the day of the week to check
     * @param startTime the appointment start time
     * @param endTime the appointment end time
     * @return true if the time slot fits within the availability, false otherwise
     */
    private boolean isTimeSlotWithinAvailability(DoctorAvailability availability, DayOfWeek dayOfWeek, 
                                                  LocalTime startTime, LocalTime endTime) {
        return availability.getDayOfWeek() == dayOfWeek &&
               (startTime.isAfter(availability.getStartTime()) || startTime.equals(availability.getStartTime())) &&
               (endTime.isBefore(availability.getEndTime()) || endTime.equals(availability.getEndTime()));
    }

    /**
     * Validates that the doctor has no overlapping appointments at the requested time.
     *
     * @param doctorId the doctor's ID
     * @param date the appointment date
     * @param time the appointment time
     * @param excludeAppointmentId appointment ID to exclude from check (null for new bookings)
     * @throws ConflictException if an overlapping appointment exists
     */
    private void validateNoDoctorOverlap(Long doctorId, LocalDate date, LocalTime time, Long excludeAppointmentId) {
        LocalTime endTime = time.plusMinutes(APPOINTMENT_DURATION_MINUTES);
        List<AppointmentStatus> activeStatuses = List.of(AppointmentStatus.BOOKED, AppointmentStatus.RESCHEDULED);

        List<Appointment> doctorAppointments = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
                doctorId, date, activeStatuses);

        boolean hasOverlap = doctorAppointments.stream()
                .filter(app -> excludeAppointmentId == null || !app.getId().equals(excludeAppointmentId))
                .anyMatch(app -> isTimeSlotOverlapping(app.getAppointmentTime(), time, endTime));

        if (hasOverlap) {
            throw new ConflictException("Doctor has an overlapping appointment at this time");
        }
    }

    /**
     * Validates that the patient has no overlapping appointments at the requested time.
     *
     * @param patientId the patient's ID
     * @param date the appointment date
     * @param time the appointment time
     * @param excludeAppointmentId appointment ID to exclude from check (null for new bookings)
     * @throws ConflictException if an overlapping appointment exists
     */
    private void validateNoPatientOverlap(Long patientId, LocalDate date, LocalTime time, Long excludeAppointmentId) {
        LocalTime endTime = time.plusMinutes(APPOINTMENT_DURATION_MINUTES);
        List<AppointmentStatus> activeStatuses = List.of(AppointmentStatus.BOOKED, AppointmentStatus.RESCHEDULED);

        List<Appointment> patientAppointments = appointmentRepository.findByPatientIdAndAppointmentDateAndStatusIn(
                patientId, date, activeStatuses);

        boolean hasOverlap = patientAppointments.stream()
                .filter(app -> excludeAppointmentId == null || !app.getId().equals(excludeAppointmentId))
                .anyMatch(app -> isTimeSlotOverlapping(app.getAppointmentTime(), time, endTime));

        if (hasOverlap) {
            throw new ConflictException("Patient has an overlapping appointment booked at this time");
        }
    }

    /**
     * Checks if two time slots overlap.
     *
     * @param existingStart the start time of existing appointment
     * @param newStart the start time of new appointment
     * @param newEnd the end time of new appointment
     * @return true if the slots overlap, false otherwise
     */
    private boolean isTimeSlotOverlapping(LocalTime existingStart, LocalTime newStart, LocalTime newEnd) {
        LocalTime existingEnd = existingStart.plusMinutes(APPOINTMENT_DURATION_MINUTES);
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
}
