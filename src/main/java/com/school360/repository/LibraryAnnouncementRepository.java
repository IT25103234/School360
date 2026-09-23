package com.school360.repository;

import com.school360.model.LibraryAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibraryAnnouncementRepository extends JpaRepository<LibraryAnnouncement, Long> {
}
