package edu.univ.erp.domain;

public class Enrollment {

    private int enrollmentId;
    private int studentId;
    private String sectionId;
    private String status;

    // Constructor used when reading from CSV
    public Enrollment(int enrollmentId, int studentId, String sectionId, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    // Constructor for new enrollments (auto-assign id)
    public Enrollment(int studentId, String sectionId, String status) {
        this.enrollmentId = -1; // placeholder; EnrollmentData will assign real ID
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int id) {
        this.enrollmentId = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        this.status = s;
    }
}
