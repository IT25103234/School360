package com.school360.repository;

import com.school360.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserUsername(String username);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByEnrollmentStatus(String enrollmentStatus);
    List<Student> findByCourseId(Long courseId);
}
