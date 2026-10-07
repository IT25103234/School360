package com.school360.config;

import com.school360.model.*;
import com.school360.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseSeederProfileTest {
    @Test void restoresPlaceholderProfileAndLeavesRestoredEnrollmentAlone() {
        DatabaseSeeder seeder = new DatabaseSeeder();
        UserRepository users = mock(UserRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        CourseRepository courses = mock(CourseRepository.class);
        EnrollmentRequestRepository requests = mock(EnrollmentRequestRepository.class);
        ReflectionTestUtils.setField(seeder, "userRepository", users);
        ReflectionTestUtils.setField(seeder, "studentRepository", students);
        ReflectionTestUtils.setField(seeder, "courseRepository", courses);
        ReflectionTestUtils.setField(seeder, "enrollmentRequestRepository", requests);
        User user = new User(); user.setId(1L); user.setRole("STUDENT");
        Student student = new Student(user, "ADM-0001", null, "", "", "UNENROLLED"); student.setId(5L);
        Course course = new Course("BSc (Hons) in Software Engineering", "SE-101", "Demo"); course.setId(8L);
        when(users.findByUsernameIgnoreCase("Sameeha")).thenReturn(Optional.of(user));
        when(students.findByUserId(1L)).thenReturn(Optional.of(student));
        when(courses.findByCode("SE-101")).thenReturn(Optional.of(course));
        when(requests.findByStudentId(5L)).thenReturn(List.of());
        seeder.restoreSampleStudentProfile();
        assertEquals("ADM-360-001", student.getAdmissionNumber());
        assertEquals(8L, student.getCourseId());
        assertEquals("Year 3", student.getClassName());
        assertEquals("Section A", student.getSection());
        assertEquals("APPROVED_STAFF", student.getEnrollmentStatus());
        seeder.restoreSampleStudentProfile();
        verify(students, times(1)).save(student);
    }

    @Test void preservesEnrollmentApplications() {
        DatabaseSeeder seeder = new DatabaseSeeder();
        UserRepository users = mock(UserRepository.class);
        StudentRepository students = mock(StudentRepository.class);
        CourseRepository courses = mock(CourseRepository.class);
        EnrollmentRequestRepository requests = mock(EnrollmentRequestRepository.class);
        ReflectionTestUtils.setField(seeder, "userRepository", users);
        ReflectionTestUtils.setField(seeder, "studentRepository", students);
        ReflectionTestUtils.setField(seeder, "courseRepository", courses);
        ReflectionTestUtils.setField(seeder, "enrollmentRequestRepository", requests);
        User user = new User(); user.setId(1L); user.setRole("STUDENT");
        Student student = new Student(user, "ADM-0001", null, "", "", "UNENROLLED"); student.setId(5L);
        when(users.findByUsernameIgnoreCase("Sameeha")).thenReturn(Optional.of(user));
        when(students.findByUserId(1L)).thenReturn(Optional.of(student));
        when(requests.findByStudentId(5L)).thenReturn(List.of(new EnrollmentRequest()));
        seeder.restoreSampleStudentProfile();
        verify(students, never()).save(any());
        verifyNoInteractions(courses);
        assertNull(student.getCourseId());
    }
}
