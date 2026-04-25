package edu.univ.erp.data;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import java.util.*;

public class EnrollmentData {

    private static final String FILE = "enrollments.csv";
    private static final String HEADER = "enrollmentId,studentId,sectionId,status";

    public static void init() {
        CSVUtil.ensureFile(FILE, HEADER);
    }

    // Read all enrollments from CSV
    public static List<Enrollment> listAll() {
        List<Enrollment> out = new ArrayList<>();

        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 4) continue;
            if (r[0].equals("enrollmentId")) continue;

            int enrollmentId = Integer.parseInt(r[0]);
            int studentId = Integer.parseInt(r[1]);
            String sectionId = r[2];
            String status = r[3];

            out.add(new Enrollment(enrollmentId, studentId, sectionId, status));
        }
        return out;
    }

    public static Enrollment getById(int enrollmentId) {
        for (Enrollment e : listAll()) {
            if (e.getEnrollmentId() == enrollmentId) return e;
        }
        return null;
    }

    // Check if already enrolled
    public static boolean isEnrolled(int studentId, String sectionId) {
        for (Enrollment e : listAll()) {
            if (e.getStudentId() == studentId &&
                e.getSectionId().equals(sectionId) &&
                e.getStatus().equals("enrolled")) {
                return true;
            }
        }
        return false;
    }

    // Count active enrollments in a section
    public static int countEnrolled(String sectionId) {
        int count = 0;
        for (Enrollment e : listAll()) {
            if (e.getSectionId().equals(sectionId) &&
                e.getStatus().equals("enrolled")) {
                count++;
            }
        }
        return count;
    }

    // REGISTER: returns null if OK or error message
    public static String register(int studentId, String sectionId) {

        Section sec = SectionData.getById(sectionId);
        if (sec == null) return "Section not found";

        if (isEnrolled(studentId, sectionId)) {
            return "Already enrolled";
        }

        // Prevent duplicate registration in another section of the same course
        String thisCourseCode = sec.getCourseCode();
        for (Enrollment e : listAll()) {
            if (e.getStudentId() == studentId && "enrolled".equals(e.getStatus())) {
                Section other = SectionData.getById(e.getSectionId());
                if (other != null && thisCourseCode.equals(other.getCourseCode())) {
                    return "Already registered in this course";
                }
            }
        }

        int enrolledCount = countEnrolled(sectionId);
        if (enrolledCount >= sec.getCapacity()) {
            return "Section full";
        }

        // Auto-generate new enrollmentId
        int newId = 1;
        for (Enrollment e : listAll()) {
            if (e.getEnrollmentId() >= newId) {
                newId = e.getEnrollmentId() + 1;
            }
        }

        CSVUtil.ensureFile(FILE, HEADER);

        String line = newId + "," +
                      studentId + "," +
                      sectionId + "," +
                      "enrolled";

        CSVUtil.appendLine(FILE, line);
        return null; // success
    }

    // DROP: set status to dropped
    public static String drop(int studentId, String sectionId) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        boolean found = false;

        for (String[] r : rows) {
            if (r.length < 4) continue;
            if (r[0].equals("enrollmentId")) continue;

            int sid = Integer.parseInt(r[1]);
            String sec = r[2];
            String status = r[3];

            if (sid == studentId && sec.equals(sectionId) && status.equals("enrolled")) {
                r[3] = "dropped"; // update status
                found = true;
            }
        }

        if (!found) return "Enrollment not found";

        CSVUtil.writeAll(FILE, rows);
        return null; // success
    }

    /**
     * Delete all enrollment rows whose sectionId is in the given list.
     * Returns true if any rows were removed.
     */
    public static boolean deleteBySectionIds(java.util.List<String> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) return false;
        List<String[]> rows = CSVUtil.readAll(FILE);
        List<String[]> out = new ArrayList<>();
        boolean removed = false;
        for (String[] r : rows) {
            if (r.length < 4) {
                out.add(r);
                continue;
            }
            if ("enrollmentId".equals(r[0])) {
                out.add(r);
                continue;
            }
            String sec = r[2];
            if (sectionIds.contains(sec)) {
                removed = true;
                continue; // skip this row
            }
            out.add(r);
        }
        if (removed) {
            CSVUtil.writeAll(FILE, out);
        }
        return removed;
    }
}
