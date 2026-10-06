package com.school360.pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school360.controller.*;
import com.school360.model.*;
import com.school360.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Real controllers and JPA, isolated from MySQL, the seeder and desktop browser launcher. */
@SpringBootTest(classes = ManagementWorkflowIntegrationTest.TestApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:pattern_workflows;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never",
    "spring.jpa.show-sql=false", "spring.datasource.hikari.maximum-pool-size=2"
})
@AutoConfigureMockMvc
class ManagementWorkflowIntegrationTest {
    @Configuration
    @EnableAutoConfiguration
    @EntityScan("com.school360.model")
    @EnableJpaRepositories("com.school360.repository")
    @Import({AdminController.class, TeacherController.class, LibraryController.class,
            EnrollmentController.class, StaffController.class, SupportController.class, StudentController.class})
    static class TestApplication {}

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired StudentRepository students;
    @Autowired StudentNotificationRepository notifications;
    @Autowired CourseRepository courses;
    @Autowired BookRepository books;
    @Autowired BookBorrowRepository borrows;

    private JsonNode postJson(String path, Object body) throws Exception {
        String result = mvc.perform(post(path).contentType("application/json").content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(result);
    }

    @Test void sixModulesPersistThroughExistingEndpointsAndInbox() throws Exception {
        JsonNode user = postJson("/api/admin/users", Map.of("username", "patternstudent", "password", "password123",
                "role", "STUDENT", "fullName", "Amali", "email", "amali@example.test", "contact", "123"));
        long userId = user.get("id").asLong();
        Student student = students.findByUserId(userId).orElseThrow();
        long studentId = student.getId();
        assertEquals("QR-STUDENT-PATTERNSTUDENT", user.get("qrCodeToken").asText());
        postJson("/api/teachers/announcements", Map.of("title", "Meeting", "content", "Room 5", "createdBy", "Teacher", "role", "TEACHER"));
        mvc.perform(get("/api/students/announcements")).andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("Meeting"));
        assertEquals(1, notifications.countByStudentIdAndIsReadFalse(studentId));

        Course course = courses.save(new Course("Science", "PATTERN-SCI", "Demo"));
        JsonNode request = postJson("/api/students/enroll", Map.of("studentId", studentId, "courseId", course.getId()));
        long requestId = request.get("id").asLong();
        postJson("/api/enrollment/applications/" + requestId + "/status", Map.of("status", "APPROVED"));
        postJson("/api/enrollment/applications/" + requestId + "/status", Map.of("status", "APPROVED"));
        assertEquals("APPROVED_EO", students.findById(studentId).orElseThrow().getEnrollmentStatus());
        postJson("/api/staff/approvals/" + requestId + "/approve", Map.of("status", "APPROVED"));
        assertEquals("APPROVED_STAFF", students.findById(studentId).orElseThrow().getEnrollmentStatus());

        Book book = new Book(); book.setTitle("Algorithms"); book.setCopies(0); book.setStatus("UNAVAILABLE");
        book = books.save(book);
        JsonNode reservation = postJson("/api/library/reservations", Map.of("memberId", studentId, "memberName", "Amali", "bookId", book.getId(), "bookTitle", "Algorithms"));
        BookBorrow borrow = new BookBorrow(); borrow.setBookId(book.getId()); borrow.setStatus("BORROWED"); borrow = borrows.save(borrow);
        for (int i = 0; i < 2; i++) mvc.perform(put("/api/library/borrows/" + borrow.getId() + "/status").contentType("application/json").content("{\"status\":\"RETURNED\"}"))
                .andExpect(status().isOk());
        assertEquals(1, books.findById(book.getId()).orElseThrow().getCopies());
        // Existing approval notification still works alongside the availability event.
        mvc.perform(put("/api/library/reservations/" + reservation.get("id").asLong()).contentType("application/json").content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());

        JsonNode autoGrade = postJson("/api/staff/exam-marks", Map.of("examTitle", "Term", "studentId", studentId, "marksObtained", 80, "totalMarks", 100));
        assertEquals("A", autoGrade.get("grade").asText());
        JsonNode manualGrade = postJson("/api/staff/exam-marks", Map.of("examTitle", "Term", "studentId", studentId, "marksObtained", 10, "totalMarks", 100, "grade", "Provided"));
        assertEquals("Provided", manualGrade.get("grade").asText());

        JsonNode ticket = postJson("/api/students/tickets", Map.of("studentId", String.valueOf(studentId), "title", "Help", "description", "Issue"));
        long ticketId = ticket.get("id").asLong();
        postJson("/api/support/tickets/" + ticketId + "/reply", Map.of("message", "We are checking", "senderName", "Officer"));
        postJson("/api/support/tickets/" + ticketId + "/status", Map.of("status", "RESOLVED"));
        postJson("/api/support/tickets/" + ticketId + "/status", Map.of("status", "RESOLVED"));
        assertEquals(7, notifications.countByStudentIdAndIsReadFalse(studentId));
        mvc.perform(get("/api/students/" + studentId + "/notifications")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7));
        long notificationId = notifications.findByStudentIdOrderByIdDesc(studentId).get(0).getId();
        mvc.perform(post("/api/students/notifications/" + notificationId + "/read")).andExpect(status().isOk());
        assertEquals(6, notifications.countByStudentIdAndIsReadFalse(studentId));
    }
}
