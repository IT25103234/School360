package com.school360.repository;

import com.school360.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStudentId(Long studentId);
    List<Ticket> findByStatus(String status);
}
