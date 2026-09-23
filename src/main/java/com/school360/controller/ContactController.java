package com.school360.controller;

import com.school360.model.SystemLog;
import com.school360.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private SystemLogRepository logRepository;

    @PostMapping
    public ResponseEntity<?> submitContactForm(@RequestBody Map<String, String> payload) {
        String name = payload.getOrDefault("name", "Anonymous").trim();
        String email = payload.getOrDefault("email", "Not provided").trim();
        String phone = payload.getOrDefault("phone", "N/A").trim();
        String category = payload.getOrDefault("category", "General").trim();
        String subject = payload.getOrDefault("subject", "Inquiry").trim();
        String message = payload.getOrDefault("message", "").trim();

        if (name.isEmpty()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Full Name is required.");
            return ResponseEntity.badRequest().body(errorResp);
        }

        if (email.isEmpty()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Email address is required.");
            return ResponseEntity.badRequest().body(errorResp);
        }

        if (message.isEmpty()) {
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Message body cannot be empty.");
            return ResponseEntity.badRequest().body(errorResp);
        }

        String refId = "S360-CNT-" + (System.currentTimeMillis() % 100000);
        String logMsg = String.format("Contact Inquiry [Ref: %s] from [%s - %s] (Phone: %s | Role: %s | Subject: %s): %s", 
            refId, name, email, phone, category, subject, message);
        logRepository.save(new SystemLog(logMsg, "INFO"));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Thank you, " + name + "! Your inquiry regarding \"" + subject + "\" has been received. Our support desk will respond shortly.");
        response.put("referenceId", refId);

        return ResponseEntity.ok(response);
    }
}
