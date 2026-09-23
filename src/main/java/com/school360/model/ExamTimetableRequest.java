package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_timetable_requests")
public class ExamTimetableRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timetableId;
    private Long teacherId;
    private String teacherName;
    private String examTitle;
    private String subject;
    
    @Column(name = "existing_date")
    private String currentDate;

    private String proposedDate;
    private String proposedStartTime;
    private String proposedEndTime;
    private String proposedVenue;
    
    @Column(columnDefinition = "TEXT")
    private String reason;

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    private String staffRemarks;
    private String requestDate;

    public ExamTimetableRequest() {}

    public ExamTimetableRequest(Long timetableId, Long teacherId, String teacherName, String examTitle, String subject, String currentDate, String proposedDate, String proposedStartTime, String proposedEndTime, String proposedVenue, String reason, String status, String staffRemarks, String requestDate) {
        this.timetableId = timetableId;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.examTitle = examTitle;
        this.subject = subject;
        this.currentDate = currentDate;
        this.proposedDate = proposedDate;
        this.proposedStartTime = proposedStartTime;
        this.proposedEndTime = proposedEndTime;
        this.proposedVenue = proposedVenue;
        this.reason = reason;
        this.status = status;
        this.staffRemarks = staffRemarks;
        this.requestDate = requestDate;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTimetableId() { return timetableId; }
    public void setTimetableId(Long timetableId) { this.timetableId = timetableId; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCurrentDate() { return currentDate; }
    public void setCurrentDate(String currentDate) { this.currentDate = currentDate; }

    public String getProposedDate() { return proposedDate; }
    public void setProposedDate(String proposedDate) { this.proposedDate = proposedDate; }

    public String getProposedStartTime() { return proposedStartTime; }
    public void setProposedStartTime(String proposedStartTime) { this.proposedStartTime = proposedStartTime; }

    public String getProposedEndTime() { return proposedEndTime; }
    public void setProposedEndTime(String proposedEndTime) { this.proposedEndTime = proposedEndTime; }

    public String getProposedVenue() { return proposedVenue; }
    public void setProposedVenue(String proposedVenue) { this.proposedVenue = proposedVenue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStaffRemarks() { return staffRemarks; }
    public void setStaffRemarks(String staffRemarks) { this.staffRemarks = staffRemarks; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }
}
