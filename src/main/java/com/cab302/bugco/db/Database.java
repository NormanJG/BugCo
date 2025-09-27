package com.cab302.bugco.db;

import java.sql.*;
import java.nio.file.*;
import java.util.List;
import java.util.ArrayList;
import com.cab302.bugco.Players;

public final class Database {

    private static String URL = "jdbc:sqlite:bugco.db";


    private static Connection sharedConnection;

    private Database() {}

    public static Connection get() throws SQLException {
        if (sharedConnection != null && !sharedConnection.isClosed()) {
            return sharedConnection;
        }
        System.out.println("DB.get() -> " + URL);
        return DriverManager.getConnection(URL);
    }

    public static void useInMemoryDatabase() {
        URL = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
            }
            sharedConnection = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open in-memory DB", e);
        }
        init();
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

        try (Statement st = get().createStatement()) {
            st.executeUpdate(ddlUsers);
            st.executeUpdate(ddlProgress);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to init DB", e);
        }


        if (URL.startsWith("jdbc:sqlite:bugco.db")) {
            Path dbPath = Paths.get("bugco.db").toAbsolutePath();
            if (Files.exists(dbPath)) {
                System.out.println("DB: connected to existing database at " + dbPath);
            } else {
                System.out.println("DB: created new database at " + dbPath);
            }
        } else {
            System.out.println("DB: using in-memory database (" + URL + ")");
        }
    }


    public static List<Players> getAllPlayers() {
        List<Players> result = new ArrayList<>();
        String sql = """
            SELECT p.username, p.achievement
            FROM progress p
            INNER JOIN (
                SELECT username, MAX(id) AS latest_id
                FROM progress
                GROUP BY username
            ) grouped ON p.username = grouped.username AND p.id = grouped.latest_id
        """;

        try (Statement stmt = get().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
        try (PreparedStatement pstmt = get().prepareStatement(sql)) {
            pstmt.setString(1, newAchievement);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
