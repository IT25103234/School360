package com.school360.pattern.strategy;

/** Context: selects the existing manual/automatic behavior and delegates to a strategy. */
public class GradeCalculator {
    private final GradeAssignmentStrategy strategy;

    public GradeCalculator(GradeAssignmentStrategy strategy) {
        this.strategy = java.util.Objects.requireNonNull(strategy);
    }
    public static GradeCalculator forGrade(String providedGrade) {
        return new GradeCalculator(providedGrade != null && !providedGrade.isBlank()
                ? new ProvidedGradeStrategy() : new AutomaticGradeStrategy());
    }
    public String assignGrade(String providedGrade, Double marks, Double total) {
        return strategy.assignGrade(providedGrade, marks, total);
    }
}
