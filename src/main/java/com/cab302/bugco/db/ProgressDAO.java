package com.cab302.bugco.db;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProgressDAO {

    // CREATE or UPDATE
    public static void saveProgress(String username, String difficulty, int questionId, String answer) {
        String sql = """
            INSERT INTO progress (username, difficulty, question_id, answer)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(username, difficulty, question_id)
            DO UPDATE SET answer = excluded.answer
            """;

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, difficulty);
            ps.setInt(3, questionId);
            ps.setString(4, answer);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save progress", e);
        }
    }

    // READ – full answers per difficulty
    public static Map<String, Map<Integer, String>> loadProgress(String username) {
        Map<String, Map<Integer, String>> result = new HashMap<>();
        String sql = "SELECT difficulty, question_id, answer FROM progress WHERE username = ?";

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String diff = rs.getString("difficulty");
                    int qId = rs.getInt("question_id");
                    String ans = rs.getString("answer");
                    result.computeIfAbsent(diff, k -> new HashMap<>()).put(qId, ans);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load progress", e);
        }

        return result;
    }


    public static void resetProgress(String username, String difficulty) {
        String sql = "DELETE FROM progress WHERE username = ? AND difficulty = ?";
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, difficulty);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // READ – just solved IDs per difficulty
    public static Map<String, Set<Integer>> loadSolvedQuestions(String username) {
        Map<String, Set<Integer>> solved = new HashMap<>();
        String sql = "SELECT difficulty, question_id FROM progress WHERE username = ?";

        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String diff = rs.getString("difficulty");
                    int qId = rs.getInt("question_id");
                    solved.computeIfAbsent(diff, k -> new HashSet<>()).add(qId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load solved questions", e);
        }

        return solved;
    }
}