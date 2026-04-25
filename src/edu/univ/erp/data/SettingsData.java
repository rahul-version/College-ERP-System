package edu.univ.erp.data;

import java.util.*;

public class SettingsData {

    private static final String FILE = "settings.csv";
    private static final String HEADER = "key,value";

    public static void init() {
        CSVUtil.ensureFile(FILE, HEADER);
    }

    public static String get(String key) {
        for (String[] r : CSVUtil.readAll(FILE)) {
            if (r.length < 2) continue;
            if ("key".equals(r[0])) continue;
            if (key.equals(r[0])) return r[1];
        }
        return null;
    }

    public static void set(String key, String value) {
        List<String[]> rows = CSVUtil.readAll(FILE);
        boolean found = false;
        for (String[] r : rows) {
            if (r.length < 2) continue;
            if ("key".equals(r[0])) continue;
            if (key.equals(r[0])) {
                r[1] = value;
                found = true;
            }
        }
        if (!found) {
            rows.add(new String[]{key, value});
        }
        if (rows.isEmpty()) {
            rows.add(new String[]{"key", "value"});
            rows.add(new String[]{key, value});
        }
        CSVUtil.writeAll(FILE, rows);
    }

    public static boolean isMaintenanceOn() {
        String v = get("maintenance");
        return v != null && v.equalsIgnoreCase("true");
    }

    // Simple register/drop deadline flags. If the key is missing, treat as open (no deadline reached yet).
    public static boolean isRegisterOpen() {
        String v = get("registerOpen");
        return v == null || !v.equalsIgnoreCase("false");
    }

    public static boolean isDropOpen() {
        String v = get("dropOpen");
        return v == null || !v.equalsIgnoreCase("false");
    }
}
