package edu.univ.erp.data;

import edu.univ.erp.domain.Instructor;
import java.util.*;

public class InstructorData {
    private static final String FILE = "instructors.csv";
    private static final String HEADER = "userId,department";

    public static void init() { CSVUtil.ensureFile(FILE, HEADER); }

    public static List<Instructor> listAll() {
        List<Instructor> out = new ArrayList<>();
        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 2) continue;
            if (r[0].equals("userId")) continue;
            int userId = Integer.parseInt(r[0]);
            String dept = r[1];
            out.add(new Instructor(userId, dept));
        }
        return out;
    }

    public static void addInstructor(Instructor i) {
        CSVUtil.ensureFile(FILE, HEADER);
        String line = String.join(",", String.valueOf(i.getUserId()), i.getDepartment());
        CSVUtil.appendLine(FILE, line);
    }

    // Remove an instructor row by userId. Returns true if found and removed.
    public static boolean deleteInstructor(int userId) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        List<String[]> out = new ArrayList<>();
        boolean removed = false;
        for (String[] r : rows) {
            if (r.length < 2) {
                out.add(r);
                continue;
            }
            if ("userId".equals(r[0])) {
                out.add(r);
                continue;
            }
            int uid = Integer.parseInt(r[0]);
            if (uid == userId) {
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
