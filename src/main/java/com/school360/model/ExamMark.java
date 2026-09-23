package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exam_marks")
public class ExamMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String examTitle;

    private Long studentId;
    private String studentName;
    private String admissionNumber;
    private Long courseId;
    private String courseName;
    private String subject;
    private Double marksObtained;
    private Double totalMarks;
    private String grade;
    private String remarks;
    private boolean published = false;

    public ExamMark() {}

    public ExamMark(String examTitle, Long studentId, String studentName, String admissionNumber, Long courseId, String courseName, String subject, Double marksObtained, Double totalMarks, String grade, String remarks, boolean published) {
        this.examTitle = examTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.admissionNumber = admissionNumber;
        this.courseId = courseId;
        this.courseName = courseName;
        this.subject = subject;
        this.marksObtained = marksObtained;
        this.totalMarks = totalMarks;
        this.grade = grade;
        this.remarks = remarks;
        this.published = published;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
