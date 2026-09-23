package com.school360.repository;

import com.school360.model.BookReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookReservationRepository extends JpaRepository<BookReservation, Long> {
    List<BookReservation> findByMemberId(Long memberId);
    List<BookReservation> findByMemberIdOrderByIdDesc(Long memberId);
    List<BookReservation> findByStatus(String status);
}
