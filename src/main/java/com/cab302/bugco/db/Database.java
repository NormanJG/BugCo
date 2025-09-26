package com.cab302.bugco.db;

import java.sql.*;
import java.nio.file.*;import java.util.List;
import java.util.ArrayList;
import com.cab302.bugco.Players;


public final class Database {
    private static final String URL = "jdbc:sqlite:bugco.db";


    private Database() {
    }


    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }


    public static void init() {
        String ddlUsers = """
                CREATE TABLE IF NOT EXISTS users (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  username TEXT NOT NULL UNIQUE,
                  password_hash TEXT NOT NULL,
                  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;

        String ddlProgress = """
                CREATE TABLE IF NOT EXISTS progress (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    difficulty TEXT NOT NULL,
                    question_id INTEGER NOT NULL,
                    answer TEXT,
                    achievement TEXT,
                    UNIQUE(username, difficulty, question_id),
                    FOREIGN KEY(username) REFERENCES users(username)
                )
                """;

        Path dbPath = Paths.get("bugco.db").toAbsolutePath();
        boolean existedBefore = Files.exists(dbPath);

        try (Connection c = get(); Statement st = c.createStatement()) {
            st.executeUpdate(ddlUsers);
            st.executeUpdate(ddlProgress); // ✅ now this works
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init DB", e);
        }

        boolean existsAfter = Files.exists(dbPath);
        if (!existedBefore && existsAfter) {
            System.out.println("DB: created new database at " + dbPath);
        } else if (existsAfter) {
            System.out.println("DB: connected to existing database at " + dbPath);
        } else {
            System.out.println("DB: WARNING – database file not found at " + dbPath);
        }
    }


    public static List<Players> getAllPlayers() {
        List<Players> result = new ArrayList<>();
        String sql = "SELECT username, achievement FROM progress"; // Or your table storing achievements
        try (Connection conn = get(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new Players(rs.getString("username"), rs.getString("achievement")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static void updatePlayerAchievement(String username, String newAchievement) {
        String sql = "UPDATE progress SET achievement = ? WHERE username = ?";
        try (Connection conn = get(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newAchievement);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}








