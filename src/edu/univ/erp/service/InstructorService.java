package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.AuthDB;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import java.util.*;

public class InstructorService {

    public List<Section> getInstructorSections(int instructorId) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "viewInstructorSections")) {
            return null;
        }

        List<Section> res = new ArrayList<>();

        for (Section s : SectionData.listAll()) {
            if (s.getInstructorId() == instructorId)
                res.add(s);
        }

        return res;
    }

    public String enterMarks(int enrollmentId, String component, double score) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "enterMarks")) {
            return "Permission denied";
        }

        Enrollment e = EnrollmentData.getById(enrollmentId);
        if (e == null) return "Enrollment not found";

        Section s = SectionData.getById(e.getSectionId());
        if (s == null) return "Section not found";

        if (s.getInstructorId() != user.getUserId()) {
            return "Not your section";
        }

        // Normalize and validate component name.
        // Only three components are allowed in the system: quiz, midsem, endsem.
        String comp = component == null ? "" : component.trim().toLowerCase();
        if (!"quiz".equals(comp) && !"midsem".equals(comp) && !"endsem".equals(comp)) {
            return "Component must be quiz, midsem, or endsem";
        }

        // Validate component-specific maximum scores so instructors
        // cannot enter marks beyond the allowed maxima.
        if ("quiz".equals(comp) && score > 20.0) {
            return "Quiz marks should be less than or equal to 20";
        }
        if ("midsem".equals(comp) && score > 30.0) {
            return "Midsem marks should be less than or equal to 30";
        }
        if ("endsem".equals(comp) && score > 50.0) {
            return "Endsem marks should be less than or equal to 50";
        }

        // store raw score; final grade is computed later as a simple sum
        // of quiz + midsem + endsem (no additional weighting).
        GradeData.addGrade(new Grade(enrollmentId, comp, score, 0));
        return null;
    }

    public String exportSectionGradesCsv(String sectionId, String filename) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "exportGrades")) {
            return "Permission denied";
        }
        if (sectionId == null || sectionId.trim().isEmpty()) {
            return "Section id is required";
        }

        sectionId = sectionId.trim();
        Section sec = SectionData.getById(sectionId);
        if (sec == null) {
            return "Section not found";
        }
        if (sec.getInstructorId() != user.getUserId()) {
            return "Not your section";
        }

        // Build lookup for student roll numbers and usernames.
        Map<Integer, String> rollByStudent = new HashMap<>();
        for (Student s : StudentData.listAll()) {
            rollByStudent.put(s.getUserId(), s.getRollNo());
        }
        Map<Integer, String> nameByStudent = new HashMap<>();
        for (User u2 : AuthDB.loadUsers()) {
            nameByStudent.put(u2.getUserId(), u2.getUsername());
        }

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"EnrollmentId", "StudentId", "Name", "RollNo", "CourseCode", "SectionId", "FinalScore"});

        for (Enrollment e : EnrollmentData.listAll()) {
            if (!"enrolled".equals(e.getStatus())) continue;
            if (!sectionId.equals(e.getSectionId())) continue;

            double finalScore = GradeData.computeFinalForEnrollment(e.getEnrollmentId());
            String roll = rollByStudent.getOrDefault(e.getStudentId(), "");
            String name = nameByStudent.getOrDefault(e.getStudentId(), "");

            rows.add(new String[]{
                    String.valueOf(e.getEnrollmentId()),
                    String.valueOf(e.getStudentId()),
                    name,
                    roll,
                    sec.getCourseCode(),
                    sectionId,
                    String.valueOf(finalScore)
            });
        }

        CSVUtil.writeAll(filename, rows);
        return null;
    }

    public double computeFinal(int enrollmentId) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "gradeSection")) {
            return -1;
        }

        Enrollment e = EnrollmentData.getById(enrollmentId);
        if (e == null) return -1;
        Section s = SectionData.getById(e.getSectionId());
        if (s == null) return -1;
        if (s.getInstructorId() != user.getUserId()) return -1;

        return GradeData.computeFinalForEnrollment(enrollmentId);
    }
}
