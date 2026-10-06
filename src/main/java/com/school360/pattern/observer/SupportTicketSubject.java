package com.school360.pattern.observer;

/** ConcreteSubject for a saved support reply, information request or status change. */
public class SupportTicketSubject extends AbstractNotificationSubject {
    public void ticketUpdated(String message) {
        publish(message);
    }
}
