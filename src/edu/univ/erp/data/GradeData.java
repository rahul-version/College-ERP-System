package edu.univ.erp.data;

import edu.univ.erp.domain.Grade;
import java.util.*;

public class GradeData {
    private static final String FILE = "grades.csv";
    private static final String HEADER = "enrollmentId,component,score,finalGrade";

    public static void init() { CSVUtil.ensureFile(FILE, HEADER); }

    public static List<Grade> listAll() {
        List<Grade> out = new ArrayList<>();
        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 4) continue;
            if (r[0].equals("enrollmentId")) continue;
            int enrollmentId = Integer.parseInt(r[0]);
            String component = r[1];
            double score = Double.parseDouble(r[2]);
            double finalG = Double.parseDouble(r[3]);
            out.add(new Grade(enrollmentId, component, score, finalG));
        }
        return out;
    }

    public static void addGrade(Grade g) {
        CSVUtil.ensureFile(FILE, HEADER);
        String line = String.join(",", String.valueOf(g.getEnrollmentId()), g.getComponent(),
                String.valueOf(g.getScore()), String.valueOf(g.getFinalGrade()));
        CSVUtil.appendLine(FILE, line);
    }

    public static List<Grade> findByEnrollment(int enrollmentId) {
        List<Grade> out = new ArrayList<>();
        for (Grade g : listAll()) if (g.getEnrollmentId() == enrollmentId) out.add(g);
        return out;
    }

    // Compute final grade as a simple total of quiz + midsem + endsem.
    // Expected maxima: quiz out of 20, midsem out of 30, endsem out of 50 (total out of 100).
    // If any component is missing, it is treated as 0.
    public static double computeFinalForEnrollment(int enrollmentId) {
        List<Grade> parts = findByEnrollment(enrollmentId);
        if (parts.isEmpty()) return 0.0;

        double quiz = 0.0;
        double midsem = 0.0;
        double endsem = 0.0;

        for (Grade g : parts) {
            String comp = g.getComponent().toLowerCase();
            if ("quiz".equals(comp)) {
                quiz = g.getScore();
            } else if ("midsem".equals(comp) || "midterm".equals(comp)) {
                midsem = g.getScore();
            } else if ("endsem".equals(comp) || "endterm".equals(comp)) {
                endsem = g.getScore();
            }
        }

        return quiz + midsem + endsem;
    }

    /**
     * Delete all grade rows whose enrollmentId is in the given list.
     * Returns true if any rows were removed.
     */
    public static boolean deleteByEnrollmentIds(java.util.List<Integer> enrollmentIds) {
        if (enrollmentIds == null || enrollmentIds.isEmpty()) return false;
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
            int eid = Integer.parseInt(r[0]);
            if (enrollmentIds.contains(eid)) {
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
