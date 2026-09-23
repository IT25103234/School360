package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_timetables")
public class ExamTimetable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String examTitle;

    private Long courseId;
    private String courseName;
    private String subject;
    private String examDate;
    private String startTime;
    private String endTime;
    private String venue;
    private boolean published = false;
    private String status = "SCHEDULED"; // SCHEDULED, UPDATED, CANCELLED

    public ExamTimetable() {}

    public ExamTimetable(String examTitle, Long courseId, String courseName, String subject, String examDate, String startTime, String endTime, String venue, boolean published, String status) {
        this.examTitle = examTitle;
        this.courseId = courseId;
        this.courseName = courseName;
        this.subject = subject;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.venue = venue;
        this.published = published;
        this.status = status;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
