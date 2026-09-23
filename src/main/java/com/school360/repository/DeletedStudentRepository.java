package com.school360.repository;

import com.school360.model.DeletedStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeletedStudentRepository extends JpaRepository<DeletedStudent, Long> {
    List<DeletedStudent> findAllByOrderByDeletedAtDesc();
}
