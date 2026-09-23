package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;
    private Long scheduleId;
    private String status; // PRESENT, ABSENT
    private String date; // yyyy-MM-dd

    public Attendance() {}

    public Attendance(Long studentId, String studentName, Long scheduleId, String status, String date) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.scheduleId = scheduleId;
        this.status = status;
        this.date = date;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
