package com.clinic.config;

import com.clinic.entity.*;
import com.clinic.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository,
                          DoctorAvailabilityRepository doctorAvailabilityRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Checking clinic database seed data status...");

        // 1. Seed Admin
        User admin;
        var adminOpt = userRepository.findByEmail("admin@clinic.com");
        if (adminOpt.isEmpty()) {
            admin = User.builder()
                    .fullName("Admin User")
                    .email("admin@clinic.com")
                    .password(passwordEncoder.encode("password123"))
                    .phone("+1234567890")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            admin = userRepository.save(admin);
            System.out.println("Seeded admin@clinic.com");
        } else {
            admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode("password123"));
            admin = userRepository.save(admin);
            System.out.println("Updated admin@clinic.com password to password123");
        }

        // 2. Seed Doctor
        User doctorUser;
        var docUserOpt = userRepository.findByEmail("doctor@clinic.com");
        if (docUserOpt.isEmpty()) {
            doctorUser = User.builder()
                    .fullName("Dr. John Doe")
                    .email("doctor@clinic.com")
                    .password(passwordEncoder.encode("password123"))
                    .phone("+1987654321")
                    .role(Role.DOCTOR)
                    .enabled(true)
                    .build();
            doctorUser = userRepository.save(doctorUser);
            System.out.println("Seeded doctor@clinic.com user");
        } else {
            doctorUser = docUserOpt.get();
            doctorUser.setPassword(passwordEncoder.encode("password123"));
            doctorUser = userRepository.save(doctorUser);
            System.out.println("Updated doctor@clinic.com password to password123");
        }

        Doctor doctorProfile;
        var docProfileOpt = doctorRepository.findByUserId(doctorUser.getId());
        if (docProfileOpt.isEmpty()) {
            doctorProfile = Doctor.builder()
                    .specialization("Cardiology")
                    .qualification("MD - Cardiology")
                    .experience(12)
                    .consultationFee(BigDecimal.valueOf(150.00))
                    .hospitalName("General City Hospital")
                    .bio("Experienced cardiologist specializing in cardiovascular health.")
                    .user(doctorUser)
                    .build();
            doctorProfile = doctorRepository.save(doctorProfile);
            System.out.println("Seeded Dr. John Doe profile");
        } else {
            doctorProfile = docProfileOpt.get();
        }

        // 3. Seed Patient
        User patientUser;
        var patUserOpt = userRepository.findByEmail("patient@clinic.com");
        if (patUserOpt.isEmpty()) {
            patientUser = User.builder()
                    .fullName("Jane Smith")
                    .email("patient@clinic.com")
                    .password(passwordEncoder.encode("password123"))
                    .phone("+1555555555")
                    .role(Role.PATIENT)
                    .enabled(true)
                    .build();
            patientUser = userRepository.save(patientUser);
            System.out.println("Seeded patient@clinic.com user");
        } else {
            patientUser = patUserOpt.get();
            patientUser.setPassword(passwordEncoder.encode("password123"));
            patientUser = userRepository.save(patientUser);
            System.out.println("Updated patient@clinic.com password to password123");
        }

        if (patientRepository.findByUserId(patientUser.getId()).isEmpty()) {
            Patient patientProfile = Patient.builder()
                    .gender("FEMALE")
                    .dateOfBirth(LocalDate.of(1992, 8, 15))
                    .address("456 Elm St, Metropia")
                    .bloodGroup("O+")
                    .emergencyContact("+1555123456")
                    .user(patientUser)
                    .build();
            patientRepository.save(patientProfile);
            System.out.println("Seeded Jane Smith patient profile");
        }

        // 4. Seed Availabilities
        var currentAvails = doctorAvailabilityRepository.findByDoctorId(doctorProfile.getId());
        boolean hasMon = currentAvails.stream().anyMatch(a -> a.getDayOfWeek() == java.time.DayOfWeek.MONDAY);
        boolean hasWed = currentAvails.stream().anyMatch(a -> a.getDayOfWeek() == java.time.DayOfWeek.WEDNESDAY);
        boolean hasFri = currentAvails.stream().anyMatch(a -> a.getDayOfWeek() == java.time.DayOfWeek.FRIDAY);
        boolean hasSun = currentAvails.stream().anyMatch(a -> a.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);

        if (!hasMon) {
            doctorAvailabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile).dayOfWeek(java.time.DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).active(true).build());
            System.out.println("Seeded Monday availability block");
        }
        if (!hasWed) {
            doctorAvailabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile).dayOfWeek(java.time.DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).active(true).build());
            System.out.println("Seeded Wednesday availability block");
        }
        if (!hasFri) {
            doctorAvailabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile).dayOfWeek(java.time.DayOfWeek.FRIDAY)
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(13, 0)).active(true).build());
            System.out.println("Seeded Friday availability block");
        }
        if (!hasSun) {
            doctorAvailabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile).dayOfWeek(java.time.DayOfWeek.SUNDAY)
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).active(true).build());
            System.out.println("Seeded Sunday availability block");
        }
    }
}
