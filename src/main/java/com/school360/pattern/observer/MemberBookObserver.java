package com.school360.pattern.observer;

import com.school360.repository.StudentNotificationRepository;

/** ConcreteObserver for a reliably identified student reservation holder. */
public class MemberBookObserver extends StudentNotificationObserver {
    public MemberBookObserver(StudentNotificationRepository repository, Long studentId, String bookTitle) {
        super(repository, studentId, null, bookTitle, "BOOK_AVAILABLE");
    }
}
