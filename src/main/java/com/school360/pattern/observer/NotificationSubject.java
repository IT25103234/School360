package com.school360.pattern.observer;

/** Subject: defines subscription and notification operations. */
public interface NotificationSubject {
    void addObserver(NotificationObserver observer);

    void removeObserver(NotificationObserver observer);

    void notifyObservers();
}
