package edu.univ.erp.auth;

import edu.univ.erp.domain.User;
import java.util.List;

public class AuthService {

    public User login(String username, String password) {
        List<User> users = AuthDB.loadUsers();

        for (User u : users) {
            if (u.getUsername().equals(username)) {
                if (!PasswordHasher.verifyPassword(password, u.getPasswordHash())) {
                    return null; // wrong password
                }
                // Check if account is disabled
                if (DisabledUsers.isDisabled(u.getUserId())) {
                    return null; // treat as failed login for disabled accounts
                }
                return u; // success
            }
        }
        return null; // user not found
    }

    /**
     * Change password for a given username, verifying the old password first.
     * Returns null on success or an error message on failure.
     */
    public String changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required";
        }
        if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return "Old and new passwords are required";
        }

        List<User> users = AuthDB.loadUsers();
        User target = null;
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                target = u;
                break;
            }
        }
        if (target == null) {
            return "User not found";
        }
        if (!PasswordHasher.verifyPassword(oldPassword, target.getPasswordHash())) {
            return "Old password is incorrect";
        }

        String newHash = PasswordHasher.hashPassword(newPassword);
        boolean ok = AuthDB.updatePasswordHash(target.getUserId(), newHash);
        if (!ok) {
            return "Failed to update password";
        }
        return null;
    }
}
