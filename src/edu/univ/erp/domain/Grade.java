package edu.univ.erp.domain;

public class Grade {
    private int enrollmentId;    // link to Enrollment
    private String component;    // e.g. "quiz", "midterm", "endsem"
    private double score;        // marks for this component
    private double finalGrade;   // computed final grade

    public Grade(int enrollmentId, String component, double score, double finalGrade) {
        this.enrollmentId = enrollmentId;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getComponent() {
        return component;
    }

    public double getScore() {
        return score;
    }

    public double getFinalGrade() {
        return finalGrade;
    }
}