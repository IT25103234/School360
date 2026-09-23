package com.school360.controller;

import com.school360.model.*;
import com.school360.model.Module;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamTimetableRepository examTimetableRepository;

    @Autowired
    private ExamTimetableRequestRepository examTimetableRequestRepository;

    @Autowired
    private BatchRepository batchRepository;


    // View Modules created by Teacher
    @GetMapping("/{teacherId}/modules")
    public ResponseEntity<?> getModules(@PathVariable("teacherId") Long teacherId) {
        return ResponseEntity.ok(moduleRepository.findByTeacherId(teacherId));
    }

    // Create Module
    @PostMapping("/modules")
    public ResponseEntity<?> createModule(@RequestBody Module module) {
        moduleRepository.save(module);
        logRepository.save(new SystemLog("Teacher created new module: " + module.getName() + " (" + module.getCode() + ")", "INFO"));
        return ResponseEntity.ok(module);
    }

    // Edit Module
    @RequestMapping(value = {"/modules/{id}", "/modules/{id}/edit"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<?> editModule(@PathVariable("id") Long id, @RequestBody Module updated) {
        Optional<Module> modOpt = moduleRepository.findById(id);
        if (modOpt.isPresent()) {
            Module module = modOpt.get();
            if (updated.getName() != null && !updated.getName().isBlank()) module.setName(updated.getName().trim());
            if (updated.getCode() != null && !updated.getCode().isBlank()) module.setCode(updated.getCode().trim());
            if (updated.getCourseId() != null) module.setCourseId(updated.getCourseId());
            moduleRepository.save(module);
            logRepository.save(new SystemLog("Teacher updated module: " + module.getName(), "INFO"));
            return ResponseEntity.ok(module);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete Module
    @RequestMapping(value = {"/modules/{id}", "/modules/{id}/delete"}, method = {RequestMethod.DELETE, RequestMethod.POST})
    public ResponseEntity<?> deleteModule(@PathVariable("id") Long id) {
        Optional<Module> modOpt = moduleRepository.findById(id);
        if (modOpt.isPresent()) {
            moduleRepository.delete(modOpt.get());
            logRepository.save(new SystemLog("Teacher deleted module: ID " + id, "INFO"));
            return ResponseEntity.ok(Map.of("message", "Module deleted successfully.", "id", id));
        }
        return ResponseEntity.notFound().build();
    }

    // Create Timetable Entry
    @PostMapping("/timetable")
    public ResponseEntity<?> createSchedule(@RequestBody Schedule schedule) {
        scheduleRepository.save(schedule);
        logRepository.save(new SystemLog("Teacher created schedule for module: " + schedule.getModuleName(), "INFO"));
        return ResponseEntity.ok(schedule);
    }

    // Edit Timetable Entry
    @PutMapping("/timetable/{id}")
    public ResponseEntity<?> editSchedule(@PathVariable("id") Long id, @RequestBody Schedule updated) {
        Optional<Schedule> schedOpt = scheduleRepository.findById(id);
        if (schedOpt.isPresent()) {
            Schedule schedule = schedOpt.get();
            schedule.setDayOfWeek(updated.getDayOfWeek());
            schedule.setStartTime(updated.getStartTime());
            schedule.setEndTime(updated.getEndTime());
            schedule.setRoom(updated.getRoom());
            scheduleRepository.save(schedule);
            logRepository.save(new SystemLog("Timetable schedule updated for: " + schedule.getModuleName(), "INFO"));
            return ResponseEntity.ok(schedule);
        }
        return ResponseEntity.notFound().build();
    }

    // Update Lecture Status (Begins late, Cancelled, etc)
    @PostMapping("/timetable/{id}/status")
    public ResponseEntity<?> updateLectureStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<Schedule> schedOpt = scheduleRepository.findById(id);
        if (schedOpt.isPresent()) {
            Schedule schedule = schedOpt.get();
            String status = payload.get("status");
            String statusMessage = payload.get("statusMessage");

            schedule.setStatus(status);
            schedule.setStatusMessage(statusMessage);
            scheduleRepository.save(schedule);

            logRepository.save(new SystemLog("Lecture status update: " + schedule.getModuleName() + " is now " + status, "WARNING"));
            return ResponseEntity.ok(schedule);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete Timetable Entry
    @DeleteMapping("/timetable/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable("id") Long id) {
        Optional<Schedule> schedOpt = scheduleRepository.findById(id);
        if (schedOpt.isPresent()) {
            scheduleRepository.delete(schedOpt.get());
            logRepository.save(new SystemLog("Schedule cancelled and deleted: ID " + id, "INFO"));
            return ResponseEntity.ok("Schedule deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }

    // Get Timetables for Teacher
    @GetMapping("/{teacherId}/timetable")
    public ResponseEntity<?> getTeacherSchedules(@PathVariable("teacherId") Long teacherId) {
        return ResponseEntity.ok(scheduleRepository.findByTeacherId(teacherId));
    }

    // View approved students module-wise
    @GetMapping("/{teacherId}/enrolled-students")
    public ResponseEntity<?> getEnrolledStudents(@PathVariable("teacherId") Long teacherId) {
        List<Module> modules = moduleRepository.findByTeacherId(teacherId);
        Set<Long> courseIds = new HashSet<>();
        for (Module m : modules) {
            courseIds.add(m.getCourseId());
        }

        List<Student> allEnrolled = new ArrayList<>();
        for (Long cId : courseIds) {
            List<Student> students = studentRepository.findByCourseId(cId);
            for (Student s : students) {
                if ("APPROVED_STAFF".equals(s.getEnrollmentStatus())) {
                    allEnrolled.add(s);
                }
            }
        }
        return ResponseEntity.ok(allEnrolled);
    }

    // Post announcements
    @PostMapping("/announcements")
    public ResponseEntity<?> createAnnouncement(@RequestBody Announcement announcement) {
        announcementRepository.save(announcement);
        logRepository.save(new SystemLog("Teacher posted announcement: " + announcement.getTitle(), "INFO"));
        return ResponseEntity.ok(announcement);
    }

    // Record Attendance
    @PostMapping("/attendance")
    public ResponseEntity<?> recordAttendance(@RequestBody Attendance record) {
        Optional<Attendance> existOpt = attendanceRepository.findByStudentIdAndScheduleIdAndDate(
            record.getStudentId(),
            record.getScheduleId(),
            record.getDate()
        );

        Attendance finalRecord;
        if (existOpt.isPresent()) {
            Attendance existing = existOpt.get();
            existing.setStatus(record.getStatus());
            finalRecord = attendanceRepository.save(existing);
        } else {
            finalRecord = attendanceRepository.save(record);
        }

        logRepository.save(new SystemLog("Attendance logged for student ID " + record.getStudentId() + ": " + record.getStatus(), "INFO"));
        return ResponseEntity.ok(finalRecord);
    }

    // View Attendance records
    @GetMapping("/attendance/schedule/{schedId}")
    public ResponseEntity<?> getAttendanceForSchedule(@PathVariable("schedId") Long schedId) {
        return ResponseEntity.ok(attendanceRepository.findByScheduleId(schedId));
    }

    // ── COURSES (TEACHER) ──
    @GetMapping("/courses")
    public ResponseEntity<?> getCourses() {
        return ResponseEntity.ok(courseRepository.findAll());
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        if (course.getName() == null || course.getName().isBlank() || course.getCode() == null || course.getCode().isBlank()) {
            return ResponseEntity.badRequest().body("Course name and code are required.");
        }
        if (courseRepository.findByCode(course.getCode()).isPresent()) {
            return ResponseEntity.badRequest().body("Course code already exists.");
        }
        Course saved = courseRepository.save(course);
        logRepository.save(new SystemLog("Teacher created new course: " + saved.getName() + " (" + saved.getCode() + ")", "INFO"));
        return ResponseEntity.ok(saved);
    }

    // Edit / Update Course (Teacher)
    @RequestMapping(value = {"/courses/{id}", "/courses/{id}/edit"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<?> updateCourse(@PathVariable("id") Long id, @RequestBody Course updated) {
        if (updated.getName() == null || updated.getName().isBlank() || updated.getCode() == null || updated.getCode().isBlank()) {
            return ResponseEntity.badRequest().body("Course name and code are required.");
        }
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setName(updated.getName().trim());
            course.setCode(updated.getCode().trim());
            if (updated.getDescription() != null) {
                course.setDescription(updated.getDescription().trim());
            }
            courseRepository.save(course);
            logRepository.save(new SystemLog("Teacher updated course details for: " + course.getName(), "INFO"));
            return ResponseEntity.ok(course);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete Course (Teacher)
    @RequestMapping(value = {"/courses/{id}", "/courses/{id}/delete"}, method = {RequestMethod.DELETE, RequestMethod.POST})
    public ResponseEntity<?> deleteCourse(@PathVariable("id") Long id) {
        Optional<Course> courseOpt = courseRepository.findById(id);
        if (courseOpt.isPresent()) {
            courseRepository.delete(courseOpt.get());
            logRepository.save(new SystemLog("Teacher deleted course ID: " + id, "INFO"));
            return ResponseEntity.ok(Map.of("message", "Course deleted successfully.", "id", id));
        }
        return ResponseEntity.notFound().build();
    }

    // Assign / Add new course for student
    @PostMapping("/assign-course")
    public ResponseEntity<?> assignStudentCourse(@RequestBody Map<String, Long> payload) {
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

            student.setCourseId(course.getId());
            student.setEnrollmentStatus("APPROVED_STAFF");
            studentRepository.save(student);

            logRepository.save(new SystemLog("Teacher assigned course " + course.getName() + " to student ID " + studentId, "INFO"));
            return ResponseEntity.ok(Map.of("message", "Course assigned successfully to student", "studentId", studentId, "courseId", courseId));
        }
        return ResponseEntity.notFound().build();
    }

    // ── EXAM TIMETABLE CHANGE REQUESTS (TEACHER SIDE) ──
    @GetMapping("/exam-timetables")
    public ResponseEntity<?> getExamTimetables() {
        return ResponseEntity.ok(examTimetableRepository.findAll());
    }

    @PostMapping("/timetable-change-request")
    public ResponseEntity<?> createExamTimetableRequest(@RequestBody ExamTimetableRequest request) {
        if (request.getTimetableId() == null || request.getProposedDate() == null || request.getProposedDate().isBlank()) {
            return ResponseEntity.badRequest().body("Timetable ID and proposed date are required.");
        }

        Optional<ExamTimetable> ttOpt = examTimetableRepository.findById(request.getTimetableId());
        if (ttOpt.isPresent()) {
            ExamTimetable tt = ttOpt.get();
            request.setExamTitle(tt.getExamTitle());
            request.setSubject(tt.getSubject());
            request.setCurrentDate(tt.getExamDate());
            request.setStatus("PENDING");
            request.setRequestDate(java.time.LocalDate.now().toString());

            ExamTimetableRequest saved = examTimetableRequestRepository.save(request);
            logRepository.save(new SystemLog("Teacher " + request.getTeacherName() + " submitted exam timetable change request for ID " + tt.getId(), "INFO"));
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{teacherId}/timetable-change-requests")
    public ResponseEntity<?> getTeacherTimetableRequests(@PathVariable("teacherId") Long teacherId) {
        return ResponseEntity.ok(examTimetableRequestRepository.findByTeacherId(teacherId));
    }

    // ── BATCH DETAILS (DATABASE SAVED) ──
    @GetMapping("/batches")
    public ResponseEntity<?> getBatches() {
        List<Batch> list = batchRepository.findAll();
        if (list.isEmpty()) {
            Batch init = new Batch("Year 1", "2026", "{\"sem1\":{\"modules\":[]},\"sem2\":{\"modules\":[]}}");
            list = List.of(batchRepository.save(init));
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/batches")
    public ResponseEntity<?> saveBatch(@RequestBody Batch batch) {
        if (batch.getLabel() == null || batch.getLabel().isBlank()) {
            return ResponseEntity.badRequest().body("Batch label is required.");
        }
        Batch saved = batchRepository.save(batch);
        logRepository.save(new SystemLog("Saved batch in database: " + saved.getLabel() + " (ID: " + saved.getId() + ")", "INFO"));
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/batches/{id}")
    public ResponseEntity<?> deleteBatch(@PathVariable("id") Long id) {
        if (batchRepository.existsById(id)) {
            batchRepository.deleteById(id);
            logRepository.save(new SystemLog("Deleted batch from database (ID: " + id + ")", "INFO"));
            return ResponseEntity.ok(Map.of("message", "Batch deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}

