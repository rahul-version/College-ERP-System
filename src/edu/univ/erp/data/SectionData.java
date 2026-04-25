package edu.univ.erp.data;

import edu.univ.erp.domain.Section;
import java.util.*;

public class SectionData {
    private static final String FILE = "sections.csv";
    private static final String HEADER = "sectionId,courseCode,instructorId,dayTime,room,capacity,semester,year";

    public static void init() { CSVUtil.ensureFile(FILE, HEADER); }

    public static List<Section> listAll() {
        List<Section> out = new ArrayList<>();
        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 8) continue;
            if (r[0].equals("sectionId")) continue;
            String id = r[0];
            String courseCode = r[1];
            int instr = Integer.parseInt(r[2]);
            String dayTime = r[3];
            String room = r[4];
            int capacity = Integer.parseInt(r[5]);
            String semester = r[6];
            int year = Integer.parseInt(r[7]);
            out.add(new Section(id, courseCode, instr, dayTime, room, capacity, semester, year));
        }
        return out;
    }

    public static Section getById(String sectionId) {
        for (Section s : listAll()) if (s.getSectionId().equals(sectionId)) return s;
        return null;
    }

    public static void addSection(Section s) {
        CSVUtil.ensureFile(FILE, HEADER);
        String line = String.join(",", s.getSectionId(), s.getCourseCode(), String.valueOf(s.getInstructorId()),
                s.getDayTime(), s.getRoom(), String.valueOf(s.getCapacity()), s.getSemester(), String.valueOf(s.getYear()));
        CSVUtil.appendLine(FILE, line);
    }

    public static boolean updateInstructor(String sectionId, int instructorId) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        boolean found = false;
        for (String[] r : rows) {
            if (r.length < 8) continue;
            if ("sectionId".equals(r[0])) continue;
            if (sectionId.equals(r[0])) {
                r[2] = String.valueOf(instructorId);
                found = true;
            }
        }
        if (!found) return false;
        CSVUtil.writeAll(FILE, rows);
        return true;
    }

    // Delete a section row by sectionId. Returns true if found and removed.
    public static boolean deleteSection(String sectionId) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        List<String[]> out = new ArrayList<>();
        boolean removed = false;
        for (String[] r : rows) {
            if (r.length < 8) {
                out.add(r);
                continue;
            }
            if ("sectionId".equals(r[0])) {
                out.add(r);
                continue;
            }
            if (sectionId.equals(r[0])) {
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
