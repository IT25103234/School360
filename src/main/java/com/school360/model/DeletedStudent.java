package com.school360.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_students")
public class DeletedStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long originalStudentId;
    private Long originalRequestId;

    private String studentName;
    private String username;
    private String email;
    private String courseName;
    private String admissionNumber;
    private String className;
    private String section;
    private String eoStatus;
    private String staffStatus;
    private String deletedBy;
    private LocalDateTime deletedAt;
    private String reason;

    public DeletedStudent() {
        this.deletedAt = LocalDateTime.now();
    }

    public DeletedStudent(Long originalStudentId, Long originalRequestId, String studentName, String username, String email,
                          String courseName, String admissionNumber, String className, String section,
                          String eoStatus, String staffStatus, String deletedBy, String reason) {
        this.originalStudentId = originalStudentId;
        this.originalRequestId = originalRequestId;
        this.studentName = studentName;
        this.username = username;
        this.email = email;
        this.courseName = courseName;
        this.admissionNumber = admissionNumber;
        this.className = className;
        this.section = section;
        this.eoStatus = eoStatus;
        this.staffStatus = staffStatus;
        this.deletedBy = deletedBy != null ? deletedBy : "Enrollment Officer";
        this.deletedAt = LocalDateTime.now();
        this.reason = reason;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOriginalStudentId() { return originalStudentId; }
    public void setOriginalStudentId(Long originalStudentId) { this.originalStudentId = originalStudentId; }

    public Long getOriginalRequestId() { return originalRequestId; }
    public void setOriginalRequestId(Long originalRequestId) { this.originalRequestId = originalRequestId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getEoStatus() { return eoStatus; }
    public void setEoStatus(String eoStatus) { this.eoStatus = eoStatus; }

    public String getStaffStatus() { return staffStatus; }
    public void setStaffStatus(String staffStatus) { this.staffStatus = staffStatus; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
