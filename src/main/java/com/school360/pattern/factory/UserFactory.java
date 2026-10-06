package com.school360.pattern.factory;

import com.school360.model.Student;
import com.school360.model.User;

/** Simple factory for the existing role-configured account and optional student profile. */
public final class UserFactory {
    private UserFactory() {}

    public static User createUser(User details) {
        User user = new User(details.getUsername(), details.getPassword(), details.getRole(),
                details.getFullName(), details.getEmail(), details.getContact(),
                "QR-" + details.getRole() + "-" + details.getUsername().toUpperCase());
        // Preserve the existing endpoint's handling of submitted entity fields.
        user.setId(details.getId());
        return user;
    }

    public static Student createStudentProfile(User user) {
        return "STUDENT".equals(user.getRole())
                ? new Student(user, null, null, null, null, null) : null;
    }
}
