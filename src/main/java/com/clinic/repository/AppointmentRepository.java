package com.clinic.repository;

import com.clinic.entity.Appointment;
import com.clinic.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByPatientUserEmail(String email);
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByDoctorUserEmail(String email);
    List<Appointment> findByAppointmentDate(LocalDate date);
    
    List<Appointment> findByAppointmentDateAndDoctorUserEmail(LocalDate date, String email);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(
            Long doctorId, LocalDate date, Collection<AppointmentStatus> statuses);

    List<Appointment> findByPatientIdAndAppointmentDateAndStatusIn(
            Long patientId, LocalDate date, Collection<AppointmentStatus> statuses);
}
