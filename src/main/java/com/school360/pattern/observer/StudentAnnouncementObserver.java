package com.school360.pattern.observer;

import java.util.Objects;
import com.school360.repository.StudentNotificationRepository;

/** ConcreteObserver: delivers to the inbox; retains the original console demo constructor. */
public class StudentAnnouncementObserver implements NotificationObserver {
    private final String studentName;
    private final NotificationObserver delivery;

    public StudentAnnouncementObserver(String studentName) {
        this.studentName = Objects.requireNonNull(studentName, "studentName must not be null");
        this.delivery = null;
    }

    public StudentAnnouncementObserver(StudentNotificationRepository repository, Long studentId, String title) {
        this.studentName = null;
        this.delivery = new StudentNotificationObserver(repository, studentId, null, title, "ANNOUNCEMENT");
    }

    @Override
    public void update(String message) {
        if (delivery != null) delivery.update(message);
        else System.out.println(studentName + " received: " + message);
    }
}
