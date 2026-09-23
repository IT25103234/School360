package com.school360.controller;

import com.school360.model.*;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/library")
@CrossOrigin(origins = "*")
public class LibraryController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookMemberRepository bookMemberRepository;

    @Autowired
    private BookBorrowRepository bookBorrowRepository;

    @Autowired
    private BookReservationRepository bookReservationRepository;

    @Autowired
    private LibraryAnnouncementRepository libraryAnnouncementRepository;

    @Autowired
    private BookVersionHistoryRepository bookVersionHistoryRepository;

    @Autowired
    private StudentNotificationRepository studentNotificationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SystemLogRepository systemLogRepository;

    // ════════════════════════════════════════════════
    // BOOKS CRUD
    // ════════════════════════════════════════════════

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @PostMapping("/books")
    public Book createBook(@RequestBody Book book) {
        if (book.getStatus() == null || book.getStatus().trim().isEmpty()) {
            book.setStatus("AVAILABLE");
        }
        return bookRepository.save(book);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable("id") Long id, @RequestBody Book bookDetails) {
        return bookRepository.findById(id).map(book -> {
            if (bookDetails.getTitle() != null) book.setTitle(bookDetails.getTitle());
            if (bookDetails.getAuthor() != null) book.setAuthor(bookDetails.getAuthor());
            if (bookDetails.getIsbn() != null) book.setIsbn(bookDetails.getIsbn());
            if (bookDetails.getCategory() != null) book.setCategory(bookDetails.getCategory());
            if (bookDetails.getEdition() != null) book.setEdition(bookDetails.getEdition());
            if (bookDetails.getCopies() != null) book.setCopies(bookDetails.getCopies());
            if (bookDetails.getStatus() != null) book.setStatus(bookDetails.getStatus());
            if (bookDetails.getLocation() != null) book.setLocation(bookDetails.getLocation());
            if (bookDetails.getDescription() != null) book.setDescription(bookDetails.getDescription());
            Book updated = bookRepository.save(book);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/books/{id}/version")
    public ResponseEntity<Book> publishBookVersion(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        return bookRepository.findById(id).map(book -> {
            String prevEdition = book.getEdition();
            String newEdition = body.get("newEdition");
            String notes = body.get("notes");
            String publishedBy = body.getOrDefault("publishedBy", "Librarian");
            String publishedDate = body.getOrDefault("publishedDate", LocalDate.now().toString());

            book.setEdition(newEdition);
            Book updated = bookRepository.save(book);

            BookVersionHistory versionHistory = new BookVersionHistory(
                book.getTitle(),
                prevEdition,
                newEdition,
                notes,
                publishedBy,
                publishedDate
            );
            bookVersionHistoryRepository.save(versionHistory);

            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ════════════════════════════════════════════════
    // MEMBERS CRUD
    // ════════════════════════════════════════════════

    @GetMapping("/members")
    public List<BookMember> getAllMembers() {
        return bookMemberRepository.findAll();
    }

    @PostMapping("/members")
    public BookMember createMember(@RequestBody BookMember member) {
        if (member.getJoined() == null || member.getJoined().trim().isEmpty()) {
            member.setJoined(LocalDate.now().toString());
        }
        if (member.getStatus() == null || member.getStatus().trim().isEmpty()) {
            member.setStatus("ACTIVE");
        }
        return bookMemberRepository.save(member);
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<BookMember> updateMember(@PathVariable("id") Long id, @RequestBody BookMember details) {
        return bookMemberRepository.findById(id).map(member -> {
            if (details.getName() != null) member.setName(details.getName());
            if (details.getMid() != null) member.setMid(details.getMid());
            if (details.getType() != null) member.setType(details.getType());
            if (details.getDept() != null) member.setDept(details.getDept());
            if (details.getEmail() != null) member.setEmail(details.getEmail());
            if (details.getPhone() != null) member.setPhone(details.getPhone());
            if (details.getStatus() != null) member.setStatus(details.getStatus());
            if (details.getJoined() != null) member.setJoined(details.getJoined());
            BookMember updated = bookMemberRepository.save(member);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("id") Long id) {
        if (bookMemberRepository.existsById(id)) {
            bookMemberRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ════════════════════════════════════════════════
    // BORROW RECORDS CRUD & STATUS / RETURNABLE FUNCTIONS
    // ════════════════════════════════════════════════

    @GetMapping("/borrows")
    public List<BookBorrow> getAllBorrows() {
        return bookBorrowRepository.findAll();
    }

    @GetMapping("/borrows/{id}/details")
    public ResponseEntity<?> getBorrowDetails(@PathVariable("id") Long id) {
        Optional<BookBorrow> borrowOpt = bookBorrowRepository.findById(id);
        if (borrowOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BookBorrow borrow = borrowOpt.get();
        Map<String, Object> detailsMap = new HashMap<>();
        detailsMap.put("borrow", borrow);

        // Fetch Member details if available
        if (borrow.getMemberId() != null) {
            bookMemberRepository.findById(borrow.getMemberId()).ifPresent(member -> {
                detailsMap.put("memberMid", member.getMid());
                detailsMap.put("memberEmail", member.getEmail());
                detailsMap.put("memberPhone", member.getPhone());
                detailsMap.put("memberType", member.getType());
                detailsMap.put("memberDept", member.getDept());
            });
        }
        // Fetch Book details if available
        if (borrow.getBookId() != null) {
            bookRepository.findById(borrow.getBookId()).ifPresent(book -> {
                detailsMap.put("bookIsbn", book.getIsbn());
                detailsMap.put("bookCategory", book.getCategory());
                detailsMap.put("bookEdition", book.getEdition());
                detailsMap.put("bookLocation", book.getLocation());
            });
        }

        // Return status calculation
        String returnStatusStr = "RETURNABLE";
        long daysDiff = 0;
        if ("RETURNED".equalsIgnoreCase(borrow.getStatus())) {
            returnStatusStr = "RETURNED";
        } else if (borrow.getDueDate() != null) {
            try {
                LocalDate dueDate = LocalDate.parse(borrow.getDueDate());
                LocalDate today = LocalDate.now();
                if (today.isAfter(dueDate)) {
                    returnStatusStr = "OVERDUE";
                    daysDiff = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                } else {
                    daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
                    returnStatusStr = "DUE_IN_" + daysDiff + "_DAYS";
                }
            } catch (Exception e) {
                returnStatusStr = borrow.getStatus();
            }
        }
        detailsMap.put("returnStatus", returnStatusStr);
        detailsMap.put("daysDifference", daysDiff);

        return ResponseEntity.ok(detailsMap);
    }

    @PostMapping("/borrows")
    public BookBorrow createBorrow(@RequestBody BookBorrow borrow) {
        if (borrow.getBorrowDate() == null || borrow.getBorrowDate().trim().isEmpty()) {
            borrow.setBorrowDate(LocalDate.now().toString());
        }
        if (borrow.getDueDate() == null || borrow.getDueDate().trim().isEmpty()) {
            borrow.setDueDate(LocalDate.now().plusDays(14).toString());
        }
        if (borrow.getStatus() == null || borrow.getStatus().trim().isEmpty()) {
            borrow.setStatus("BORROWED");
        }
        BookBorrow saved = bookBorrowRepository.save(borrow);

        // Update book copies count if bookId provided
        if (borrow.getBookId() != null) {
            bookRepository.findById(borrow.getBookId()).ifPresent(book -> {
                if (book.getCopies() != null && book.getCopies() > 0) {
                    book.setCopies(book.getCopies() - 1);
                    if (book.getCopies() == 0) {
                        book.setStatus("UNAVAILABLE");
                    }
                    bookRepository.save(book);
                }
            });
        }

        return saved;
    }

    @PutMapping("/borrows/{id}/status")
    public ResponseEntity<?> updateBorrowStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status"); // BORROWED, RETURNED, OVERDUE, EXTENDED, LOST
        String notes = payload.get("notes");
        String returnDateParam = payload.get("returnDate");

        return bookBorrowRepository.findById(id).map(borrow -> {
            String prevStatus = borrow.getStatus();
            borrow.setStatus(newStatus);
            if (notes != null) borrow.setNotes(notes);

            if ("RETURNED".equalsIgnoreCase(newStatus)) {
                String rDate = (returnDateParam != null && !returnDateParam.trim().isEmpty()) ? returnDateParam : LocalDate.now().toString();
                borrow.setReturnDate(rDate);

                // Increment book copies if was previously not returned
                if (!"RETURNED".equalsIgnoreCase(prevStatus) && borrow.getBookId() != null) {
                    bookRepository.findById(borrow.getBookId()).ifPresent(book -> {
                        int currentCopies = (book.getCopies() != null) ? book.getCopies() : 0;
                        book.setCopies(currentCopies + 1);
                        if ("UNAVAILABLE".equalsIgnoreCase(book.getStatus()) && book.getCopies() > 0) {
                            book.setStatus("AVAILABLE");
                        }
                        bookRepository.save(book);
                    });
                }
            } else if (returnDateParam != null) {
                borrow.setReturnDate(returnDateParam);
            }

            BookBorrow updated = bookBorrowRepository.save(borrow);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/borrows/{id}")
    public ResponseEntity<BookBorrow> updateBorrow(@PathVariable("id") Long id, @RequestBody BookBorrow details) {
        return bookBorrowRepository.findById(id).map(borrow -> {
            String prevStatus = borrow.getStatus();
            if (details.getMemberId() != null) borrow.setMemberId(details.getMemberId());
            if (details.getMemberName() != null) borrow.setMemberName(details.getMemberName());
            if (details.getBookId() != null) borrow.setBookId(details.getBookId());
            if (details.getBookTitle() != null) borrow.setBookTitle(details.getBookTitle());
            if (details.getBorrowDate() != null) borrow.setBorrowDate(details.getBorrowDate());
            if (details.getDueDate() != null) borrow.setDueDate(details.getDueDate());
            if (details.getReturnDate() != null) borrow.setReturnDate(details.getReturnDate());
            if (details.getNotes() != null) borrow.setNotes(details.getNotes());
            if (details.getStatus() != null) {
                borrow.setStatus(details.getStatus());
                if ("RETURNED".equalsIgnoreCase(details.getStatus()) && !"RETURNED".equalsIgnoreCase(prevStatus)) {
                    if (borrow.getReturnDate() == null || borrow.getReturnDate().trim().isEmpty()) {
                        borrow.setReturnDate(LocalDate.now().toString());
                    }
                    if (borrow.getBookId() != null) {
                        bookRepository.findById(borrow.getBookId()).ifPresent(book -> {
                            int currentCopies = (book.getCopies() != null) ? book.getCopies() : 0;
                            book.setCopies(currentCopies + 1);
                            if ("UNAVAILABLE".equalsIgnoreCase(book.getStatus()) && book.getCopies() > 0) {
                                book.setStatus("AVAILABLE");
                            }
                            bookRepository.save(book);
                        });
                    }
                }
            }
            BookBorrow updated = bookBorrowRepository.save(borrow);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/borrows/returnable")
    public List<Map<String, Object>> getReturnableCustomers() {
        List<BookBorrow> allBorrows = bookBorrowRepository.findAll();
        List<BookMember> allMembers = bookMemberRepository.findAll();

        Map<Long, BookMember> memberMap = new HashMap<>();
        for (BookMember m : allMembers) {
            memberMap.put(m.getId(), m);
        }

        List<Map<String, Object>> returnableList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BookBorrow b : allBorrows) {
            if ("RETURNED".equalsIgnoreCase(b.getStatus())) {
                continue; // Skip returned books
            }
            Map<String, Object> item = new HashMap<>();
            item.put("borrowId", b.getId());
            item.put("memberId", b.getMemberId());
            item.put("memberName", b.getMemberName());
            item.put("bookId", b.getBookId());
            item.put("bookTitle", b.getBookTitle());
            item.put("borrowDate", b.getBorrowDate());
            item.put("dueDate", b.getDueDate());
            item.put("status", b.getStatus());
            item.put("notes", b.getNotes());

            BookMember member = memberMap.get(b.getMemberId());
            if (member != null) {
                item.put("memberMid", member.getMid());
                item.put("email", member.getEmail());
                item.put("phone", member.getPhone());
                item.put("type", member.getType());
                item.put("dept", member.getDept());
            } else {
                item.put("memberMid", "N/A");
                item.put("email", "N/A");
                item.put("phone", "N/A");
                item.put("type", "Member");
                item.put("dept", "N/A");
            }

            boolean isOverdue = false;
            long daysDiff = 0;
            if (b.getDueDate() != null) {
                try {
                    LocalDate dueDate = LocalDate.parse(b.getDueDate());
                    if (today.isAfter(dueDate)) {
                        isOverdue = true;
                        daysDiff = java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
                    } else {
                        daysDiff = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);
                    }
                } catch (Exception e) {}
            }
            item.put("isOverdue", isOverdue);
            item.put("daysDiff", daysDiff);
            item.put("returnStatus", isOverdue ? ("Overdue by " + daysDiff + " day(s)") : ("Due in " + daysDiff + " day(s)"));

            returnableList.add(item);
        }

        return returnableList;
    }

    @DeleteMapping("/borrows/{id}")
    public ResponseEntity<Void> deleteBorrow(@PathVariable("id") Long id) {
        if (bookBorrowRepository.existsById(id)) {
            bookBorrowRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ════════════════════════════════════════════════
    // RESERVATIONS CRUD
    // ════════════════════════════════════════════════

    @GetMapping("/reservations")
    public List<BookReservation> getAllReservations() {
        return bookReservationRepository.findAll();
    }

    @GetMapping("/reservations/student/{studentId}")
    public List<BookReservation> getStudentReservations(@PathVariable("studentId") Long studentId) {
        return bookReservationRepository.findByMemberIdOrderByIdDesc(studentId);
    }

    @PostMapping("/reservations")
    public BookReservation createReservation(@RequestBody BookReservation reservation) {
        if (reservation.getReservedOn() == null || reservation.getReservedOn().trim().isEmpty()) {
            reservation.setReservedOn(LocalDate.now().toString());
        }
        if (reservation.getExpiry() == null || reservation.getExpiry().trim().isEmpty()) {
            reservation.setExpiry(LocalDate.now().plusDays(7).toString());
        }
        if (reservation.getStatus() == null || reservation.getStatus().trim().isEmpty()) {
            reservation.setStatus("PENDING");
        }
        BookReservation saved = bookReservationRepository.save(reservation);
        try {
            systemLogRepository.save(new SystemLog("New book reservation request submitted for '" + saved.getBookTitle() + "' by " + saved.getMemberName(), "INFO"));
        } catch (Exception e) {}
        return saved;
    }

    @PutMapping("/reservations/{id}")
    public ResponseEntity<BookReservation> updateReservation(@PathVariable("id") Long id, @RequestBody BookReservation details) {
        return bookReservationRepository.findById(id).map(res -> {
            String prevStatus = res.getStatus();
            if (details.getMemberId() != null) res.setMemberId(details.getMemberId());
            if (details.getMemberName() != null) res.setMemberName(details.getMemberName());
            if (details.getBookId() != null) res.setBookId(details.getBookId());
            if (details.getBookTitle() != null) res.setBookTitle(details.getBookTitle());
            if (details.getReservedOn() != null) res.setReservedOn(details.getReservedOn());
            if (details.getExpiry() != null) res.setExpiry(details.getExpiry());
            if (details.getStatus() != null) res.setStatus(details.getStatus());

            BookReservation updated = bookReservationRepository.save(res);

            // Handle notification when Library Management updates status
            String newStatus = updated.getStatus();
            if (newStatus != null && !newStatus.equalsIgnoreCase(prevStatus)) {
                Long targetStudentId = updated.getMemberId();
                // If memberId is null or 0, try to locate student by memberName
                if (targetStudentId == null || targetStudentId == 0L) {
                    Optional<Student> studentOpt = studentRepository.findAll().stream()
                            .filter(s -> s.getUser() != null && s.getUser().getFullName().equalsIgnoreCase(updated.getMemberName()))
                            .findFirst();
                    if (studentOpt.isPresent()) {
                        targetStudentId = studentOpt.get().getId();
                    }
                }

                if (targetStudentId != null && targetStudentId > 0L) {
                    if ("APPROVED".equalsIgnoreCase(newStatus)) {
                        StudentNotification notif = new StudentNotification(
                            targetStudentId,
                            null,
                            updated.getBookTitle(),
                            "Book Reserved: Your reservation request for '" + updated.getBookTitle() + "' has been approved by Library Management! Please collect your book at the library before " + updated.getExpiry() + ".",
                            "BOOK_RESERVED"
                        );
                        studentNotificationRepository.save(notif);
                        try {
                            systemLogRepository.save(new SystemLog("Library Management approved reservation for '" + updated.getBookTitle() + "' requested by " + updated.getMemberName() + ". Student notified.", "INFO"));
                        } catch (Exception e) {}
                    } else if ("CANCELLED".equalsIgnoreCase(newStatus) || "REJECTED".equalsIgnoreCase(newStatus)) {
                        StudentNotification notif = new StudentNotification(
                            targetStudentId,
                            null,
                            updated.getBookTitle(),
                            "Reservation Update: Your reservation request for '" + updated.getBookTitle() + "' was cancelled/declined by Library Management.",
                            "RESERVATION_CANCELLED"
                        );
                        studentNotificationRepository.save(notif);
                        try {
                            systemLogRepository.save(new SystemLog("Library Management cancelled reservation for '" + updated.getBookTitle() + "' requested by " + updated.getMemberName() + ".", "INFO"));
                        } catch (Exception e) {}
                    }
                }
            }

            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable("id") Long id) {
        if (bookReservationRepository.existsById(id)) {
            bookReservationRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ════════════════════════════════════════════════
    // ANNOUNCEMENTS CRUD
    // ════════════════════════════════════════════════

    @GetMapping("/announcements")
    public List<LibraryAnnouncement> getAllAnnouncements() {
        return libraryAnnouncementRepository.findAll();
    }

    @PostMapping("/announcements")
    public LibraryAnnouncement createAnnouncement(@RequestBody LibraryAnnouncement announcement) {
        if (announcement.getPostedOn() == null || announcement.getPostedOn().trim().isEmpty()) {
            announcement.setPostedOn(LocalDate.now().toString());
        }
        if (announcement.getPostedBy() == null || announcement.getPostedBy().trim().isEmpty()) {
            announcement.setPostedBy("Librarian");
        }
        return libraryAnnouncementRepository.save(announcement);
    }

    @PutMapping("/announcements/{id}")
    public ResponseEntity<LibraryAnnouncement> updateAnnouncement(@PathVariable("id") Long id, @RequestBody LibraryAnnouncement details) {
        return libraryAnnouncementRepository.findById(id).map(ann -> {
            if (details.getTitle() != null) ann.setTitle(details.getTitle());
            if (details.getType() != null) ann.setType(details.getType());
            if (details.getContent() != null) ann.setContent(details.getContent());
            if (details.getPostedBy() != null) ann.setPostedBy(details.getPostedBy());
            if (details.getPostedOn() != null) ann.setPostedOn(details.getPostedOn());
            LibraryAnnouncement updated = libraryAnnouncementRepository.save(ann);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/announcements/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable("id") Long id) {
        if (libraryAnnouncementRepository.existsById(id)) {
            libraryAnnouncementRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ════════════════════════════════════════════════
    // VERSION HISTORY
    // ════════════════════════════════════════════════

    @GetMapping("/version-history")
    public List<BookVersionHistory> getVersionHistory() {
        return bookVersionHistoryRepository.findAll();
    }

    @PostMapping("/version-history")
    public BookVersionHistory createVersionHistory(@RequestBody BookVersionHistory history) {
        if (history.getPublishedDate() == null || history.getPublishedDate().trim().isEmpty()) {
            history.setPublishedDate(java.time.LocalDate.now().toString());
        }
        return bookVersionHistoryRepository.save(history);
    }

    @DeleteMapping("/version-history/{id}")
    public ResponseEntity<Void> deleteVersionHistory(@PathVariable("id") Long id) {
        if (bookVersionHistoryRepository.existsById(id)) {
            bookVersionHistoryRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}

