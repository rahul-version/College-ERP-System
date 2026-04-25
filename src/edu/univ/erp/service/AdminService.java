package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.DisabledUsers;
import edu.univ.erp.auth.PasswordHasher;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import java.util.List;

public class AdminService {

    public String createUser(int id, String username, String role, String passwordPlain) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }

        // Enforce unique numeric userId and username in auth DB
        List<User> existing = AuthDB.loadUsers();
        for (User u : existing) {
            if (u.getUserId() == id) {
                return "User ID already exists";
            }
            if (u.getUsername().equalsIgnoreCase(username)) {
                return "Username already exists";
            }
        }

        String hashed = PasswordHasher.hashPassword(passwordPlain);

        CSVUtil.appendLine("auth.csv",
                id + "," + username + "," + role + "," + hashed);

        return null;
    }

    public String addStudent(Student s) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }

        // Enforce unique student userId and roll number
        for (Student existing : StudentData.listAll()) {
            if (existing.getUserId() == s.getUserId()) {
                return "Student user ID already exists";
            }
            if (existing.getRollNo().equalsIgnoreCase(s.getRollNo())) {
                return "Student with this roll number already exists";
            }
        }

        StudentData.addStudent(s);
        return null;
    }

    public String addInstructor(Instructor i) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }

        // Enforce unique instructor userId
        for (Instructor existing : InstructorData.listAll()) {
            if (existing.getUserId() == i.getUserId()) {
                return "Instructor user ID already exists";
            }
        }

        InstructorData.addInstructor(i);
        return null;
    }

    public String deleteStudent(int userId) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }
        boolean ok = StudentData.deleteStudent(userId);
        if (!ok) {
            return "Student not found";
        }
        // Also remove the login entry from auth.csv so the ID/username can be reused
        AuthDB.deleteUserById(userId);
        return null;
    }

    public String deleteInstructor(int userId) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }
        boolean ok = InstructorData.deleteInstructor(userId);
        if (!ok) {
            return "Instructor not found";
        }
        // Also remove the login entry from auth.csv so the ID/username can be reused
        AuthDB.deleteUserById(userId);
        return null;
    }

    public String disableUser(int userId) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }
        DisabledUsers.disable(userId);
        return null;
    }

    public String enableUser(int userId) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createUser")) {
            return "Permission denied";
        }
        DisabledUsers.enable(userId);
        return null;
    }

    public String createCourse(Course c) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createCourse")) {
            return "Permission denied";
        }

        // Prevent duplicate courses with same code
        for (Course existing : CourseData.listAll()) {
            if (existing.getCode().equalsIgnoreCase(c.getCode())) {
                return "Course already exists";
            }
        }

        CourseData.addCourse(c);
        return null;
    }

    /**
     * Delete a course permanently by code, and cascade delete:
     * - all sections for that course
     * - all enrollments in those sections
     * - all grades for those enrollments
     */
    public String deleteCourse(String code) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createCourse")) {
            return "Permission denied";
        }
        if (code == null || code.trim().isEmpty()) {
            return "Course code is required";
        }
        code = code.trim();

        // Find all sections that belong to this course code
        List<Section> allSections = SectionData.listAll();
        List<String> sectionIds = new java.util.ArrayList<>();
        for (Section s : allSections) {
            if (code.equalsIgnoreCase(s.getCourseCode())) {
                sectionIds.add(s.getSectionId());
            }
        }

        // For those sections, collect all enrollmentIds
        List<Integer> enrollmentIds = new java.util.ArrayList<>();
        if (!sectionIds.isEmpty()) {
            for (Enrollment e : EnrollmentData.listAll()) {
                if (sectionIds.contains(e.getSectionId())) {
                    enrollmentIds.add(e.getEnrollmentId());
                }
            }
        }

        // Delete grades for these enrollments
        if (!enrollmentIds.isEmpty()) {
            GradeData.deleteByEnrollmentIds(enrollmentIds);
        }
        // Delete enrollments for these sections
        if (!sectionIds.isEmpty()) {
            EnrollmentData.deleteBySectionIds(sectionIds);
        }
        // Delete the sections themselves
        for (String sectionId : sectionIds) {
            SectionData.deleteSection(sectionId);
        }

        // Finally delete the course row
        boolean ok = CourseData.deleteCourse(code);
        return ok ? null : "Course not found";
    }

    public String createSection(Section s) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createSection")) {
            return "Permission denied";
        }

        // Validate that the instructorId actually belongs to an instructor.
        boolean isInstructor = false;
        for (Instructor inst : InstructorData.listAll()) {
            if (inst.getUserId() == s.getInstructorId()) {
                isInstructor = true;
                break;
            }
        }
        if (!isInstructor) {
            return "Instructor not found for given userId";
        }

        SectionData.addSection(s);
        return null;
    }

    public String assignInstructor(String sectionId, int instructorId) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "assignInstructor")) {
            return "Permission denied";
        }

        // Check existing section and prevent reassignment to a different instructor.
        Section sec = SectionData.getById(sectionId);
        if (sec == null) {
            return "Section not found";
        }
        int currentInstr = sec.getInstructorId();
        if (currentInstr != 0 && currentInstr != instructorId) {
            return "This section already has an instructor assigned and cannot be reassigned";
        }
        // If the same instructorId is provided again, treat as success without modifying storage.
        if (currentInstr == instructorId) {
            return null;
        }

        // Validate that the instructorId actually belongs to an instructor.
        boolean isInstructor = false;
        for (Instructor inst : InstructorData.listAll()) {
            if (inst.getUserId() == instructorId) {
                isInstructor = true;
                break;
            }
        }
        if (!isInstructor) {
            return "Instructor not found for given userId";
        }

        boolean ok = SectionData.updateInstructor(sectionId, instructorId);
        return ok ? null : "Section not found";
    }

    public String deleteSection(String sectionId) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "createSection")) {
            return "Permission denied";
        }
        if (sectionId == null || sectionId.trim().isEmpty()) {
            return "Section id is required";
        }
        sectionId = sectionId.trim();

        // Block deletion if any student is currently enrolled in this section.
        for (Enrollment e : EnrollmentData.listAll()) {
            if (sectionId.equals(e.getSectionId()) && "enrolled".equals(e.getStatus())) {
                return "Students are already enrolled in this course";
            }
        }

        boolean ok = SectionData.deleteSection(sectionId);
        return ok ? null : "Section not found";
    }

    public String toggleMaintenance(boolean on) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "toggleMaintenance")) {
            return "Permission denied";
        }
        if (on) {
            AccessControl.enableMaintenance();
        } else {
            AccessControl.disableMaintenance();
        }
        return null;
    }
}
