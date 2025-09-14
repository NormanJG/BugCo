package com.cab302.bugco.db;

import java.sql.*;

public class UserDao {

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

    public void insertUser(String username, String passwordHash) {
        String sql = "INSERT INTO users(username, password_hash) VALUES(?, ?)";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                throw new IllegalStateException("Username already exists");
            }
            throw new RuntimeException(e);
        }
    }

    public String getHashForUser(String username) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

