package com.school360.repository;

import com.school360.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByScheduleId(Long scheduleId);
    Optional<Attendance> findByStudentIdAndScheduleIdAndDate(Long studentId, Long scheduleId, String date);
}
