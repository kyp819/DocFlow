package com.clinic.mapper;

import com.clinic.dto.*;
import com.clinic.entity.*;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public DoctorResponse toDoctorResponse(Doctor doctor) {
        if (doctor == null) return null;
        return DoctorResponse.builder()
                .id(doctor.getId())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .experience(doctor.getExperience())
                .consultationFee(doctor.getConsultationFee())
                .hospitalName(doctor.getHospitalName())
                .bio(doctor.getBio())
                .user(toUserResponse(doctor.getUser()))
                .build();
    }

    public PatientResponse toPatientResponse(Patient patient) {
        if (patient == null) return null;
        return PatientResponse.builder()
                .id(patient.getId())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth())
                .address(patient.getAddress())
                .bloodGroup(patient.getBloodGroup())
                .emergencyContact(patient.getEmergencyContact())
                .user(toUserResponse(patient.getUser()))
                .build();
    }

    public AvailabilityResponse toAvailabilityResponse(DoctorAvailability availability) {
        if (availability == null) return null;
        return AvailabilityResponse.builder()
                .id(availability.getId())
                .doctorId(availability.getDoctor().getId())
                .doctorName(availability.getDoctor().getUser().getFullName())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .active(availability.isActive())
                .build();
    }

    public AppointmentResponse toAppointmentResponse(Appointment appointment) {
        if (appointment == null) return null;
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .doctor(toDoctorResponse(appointment.getDoctor()))
                .patient(toPatientResponse(appointment.getPatient()))
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
