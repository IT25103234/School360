package com.school360.pattern.observer;

import com.school360.model.Announcement;
import java.util.Objects;

/** ConcreteSubject for a successfully saved teacher announcement. */
public class AnnouncementSubject extends AbstractNotificationSubject {
    public void publishAnnouncement(Announcement announcement) {
        Objects.requireNonNull(announcement, "announcement must not be null");
        publish("Announcement: " + announcement.getTitle() + " - " + announcement.getContent());
    }
}
