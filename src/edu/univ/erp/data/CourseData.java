package edu.univ.erp.data;

import edu.univ.erp.domain.Course;
import java.util.*;

public class CourseData {
    private static final String FILE = "courses.csv";
    private static final String HEADER = "code,title,credits";

    public static void init() {
        CSVUtil.ensureFile(FILE, HEADER);
    }

    public static List<Course> listAll() {
        List<Course> out = new ArrayList<>();
        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 3) continue;
            if (r[0].equals("code")) continue;
            String code = r[0];
            String title = r[1];
            int credits = Integer.parseInt(r[2]);
            out.add(new Course(code, title, credits));
        }
        return out;
    }

    public static void addCourse(Course c) {
        CSVUtil.ensureFile(FILE, HEADER);
        String line = String.join(",", c.getCode(), c.getTitle(), String.valueOf(c.getCredits()));
        CSVUtil.appendLine(FILE, line);
    }

    /**
     * Permanently delete a course by its code from courses.csv.
     * Returns true if a row was removed, false if not found.
     */
    public static boolean deleteCourse(String code) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        List<String[]> out = new ArrayList<>();
        boolean removed = false;
        for (String[] r : rows) {
            if (r.length < 3) {
                out.add(r);
                continue;
            }
            if ("code".equals(r[0])) {
                out.add(r);
                continue;
            }
            if (code.equalsIgnoreCase(r[0])) {
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
