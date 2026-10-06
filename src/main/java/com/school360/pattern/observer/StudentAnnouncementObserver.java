package com.school360.pattern.observer;

import java.util.Objects;

/** ConcreteObserver: displays notifications for a demo student in the console. */
public class StudentAnnouncementObserver implements NotificationObserver {
    private final String studentName;

    public StudentAnnouncementObserver(String studentName) {
        this.studentName = Objects.requireNonNull(studentName, "studentName must not be null");
    }

    @Override
    public void update(String message) {
        System.out.println(studentName + " received: " + message);
    }
}
