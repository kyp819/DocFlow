package com.clinic.repository;

import com.clinic.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);

    @Query("SELECT d FROM Doctor d JOIN d.user u WHERE " +
           "(:specialization IS NULL OR LOWER(d.specialization) LIKE :specialization) AND " +
           "(:name IS NULL OR LOWER(u.fullName) LIKE :name)")
    List<Doctor> searchDoctors(@Param("specialization") String specialization, @Param("name") String name);
}
