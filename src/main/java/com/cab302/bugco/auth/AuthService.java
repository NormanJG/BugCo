package com.cab302.bugco.auth;

import com.cab302.bugco.db.UserDao;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public void register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username required");
        }
        if (!isPasswordStrong(password)) {
            throw new IllegalArgumentException(
                    "Password must be at least 12 chars, include upper, lower, number, and special char");
        }
        if (userDao.usernameExists(username)) {
            throw new IllegalStateException("Username already exists");
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        userDao.insertUser(username, hash);
    }

    public boolean authenticate(String username, String password) {
        String stored = userDao.getHashForUser(username);
        return stored != null && BCrypt.checkpw(password, stored);
    }

    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*\\d.*")) return false;
        if (!password.matches(".*[^a-zA-Z0-9].*")) return false;
        return true;
    }
}
