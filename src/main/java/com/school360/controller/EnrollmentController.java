package com.school360.controller;

import com.school360.model.*;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/enrollment")
@CrossOrigin(origins = "*")
public class EnrollmentController {

    @Autowired
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private DeletedStudentRepository deletedStudentRepository;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    private String toJsonString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String str) return str;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    // ─────────────────────────────────────────────
    // VIEW ALL APPLICATIONS
    // ─────────────────────────────────────────────
    @GetMapping("/applications")
    public ResponseEntity<?> getAllApplications() {
        return ResponseEntity.ok(enrollmentRequestRepository.findAll());
    }

    // ─────────────────────────────────────────────
    // LIST ALL STUDENTS (with merged user info)
    // ─────────────────────────────────────────────
    @GetMapping("/all-students")
    public ResponseEntity<?> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Student s : students) {
            if ("DELETED".equalsIgnoreCase(s.getEnrollmentStatus())) continue;
            Map<String, Object> row = new HashMap<>();
            row.put("id",               s.getId());
            row.put("userId",           s.getUser() != null ? s.getUser().getId() : null);
            row.put("fullName",         s.getUser() != null ? s.getUser().getFullName() : "—");
            row.put("username",         s.getUser() != null ? s.getUser().getUsername() : "—");
            row.put("email",            s.getUser() != null ? s.getUser().getEmail() : "—");
            row.put("admissionNumber",  s.getAdmissionNumber());
            row.put("className",        s.getClassName());
            row.put("section",          s.getSection());
            row.put("enrollmentStatus", s.getEnrollmentStatus());
            row.put("courseId",         s.getCourseId());
            result.add(row);
        }
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────
    // REGISTER NEW STUDENT (by Enrollment Officer)
    // ─────────────────────────────────────────────
    @PostMapping("/register-student")
    @Transactional
    public ResponseEntity<?> registerStudent(@RequestBody Map<String, Object> payload) {
        String fullName  = clean(payload.get("fullName"));
        String username  = clean(payload.get("username"));
        String password  = payload.get("password") != null ? payload.get("password").toString() : null;
        String email     = clean(payload.get("email"));
        String contact   = clean(payload.get("contact"));

        if (fullName == null || username == null || password == null || password.isBlank()
                || email == null || contact == null) {
            return ResponseEntity.badRequest().body("Full name, username, password, email and contact are required.");
        }
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
        }
        if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            return ResponseEntity.badRequest().body("Name must contain letters and spaces only (no numbers or symbols).");
        }
        if (!contact.matches("^\\d{1,16}$")) {
            return ResponseEntity.badRequest().body("Phone number must contain numbers only and be at most 16 digits.");
        }
        String nic = clean(payload.get("nic"));
        if (nic != null && !nic.matches("^\\d{16}$")) {
            return ResponseEntity.badRequest().body("NIC must be exactly 16 digits (numbers only, no symbols or letters).");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken. Please choose another.");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest().body("An account with this email already exists.");
        }

        // Create User account
        User user = new User(username, password, "STUDENT", fullName, email, contact,
                "QR-STUDENT-" + UUID.randomUUID());
        userRepository.save(user);

        // Create Student record
        String admissionNumber = clean(payload.get("admissionNumber"));
        String className       = clean(payload.get("className"));
        String section         = clean(payload.get("section"));
        Long courseId = payload.get("courseId") != null ? Long.valueOf(payload.get("courseId").toString()) : null;

        Student student = new Student(user,
                admissionNumber != null ? admissionNumber : "",
                courseId, className != null ? className : "",
                section != null ? section : "",
                "PENDING_EO");
        studentRepository.save(student);

        // Auto-create Enrollment Request
        String courseName = "Unknown Course";
        if (courseId != null) {
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isPresent()) {
                courseName = courseOpt.get().getName();
            }
        }
        EnrollmentRequest req = new EnrollmentRequest(
                student.getId(), courseId, fullName, courseName, "PENDING", "PENDING");
        if (admissionNumber != null) req.setAdmissionNumber(admissionNumber);
        if (className != null) req.setClassName(className);
        if (section != null) req.setSection(section);
        if (payload.get("batch") != null) req.setBatch(payload.get("batch").toString());
        if (payload.get("intake") != null) req.setIntake(payload.get("intake").toString());
        
        Object uploadedDocsObj = payload.get("uploadedDocs");
        Object docStatesObj    = payload.get("docStates");
        String docStatus       = clean(payload.get("docStatus"));
        
        if (uploadedDocsObj != null) req.setUploadedDocs(toJsonString(uploadedDocsObj));
        if (docStatesObj != null)    req.setDocStates(toJsonString(docStatesObj));
        if (docStatus != null)       req.setDocStatus(docStatus);

        req.setRemarks("Registered by Enrollment Officer");
        enrollmentRequestRepository.save(req);

        logRepository.save(new SystemLog("Enrollment Officer registered new student: " + fullName + " (" + username + ")", "INFO"));

        Map<String, Object> response = new HashMap<>();
        response.put("id",       user.getId());
        response.put("username", user.getUsername());
        response.put("role",     user.getRole());
        response.put("fullName", user.getFullName());
        response.put("email",    user.getEmail());
        response.put("contact",  user.getContact());
        response.put("qrCodeToken", user.getQrCodeToken());
        return ResponseEntity.ok(response);
    }

    // ─────────────────────────────────────────────
    // UPDATE STUDENT CREDENTIALS
    // ─────────────────────────────────────────────
    @PostMapping("/students/{userId}/credentials")
    @Transactional
    public ResponseEntity<?> updateCredentials(@PathVariable("userId") Long userId, @RequestBody Map<String, String> payload) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        String newUsername = payload.get("username");
        String newPassword = payload.get("password");

        if (newUsername != null && !newUsername.isBlank() && !newUsername.equals(user.getUsername())) {
            if (userRepository.findByUsername(newUsername).isPresent()) {
                return ResponseEntity.badRequest().body("Username already taken.");
            }
            user.setUsername(newUsername);
        }
        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
            }
            user.setPassword(newPassword);
        }
        userRepository.save(user);
        logRepository.save(new SystemLog("Enrollment Officer updated credentials for user ID: " + userId, "INFO"));
        return ResponseEntity.ok(user);
    }

    // ─────────────────────────────────────────────
    // SAVE DOCUMENT VERIFICATION STATUS
    // ─────────────────────────────────────────────
    @PostMapping("/applications/{id}/documents")
    public ResponseEntity<?> saveDocumentVerification(@PathVariable("id") Long id, @RequestBody Map<String, Object> payload) {
        Optional<EnrollmentRequest> requestOpt = enrollmentRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();
            String docStatus = payload.get("docStatus") != null ? payload.get("docStatus").toString() : "PENDING";
            String remarks   = payload.get("remarks")   != null ? payload.get("remarks").toString()   : null;
            Object docStatesObj    = payload.get("docStates");
            Object uploadedDocsObj = payload.get("uploadedDocs");

            request.setDocStatus(docStatus);
            if (docStatesObj    != null) request.setDocStates(toJsonString(docStatesObj));
            if (uploadedDocsObj != null) request.setUploadedDocs(toJsonString(uploadedDocsObj));
            if (remarks != null && !remarks.isBlank()) request.setRemarks(remarks);
            enrollmentRequestRepository.save(request);
            logRepository.save(new SystemLog("Document verification saved for Enrollment Request ID " + id + ": " + docStatus, "INFO"));
            return ResponseEntity.ok(request);
        }
        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────
    // PROCESS APPLICATION STATUS (Accept / Reject)
    // ─────────────────────────────────────────────
    @PostMapping("/applications/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<EnrollmentRequest> requestOpt = enrollmentRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();
            String status    = payload.get("status");
            String remarks   = payload.get("remarks");
            String docStatus = payload.get("docStatus");

            request.setEoStatus(status);
            if (remarks   != null) request.setRemarks(remarks);
            if (docStatus != null) request.setDocStatus(docStatus);
            enrollmentRequestRepository.save(request);

            Optional<Student> studentOpt = studentRepository.findById(request.getStudentId());
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                student.setEnrollmentStatus("APPROVED".equalsIgnoreCase(status) ? "APPROVED_EO" : "REJECTED_EO");
                studentRepository.save(student);
            }

            logRepository.save(new SystemLog("Enrollment Officer processed Request ID " + id + " → " + status, "INFO"));
            return ResponseEntity.ok(request);
        }
        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────
    // ASSIGN CLASS / BATCH DETAILS
    // ─────────────────────────────────────────────
    @PostMapping("/applications/{id}/details")
    public ResponseEntity<?> assignDetails(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<EnrollmentRequest> requestOpt = enrollmentRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();
            request.setAdmissionNumber(payload.get("admissionNumber"));
            request.setClassName(payload.get("className"));
            request.setSection(payload.get("section"));
            if (payload.containsKey("batch"))  request.setBatch(payload.get("batch"));
            if (payload.containsKey("intake")) request.setIntake(payload.get("intake"));
            enrollmentRequestRepository.save(request);

            Optional<Student> studentOpt = studentRepository.findById(request.getStudentId());
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                student.setAdmissionNumber(payload.get("admissionNumber"));
                student.setClassName(payload.get("className"));
                student.setSection(payload.get("section"));
                studentRepository.save(student);
            }
            return ResponseEntity.ok(request);
        }
        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────
    // DELETE PAST STUDENT (Archive to deleted_students)
    // ─────────────────────────────────────────────
    @RequestMapping(value = {"/applications/{id}", "/applications/{id}/delete"}, method = {RequestMethod.DELETE, RequestMethod.POST})
    @Transactional
    public ResponseEntity<?> deletePastStudent(@PathVariable("id") Long id, @RequestBody(required = false) Map<String, String> payload) {
        String reason = payload != null ? payload.get("reason") : "Deleted by Enrollment Officer";
        Optional<EnrollmentRequest> requestOpt = enrollmentRequestRepository.findById(id);

        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();

            String username = null;
            String email = null;
            Optional<Student> studentOpt = studentRepository.findById(request.getStudentId());
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                if (student.getUser() != null) {
                    username = student.getUser().getUsername();
                    email = student.getUser().getEmail();
                }
                student.setEnrollmentStatus("DELETED");
                studentRepository.save(student);
            }

            DeletedStudent deleted = new DeletedStudent(
                    request.getStudentId(),
                    request.getId(),
                    request.getStudentName(),
                    username,
                    email,
                    request.getCourseName(),
                    request.getAdmissionNumber(),
                    request.getClassName(),
                    request.getSection(),
                    request.getEoStatus(),
                    request.getStaffStatus(),
                    "Enrollment Officer",
                    reason
            );
            deletedStudentRepository.save(deleted);

            enrollmentRequestRepository.delete(request);

            logRepository.save(new SystemLog("Enrollment Officer deleted past student record: " + request.getStudentName() + " (ID: " + id + ")", "WARN"));
            return ResponseEntity.ok(Map.of("message", "Past student deleted successfully.", "deleted", deleted));
        }

        // Fallback: Check if ID is student ID directly
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            String studentName = student.getUser() != null ? student.getUser().getFullName() : "Student #" + id;
            String username = student.getUser() != null ? student.getUser().getUsername() : null;
            String email = student.getUser() != null ? student.getUser().getEmail() : null;

            student.setEnrollmentStatus("DELETED");
            studentRepository.save(student);

            String courseName = "—";
            if (student.getCourseId() != null) {
                courseName = courseRepository.findById(student.getCourseId())
                        .map(Course::getName)
                        .orElse("—");
            }

            DeletedStudent deleted = new DeletedStudent(
                    student.getId(),
                    null,
                    studentName,
                    username,
                    email,
                    courseName,
                    student.getAdmissionNumber(),
                    student.getClassName(),
                    student.getSection(),
                    student.getEnrollmentStatus(),
                    "PENDING",
                    "Enrollment Officer",
                    reason
            );
            deletedStudentRepository.save(deleted);

            List<EnrollmentRequest> reqs = enrollmentRequestRepository.findByStudentId(student.getId());
            if (!reqs.isEmpty()) {
                enrollmentRequestRepository.deleteAll(reqs);
            }

            logRepository.save(new SystemLog("Enrollment Officer deleted student record: " + studentName + " (Student ID: " + id + ")", "WARN"));
            return ResponseEntity.ok(Map.of("message", "Student deleted successfully.", "deleted", deleted));
        }

        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────
    // VIEW DELETED STUDENTS ARCHIVE
    // ─────────────────────────────────────────────
    @GetMapping("/deleted-students")
    public ResponseEntity<?> getDeletedStudents() {
        return ResponseEntity.ok(deletedStudentRepository.findAllByOrderByDeletedAtDesc());
    }

    private String clean(Object val) {
        if (val == null) return null;
        String s = val.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
