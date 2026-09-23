package com.school360.repository;

import com.school360.model.SystemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
    List<SystemLog> findAllByOrderByIdDesc();
}
