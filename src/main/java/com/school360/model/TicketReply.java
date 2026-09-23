package com.school360.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "ticket_replies")
public class TicketReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketId;
    private String senderName;
    private String senderRole; // STUDENT, SUPPORT
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    private String attachmentName; // e.g. screenshot.png
    
    @Column(columnDefinition = "LONGTEXT")
    private String attachmentData; // base64 representation of attachment for client-side storage
    private String createdDate;

    public TicketReply() {
        this.createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public TicketReply(Long ticketId, String senderName, String senderRole, String message, String attachmentName, String attachmentData) {
        this();
        this.ticketId = ticketId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.message = message;
        this.attachmentName = attachmentName;
        this.attachmentData = attachmentData;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String attachmentName) { this.attachmentName = attachmentName; }

    public String getAttachmentData() { return attachmentData; }
    public void setAttachmentData(String attachmentData) { this.attachmentData = attachmentData; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}
