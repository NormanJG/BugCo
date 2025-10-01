package com.cab302.bugco.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTestUtil {

    /**
     * Reset DB for tests: fresh in-memory DB, schema init, clear tables,
     * and insert a default "testuser".
     */
    public static void resetForTests() {
        // Switch DB into in-memory mode
        Database.useInMemoryDatabase();


        // Clear tables and insert test user
        try (Connection conn = Database.get(); Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM progress");
            st.executeUpdate("DELETE FROM users");

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (username, password_hash) VALUES (?, ?)"
            )) {
                ps.setString(1, "testuser");
                ps.setString(2, "hash123"); // dummy password hash
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset in-memory DB for tests", e);
        }
    }
}
