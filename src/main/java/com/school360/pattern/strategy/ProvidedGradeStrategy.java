package com.school360.pattern.strategy;

/** Retains the supplied grade exactly, including its existing formatting. */
public class ProvidedGradeStrategy implements GradeAssignmentStrategy {
    @Override public String assignGrade(String providedGrade, Double marks, Double total) {
        return providedGrade;
    }
}
