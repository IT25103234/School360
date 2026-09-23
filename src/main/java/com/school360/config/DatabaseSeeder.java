package com.school360.config;

import com.school360.model.*;
import com.school360.model.Module;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DatabaseSeeder implements CommandLineRunner {

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
    private SystemLogRepository logRepository;

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
    private ExamTimetableRepository examTimetableRepository;

    @Autowired
    private ExamMarkRepository examMarkRepository;

    @Autowired
    private ExamTimetableRequestRepository examTimetableRequestRepository;


    @Override
    public void run(String... args) throws Exception {
        java.util.List<User> existingUsers = userRepository.findAll();

        // ── Seed Users & Initial Data (only runs when DB is empty) ──
        if (existingUsers.isEmpty()) {
            logRepository.save(new SystemLog("Starting database seeding process...", "INFO"));

            // 0. Librarian
            User librarianUser = new User("aathu11", "aathu_11", "LIBRARIAN", "Aathmika", "aathu@school360.com", "+94 70 000 0000", "QR-LIBRARIAN-AATHU11");
            userRepository.save(librarianUser);
            logRepository.save(new SystemLog("Librarian account 'aathu11' created.", "INFO"));

            // 1. Student
            User studentUser = new User(
                "Sameeha",
                "Sameeha@360",
                "STUDENT",
                "Sameeha",
                "sameeha@school360.com",
                "+94 77 123 4567",
                "QR-STUDENT-SAMEEHA"
            );
            userRepository.save(studentUser);

            // 2. Teacher
            User teacherUser = new User(
                "Madawala",
                "Sameeha@360",
                "TEACHER",
                "Madawala",
                "madawala@school360.com",
                "+94 77 987 6543",
                "QR-TEACHER-MADAWALA"
            );
            userRepository.save(teacherUser);

            // 3. Admin
            User adminUser = new User(
                "Sameehaa",
                "Sameehaa@360",
                "ADMIN",
                "Sameehaa",
                "sameehaa@school360.com",
                "+94 71 555 1234",
                "QR-ADMIN-SAMEEHAA"
            );
            userRepository.save(adminUser);

            // 4. Student Support Team
            User supportUser = new User(
                "Fatheen",
                "Fatheen@360",
                "SUPPORT",
                "Fatheen",
                "fatheen@school360.com",
                "+94 76 222 3344",
                "QR-SUPPORT-FATHEEN"
            );
            userRepository.save(supportUser);

            // 5. Administrative Officer (Receptionist / Staff)
            User staffUser = new User(
                "Kavinde",
                "Kavinde@360",
                "STAFF",
                "Kavinde",
                "kavinde@school360.com",
                "+94 72 444 5566",
                "QR-STAFF-KAVINDE"
            );
            userRepository.save(staffUser);

            // 6. Enrollment Officer
            User enrollmentUser = new User(
                "Shakeer",
                "Shakeer@360",
                "ENROLLMENT",
                "Shakeer",
                "shakeer@school360.com",
                "+94 75 777 8899",
                "QR-ENROLLMENT-SHAKEER"
            );
            userRepository.save(enrollmentUser);

            // Seed default Courses
            Course course1 = new Course("BSc (Hons) in Software Engineering", "SE-101", "A standard program covering software development methodologies and fullstack development.");
            Course course2 = new Course("BSc (Hons) in Business Management", "BM-202", "Provides fundamental business, leadership, and management skills.");
            Course course3 = new Course("BSc (Hons) in Cyber Security", "CS-303", "Explores network defense, cryptography, and ethical hacking.");
            courseRepository.saveAll(Arrays.asList(course1, course2, course3));

            // Create Student detail record for Sameeha
            Student student = new Student(studentUser, "ADM-360-001", course1.getId(), "Year 3", "Section A", "APPROVED_STAFF");
            studentRepository.save(student);

            // Create Modules for Course 1, assigned to Teacher (Madawala)
            Module mod1 = new Module("Advanced Programming in Java", "SE-302", course1.getId(), teacherUser.getId());
            Module mod2 = new Module("Web Applications Architecture", "SE-304", course1.getId(), teacherUser.getId());
            Module mod3 = new Module("Database Design & Management", "SE-306", course1.getId(), teacherUser.getId());
            moduleRepository.saveAll(Arrays.asList(mod1, mod2, mod3));

            // Create Timetable Timings
            Schedule sched1 = new Schedule(mod1.getId(), mod1.getName(), teacherUser.getId(), "Monday", "08:30", "10:30", "Lab 3 (Level 2)", "ACTIVE", "Lecture begins at 08:30 AM");
            Schedule sched2 = new Schedule(mod2.getId(), mod2.getName(), teacherUser.getId(), "Wednesday", "11:00", "13:00", "Hall C (Ground Floor)", "ACTIVE", "Lecture begins at 11:00 AM");
            Schedule sched3 = new Schedule(mod3.getId(), mod3.getName(), teacherUser.getId(), "Friday", "14:00", "16:00", "Lab 2 (Level 2)", "ACTIVE", "Lecture begins at 02:00 PM");
            scheduleRepository.saveAll(Arrays.asList(sched1, sched2, sched3));

            // Seed Announcements
            announcementRepository.save(new Announcement("Welcome to School360", "Welcome all students and teachers to the School360 Academic portal. Please review your schedules and profile details.", adminUser.getFullName(), "ADMIN"));
            announcementRepository.save(new Announcement("Java Assignment Due", "Please make sure to submit your Advanced Java assignment before the deadline on Friday.", teacherUser.getFullName(), "TEACHER"));

            // Seed Support Tickets
            Ticket ticket1 = new Ticket(student.getId(), studentUser.getFullName(), "Portal Access Slow", "I am experiencing slow loading speeds when opening the course materials page.", "OPEN", "MEDIUM", "NONE");
            ticketRepository.save(ticket1);

            ticketReplyRepository.save(new TicketReply(ticket1.getId(), studentUser.getFullName(), "STUDENT", "Hi, the dashboard sometimes takes more than 5 seconds to show the timetable details. Please assist.", null, null));

            // Seed Library Database Tables
            if (bookRepository.count() == 0) {
                String today = java.time.LocalDate.now().toString();
                String minus10 = java.time.LocalDate.now().minusDays(10).toString();
                String minus20 = java.time.LocalDate.now().minusDays(20).toString();
                String minus5 = java.time.LocalDate.now().minusDays(5).toString();
                String plus4 = java.time.LocalDate.now().plusDays(4).toString();
                String plus7 = java.time.LocalDate.now().plusDays(7).toString();
                String minus30 = java.time.LocalDate.now().minusDays(30).toString();
                String minus3 = java.time.LocalDate.now().minusDays(3).toString();
                String minus60 = java.time.LocalDate.now().minusDays(60).toString();

                Book b1 = new Book("Introduction to Algorithms", "Cormen et al.", "9780262046305", "Computer Science", "4th Ed", 5, "AVAILABLE", "Sec A, Row 1", "A comprehensive textbook on algorithms and data structures.");
                Book b2 = new Book("Atomic Habits", "James Clear", "9780735211292", "Self-Help", "1st Ed", 3, "AVAILABLE", "Sec C, Row 2", "Tiny changes that create remarkable results.");
                Book b3 = new Book("The Art of War", "Sun Tzu", "9781590302255", "Philosophy", "Classics Ed", 0, "UNAVAILABLE", "Sec D, Row 5", "Ancient Chinese military and strategic treatise.");
                Book b4 = new Book("Physics for Scientists", "Serway & Jewett", "9781133947271", "Physics", "10th Ed", 8, "AVAILABLE", "Sec B, Row 3", "Calculus-based physics for engineers and scientists.");
                bookRepository.saveAll(Arrays.asList(b1, b2, b3, b4));

                BookMember m1 = new BookMember("Alice Johnson", "LIB-2024-001", "Student", "Grade 10", "alice@school.edu", "+1 555-0101", "ACTIVE", today);
                BookMember m2 = new BookMember("Bob Smith", "LIB-2024-002", "Teacher", "Science", "bob@school.edu", "+1 555-0102", "ACTIVE", today);
                BookMember m3 = new BookMember("Carol White", "LIB-2024-003", "Staff", "Admin", "carol@school.edu", "+1 555-0103", "SUSPENDED", minus30);
                bookMemberRepository.saveAll(Arrays.asList(m1, m2, m3));

                BookBorrow bw1 = new BookBorrow(m1.getId(), m1.getName(), b1.getId(), b1.getTitle(), minus10, plus4, null, "BORROWED", "");
                BookBorrow bw2 = new BookBorrow(m2.getId(), m2.getName(), b2.getId(), b2.getTitle(), minus20, minus5, null, "OVERDUE", "");
                bookBorrowRepository.saveAll(Arrays.asList(bw1, bw2));

                BookReservation res1 = new BookReservation(m1.getId(), m1.getName(), b3.getId(), b3.getTitle(), today, plus7, "PENDING");
                bookReservationRepository.saveAll(Arrays.asList(res1));

                LibraryAnnouncement la1 = new LibraryAnnouncement("Library Open on Saturday", "Notice", "The library will be open this Saturday from 9AM–4PM for exam preparation.", "Librarian Aathmika", today);
                LibraryAnnouncement la2 = new LibraryAnnouncement("New Books Arrived", "General", "Fifty new titles across Science and Literature sections have been added.", "Librarian Aathmika", minus3);
                libraryAnnouncementRepository.saveAll(Arrays.asList(la1, la2));

                BookVersionHistory vh1 = new BookVersionHistory("Introduction to Algorithms", "3rd Ed", "4th Ed", "Updated graph algorithm chapters and new exercises.", "Librarian Aathmika", minus60);
                bookVersionHistoryRepository.saveAll(Arrays.asList(vh1));

                logRepository.save(new SystemLog("Initial Library database tables seeded successfully.", "INFO"));
            }

            logRepository.save(new SystemLog("Database seeding complete. Default credentials loaded.", "INFO"));
        } else {
            java.util.List<User> dirtyUsers = new java.util.ArrayList<>();

            // Guarantee default accounts exist
            if (existingUsers.stream().noneMatch(u -> "Sameeha".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("Sameeha", "Sameeha@360", "STUDENT", "Sameeha", "sameeha@school360.com", "+94 77 123 4567", "QR-STUDENT-SAMEEHA"));
            }
            if (existingUsers.stream().noneMatch(u -> "Madawala".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("Madawala", "Sameeha@360", "TEACHER", "Madawala", "madawala@school360.com", "+94 77 987 6543", "QR-TEACHER-MADAWALA"));
            }
            if (existingUsers.stream().noneMatch(u -> "Sameehaa".equalsIgnoreCase(u.getUsername()) || "Aathmika".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("Sameehaa", "Sameehaa@360", "ADMIN", "Sameehaa", "sameehaa@school360.com", "+94 71 555 1234", "QR-ADMIN-SAMEEHAA"));
            }
            if (existingUsers.stream().noneMatch(u -> "Fatheen".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("Fatheen", "Fatheen@360", "SUPPORT", "Fatheen", "fatheen@school360.com", "+94 76 222 3344", "QR-SUPPORT-FATHEEN"));
            }
            if (existingUsers.stream().noneMatch(u -> "Kavinde".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("Kavinde", "Kavinde@360", "STAFF", "Kavinde", "kavinde@school360.com", "+94 72 444 5566", "QR-STAFF-KAVINDE"));
            }
            if (existingUsers.stream().noneMatch(u -> "Shakeer".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("Shakeer", "Shakeer@360", "ENROLLMENT", "Shakeer", "shakeer@school360.com", "+94 75 777 8899", "QR-ENROLLMENT-SHAKEER"));
            }
            if (existingUsers.stream().noneMatch(u -> "aathu11".equalsIgnoreCase(u.getUsername()))) {
                dirtyUsers.add(new User("aathu11", "aathu_11", "LIBRARIAN", "Aathmika", "aathu@school360.com", "+94 70 000 0000", "QR-LIBRARIAN-AATHU11"));
            }

            java.util.Map<String,String> usernameFullNameMap = new java.util.LinkedHashMap<>();
            usernameFullNameMap.put("Sameeha",  "Sameeha");
            usernameFullNameMap.put("Madawala", "Madawala");
            usernameFullNameMap.put("Aathmika", "Aathmika Shanaz");
            usernameFullNameMap.put("Fatheen",  "Fatheen");
            usernameFullNameMap.put("Kavinde",  "Kavinde");
            usernameFullNameMap.put("Shakeer",  "Shakeer");
            usernameFullNameMap.put("aathu11",  "Aathmika");

            java.util.Map<String,String> usernameMap = new java.util.LinkedHashMap<>();
            usernameMap.put("Sameeha_school360",  "Sameeha");
            usernameMap.put("Madawala_school360", "Madawala");
            usernameMap.put("Aathmika_school360", "Aathmika");
            usernameMap.put("Fatheen_school360",  "Fatheen");
            usernameMap.put("Kavinde_school360",  "Kavinde");
            usernameMap.put("Shakeer_school360",  "Shakeer");
            usernameMap.put("Nadeesha_school360", "Nadeesha");

            // Deduplicate Sameeha accounts if duplicate entries exist in DB
            java.util.List<User> sameehaUsers = existingUsers.stream()
                .filter(u -> "Sameeha".equalsIgnoreCase(u.getUsername()))
                .toList();
            if (sameehaUsers.size() > 1) {
                for (int i = 1; i < sameehaUsers.size(); i++) {
                    userRepository.delete(sameehaUsers.get(i));
                }
            }

            for (User u : existingUsers) {
                boolean mutated = false;
                String mappedUsername = usernameMap.get(u.getUsername());
                if (mappedUsername != null && !mappedUsername.equals(u.getUsername())) {
                    u.setUsername(mappedUsername);
                    mutated = true;
                }
                String mappedFullName = usernameFullNameMap.get(u.getUsername());
                if (mappedFullName != null && !mappedFullName.equals(u.getFullName())) {
                    u.setFullName(mappedFullName);
                    mutated = true;
                } else if (mappedFullName == null && "Sameeha".equalsIgnoreCase(u.getFullName()) && !"Sameeha".equalsIgnoreCase(u.getUsername())) {
                    u.setFullName(u.getUsername().substring(0, 1).toUpperCase() + u.getUsername().substring(1));
                    mutated = true;
                }
                if (u.getQrCodeToken() == null || u.getQrCodeToken().isBlank()) {
                    u.setQrCodeToken("QR-" + u.getRole() + "-" + u.getUsername().toUpperCase());
                    mutated = true;
                }
                if (mutated) {
                    dirtyUsers.add(u);
                }
            }

            if (!dirtyUsers.isEmpty()) {
                userRepository.saveAll(dirtyUsers);
            }
        }

        // Seed initial Exam data if missing
        if (examTimetableRepository.count() == 0) {
            Course c1 = courseRepository.findAll().stream().findFirst().orElse(null);
            Long cId = c1 != null ? c1.getId() : 1L;
            String cName = c1 != null ? c1.getName() : "BSc (Hons) in Software Engineering";

            ExamTimetable et1 = new ExamTimetable("Mid-Term Assessment 2026", cId, cName, "Advanced Programming in Java", "2026-09-15", "09:00 AM", "11:00 AM", "Main Exam Hall A", true, "SCHEDULED");
            ExamTimetable et2 = new ExamTimetable("Mid-Term Assessment 2026", cId, cName, "Web Applications Architecture", "2026-09-17", "01:30 PM", "03:30 PM", "Lab Complex 2", true, "SCHEDULED");
            ExamTimetable et3 = new ExamTimetable("Final Examination 2026", cId, cName, "Database Design & Management", "2026-11-20", "10:00 AM", "12:00 PM", "Auditorium B", false, "SCHEDULED");
            examTimetableRepository.saveAll(Arrays.asList(et1, et2, et3));

            Student studentSample = studentRepository.findAll().stream().findFirst().orElse(null);
            if (studentSample != null) {
                String stName = studentSample.getUser() != null ? studentSample.getUser().getFullName() : "Sameeha";
                ExamMark em1 = new ExamMark("Mid-Term Assessment 2026", studentSample.getId(), stName, studentSample.getAdmissionNumber(), cId, cName, "Advanced Programming in Java", 88.5, 100.0, "A", "Excellent practical proficiency", true);
                ExamMark em2 = new ExamMark("Mid-Term Assessment 2026", studentSample.getId(), stName, studentSample.getAdmissionNumber(), cId, cName, "Web Applications Architecture", 92.0, 100.0, "A+", "Outstanding score", true);
                ExamMark em3 = new ExamMark("Final Examination 2026", studentSample.getId(), stName, studentSample.getAdmissionNumber(), cId, cName, "Database Design & Management", 78.0, 100.0, "B+", "Good effort, work on normalization", false);
                examMarkRepository.saveAll(Arrays.asList(em1, em2, em3));
            }

            User teacherSample = userRepository.findAll().stream().filter(u -> "TEACHER".equals(u.getRole())).findFirst().orElse(null);
            if (teacherSample != null) {
                ExamTimetableRequest req = new ExamTimetableRequest(
                    et2.getId(), teacherSample.getId(), teacherSample.getFullName(), et2.getExamTitle(), et2.getSubject(),
                    et2.getExamDate(), "2026-09-18", "02:00 PM", "04:00 PM", "Lab Complex 3",
                    "Overlapping lab setup required for Web Apps practical test.", "PENDING", null, "2026-08-22"
                );
                examTimetableRequestRepository.save(req);
            }

            logRepository.save(new SystemLog("Exam timetables, marks lists, and change requests initialized.", "INFO"));
        }

        userRepository.findAll().stream()
            .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
            .forEach(admin -> {
                if (!"Sameehaa".equals(admin.getUsername()) || !"Sameehaa".equals(admin.getFullName())) {
                    admin.setUsername("Sameehaa");
                    admin.setPassword("Sameehaa@360");
                    admin.setFullName("Sameehaa");
                    admin.setEmail("sameehaa@school360.com");
                    admin.setQrCodeToken("QR-ADMIN-SAMEEHAA");
                    userRepository.save(admin);
                    logRepository.save(new SystemLog("Updated ADMIN account username/fullname to 'Sameehaa'", "INFO"));
                }
            });
    }
}

