package com.school360.pattern;

import com.school360.controller.*;
import com.school360.model.*;
import com.school360.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ManagementPatternsTest {
    private static void inject(Object target, String name, Object value) {
        ReflectionTestUtils.setField(target, name, value);
    }

    @Test void administratorCreatesExistingRoleAccountsAndOnlyStudentProfiles() {
        for (String role : List.of("STUDENT", "TEACHER", "LIBRARIAN", "ENROLLMENT", "STAFF", "SUPPORT", "ADMIN")) {
            AdminController controller = new AdminController();
            UserRepository users = mock(UserRepository.class);
            StudentRepository students = mock(StudentRepository.class);
            inject(controller, "userRepository", users);
            inject(controller, "studentRepository", students);
            inject(controller, "logRepository", mock(SystemLogRepository.class));
            User input = new User("amali", "password123", role, "Amali", "a@example.test", "123", "old");
            User result = (User) controller.createUser(input).getBody();
            assertNotNull(result);
            assertEquals(role, result.getRole());
            assertEquals("QR-" + role + "-AMALI", result.getQrCodeToken());
            assertEquals(input.getPassword(), result.getPassword());
            verify(users).save(result);
            if (role.equals("STUDENT")) {
                ArgumentCaptor<Student> saved = ArgumentCaptor.forClass(Student.class);
                verify(students).save(saved.capture());
                assertSame(result, saved.getValue().getUser());
            } else verifyNoInteractions(students);
        }
    }

    @Test void invalidAccountStillReturnsBadRequestWithoutSaving() {
        AdminController controller = new AdminController();
        UserRepository users = mock(UserRepository.class);
        inject(controller, "userRepository", users);
        User input = new User("amali", "short", "STUDENT", "Amali", null, null, null);
        assertEquals(400, controller.createUser(input).getStatusCode().value());
        verifyNoInteractions(users);
    }

    @Test void teacherAnnouncementNotifiesStudentsOnceAndKeepsExistingSave() {
        TeacherController controller = new TeacherController();
        AnnouncementRepository announcements = mock(AnnouncementRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        StudentNotificationRepository notifications = mock(StudentNotificationRepository.class);
        inject(controller, "announcementRepository", announcements);
        inject(controller, "studentRepository", students);
        inject(controller, "studentNotificationRepository", notifications);
        inject(controller, "logRepository", mock(SystemLogRepository.class));
        Student active = student(7L, "Amali");
        Student deleted = student(8L, "Nimal");
        deleted.setEnrollmentStatus("DELETED");
        when(students.findAll()).thenReturn(List.of(active, deleted));
        Announcement announcement = new Announcement("Meeting", "Room 5", "Teacher", "TEACHER");
        assertSame(announcement, controller.createAnnouncement(announcement).getBody());
        verify(announcements).save(announcement);
        ArgumentCaptor<StudentNotification> saved = ArgumentCaptor.forClass(StudentNotification.class);
        verify(notifications).save(saved.capture());
        assertEquals(7L, saved.getValue().getStudentId());
        assertEquals("ANNOUNCEMENT", saved.getValue().getType());
        assertNull(saved.getValue().getTicketId());
        assertEquals("Announcement: Meeting - Room 5", saved.getValue().getMessage());
    }
    private static Student student(Long id, String name) {
        Student student = new Student(new User(name, "password123", "STUDENT", name, name + "@example.test", "123", null),
                null, null, null, null, "APPROVED_STAFF");
        student.setId(id);
        return student;
    }
    @Test void observerSubscriptionDeduplicatesAndSupportsRemoval() {
        com.school360.pattern.observer.AnnouncementSubject subject = new com.school360.pattern.observer.AnnouncementSubject();
        com.school360.pattern.observer.NotificationObserver observer = mock(com.school360.pattern.observer.NotificationObserver.class);
        subject.notifyObservers();
        subject.addObserver(observer);
        subject.addObserver(observer);
        subject.publishAnnouncement(new Announcement("One", "Body", "Teacher", "TEACHER"));
        subject.removeObserver(observer);
        subject.publishAnnouncement(new Announcement("Two", "Body", "Teacher", "TEACHER"));
        verify(observer).update("Announcement: One - Body");
        verifyNoMoreInteractions(observer);
    }

    @Test void bothReturnEndpointsNotifyOnlyReliablePendingStudentsOnce() {
        for (boolean fullUpdate : List.of(false, true)) {
            LibraryController controller = new LibraryController();
            BookRepository books = mock(BookRepository.class);
            BookBorrowRepository borrows = mock(BookBorrowRepository.class);
            BookReservationRepository reservations = mock(BookReservationRepository.class);
            BookMemberRepository members = mock(BookMemberRepository.class);
            StudentRepository students = mock(StudentRepository.class);
            StudentNotificationRepository notifications = mock(StudentNotificationRepository.class);
            inject(controller, "bookRepository", books);
            inject(controller, "bookBorrowRepository", borrows);
            inject(controller, "bookReservationRepository", reservations);
            inject(controller, "bookMemberRepository", members);
            inject(controller, "studentRepository", students);
            inject(controller, "studentNotificationRepository", notifications);
            Book book = new Book(); book.setId(10L); book.setTitle("Algorithms"); book.setCopies(0); book.setStatus("UNAVAILABLE");
            BookBorrow borrow = new BookBorrow(); borrow.setId(20L); borrow.setBookId(10L); borrow.setStatus("BORROWED");
            when(books.findById(10L)).thenReturn(Optional.of(book));
            when(borrows.findById(20L)).thenReturn(Optional.of(borrow));
            when(borrows.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(students.findById(7L)).thenReturn(Optional.of(student(7L, "Amali")));
            when(members.existsById(8L)).thenReturn(true);
            String future = java.time.LocalDate.now().plusDays(2).toString();
            BookReservation valid = new BookReservation(7L, "Amali", 10L, "Algorithms", null, future, "PENDING");
            BookReservation collision = new BookReservation(8L, "Other", 10L, "Algorithms", null, future, "PENDING");
            BookReservation expired = new BookReservation(9L, "Expired", 10L, "Algorithms", null, "2000-01-01", "PENDING");
            BookReservation otherBook = new BookReservation(7L, "Amali", 11L, "Other", null, future, "PENDING");
            BookReservation mismatch = new BookReservation(7L, "Someone Else", 10L, "Algorithms", null, future, "PENDING");
            when(reservations.findByStatus("PENDING")).thenReturn(List.of(valid, valid, collision, expired, otherBook, mismatch));
            for (int call = 0; call < 2; call++) {
                if (fullUpdate) { BookBorrow details = new BookBorrow(); details.setStatus("RETURNED");
                    assertEquals(200, controller.updateBorrow(20L, details).getStatusCode().value());
                } else assertEquals(200, controller.updateBorrowStatus(20L, Map.of("status", "RETURNED")).getStatusCode().value());
            }
            assertEquals(1, book.getCopies()); assertEquals("AVAILABLE", book.getStatus());
            ArgumentCaptor<StudentNotification> saved = ArgumentCaptor.forClass(StudentNotification.class);
            verify(notifications).save(saved.capture());
            assertEquals(7L, saved.getValue().getStudentId());
            assertEquals("BOOK_AVAILABLE", saved.getValue().getType());
            verify(students, never()).findById(8L);
            verify(students, never()).findById(9L);
        }
    }

    @Test void enrollmentDecisionsPreserveBothStagesAndNotifyOnlyTransitions() {
        for (boolean finalStage : List.of(false, true)) {
            Object controller = finalStage ? new StaffController() : new EnrollmentController();
            EnrollmentRequestRepository requests = mock(EnrollmentRequestRepository.class);
            StudentRepository students = mock(StudentRepository.class);
            StudentNotificationRepository notifications = mock(StudentNotificationRepository.class);
            inject(controller, "enrollmentRequestRepository", requests);
            inject(controller, "studentRepository", students);
            inject(controller, "studentNotificationRepository", notifications);
            inject(controller, "logRepository", mock(SystemLogRepository.class));
            EnrollmentRequest request = new EnrollmentRequest(7L, 10L, "Amali", "Science", "PENDING", "PENDING");
            request.setId(30L);
            Student student = student(7L, "Amali");
            when(requests.findById(30L)).thenReturn(Optional.of(request));
            when(students.findById(7L)).thenReturn(Optional.of(student));
            for (String decision : List.of("APPROVED", "APPROVED", "REJECTED", "REJECTED")) {
                if (finalStage) ((StaffController) controller).approveEnrollment(30L, Map.of("status", decision));
                else ((EnrollmentController) controller).updateStatus(30L, Map.of("status", decision));
                assertEquals(decision + (finalStage ? "_STAFF" : "_EO"), student.getEnrollmentStatus());
            }
            ArgumentCaptor<StudentNotification> saved = ArgumentCaptor.forClass(StudentNotification.class);
            verify(notifications, times(2)).save(saved.capture());
            assertTrue(saved.getAllValues().get(0).getMessage().contains(finalStage ? "Administrative Staff" : "awaits final staff approval"));
            assertTrue(saved.getAllValues().get(1).getMessage().contains("rejected"));
            for (StudentNotification notification : saved.getAllValues()) {
                assertEquals(7L, notification.getStudentId());
                assertEquals("ENROLLMENT_STATUS", notification.getType());
                assertNull(notification.getTicketId());
            }
            assertEquals("REJECTED", finalStage ? request.getStaffStatus() : request.getEoStatus());
        }
    }

    @Test void gradeStrategiesPreserveThresholdsManualPrecedenceAndInvalidInputs() {
        var automatic = com.school360.pattern.strategy.GradeCalculator.forGrade(null);
        double[] marks = {0, 49.99, 50, 59.99, 60, 69.99, 70, 79.99, 80, 89.99, 90, 100};
        String[] grades = {"F", "F", "D", "D", "C", "C", "B", "B", "A", "A", "A+", "A+"};
        for (int i = 0; i < marks.length; i++) assertEquals(grades[i], automatic.assignGrade(null, marks[i], 100.0));
        assertEquals("A", automatic.assignGrade(null, 40.0, 50.0));
        assertEquals("N/A", automatic.assignGrade(null, null, 100.0));
        assertEquals("N/A", automatic.assignGrade(null, 50.0, null));
        assertEquals("N/A", automatic.assignGrade(null, 50.0, 0.0));
        assertEquals("N/A", automatic.assignGrade(null, 50.0, -1.0));
        assertEquals(" custom ", com.school360.pattern.strategy.GradeCalculator.forGrade(" custom ").assignGrade(" custom ", null, 0.0));
        assertEquals("B", com.school360.pattern.strategy.GradeCalculator.forGrade("  ").assignGrade("  ", 70.0, 100.0));
    }
    @Test void examCreateAndUpdateUseStrategiesWithoutChangingReleaseState() {
        StaffController controller = new StaffController();
        ExamMarkRepository marks = mock(ExamMarkRepository.class);
        inject(controller, "examMarkRepository", marks);
        inject(controller, "logRepository", mock(SystemLogRepository.class));
        when(marks.save(any())).thenAnswer(inv -> inv.getArgument(0));
        ExamMark mark = new ExamMark(); mark.setId(40L); mark.setStudentId(7L); mark.setExamTitle("Term");
        mark.setMarksObtained(80.0); mark.setTotalMarks(100.0);
        assertSame(mark, controller.createExamMark(mark).getBody());
        assertEquals("A", mark.getGrade()); assertFalse(mark.isPublished());
        when(marks.findById(40L)).thenReturn(Optional.of(mark));
        ExamMark updated = new ExamMark(); updated.setGrade("Manual"); updated.setPublished(true);
        controller.updateExamMark(40L, updated);
        assertEquals("Manual", mark.getGrade()); assertTrue(mark.isPublished());
        updated.setGrade(" "); updated.setMarksObtained(50.0); updated.setTotalMarks(100.0);
        controller.updateExamMark(40L, updated);
        assertEquals("D", mark.getGrade()); assertTrue(mark.isPublished());
        assertEquals(400, controller.createExamMark(new ExamMark()).getStatusCode().value());
    }

    @Test void supportRepliesAndRequestsKeepOneNotificationAndStatusChangesDoNotRepeat() {
        SupportController controller = new SupportController();
        TicketRepository tickets = mock(TicketRepository.class);
        TicketReplyRepository replies = mock(TicketReplyRepository.class);
        StudentNotificationRepository notifications = mock(StudentNotificationRepository.class);
        inject(controller, "ticketRepository", tickets);
        inject(controller, "ticketReplyRepository", replies);
        inject(controller, "studentNotificationRepository", notifications);
        inject(controller, "logRepository", mock(SystemLogRepository.class));
        Ticket ticket = new Ticket(7L, "Amali", "Help", "Issue", "OPEN", "LOW", "NONE"); ticket.setId(50L);
        when(tickets.findById(50L)).thenReturn(Optional.of(ticket));
        controller.replyToTicket(50L, Map.of("message", "We are checking", "senderName", "Officer"));
        assertEquals("PENDING", ticket.getStatus());
        controller.replyToTicket(50L, Map.of("message", "Send a file", "senderName", "Officer", "requestAttachment", "true"));
        controller.replyToTicket(50L, Map.of("message", "Tell us more", "senderName", "Officer", "requestInfo", "true"));
        controller.requestInfo(50L, Map.of("type", "INFO_REQUEST", "message", "Which course?"));
        controller.updateStatus(50L, Map.of("status", "RESOLVED"));
        controller.updateStatus(50L, Map.of("status", "RESOLVED"));
        assertEquals("RESOLVED", ticket.getStatus());
        ArgumentCaptor<StudentNotification> saved = ArgumentCaptor.forClass(StudentNotification.class);
        verify(notifications, times(5)).save(saved.capture());
        assertEquals(List.of("SUPPORT_REPLY", "ATTACHMENT_REQUEST", "INFO_REQUEST", "INFO_REQUEST", "SUPPORT_STATUS"),
                saved.getAllValues().stream().map(StudentNotification::getType).toList());
        assertEquals("Support Team replied to Ticket #50: Help", saved.getAllValues().get(0).getMessage());
        assertEquals("Action Required: Which course?", saved.getAllValues().get(3).getMessage());
        for (StudentNotification n : saved.getAllValues()) { assertEquals(7L, n.getStudentId()); assertEquals(50L, n.getTicketId()); }
        verify(replies, times(4)).save(any());
    }
}
