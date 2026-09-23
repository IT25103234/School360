package com.school360.repository;

import com.school360.model.StudentNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentNotificationRepository extends JpaRepository<StudentNotification, Long> {
    List<StudentNotification> findByStudentIdOrderByIdDesc(Long studentId);
    List<StudentNotification> findByStudentIdAndIsReadFalseOrderByIdDesc(Long studentId);
    long countByStudentIdAndIsReadFalse(Long studentId);
}
