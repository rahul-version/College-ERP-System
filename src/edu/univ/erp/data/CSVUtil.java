package edu.univ.erp.data;

import java.io.*;
import java.util.*;

public class CSVUtil {

    public static List<String[]> readAll(String filename) {
        List<String[]> rows = new ArrayList<>();
        File f = new File(filename);
        if (!f.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                // simple CSV split (no quoted fields) - adequate for project
                String[] parts = line.split(",", -1);
                rows.add(parts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows;
    }

    public static void writeAll(String filename, List<String[]> rows) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, false))) {
            for (String[] row : rows) {
                bw.write(String.join(",", row));
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void appendLine(String filename, String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            bw.write(line);
            bw.newLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // convenience to ensure file exists and optionally write header
    public static void ensureFile(String filename, String header) {
        File f = new File(filename);
        if (!f.exists()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                if (header != null && !header.isEmpty()) bw.write(header + System.lineSeparator());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}