package edu.univ.erp.domain;

public class Instructor {
    private int userId;
    private String department;

    public Instructor(int userId, String department) {
        this.userId = userId;
        this.department = department;
    }

    public int getUserId() {
        return userId;
    }

    public String getDepartment() {
        return department;
    }
}
