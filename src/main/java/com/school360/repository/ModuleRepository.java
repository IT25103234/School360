package com.school360.repository;

import com.school360.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseId(Long courseId);
    List<Module> findByTeacherId(Long teacherId);
}
