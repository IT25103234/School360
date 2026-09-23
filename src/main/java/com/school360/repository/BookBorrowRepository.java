package com.school360.repository;

import com.school360.model.BookBorrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookBorrowRepository extends JpaRepository<BookBorrow, Long> {
    List<BookBorrow> findByMemberId(Long memberId);
    List<BookBorrow> findByStatus(String status);
}
