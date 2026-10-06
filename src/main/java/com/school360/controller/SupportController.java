package com.school360.controller;

import com.school360.model.*;
import com.school360.repository.*;
import com.school360.pattern.observer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/support")
@CrossOrigin(origins = "*")
public class SupportController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SupportController.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketReplyRepository ticketReplyRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private StudentNotificationRepository studentNotificationRepository;

    // View All Tickets
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets() {
        return ResponseEntity.ok(ticketRepository.findAll());
    }

    // Update Ticket Status
    @PostMapping("/tickets/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            String status = payload.get("status"); // OPEN, PENDING, IN_PROGRESS, RESOLVED, CLOSED
            String previousStatus = ticket.getStatus();
            ticket.setStatus(status);
            ticketRepository.save(ticket);

            if (status != null && !status.equalsIgnoreCase(previousStatus) && ticket.getStudentId() != null) {
                try {
                    notifyStudent(ticket, "SUPPORT_STATUS", "Ticket #" + id + ": " + ticket.getTitle()
                            + " status changed to " + status + ".");
                } catch (RuntimeException ex) {
                    logger.warn("Ticket {} saved but status notification failed", id, ex);
                }
            }

            logRepository.save(new SystemLog("Ticket ID " + id + " status changed to " + status, "INFO"));
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    // Modify Ticket Priority
    @PostMapping("/tickets/{id}/priority")
    public ResponseEntity<?> updatePriority(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            String priority = payload.get("priority"); // LOW, MEDIUM, HIGH
            ticket.setPriority(priority);
            ticketRepository.save(ticket);

            logRepository.save(new SystemLog("Ticket ID " + id + " priority changed to " + priority, "INFO"));
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    // Escalate Ticket to Admin
    @PostMapping("/tickets/{id}/escalate")
    public ResponseEntity<?> escalateTicket(@PathVariable("id") Long id) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setEscalationStatus("ESCALATED");
            ticketRepository.save(ticket);

            logRepository.save(new SystemLog("Ticket ID " + id + " escalated to Administrator", "WARNING"));
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    // Send Support Agent Reply (and notify student)
    @PostMapping("/tickets/{id}/reply")
    public ResponseEntity<?> replyToTicket(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            String message = payload.get("message");
            String senderName = payload.get("senderName");
            boolean requestInfo = "true".equalsIgnoreCase(payload.get("requestInfo"));
            boolean requestAttachment = "true".equalsIgnoreCase(payload.get("requestAttachment"));

            String finalMessage = message;
            if (requestAttachment && !message.contains("attach")) {
                finalMessage = "📎 [Attachment Requested] " + message;
            } else if (requestInfo && !message.contains("details")) {
                finalMessage = "ℹ️ [Additional Info Requested] " + message;
            }

            TicketReply reply = new TicketReply(
                id,
                senderName,
                "SUPPORT",
                finalMessage,
                null,
                null
            );
            ticketReplyRepository.save(reply);

            // Set ticket status to PENDING if support replied
            ticket.setStatus("PENDING");
            ticketRepository.save(ticket);

            // Notify student
            if (ticket.getStudentId() != null) {
                String notifType = requestAttachment ? "ATTACHMENT_REQUEST" : (requestInfo ? "INFO_REQUEST" : "SUPPORT_REPLY");
                String notifMsg = requestAttachment ? 
                    ("Support Team requested a file attachment for Ticket #" + id + ": " + ticket.getTitle()) :
                    (requestInfo ? 
                        ("Support Team requested additional info for Ticket #" + id + ": " + ticket.getTitle()) :
                        ("Support Team replied to Ticket #" + id + ": " + ticket.getTitle()));

                notifyStudent(ticket, notifType, notifMsg);
            }

            return ResponseEntity.ok(reply);
        }
        return ResponseEntity.notFound().build();
    }

    // Explicit Request Additional Info / Attachment from Student
    @PostMapping("/tickets/{id}/request-info")
    public ResponseEntity<?> requestInfo(@PathVariable("id") Long id, @RequestBody Map<String, String> payload) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            String senderName = payload.getOrDefault("senderName", "Support Team");
            String customMessage = payload.get("message");
            String reqType = payload.getOrDefault("type", "ATTACHMENT_REQUEST");

            String msg = (customMessage != null && !customMessage.trim().isEmpty()) ? customMessage :
                ("ATTACHMENT_REQUEST".equalsIgnoreCase(reqType) ?
                    "Please attach a file or screenshot to help us resolve your issue." :
                    "Please provide additional details regarding your ticket.");

            String displayMsg = "⚠️ [Action Required] " + msg;

            TicketReply reply = new TicketReply(
                id,
                senderName,
                "SUPPORT",
                displayMsg,
                null,
                null
            );
            ticketReplyRepository.save(reply);

            ticket.setStatus("PENDING");
            ticketRepository.save(ticket);

            if (ticket.getStudentId() != null) {
                notifyStudent(ticket, reqType, "Action Required: " + msg);
            }

            logRepository.save(new SystemLog("Support team requested additional info/file for Ticket #" + id, "INFO"));
            return ResponseEntity.ok(reply);
        }
        return ResponseEntity.notFound().build();
    }

    private void notifyStudent(Ticket ticket, String type, String message) {
        SupportTicketSubject subject = new SupportTicketSubject();
        subject.addObserver(new StudentSupportObserver(studentNotificationRepository, ticket, type));
        subject.ticketUpdated(message);
    }

    // Delete Ticket (Support Team / Admin)
    @RequestMapping(value = {"/tickets/{id}", "/tickets/{id}/delete"}, method = {RequestMethod.DELETE, RequestMethod.POST})
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteTicket(@PathVariable("id") Long id) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            List<TicketReply> replies = ticketReplyRepository.findByTicketIdOrderByIdAsc(id);
            if (!replies.isEmpty()) {
                ticketReplyRepository.deleteAll(replies);
            }
            ticketRepository.delete(ticket);

            logRepository.save(new SystemLog("Support Team deleted ticket #" + id + " (" + ticket.getTitle() + ")", "WARN"));
            return ResponseEntity.ok(Map.of("message", "Ticket deleted successfully.", "id", id));
        }
        return ResponseEntity.notFound().build();
    }
}
