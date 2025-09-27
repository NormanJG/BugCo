// src/main/java/com/cab302/bugco/db/UserDao.java
package com.cab302.bugco.db;

import java.sql.*;

public class UserDao {

    public long insertUser(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1L;
            }
        } catch (SQLException e) {
            if (isUniqueViolation(e)) throw new IllegalStateException("Username already exists", e);
            throw new RuntimeException(e);
        }
    }


    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /** Needed by AuthService.authenticate() */
    public String getHashForUser(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /** Used by HomeController.onChangeUsername() */
    public boolean updateUsernameByUsername(String oldName, String newName) {
        String sql = "UPDATE users SET username = ? WHERE username = ?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, oldName);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            if (isUniqueViolation(e)) throw new IllegalStateException("Username already exists", e);
            throw new RuntimeException(e);
        }
    }

    public boolean updatePasswordHash(long id, String newHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setLong(2, id);
            int n = ps.executeUpdate();
            return n == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updatePasswordHashByUsername(String username, String newHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (var c = Database.get(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- helpers ---
    private static boolean isUniqueViolation(SQLException e) {
        String m = e.getMessage();
        return m != null && m.toLowerCase().contains("unique");
    }

    private static UserRow map(ResultSet rs) throws SQLException {
        return new UserRow(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password_hash")
        );
    }

    // keep simple; record or class both fine
    public static final class UserRow {
        public final long id;
        public final String username;
        public final String passwordHash;

        public UserRow(long id, String username, String passwordHash) {
            this.id = id;
            this.username = username;
            this.passwordHash = passwordHash;
        }
    }
}
