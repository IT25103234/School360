package com.school360.controller;

import com.school360.model.Student;
import com.school360.model.User;
import com.school360.repository.StudentRepository;
import com.school360.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String fullName = clean(payload.get("fullName"));
        String username = clean(payload.get("username"));
        String password = payload.get("password");
        String email = clean(payload.get("email"));
        String contact = clean(payload.get("contact"));

        if (fullName == null || username == null || password == null
                || password.isBlank() || email == null || contact == null) {
            return ResponseEntity.badRequest().body("All registration fields are required.");
        }
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long.");
        }
        if (!fullName.matches("^[a-zA-Z\\s]+$")) {
            return ResponseEntity.badRequest().body("Name must contain letters and spaces only (no numbers or symbols).");
        }
        if (!contact.matches("^\\d{1,16}$")) {
            return ResponseEntity.badRequest().body("Phone number must contain numbers only and be at most 16 digits.");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("This username is already taken. Please choose another.");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("An account with this email already exists.");
        }

        User user = new User(
                username,
                password,
                "STUDENT",
                fullName,
                email,
                contact,
                "QR-STUDENT-" + UUID.randomUUID()
        );
        userRepository.save(user);
        studentRepository.save(new Student(user, "", null, "", "", null));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("contact", user.getContact());
        response.put("qrCodeToken", user.getQrCodeToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/directory")
    public ResponseEntity<List<Map<String, Object>>> directory() {
        List<Map<String, Object>> accounts = userRepository.findAll().stream()
                .filter(user -> user.getQrCodeToken() != null && !user.getQrCodeToken().isBlank())
                .map(user -> {
                    Map<String, Object> account = new HashMap<>();
                    account.put("id", user.getId());
                    account.put("fullName", user.getFullName());
                    account.put("role", user.getRole());
                    account.put("qrCodeToken", user.getQrCodeToken());
                    return account;
                })
                .toList();

        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        String trimUser = username.trim();
        String trimPass = password.trim();

        Optional<User> userOpt = userRepository.findByUsername(trimUser);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByUsernameIgnoreCase(trimUser);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(trimPass)) {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("role", user.getRole());
                response.put("fullName", user.getFullName());
                response.put("email", user.getEmail());
                response.put("contact", user.getContact());
                response.put("qrCodeToken", user.getQrCodeToken());
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).body("Invalid username or password.");
    }

    @PostMapping("/qr-login")
    public ResponseEntity<?> qrLogin(@RequestBody Map<String, String> payload) {
        String qrToken = payload.get("qrToken");
        if (qrToken == null || qrToken.isBlank()) {
            return ResponseEntity.badRequest().body("QR Token is required.");
        }

        String token = qrToken.trim();
        Optional<User> userOpt = userRepository.findByQrCodeToken(token);
        if (!userOpt.isPresent()) {
            List<User> all = userRepository.findAll();
            userOpt = all.stream().filter(u -> u.getQrCodeToken() != null &&
                (u.getQrCodeToken().equalsIgnoreCase(token) ||
                 token.toLowerCase().contains(u.getUsername().toLowerCase()) ||
                 u.getQrCodeToken().toLowerCase().contains(token.toLowerCase()) ||
                 (u.getRole() != null && token.toLowerCase().contains(u.getRole().toLowerCase()))
                )
            ).findFirst();
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("fullName", user.getFullName());
            response.put("email", user.getEmail());
            response.put("contact", user.getContact());
            response.put("qrCodeToken", user.getQrCodeToken());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body("Invalid or unrecognized QR Login Code.");
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
