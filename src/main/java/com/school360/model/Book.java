package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private String category;
    private String edition;
    private Integer copies;
    private String status; // AVAILABLE, UNAVAILABLE
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Book() {}

    public Book(String title, String author, String isbn, String category, String edition, Integer copies, String status, String location, String description) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.category = category;
        this.edition = edition;
        this.copies = copies;
        this.status = status;
        this.location = location;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public Integer getCopies() { return copies; }
    public void setCopies(Integer copies) { this.copies = copies; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
