package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_version_histories")
public class BookVersionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookTitle;
    private String previousEdition;
    private String newEdition;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String publishedBy;
    private String publishedDate;

    public BookVersionHistory() {}

    public BookVersionHistory(String bookTitle, String previousEdition, String newEdition, String notes, String publishedBy, String publishedDate) {
        this.bookTitle = bookTitle;
        this.previousEdition = previousEdition;
        this.newEdition = newEdition;
        this.notes = notes;
        this.publishedBy = publishedBy;
        this.publishedDate = publishedDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getPreviousEdition() { return previousEdition; }
    public void setPreviousEdition(String previousEdition) { this.previousEdition = previousEdition; }

    public String getNewEdition() { return newEdition; }
    public void setNewEdition(String newEdition) { this.newEdition = newEdition; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public String getPublishedDate() { return publishedDate; }
    public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }
}
