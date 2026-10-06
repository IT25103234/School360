package com.school360.pattern.strategy;

/** The original percentage thresholds, unchanged. */
public class AutomaticGradeStrategy implements GradeAssignmentStrategy {
    @Override public String assignGrade(String providedGrade, Double marks, Double total) {
        if (marks == null || total == null || total <= 0) return "N/A";
        double percentage = (marks / total) * 100.0;
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }
}
