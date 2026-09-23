package com.school360.controller;

import com.school360.model.*;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*")
public class StaffController {

    @Autowired
    private EnrollmentRequestRepository enrollmentRequestRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ExamTimetableRepository examTimetableRepository;

    @Autowired
    private ExamMarkRepository examMarkRepository;

    @Autowired
    private ExamTimetableRequestRepository examTimetableRequestRepository;

    // View applications accepted by EO (ready for receptionist approval)
    @GetMapping("/approvals")
    public ResponseEntity<?> getPendingApprovals() {
        return ResponseEntity.ok(enrollmentRequestRepository.findByEoStatus("APPROVED"));
    }

    // Final Approval by Administrative Staff (Receptionist)
    @PostMapping("/approvals/{id}/approve")
    public ResponseEntity<?> approveEnrollment(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<EnrollmentRequest> requestOpt = enrollmentRequestRepository.findById(id);
        if (requestOpt.isPresent()) {
            EnrollmentRequest request = requestOpt.get();
            String status = payload.getOrDefault("status", "APPROVED"); // APPROVED or REJECTED

            request.setStaffStatus(status);
            enrollmentRequestRepository.save(request);

            // Update student status to APPROVED_STAFF (fully active student in course)
            Optional<Student> studentOpt = studentRepository.findById(request.getStudentId());
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                if ("APPROVED".equalsIgnoreCase(status)) {
                    student.setEnrollmentStatus("APPROVED_STAFF");
                } else {
                    student.setEnrollmentStatus("REJECTED_STAFF");
                }
                studentRepository.save(student);
            }

            logRepository.save(new SystemLog("Administrative Officer approved student enrollment for Request ID " + id, "INFO"));
            return ResponseEntity.ok(request);
        }
        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────────────────
    // 1. EXAM TIMETABLES & RELEASES (STAFF)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/exam-timetables")
    public ResponseEntity<?> getAllExamTimetables() {
        return ResponseEntity.ok(examTimetableRepository.findAll());
    }

    @PostMapping("/exam-timetables")
    public ResponseEntity<?> createExamTimetable(@RequestBody ExamTimetable timetable) {
        if (timetable.getExamTitle() == null || timetable.getExamTitle().isBlank()) {
            return ResponseEntity.badRequest().body("Exam title is required.");
        }
        ExamTimetable saved = examTimetableRepository.save(timetable);
        logRepository.save(new SystemLog("Staff created exam timetable entry for: " + saved.getExamTitle() + " - " + saved.getSubject(), "INFO"));
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/exam-timetables/{id}")
    public ResponseEntity<?> updateExamTimetable(@PathVariable("id") Long id, @RequestBody ExamTimetable updated) {
        Optional<ExamTimetable> ttOpt = examTimetableRepository.findById(id);
        if (ttOpt.isPresent()) {
            ExamTimetable tt = ttOpt.get();
            tt.setExamTitle(updated.getExamTitle());
            tt.setCourseId(updated.getCourseId());
            tt.setCourseName(updated.getCourseName());
            tt.setSubject(updated.getSubject());
            tt.setExamDate(updated.getExamDate());
            tt.setStartTime(updated.getStartTime());
            tt.setEndTime(updated.getEndTime());
            tt.setVenue(updated.getVenue());
            if (updated.getStatus() != null) tt.setStatus(updated.getStatus());

            ExamTimetable saved = examTimetableRepository.save(tt);
            logRepository.save(new SystemLog("Staff updated exam timetable ID " + id, "INFO"));
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/exam-timetables/{id}/release")
    public ResponseEntity<?> toggleReleaseTimetable(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> payload) {
        Optional<ExamTimetable> ttOpt = examTimetableRepository.findById(id);
        if (ttOpt.isPresent()) {
            ExamTimetable tt = ttOpt.get();
            boolean published = payload.getOrDefault("published", !tt.isPublished());
            tt.setPublished(published);
            examTimetableRepository.save(tt);

            logRepository.save(new SystemLog("Staff " + (published ? "released" : "unpublished") + " exam timetable: " + tt.getExamTitle(), "INFO"));
            return ResponseEntity.ok(tt);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/exam-timetables/{id}")
    public ResponseEntity<?> deleteExamTimetable(@PathVariable("id") Long id) {
        Optional<ExamTimetable> ttOpt = examTimetableRepository.findById(id);
        if (ttOpt.isPresent()) {
            examTimetableRepository.delete(ttOpt.get());
            logRepository.save(new SystemLog("Staff deleted exam timetable ID " + id, "INFO"));
            return ResponseEntity.ok(Map.of("message", "Exam timetable deleted successfully."));
        }
        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────────────────
    // 2. EXAM MARKS LISTS & RESULTS RELEASING (STAFF)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/exam-marks")
    public ResponseEntity<?> getAllExamMarks() {
        return ResponseEntity.ok(examMarkRepository.findAll());
    }

    @PostMapping("/exam-marks")
    public ResponseEntity<?> createExamMark(@RequestBody ExamMark mark) {
        if (mark.getExamTitle() == null || mark.getStudentId() == null) {
            return ResponseEntity.badRequest().body("Exam title and Student ID are required.");
        }
        // Auto-calculate grade if missing
        if (mark.getGrade() == null || mark.getGrade().isBlank()) {
            mark.setGrade(calculateGrade(mark.getMarksObtained(), mark.getTotalMarks()));
        }
        ExamMark saved = examMarkRepository.save(mark);
        logRepository.save(new SystemLog("Staff added exam mark record for student ID " + mark.getStudentId(), "INFO"));
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/exam-marks/{id}")
    public ResponseEntity<?> updateExamMark(@PathVariable("id") Long id, @RequestBody ExamMark updated) {
        Optional<ExamMark> markOpt = examMarkRepository.findById(id);
        if (markOpt.isPresent()) {
            ExamMark mark = markOpt.get();
            mark.setExamTitle(updated.getExamTitle());
            mark.setStudentName(updated.getStudentName());
            mark.setAdmissionNumber(updated.getAdmissionNumber());
            mark.setSubject(updated.getSubject());
            mark.setMarksObtained(updated.getMarksObtained());
            mark.setTotalMarks(updated.getTotalMarks());
            mark.setGrade(updated.getGrade() != null && !updated.getGrade().isBlank() ? 
                updated.getGrade() : calculateGrade(updated.getMarksObtained(), updated.getTotalMarks()));
            mark.setRemarks(updated.getRemarks());
            if (updated.isPublished() != mark.isPublished()) {
                mark.setPublished(updated.isPublished());
            }

            ExamMark saved = examMarkRepository.save(mark);
            logRepository.save(new SystemLog("Staff updated exam mark record ID " + id, "INFO"));
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/exam-marks/{id}/release")
    public ResponseEntity<?> toggleReleaseMark(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> payload) {
        Optional<ExamMark> markOpt = examMarkRepository.findById(id);
        if (markOpt.isPresent()) {
            ExamMark mark = markOpt.get();
            boolean published = payload.getOrDefault("published", !mark.isPublished());
            mark.setPublished(published);
            examMarkRepository.save(mark);

            logRepository.save(new SystemLog("Staff " + (published ? "released" : "unpublished") + " exam result for student " + mark.getStudentName(), "INFO"));
            return ResponseEntity.ok(mark);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/exam-marks/batch-release")
    public ResponseEntity<?> batchReleaseMarks(@RequestBody Map<String, Object> payload) {
        String examTitle = (String) payload.get("examTitle");
        Boolean releaseAll = (Boolean) payload.getOrDefault("release", true);

        List<ExamMark> marks;
        if (examTitle != null && !examTitle.isBlank() && !"ALL".equalsIgnoreCase(examTitle)) {
            marks = examMarkRepository.findByExamTitle(examTitle);
        } else {
            marks = examMarkRepository.findAll();
        }

        for (ExamMark m : marks) {
            m.setPublished(releaseAll != null ? releaseAll : true);
        }
        examMarkRepository.saveAll(marks);

        logRepository.save(new SystemLog("Staff batch " + (releaseAll ? "released" : "unpublished") + " exam results (" + marks.size() + " records)", "INFO"));
        return ResponseEntity.ok(Map.of("message", "Batch status updated successfully", "count", marks.size()));
    }

    @DeleteMapping("/exam-marks/{id}")
    public ResponseEntity<?> deleteExamMark(@PathVariable("id") Long id) {
        Optional<ExamMark> markOpt = examMarkRepository.findById(id);
        if (markOpt.isPresent()) {
            examMarkRepository.delete(markOpt.get());
            logRepository.save(new SystemLog("Staff deleted exam mark entry ID " + id, "INFO"));
            return ResponseEntity.ok(Map.of("message", "Exam mark deleted successfully."));
        }
        return ResponseEntity.notFound().build();
    }

    // ─────────────────────────────────────────────────────────
    // 3. RECEIVE & PROCESS TEACHER TIMETABLE CHANGE REQUESTS (STAFF)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/timetable-change-requests")
    public ResponseEntity<?> getAllTimetableChangeRequests() {
        return ResponseEntity.ok(examTimetableRequestRepository.findAll());
    }

    @PostMapping("/timetable-change-requests/{id}/process")
    public ResponseEntity<?> processTimetableChangeRequest(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        String status = payload.getOrDefault("status", "APPROVED"); // APPROVED or REJECTED
        String staffRemarks = payload.get("staffRemarks");

        Optional<ExamTimetableRequest> reqOpt = examTimetableRequestRepository.findById(id);
        if (reqOpt.isPresent()) {
            ExamTimetableRequest req = reqOpt.get();
            req.setStatus(status);
            if (staffRemarks != null) req.setStaffRemarks(staffRemarks);
            examTimetableRequestRepository.save(req);

            // If APPROVED, auto-update the target ExamTimetable record!
            if ("APPROVED".equalsIgnoreCase(status) && req.getTimetableId() != null) {
                Optional<ExamTimetable> ttOpt = examTimetableRepository.findById(req.getTimetableId());
                if (ttOpt.isPresent()) {
                    ExamTimetable tt = ttOpt.get();
                    if (req.getProposedDate() != null && !req.getProposedDate().isBlank()) tt.setExamDate(req.getProposedDate());
                    if (req.getProposedStartTime() != null && !req.getProposedStartTime().isBlank()) tt.setStartTime(req.getProposedStartTime());
                    if (req.getProposedEndTime() != null && !req.getProposedEndTime().isBlank()) tt.setEndTime(req.getProposedEndTime());
                    if (req.getProposedVenue() != null && !req.getProposedVenue().isBlank()) tt.setVenue(req.getProposedVenue());
                    tt.setStatus("UPDATED");
                    examTimetableRepository.save(tt);
                }
            }

            logRepository.save(new SystemLog("Staff " + status.toLowerCase() + " exam timetable change request ID " + id, "INFO"));
            return ResponseEntity.ok(req);
        }
        return ResponseEntity.notFound().build();
    }

    // Helper: calculate grade
    private String calculateGrade(Double marks, Double total) {
        if (marks == null || total == null || total <= 0) return "N/A";
        double percentage = (marks / total) * 100.0;
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
}

