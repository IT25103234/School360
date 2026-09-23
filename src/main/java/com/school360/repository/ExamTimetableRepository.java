package com.school360.repository;

import com.school360.model.ExamTimetable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamTimetableRepository extends JpaRepository<ExamTimetable, Long> {
    List<ExamTimetable> findByPublished(boolean published);
    List<ExamTimetable> findByCourseIdAndPublished(Long courseId, boolean published);
    List<ExamTimetable> findByCourseId(Long courseId);
}
