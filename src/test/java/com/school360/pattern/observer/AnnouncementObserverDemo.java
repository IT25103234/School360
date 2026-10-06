package com.school360.pattern.observer;

import com.school360.model.Announcement;

/** Run main() from the IDE. No Spring application or database is started. */
public class AnnouncementObserverDemo {
    public static void main(String[] args) {
        AnnouncementSubject subject = new AnnouncementSubject();
        NotificationObserver studentOne = new StudentAnnouncementObserver("Amali");
        NotificationObserver studentTwo = new StudentAnnouncementObserver("Nimal");

        subject.addObserver(studentOne);
        subject.addObserver(studentTwo);

        Announcement first = new Announcement(
                "Science meeting", "Meet in Room 5 at 10 AM.", "Ms. Silva", "TEACHER");
        subject.publishAnnouncement(first);

        subject.removeObserver(studentTwo);

        Announcement second = new Announcement(
                "Science meeting update", "Please bring your notebook.", "Ms. Silva", "TEACHER");
        subject.publishAnnouncement(second);
    }
}
