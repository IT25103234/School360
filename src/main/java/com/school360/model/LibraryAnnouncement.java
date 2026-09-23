package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "library_announcements")
public class LibraryAnnouncement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String type; // General, Notice, Holiday, Event

    @Column(columnDefinition = "TEXT")
    private String content;

    private String postedBy;
    private String postedOn;

    public LibraryAnnouncement() {}

    public LibraryAnnouncement(String title, String type, String content, String postedBy, String postedOn) {
        this.title = title;
        this.type = type;
        this.content = content;
        this.postedBy = postedBy;
        this.postedOn = postedOn;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPostedBy() { return postedBy; }
    public void setPostedBy(String postedBy) { this.postedBy = postedBy; }

    public String getPostedOn() { return postedOn; }
    public void setPostedOn(String postedOn) { this.postedOn = postedOn; }
}
