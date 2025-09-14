package com.cab302.bugco.db;

import java.sql.*;
import java.nio.file.*;

public final class Database {
    private static final String URL = "jdbc:sqlite:bugco.db";

    private Database() {}

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        String ddl = """
            CREATE TABLE IF NOT EXISTS users (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              username TEXT NOT NULL UNIQUE,
              password_hash TEXT NOT NULL,
              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        Path dbPath = Paths.get("bugco.db").toAbsolutePath();
        boolean existedBefore = Files.exists(dbPath);

        try (Connection c = get(); Statement st = c.createStatement()) {
            st.executeUpdate(ddl);
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
}
