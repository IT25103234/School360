package com.school360.controller;

import com.school360.model.*;
import com.school360.model.Module;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReplyRepository ticketReplyRepository;

    @Autowired
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private StudentNotificationRepository studentNotificationRepository;

    @Autowired
    private ExamTimetableRepository examTimetableRepository;

    @Autowired
    private ExamMarkRepository examMarkRepository;


    // Get Student details
    @GetMapping("/profile/{userId}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> getProfile(@PathVariable("userId") Long userId) {
        Optional<Student> studentOpt = studentRepository.findByUserId(userId);
        if (studentOpt.isPresent()) {
            return ResponseEntity.ok(studentOpt.get());
        }
        studentOpt = studentRepository.findById(userId);
        if (studentOpt.isPresent()) {
            return ResponseEntity.ok(studentOpt.get());
        }
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Student newStudent = new Student(user, "ADM-" + String.format("%04d", user.getId()), null, "", "", "UNENROLLED");
            newStudent = studentRepository.save(newStudent);
            return ResponseEntity.ok(newStudent);
        }
        return ResponseEntity.notFound().build();
    }

    // Edit Profile
    @PostMapping("/profile/{userId}/edit")
    public ResponseEntity<?> editProfile(@PathVariable("userId") Long userId, @RequestBody Map<String, String> data) {
        Optional<Student> studentOpt = studentRepository.findByUserId(userId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            User user = student.getUser();
            if (data.containsKey("fullName") && data.get("fullName") != null) {
                String fullName = data.get("fullName").trim();
                if (!fullName.matches("^[a-zA-Z\\s]+$")) {
                    return ResponseEntity.badRequest().body("Name must contain letters and spaces only (no numbers or symbols).");
                }
                user.setFullName(fullName);
            }
            if (data.containsKey("email") && data.get("email") != null) {
                user.setEmail(data.get("email").trim());
            }
            if (data.containsKey("contact") && data.get("contact") != null) {
                String contact = data.get("contact").trim();
                if (!contact.matches("^\\d{1,16}$")) {
                    return ResponseEntity.badRequest().body("Phone number must contain numbers only and be at most 16 digits.");
                }
                user.setContact(contact);
            }
            userRepository.save(user);
            logRepository.save(new SystemLog("Student " + user.getUsername() + " updated profile information.", "INFO"));
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    // Browse Available Courses
    @GetMapping("/courses")
    public ResponseEntity<?> getCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    // Request Course Enrollment
    @PostMapping("/enroll")
    public ResponseEntity<?> enrollInCourse(@RequestBody Map<String, Long> payload) {
        Long studentId = payload.get("studentId");
        Long courseId = payload.get("courseId");

        if (studentId == null || courseId == null) {
            return ResponseEntity.badRequest().body("Student ID and Course ID are required.");
        }

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<Course> courseOpt = courseRepository.findById(courseId);

        if (studentOpt.isPresent() && courseOpt.isPresent()) {
            Student student = studentOpt.get();
            Course course = courseOpt.get();

            // Create enrollment request
            EnrollmentRequest request = new EnrollmentRequest(
                student.getId(),
                course.getId(),
                student.getUser().getFullName(),
                course.getName(),
                "PENDING",
                "PENDING"
            );

            // Update student status
            student.setCourseId(course.getId());
            student.setEnrollmentStatus("PENDING_EO");
            studentRepository.save(student);
            enrollmentRequestRepository.save(request);

            logRepository.save(new SystemLog("Student " + student.getUser().getUsername() + " submitted enrollment request for " + course.getName(), "INFO"));
            return ResponseEntity.ok(request);
        }
        return ResponseEntity.badRequest().body("Invalid student or course reference.");
    }

    // View approved modules (after receptionist approves: enrollmentStatus == 'APPROVED_STAFF')
    @GetMapping("/{studentId}/modules")
    public ResponseEntity<?> getApprovedModules(@PathVariable("studentId") Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if ("APPROVED_STAFF".equals(student.getEnrollmentStatus())) {
                List<Module> modules = moduleRepository.findByCourseId(student.getCourseId());
                return ResponseEntity.ok(modules);
            }
            return ResponseEntity.ok(new ArrayList<Module>()); // Return empty if not fully approved
        }
        return ResponseEntity.notFound().build();
    }

    // View Timetable
    @GetMapping("/{studentId}/timetable")
    public ResponseEntity<?> getTimetable(@PathVariable("studentId") Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if ("APPROVED_STAFF".equals(student.getEnrollmentStatus())) {
                List<Module> modules = moduleRepository.findByCourseId(student.getCourseId());
                List<Long> moduleIds = new ArrayList<>();
                for (Module m : modules) {
                    moduleIds.add(m.getId());
                }
                if (moduleIds.isEmpty()) {
                    return ResponseEntity.ok(new ArrayList<Schedule>());
                }
                return ResponseEntity.ok(scheduleRepository.findByModuleIdIn(moduleIds));
            }
            return ResponseEntity.ok(new ArrayList<Schedule>());
        }
        return ResponseEntity.notFound().build();
    }

    // View announcements
    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements() {
        return ResponseEntity.ok(announcementRepository.findAllByOrderByIdDesc());
    }

    // Raise Support Ticket
    @PostMapping("/tickets")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> raiseTicket(@RequestBody Map<String, String> payload) {
        if (payload.get("studentId") == null) {
            return ResponseEntity.badRequest().body("studentId is required");
        }
        Long studentId = Long.parseLong(payload.get("studentId"));
        String title = payload.get("title");
        String description = payload.get("description");

        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            studentOpt = studentRepository.findByUserId(studentId);
        }
        if (studentOpt.isEmpty()) {
            Optional<User> userOpt = userRepository.findById(studentId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Student newStudent = new Student(user, "ADM-" + String.format("%04d", user.getId()), null, "", "", "UNENROLLED");
                studentOpt = Optional.of(studentRepository.save(newStudent));
            }
        }
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            Ticket ticket = new Ticket(
                student.getId(),
                student.getUser().getFullName(),
                title,
                description,
                "OPEN",
                "LOW",
                "NONE"
            );
            ticketRepository.save(ticket);
            logRepository.save(new SystemLog("Student " + student.getUser().getUsername() + " raised support ticket: " + title, "INFO"));
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    // View raised support tickets
    @GetMapping("/{studentId}/tickets")
    public ResponseEntity<?> getTickets(@PathVariable("studentId") Long studentId) {
        List<Ticket> tickets = ticketRepository.findByStudentId(studentId);
        if (tickets.isEmpty()) {
            Optional<Student> sOpt = studentRepository.findByUserId(studentId);
            if (sOpt.isPresent()) {
                tickets = ticketRepository.findByStudentId(sOpt.get().getId());
            }
        }
        return ResponseEntity.ok(tickets);
    }

    // Cancel enrollment request (before approval)
    @DeleteMapping("/enrollment-request/{studentId}")
    public ResponseEntity<?> cancelEnrollmentRequest(@PathVariable("studentId") Long studentId) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if ("PENDING_EO".equals(student.getEnrollmentStatus())) {
                student.setEnrollmentStatus(null);
                student.setCourseId(null);
                studentRepository.save(student);

                List<EnrollmentRequest> requests = enrollmentRequestRepository.findByStudentId(studentId);
                for (EnrollmentRequest req : requests) {
                    if ("PENDING".equals(req.getEoStatus())) {
                        enrollmentRequestRepository.delete(req);
                    }
                }
                logRepository.save(new SystemLog("Student " + student.getUser().getUsername() + " cancelled enrollment request.", "INFO"));
                return ResponseEntity.ok("Enrollment request cancelled successfully.");
            }
            return ResponseEntity.badRequest().body("Cannot cancel request already processed.");
        }
        return ResponseEntity.notFound().build();
    }

    // Ticket replies
    @GetMapping("/tickets/{ticketId}/replies")
    public ResponseEntity<?> getTicketReplies(@PathVariable("ticketId") Long ticketId) {
        return ResponseEntity.ok(ticketReplyRepository.findByTicketIdOrderByIdAsc(ticketId));
    }

    // Post reply or add details
    @PostMapping("/tickets/{ticketId}/replies")
    public ResponseEntity<?> replyToTicket(@PathVariable("ticketId") Long ticketId, @RequestBody Map<String, String> payload) {
        String senderName = payload.get("senderName");
        String message = payload.get("message");
        String attachmentName = payload.get("attachmentName");
        String attachmentData = payload.get("attachmentData");

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            TicketReply reply = new TicketReply(
                ticketId,
                senderName,
                "STUDENT",
                message,
                attachmentName,
                attachmentData
            );
            ticketReplyRepository.save(reply);
            
            // Re-open ticket if closed, or mark as Open
            if ("CLOSED".equals(ticket.getStatus()) || "RESOLVED".equals(ticket.getStatus())) {
                ticket.setStatus("OPEN");
                ticketRepository.save(ticket);
            }

            return ResponseEntity.ok(reply);
        }
        return ResponseEntity.notFound().build();
    }

    // ════════════════════════════════════════════════
    // STUDENT NOTIFICATIONS
    // ════════════════════════════════════════════════

    @GetMapping("/{studentId}/notifications")
    public ResponseEntity<?> getStudentNotifications(@PathVariable("studentId") Long studentId) {
        return ResponseEntity.ok(studentNotificationRepository.findByStudentIdOrderByIdDesc(studentId));
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable("id") Long id) {
        return studentNotificationRepository.findById(id).map(notif -> {
            notif.setIsRead(true);
            studentNotificationRepository.save(notif);
            return ResponseEntity.ok(notif);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{studentId}/notifications/unread-count")
    public ResponseEntity<?> getUnreadNotificationCount(@PathVariable("studentId") Long studentId) {
        long count = studentNotificationRepository.countByStudentIdAndIsReadFalse(studentId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // ── RELEASED EXAM TIMETABLES (STUDENT) ──
    @GetMapping("/{userId}/exam-timetables")
    public ResponseEntity<?> getStudentExamTimetables(@PathVariable("userId") Long userId) {
        Optional<Student> sOpt = studentRepository.findByUserId(userId);
        if (sOpt.isPresent()) {
            Long courseId = sOpt.get().getCourseId();
            if (courseId != null) {
                return ResponseEntity.ok(examTimetableRepository.findByCourseIdAndPublished(courseId, true));
            }
            return ResponseEntity.ok(examTimetableRepository.findByPublished(true));
        }
        return ResponseEntity.ok(examTimetableRepository.findByPublished(true));
    }

    // ── RELEASED EXAM RESULTS (STUDENT) ──
    @GetMapping("/{userId}/exam-results")
    public ResponseEntity<?> getStudentExamResults(@PathVariable("userId") Long userId) {
        Optional<Student> sOpt = studentRepository.findByUserId(userId);
        if (sOpt.isPresent()) {
            Student student = sOpt.get();
            return ResponseEntity.ok(examMarkRepository.findByStudentIdAndPublished(student.getId(), true));
        }
        return ResponseEntity.ok(Collections.emptyList());
    }
}

