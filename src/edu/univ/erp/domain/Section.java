package edu.univ.erp.domain;

public class Section {
    private String sectionId;     // e.g., "DSA101-A"
    private String courseCode;    // links to Course.code
    private int instructorId;     // links to Instructor.userId
    private String dayTime;       // e.g., "Mon 10:00-11:00"
    private String room;          // e.g., "R-203"
    private int capacity;         // max seats
    private String semester;      // e.g., "Fall"
    private int year;             // e.g., 2025

    public Section(String sectionId, String courseCode, int instructorId, 
                   String dayTime, String room, int capacity, 
                   String semester, int year) {

        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public String getDayTime() {
        return dayTime;
    }

    public String getRoom() {
        return room;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getSemester() {
        return semester;
    }

    public int getYear() {
        return year;
    }
}