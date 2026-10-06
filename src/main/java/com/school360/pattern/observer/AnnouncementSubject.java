package com.school360.pattern.observer;

import com.school360.model.Announcement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ConcreteSubject for an explicitly invoked, in-memory announcement demo.
 * Reads announcement data without changing or saving the existing entity.
 * Create one instance for a group of subscribers; this is not a Spring singleton.
 */
public class AnnouncementSubject implements NotificationSubject {
    private final List<NotificationObserver> observers = new ArrayList<>();
    private String message;

    @Override
    public void addObserver(NotificationObserver observer) {
        Objects.requireNonNull(observer, "observer must not be null");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    /** Records the announcement event locally, then notifies current subscribers. */
    public void publishAnnouncement(Announcement announcement) {
        Objects.requireNonNull(announcement, "announcement must not be null");
        message = "Announcement: " + announcement.getTitle() + " - " + announcement.getContent();
        notifyObservers();
    }

    @Override
    public void notifyObservers() {
        if (message == null) {
            return;
        }
        // Snapshot allows an observer to unsubscribe during its callback.
        for (NotificationObserver observer : new ArrayList<>(observers)) {
            observer.update(message);
        }
    }
}
