package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String admissionNumber;
    private Long courseId;
    private String className;
    private String section;
    private String enrollmentStatus; // PENDING_EO, REJECTED_EO, APPROVED_EO, APPROVED_STAFF

    public Student() {}

    public Student(User user, String admissionNumber, Long courseId, String className, String section, String enrollmentStatus) {
        this.user = user;
        this.admissionNumber = admissionNumber;
        this.courseId = courseId;
        this.className = className;
        this.section = section;
        this.enrollmentStatus = enrollmentStatus;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAdmissionNumber() { return admissionNumber; }
    public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(String enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }
}
