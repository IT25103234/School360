package com.school360.repository;

import com.school360.model.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {
    List<TicketReply> findByTicketIdOrderByIdAsc(Long ticketId);
}
