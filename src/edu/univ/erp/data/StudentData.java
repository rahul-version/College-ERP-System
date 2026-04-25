package edu.univ.erp.data;

import edu.univ.erp.domain.Student;
import java.util.*;

public class StudentData {
    private static final String FILE = "students.csv";
    private static final String HEADER = "userId,rollNo,program,year";

    public static void init() { CSVUtil.ensureFile(FILE, HEADER); }

    public static List<Student> listAll() {
        List<Student> out = new ArrayList<>();
        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 4) continue;
            if (r[0].equals("userId")) continue;
            int userId = Integer.parseInt(r[0]);
            String rollNo = r[1];
            String program = r[2];
            int year = Integer.parseInt(r[3]);
            out.add(new Student(userId, rollNo, program, year));
        }
        return out;
    }

    public static void addStudent(Student s) {
        CSVUtil.ensureFile(FILE, HEADER);
        String line = String.join(",", String.valueOf(s.getUserId()), s.getRollNo(), s.getProgram(), String.valueOf(s.getYear()));
        CSVUtil.appendLine(FILE, line);
    }

    // Remove a student row by userId. Returns true if found and removed.
    public static boolean deleteStudent(int userId) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        List<String[]> out = new ArrayList<>();
        boolean removed = false;
        for (String[] r : rows) {
            if (r.length < 4) {
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
