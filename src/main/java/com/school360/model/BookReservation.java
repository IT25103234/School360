package com.school360.model;

import jakarta.persistence.*;

@Entity
@Table(name = "book_reservations")
public class BookReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private String memberName;
    private Long bookId;
    private String bookTitle;
    private String reservedOn;
    private String expiry;
    private String status; // PENDING, APPROVED, CANCELLED

    public BookReservation() {}

    public BookReservation(Long memberId, String memberName, Long bookId, String bookTitle, String reservedOn, String expiry, String status) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.reservedOn = reservedOn;
        this.expiry = expiry;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getReservedOn() { return reservedOn; }
    public void setReservedOn(String reservedOn) { this.reservedOn = reservedOn; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
