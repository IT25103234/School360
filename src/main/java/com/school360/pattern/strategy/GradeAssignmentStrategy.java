package com.school360.pattern.strategy;

/** Strategy for the two grade-assignment approaches already supported by the API. */
public interface GradeAssignmentStrategy {
    String assignGrade(String providedGrade, Double marks, Double total);
}
