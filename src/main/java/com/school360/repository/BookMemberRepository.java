package com.school360.repository;

import com.school360.model.BookMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookMemberRepository extends JpaRepository<BookMember, Long> {
    Optional<BookMember> findByMid(String mid);
}
