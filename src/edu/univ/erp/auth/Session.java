package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

public class Session {
    private static User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }
}