package com.school360.controller;

import com.school360.config.DatabaseSeeder;
import com.school360.model.SystemLog;
import com.school360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/db", "/api/admin/db"})
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private SystemLogRepository logRepository;

    @Autowired
    private DatabaseSeeder databaseSeeder;

    /**
     * Check Database Health & Status Endpoint
     */
    @GetMapping("/status")
    public ResponseEntity<?> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            long pingMs = System.currentTimeMillis() - startTime;
            boolean isLive = rs.next() && rs.getInt(1) == 1;

            response.put("status", isLive ? "CONNECTED" : "DEGRADED");
            response.put("pingMs", pingMs);
            response.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
            response.put("databaseVersion", conn.getMetaData().getDatabaseProductVersion());
            response.put("driverName", conn.getMetaData().getDriverName());
            response.put("url", conn.getMetaData().getURL());
            response.put("userCount", userRepository.count());
            response.put("studentCount", studentRepository.count());
            response.put("courseCount", courseRepository.count());
            response.put("ticketCount", ticketRepository.count());
            response.put("logCount", logRepository.count());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "DISCONNECTED");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Trigger Database Re-connection Verification & Refresh
     */
    @PostMapping("/reconnect")
    public ResponseEntity<?> reconnectDatabase() {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            long pingMs = System.currentTimeMillis() - startTime;
            logRepository.save(new SystemLog("Manual Database connection re-test successful (" + pingMs + "ms).", "INFO"));

            response.put("success", true);
            response.put("status", "CONNECTED");
            response.put("message", "Database connection re-established and verified successfully.");
            response.put("pingMs", pingMs);
            response.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
            response.put("userCount", userRepository.count());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "DISCONNECTED");
            response.put("message", "Database connection test failed: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Trigger Database Seed Re-initialization
     */
    @PostMapping("/reset")
    public ResponseEntity<?> resetDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            databaseSeeder.run();
            logRepository.save(new SystemLog("Database seed verified and synchronized by Admin request.", "INFO"));

            response.put("success", true);
            response.put("status", "CONNECTED");
            response.put("message", "Database seeded and synchronized successfully.");
            response.put("userCount", userRepository.count());
            response.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Database re-seed failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
