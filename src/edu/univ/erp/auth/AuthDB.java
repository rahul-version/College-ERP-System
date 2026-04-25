package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import java.io.*;
import java.util.*;

public class AuthDB {

    private static final String AUTH_FILE = "auth.csv";

    public static List<User> loadUsers() {
        List<User> users = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(AUTH_FILE))) {
            String line;
            br.readLine(); // skip header

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                int userId = Integer.parseInt(parts[0]);
                String username = parts[1];
                String role = parts[2];
                String hash = parts[3];

                users.add(new User(userId, username, role, hash));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Delete a user row from auth.csv by numeric userId.
     * Keeps the header intact and rewrites the file without the matching row.
     */
    public static void deleteUserById(int userIdToDelete) {
        File f = new File(AUTH_FILE);
        if (!f.exists()) return;

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    // Always keep header as-is
                    lines.add(line);
                    first = false;
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(",");
                if (parts.length < 1) {
                    continue;
                }
                try {
                    int id = Integer.parseInt(parts[0]);
                    if (id == userIdToDelete) {
                        // skip this line (delete)
                        continue;
                    }
                } catch (NumberFormatException ignored) {
                    // If the first column is not numeric, just keep the line
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update the password hash for a given numeric userId in auth.csv.
     * Returns true if a row was updated.
     */
    public static boolean updatePasswordHash(int userIdToUpdate, String newHash) {
        File f = new File(AUTH_FILE);
        if (!f.exists()) return false;

        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) {
                    lines.add(line); // header
                    first = false;
                    continue;
                }
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split(",", -1);
                if (parts.length < 4) {
                    lines.add(line);
                    continue;
                }
                try {
                    int id = Integer.parseInt(parts[0]);
                    if (id == userIdToUpdate) {
                        parts[3] = newHash;
                        line = String.join(",", parts);
                        updated = true;
                    }
                } catch (NumberFormatException ignored) {
                    // keep line as-is if the first column is not numeric
                }
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!updated) return false;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, false))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
