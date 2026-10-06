package com.school360.pattern.observer;

import com.school360.model.StudentNotification;
import com.school360.repository.StudentNotificationRepository;

/** Persistence adapter for the existing student notification inbox. */
public class StudentNotificationObserver implements NotificationObserver {
    private final StudentNotificationRepository repository;
    private final Long studentId;
    private final Long ticketId;
    private final String title;
    private final String type;

    public StudentNotificationObserver(StudentNotificationRepository repository, Long studentId,
                                       Long ticketId, String title, String type) {
        this.repository = repository;
        this.studentId = studentId;
        this.ticketId = ticketId;
        this.title = title;
        this.type = type;
    }
    @Override public void update(String message) {
        repository.save(new StudentNotification(studentId, ticketId, title, message, type));
    }
}
