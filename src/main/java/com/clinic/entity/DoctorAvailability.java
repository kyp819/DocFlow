package com.clinic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Represents a doctor's available time slot for appointments.
 * 
 * <p>This entity defines recurring weekly availability windows during which a doctor
 * can accept appointments. Each slot specifies a day of the week and time range.</p>
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Define doctor's recurring weekly schedule</li>
 *   <li>Specify available time slots (start and end times)</li>
 *   <li>Track active/inactive status</li>
 * </ul>
 * </p>
 *
 * <p>Business rules:
 * <ul>
 *   <li>Multiple availability slots can exist for the same doctor on the same day</li>
 *   <li>Start time must be before end time</li>
 *   <li>Times are specified in 24-hour format</li>
 *   <li>Active flag allows temporary disabling without deletion</li>
 * </ul>
 * </p>
 *
 * <p>Usage:
 * <ul>
 *   <li>Used during appointment validation to check if booking time is valid</li>
 *   <li>Retrieved during availability query for UI display</li>
 *   <li>Combined with existing appointments to find free slots</li>
 * </ul>
 * </p>
 *
 * @see Doctor
 * @see Appointment
 */
@Entity
@Table(name = "doctor_availabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
