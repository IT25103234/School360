package com.school360.repository;

import com.school360.model.ExamMark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamMarkRepository extends JpaRepository<ExamMark, Long> {
    List<ExamMark> findByStudentId(Long studentId);
    List<ExamMark> findByStudentIdAndPublished(Long studentId, boolean published);
    List<ExamMark> findByExamTitle(String examTitle);
    List<ExamMark> findByCourseId(Long courseId);
}
