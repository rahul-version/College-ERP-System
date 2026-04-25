package edu.univ.erp.auth;

import java.io.*;
import java.util.*;

/**
 * Simple CSV-backed store of disabled userIds.
 * File format: first line header "userId", then one user id per line.
 */
public class DisabledUsers {

    private static final String FILE = "disabled_users.csv";

    private static void ensureFile() {
        File f = new File(FILE);
        if (!f.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("userId");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Set<Integer> loadAll() {
        ensureFile();
        Set<Integer> out = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            // skip header
            br.readLine();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    int id = Integer.parseInt(line);
                    out.add(id);
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static boolean isDisabled(int userId) {
        return loadAll().contains(userId);
    }

    public static void disable(int userId) {
        Set<Integer> ids = loadAll();
        if (!ids.add(userId)) {
            return; // already disabled
        }
        writeAll(ids);
    }

    public static void enable(int userId) {
        Set<Integer> ids = loadAll();
        if (!ids.remove(userId)) {
            return; // not disabled
        }
        writeAll(ids);
    }

    private static void writeAll(Set<Integer> ids) {
        ensureFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("userId");
            for (Integer id : ids) {
                pw.println(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
