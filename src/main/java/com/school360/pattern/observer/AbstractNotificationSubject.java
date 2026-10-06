package com.school360.pattern.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Shared subscriptions; create one subject per event, never a mutable singleton. */
public abstract class AbstractNotificationSubject implements NotificationSubject {
    private final List<NotificationObserver> observers = new ArrayList<>();
    private String message;

    @Override public void addObserver(NotificationObserver observer) {
        Objects.requireNonNull(observer, "observer must not be null");
        if (!observers.contains(observer)) observers.add(observer);
    }
    @Override public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }
    protected void publish(String message) {
        this.message = Objects.requireNonNull(message, "message must not be null");
        notifyObservers();
    }
    @Override public void notifyObservers() {
        if (message == null) return;
        for (NotificationObserver observer : new ArrayList<>(observers)) observer.update(message);
    }
}
