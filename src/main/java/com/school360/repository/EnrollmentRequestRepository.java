package com.school360.repository;

import com.school360.model.EnrollmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, Long> {
    List<EnrollmentRequest> findByEoStatus(String eoStatus);
    List<EnrollmentRequest> findByStaffStatus(String staffStatus);
    List<EnrollmentRequest> findByStudentId(Long studentId);
}
