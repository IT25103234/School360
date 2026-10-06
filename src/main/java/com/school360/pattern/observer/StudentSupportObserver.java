package com.school360.pattern.observer;

import com.school360.model.Ticket;
import com.school360.repository.StudentNotificationRepository;

/** ConcreteObserver preserves the existing ticket link, message type and recipient. */
public class StudentSupportObserver extends StudentNotificationObserver {
    public StudentSupportObserver(StudentNotificationRepository repository, Ticket ticket, String type) {
        super(repository, ticket.getStudentId(), ticket.getId(), ticket.getTitle(), type);
    }
}
