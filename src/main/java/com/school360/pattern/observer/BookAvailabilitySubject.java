package com.school360.pattern.observer;

import com.school360.model.Book;

/** ConcreteSubject for a returned book changing from unavailable to available. */
public class BookAvailabilitySubject extends AbstractNotificationSubject {
    public void bookAvailable(Book book) {
        publish("The book '" + book.getTitle() + "' is now available. Please contact the library about your pending reservation.");
    }
}
