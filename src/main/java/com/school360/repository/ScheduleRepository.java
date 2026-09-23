package com.school360.repository;

import com.school360.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByTeacherId(Long teacherId);
    List<Schedule> findByModuleId(Long moduleId);
    List<Schedule> findByModuleIdIn(List<Long> moduleIds);
}
