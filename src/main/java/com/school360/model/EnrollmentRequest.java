package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollment_requests")
public class EnrollmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private String studentName;
    private String courseName;

    private String eoStatus;    // PENDING, APPROVED, REJECTED
    private String staffStatus; // PENDING, APPROVED, REJECTED
    private String remarks;

    private String admissionNumber;
    private String className;
    private String section;

    // New fields
    private String batch;      // e.g. "2026"
    private String intake;     // e.g. "September 2026"
    private String docStatus;  // PENDING, VERIFIED, REJECTED

    @Column(length = 4000)
    private String uploadedDocs; // JSON/String map of document key -> file name

    @Column(length = 4000)
    private String docStates;    // JSON/String map of document key -> verification status

    public EnrollmentRequest() {}

    public EnrollmentRequest(Long studentId, Long courseId, String studentName, String courseName, String eoStatus, String staffStatus) {
        this.studentId   = studentId;
        this.courseId    = courseId;
        this.studentName = studentName;
        this.courseName  = courseName;
        this.eoStatus    = eoStatus;
        this.staffStatus = staffStatus;
        this.docStatus   = "PENDING";
    }

    // ─── Getters & Setters ───────────────────────
    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }

    public Long getStudentId()           { return studentId; }
    public void setStudentId(Long v)     { this.studentId = v; }

    public Long getCourseId()            { return courseId; }
    public void setCourseId(Long v)      { this.courseId = v; }

    public String getStudentName()       { return studentName; }
    public void setStudentName(String v) { this.studentName = v; }

    public String getCourseName()        { return courseName; }
    public void setCourseName(String v)  { this.courseName = v; }

    public String getEoStatus()          { return eoStatus; }
    public void setEoStatus(String v)    { this.eoStatus = v; }

    public String getStaffStatus()       { return staffStatus; }
    public void setStaffStatus(String v) { this.staffStatus = v; }

    public String getRemarks()           { return remarks; }
    public void setRemarks(String v)     { this.remarks = v; }

    public String getAdmissionNumber()       { return admissionNumber; }
    public void setAdmissionNumber(String v) { this.admissionNumber = v; }

    public String getClassName()         { return className; }
    public void setClassName(String v)   { this.className = v; }

    public String getSection()           { return section; }
    public void setSection(String v)     { this.section = v; }

    public String getBatch()             { return batch; }
    public void setBatch(String v)       { this.batch = v; }

    public String getIntake()            { return intake; }
    public void setIntake(String v)      { this.intake = v; }

    public String getDocStatus()         { return docStatus; }
    public void setDocStatus(String v)   { this.docStatus = v; }

    public String getUploadedDocs()       { return uploadedDocs; }
    public void setUploadedDocs(String v) { this.uploadedDocs = v; }

    public String getDocStates()         { return docStates; }
    public void setDocStates(String v)   { this.docStates = v; }
}
