package com.school360.repository;

import com.school360.model.BookVersionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookVersionHistoryRepository extends JpaRepository<BookVersionHistory, Long> {
}
