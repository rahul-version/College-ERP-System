package edu.univ.erp.service;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.auth.Session;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;

import java.util.*;

public class StudentService {

    public String registerSection(int studentId, String sectionId) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "register")) {
            return "Permission denied";
        }

        // Deadline-style rule: if registration is closed globally, block with a clear message.
        if (!SettingsData.isRegisterOpen()) {
            return "Registration deadline has passed";
        }

        // Per-section register deadline: 5-minute window after section is assigned.
        if (sectionId != null) {
            String key = "registerUntil_" + sectionId;
            String untilStr = SettingsData.get(key);
            if (untilStr != null) {
                try {
                    long until = Long.parseLong(untilStr);
                    if (System.currentTimeMillis() > until) {
                        return "Registration deadline has passed";
                    }
                } catch (NumberFormatException ignored) {
                    // If corrupted, fall back to allowing registration.
                }
            }
        }

        return EnrollmentData.register(studentId, sectionId);
    }

    public String dropSection(int studentId, String sectionId) {

        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "drop")) {
            return "Permission denied";
        }

        if (!SettingsData.isDropOpen()) {
            return "Drop deadline has passed";
        }

        // Per-section drop deadline: 5-minute window after section is assigned.
        if (sectionId != null) {
            String key = "dropUntil_" + sectionId;
            String untilStr = SettingsData.get(key);
            if (untilStr != null) {
                try {
                    long until = Long.parseLong(untilStr);
                    if (System.currentTimeMillis() > until) {
                        return "Drop deadline has passed";
                    }
                } catch (NumberFormatException ignored) {
                    // If corrupted, fall back to allowing drop.
                }
            }
        }

        return EnrollmentData.drop(studentId, sectionId);
    }

    public List<Enrollment> getEnrollments(int studentId) {
        List<Enrollment> out = new ArrayList<>();
        for (Enrollment e : EnrollmentData.listAll()) {
            if (e.getStudentId() == studentId && e.getStatus().equals("enrolled")) {
                out.add(e);
            }
        }
        return out;
    }

    public List<Section> viewTimetable(int studentId) {
        List<Section> res = new ArrayList<>();
        for (Enrollment e : getEnrollments(studentId)) {
            Section s = SectionData.getById(e.getSectionId());
            if (s != null) res.add(s);
        }
        return res;
    }

    public Map<String, Double> viewTranscript(int studentId) {
        Map<String, Double> transcript = new LinkedHashMap<>();

        for (Enrollment e : EnrollmentData.listAll()) {
            if (e.getStudentId() == studentId && e.getStatus().equals("enrolled")) {
                double finalScore = GradeData.computeFinalForEnrollment(e.getEnrollmentId());
                transcript.put(e.getSectionId(), finalScore);
            }
        }

        return transcript;
    }

    public String exportTranscriptCsv(int studentId, String filename) {
        User user = Session.getUser();
        if (!AccessControl.isAllowed(user, "exportTranscript")) {
            return "Permission denied";
        }

        Map<String, Double> transcript = viewTranscript(studentId);

        // Build lookup maps so we can include course code and course name
        // for each section in the exported CSV.
        Map<String, Section> sectionById = new HashMap<>();
        for (Section s : SectionData.listAll()) {
            sectionById.put(s.getSectionId(), s);
        }
        Map<String, Course> courseByCode = new HashMap<>();
        for (Course c : CourseData.listAll()) {
            courseByCode.put(c.getCode(), c);
        }

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"CourseCode", "CourseName", "Section", "FinalScore"});
        for (Map.Entry<String, Double> e : transcript.entrySet()) {
            String sectionId = e.getKey();
            double finalScore = e.getValue();

            String courseCode = "";
            String courseName = "";

            Section sec = sectionById.get(sectionId);
            if (sec != null) {
                courseCode = sec.getCourseCode();
                Course course = courseByCode.get(courseCode);
                if (course != null) {
                    courseName = course.getTitle();
                }
            }

            rows.add(new String[]{
                    courseCode,
                    courseName,
                    sectionId,
                    String.valueOf(finalScore)
            });
        }
        CSVUtil.writeAll(filename, rows);
        return null;
    }
}
