package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_members")
public class BookMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String mid; // Member ID e.g. LIB-2024-001
    private String type; // Student, Teacher, Staff, Guest
    private String dept; // Class or Department
    private String email;
    private String phone;
    private String status; // ACTIVE, SUSPENDED, INACTIVE
    private String joined; // Date string yyyy-MM-dd

    public BookMember() {}

    public BookMember(String name, String mid, String type, String dept, String email, String phone, String status, String joined) {
        this.name = name;
        this.mid = mid;
        this.type = type;
        this.dept = dept;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.joined = joined;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMid() { return mid; }
    public void setMid(String mid) { this.mid = mid; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getJoined() { return joined; }
    public void setJoined(String joined) { this.joined = joined; }
}
