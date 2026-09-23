package com.school360.controller;

import com.school360.model.*;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SystemLogRepository logRepository;

    // View All Users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Create User Account
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
        }
        if (user.getFullName() != null && !user.getFullName().matches("^[a-zA-Z\\s]+$")) {
            return ResponseEntity.badRequest().body("Name must contain letters and spaces only (no numbers or symbols).");
        }
        if (user.getContact() != null && !user.getContact().matches("^\\d{1,16}$")) {
            return ResponseEntity.badRequest().body("Phone number must contain numbers only and be at most 16 digits.");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }
        
        // Generate automatic QR code token for user
        user.setQrCodeToken("QR-" + user.getRole() + "-" + user.getUsername().toUpperCase());
        userRepository.save(user);

        // If user is a student, create student detail record as well
        if ("STUDENT".equals(user.getRole())) {
            Student student = new Student(user, null, null, null, null, null);
            studentRepository.save(student);
        }

        logRepository.save(new SystemLog("Administrator created new user account: " + user.getUsername(), "INFO"));
        return ResponseEntity.ok(user);
    }

    // Update User Role/Details
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User updated) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            if (updated.getFullName() != null && !updated.getFullName().matches("^[a-zA-Z\\s]+$")) {
                return ResponseEntity.badRequest().body("Name must contain letters and spaces only (no numbers or symbols).");
            }
            if (updated.getContact() != null && !updated.getContact().matches("^\\d{1,16}$")) {
                return ResponseEntity.badRequest().body("Phone number must contain numbers only and be at most 16 digits.");
            }
            User user = userOpt.get();
            user.setFullName(updated.getFullName());
            user.setEmail(updated.getEmail());
            user.setContact(updated.getContact());
            user.setRole(updated.getRole());
            userRepository.save(user);

            logRepository.save(new SystemLog("User details updated for: " + user.getUsername(), "INFO"));
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete User
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // If Student, clean detail record
            if ("STUDENT".equals(user.getRole())) {
                Optional<Student> studentOpt = studentRepository.findByUserId(id);
                studentOpt.ifPresent(student -> studentRepository.delete(student));
            }

            userRepository.delete(user);
            logRepository.save(new SystemLog("User deleted: " + user.getUsername(), "INFO"));
            return ResponseEntity.ok("User deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    // System Statistics (for charts)
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 1. Student Count
        long studentCount = studentRepository.count();
        stats.put("studentCount", studentCount);

        // 2. Attendance Percentage (overall average)
        List<Attendance> attendances = attendanceRepository.findAll();
        double attendancePercent = 100.0;
        if (!attendances.isEmpty()) {
            long presentCount = attendances.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus())).count();
            attendancePercent = (double) presentCount / attendances.size() * 100.0;
        }
        stats.put("attendancePercentage", Math.round(attendancePercent * 10.0) / 10.0);

        // 3. Ticket Status Count
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Long> ticketStatusBreakdown = new HashMap<>();
        ticketStatusBreakdown.put("OPEN", tickets.stream().filter(t -> "OPEN".equalsIgnoreCase(t.getStatus())).count());
        ticketStatusBreakdown.put("PENDING", tickets.stream().filter(t -> "PENDING".equalsIgnoreCase(t.getStatus())).count());
        ticketStatusBreakdown.put("IN_PROGRESS", tickets.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count());
        ticketStatusBreakdown.put("RESOLVED", tickets.stream().filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus())).count());
        ticketStatusBreakdown.put("CLOSED", tickets.stream().filter(t -> "CLOSED".equalsIgnoreCase(t.getStatus())).count());
        stats.put("ticketStatusBreakdown", ticketStatusBreakdown);

        // 4. Counts by Role
        Map<String, Long> roleCounts = new HashMap<>();
        roleCounts.put("STUDENT", 0L);
        roleCounts.put("TEACHER", 0L);
        roleCounts.put("STAFF", 0L);
        roleCounts.put("SUPPORT", 0L);
        roleCounts.put("ENROLLMENT", 0L);
        userRepository.findAll().forEach(u -> {
            if (u.getRole() != null) {
                String roleKey = u.getRole().toUpperCase();
                roleCounts.computeIfPresent(roleKey, (k, v) -> v + 1L);
            }
        });
        stats.put("roleCounts", roleCounts);

        return ResponseEntity.ok(stats);
    }

    // View Audit Logs
    @GetMapping("/logs")
    public ResponseEntity<?> getLogs() {
        return ResponseEntity.ok(logRepository.findAllByOrderByIdDesc());
    }

    // Create Course (CRUD)
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        if (course.getName() == null || course.getName().isBlank() || course.getCode() == null || course.getCode().isBlank()) {
            return ResponseEntity.badRequest().body("Course name and code are required.");
        }
        courseRepository.save(course);
        logRepository.save(new SystemLog("Created course: " + course.getName(), "INFO"));
        return ResponseEntity.ok(course);
    }

    // Update Course
    @RequestMapping(value = {"/courses/{id}", "/courses/{id}/edit"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<?> updateCourse(@PathVariable("id") Long id, @RequestBody Course updated) {
        if (updated.getName() == null || updated.getName().isBlank() || updated.getCode() == null || updated.getCode().isBlank()) {
            return ResponseEntity.badRequest().body("Course name and code are required.");
        }
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setName(updated.getName());
            course.setCode(updated.getCode());
            course.setDescription(updated.getDescription());
            courseRepository.save(course);
            logRepository.save(new SystemLog("Updated course details for: " + course.getName(), "INFO"));
            return ResponseEntity.ok(course);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete Course
    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable("id") Long id) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isPresent()) {
            courseRepository.delete(courseOpt.get());
            logRepository.save(new SystemLog("Deleted course ID: " + id, "INFO"));
            return ResponseEntity.ok("Course deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }
}
