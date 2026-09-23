package com.school360.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String studentName;

    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    private String status; // OPEN, PENDING, IN_PROGRESS, RESOLVED, CLOSED
    private String priority; // LOW, MEDIUM, HIGH
    private String escalationStatus; // ESCALATED, NONE
    private String createdDate;

    public Ticket() {
        this.createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Ticket(Long studentId, String studentName, String title, String description, String status, String priority, String escalationStatus) {
        this();
        this.studentId = studentId;
        this.studentName = studentName;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.escalationStatus = escalationStatus;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getEscalationStatus() { return escalationStatus; }
    public void setEscalationStatus(String escalationStatus) { this.escalationStatus = escalationStatus; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
