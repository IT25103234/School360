package com.school360.pattern.observer;

import com.school360.repository.StudentNotificationRepository;

/** ConcreteObserver for the student belonging to the processed enrollment request. */
public class StudentEnrollmentObserver extends StudentNotificationObserver {
    public StudentEnrollmentObserver(StudentNotificationRepository repository, Long studentId, String courseName) {
        super(repository, studentId, null, courseName, "ENROLLMENT_STATUS");
    }
}
