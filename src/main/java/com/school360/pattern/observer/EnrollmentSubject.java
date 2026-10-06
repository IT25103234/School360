package com.school360.pattern.observer;

/** ConcreteSubject for existing officer/final-staff approval and rejection transitions. */
public class EnrollmentSubject extends AbstractNotificationSubject {
    public void statusChanged(String previous, String current, boolean finalApproval) {
        if (current == null || current.equalsIgnoreCase(previous)) return;
        if ("APPROVED".equalsIgnoreCase(current)) {
            publish(finalApproval ? "Your enrollment has been approved by Administrative Staff."
                    : "Your enrollment has been approved by the Enrollment Officer and awaits final staff approval.");
        } else if ("REJECTED".equalsIgnoreCase(current)) {
            publish("Your enrollment has been rejected by "
                    + (finalApproval ? "Administrative Staff." : "the Enrollment Officer."));
        }
    }
}
