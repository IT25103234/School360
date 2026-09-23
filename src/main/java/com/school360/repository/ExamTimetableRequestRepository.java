package com.school360.repository;

import com.school360.model.ExamTimetableRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExamTimetableRequestRepository extends JpaRepository<ExamTimetableRequest, Long> {
    List<ExamTimetableRequest> findByTeacherId(Long teacherId);
    List<ExamTimetableRequest> findByStatus(String status);
}
