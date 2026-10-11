package com.easyreg.repository;

import com.easyreg.entity.RegistrationApplication;
import com.easyreg.entity.User;
import com.easyreg.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationApplicationRepository extends JpaRepository<RegistrationApplication, Long> {

    List<RegistrationApplication> findByStudentOrderBySubmittedAtDesc(User student);

    Optional<RegistrationApplication> findByApplicationNumber(String applicationNumber);

    List<RegistrationApplication> findByStatusOrderBySubmittedAtAsc(RegistrationStatus status);

    List<RegistrationApplication> findByDepartmentAndStatusOrderBySubmittedAtAsc(String department, RegistrationStatus status);

    boolean existsByStudentAndAcademicYearAndTargetSemester(User student, String academicYear, Integer targetSemester);

    long countByStatus(RegistrationStatus status);

    @Query("SELECT COUNT(a) FROM RegistrationApplication a WHERE a.status = 'APPROVED'")
    long countApprovedApplications();

    @Query("SELECT COUNT(a) FROM RegistrationApplication a WHERE a.status = 'REJECTED'")
    long countRejectedApplications();
}
