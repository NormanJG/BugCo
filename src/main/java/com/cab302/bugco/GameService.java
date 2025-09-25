package com.cab302.bugco;

import com.cab302.bugco.db.Database;
import com.cab302.bugco.db.UserDao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class GameService {

    // singleton
    private static final GameService ME = new GameService();
    public static GameService getInstance() { return ME; }

    private final UserDao users = new UserDao();

    private GameService() {
        makeTablesIfMissing();
        ensureScoresHasDifficulty();
    }

    // make tables
    private void makeTablesIfMissing() {
        String makeChallenges = """
            CREATE TABLE IF NOT EXISTS challenges (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              difficulty TEXT NOT NULL,
              ordinal    INTEGER NOT NULL,
              title      TEXT NOT NULL,
              prompt     TEXT NOT NULL,
              buggy_code TEXT NOT NULL,
              correct_answer TEXT NOT NULL,
              base_points INTEGER NOT NULL,
              UNIQUE(difficulty, ordinal)
            )
            """;

        String makeSubmissions = """
            CREATE TABLE IF NOT EXISTS submissions (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              user_id INTEGER NOT NULL,
              challenge_id INTEGER NOT NULL,
              answer TEXT NOT NULL,
              is_correct INTEGER NOT NULL,
              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String makeScores = """
            CREATE TABLE IF NOT EXISTS scores (
              user_id INTEGER NOT NULL,
              difficulty TEXT NOT NULL,
              total_points INTEGER NOT NULL,
              PRIMARY KEY (user_id, difficulty)
            )
            """;

        try (Connection c = Database.get(); Statement st = c.createStatement()) {
            st.executeUpdate(makeChallenges);
            st.executeUpdate(makeSubmissions);
            st.executeUpdate(makeScores);
        } catch (SQLException e) {
            throw new RuntimeException("MAKE TABLES FAILED", e);
        }
    }

    // add difficulty col
    private void ensureScoresHasDifficulty() {
        String mySql = "PRAGMA table_info(scores)";
        boolean hasDiff = false;

        try (var c = Database.get(); var st = c.createStatement(); var rs = st.executeQuery(mySql)) {
            while (rs.next()) {
                if ("difficulty".equalsIgnoreCase(rs.getString("name"))) { hasDiff = true; break; }
            }
        } catch (SQLException ignored) { }

        if (hasDiff) return;

        try (var c = Database.get()) {
            c.setAutoCommit(false);
            try (var st = c.createStatement()) {
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS scores_new (
                      user_id INTEGER NOT NULL,
                      difficulty TEXT NOT NULL,
                      total_points INTEGER NOT NULL,
                      PRIMARY KEY (user_id, difficulty)
                    )
                    """);
                st.executeUpdate("""
                    INSERT INTO scores_new(user_id, difficulty, total_points)
                    SELECT user_id, 'EASY', total_points FROM scores
                    """);
                st.executeUpdate("DROP TABLE scores");
                st.executeUpdate("ALTER TABLE scores_new RENAME TO scores");
            }
            c.commit();
        } catch (SQLException e) {
            throw new RuntimeException("SCORES MIGRATION FAILED", e);
        }
    }

    // seed easy set
    public void seedEasyIfMissing() {
        String mySql = "SELECT COUNT(*) FROM challenges WHERE difficulty=?";
        try (var c = Database.get(); var ps = c.prepareStatement(mySql)) {
            ps.setString(1, Difficulty.EASY.name());
            try (var rs = ps.executeQuery()) {
                int howMany = rs.next() ? rs.getInt(1) : 0;
                if (howMany >= 9) return;
            }
        } catch (SQLException e) { throw new RuntimeException("SEED CHECK FAILED", e); }

        try (var c = Database.get(); var ps = c.prepareStatement("DELETE FROM challenges WHERE difficulty=?")) {
            ps.setString(1, Difficulty.EASY.name());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("CLEAR EASY FAILED", e); }

        String insertSql = """
            INSERT INTO challenges(difficulty, ordinal, title, prompt, buggy_code, correct_answer, base_points)
            VALUES(?,?,?,?,?,?,?)
            """;
        try (var c = Database.get(); var ps = c.prepareStatement(insertSql)) {
            for (Challenge ch : buildEasySet()) {
                ps.setString(1, ch.difficulty().name());
                ps.setInt(2, ch.ordinal());
                ps.setString(3, ch.title());
                ps.setString(4, ch.prompt());
                ps.setString(5, ch.buggyCode());
                ps.setString(6, ch.correctAnswer());
                ps.setInt(7, ch.basePoints());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) { throw new RuntimeException("SEED FAILED", e); }
    }

    // get one challenge
    public Challenge getChallenge(Difficulty d, int number) {
        String mySql = """
            SELECT id, difficulty, ordinal, title, prompt, buggy_code, correct_answer, base_points
            FROM challenges
            WHERE difficulty=? AND ordinal=?
            """;
        try (var c = Database.get(); var ps = c.prepareStatement(mySql)) {
            ps.setString(1, d.name());
            ps.setInt(2, number);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Challenge(
                        rs.getLong("id"),
                        Difficulty.valueOf(rs.getString("difficulty")),
                        rs.getInt("ordinal"),
                        rs.getString("title"),
                        rs.getString("prompt"),
                        rs.getString("buggy_code"),
                        rs.getString("correct_answer"),
                        rs.getInt("base_points")
                );
            }
        } catch (SQLException e) { throw new RuntimeException("LOAD CHALLENGE FAILED", e); }
    }

    // submit answer
    public String submitAndMark(String theUserName, Challenge theChallenge, String theAnswer) {
        if (theChallenge == null) return "NO QUESTION.";
        boolean isRight = theAnswer != null && theAnswer.contains(theChallenge.correctAnswer());

        long theUserId = users.findByUsername(theUserName).map(UserDao.UserRow::id).orElse(-1L);
        if (theUserId <= 0) return "NO USER.";

        String mySql = """
            SELECT 1 FROM submissions
            WHERE user_id=? AND challenge_id=? AND is_correct=1
            LIMIT 1
            """;
        try (var c = Database.get(); var ps = c.prepareStatement(mySql)) {
            ps.setLong(1, theUserId);
            ps.setLong(2, theChallenge.id());
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    saveSubmission(theUserId, theChallenge.id(), theAnswer, isRight);
                    return "ALREADY SOLVED BEFORE.";
                }
            }
        } catch (SQLException e) { throw new RuntimeException("CHECK SOLVED FAILED", e); }

        saveSubmission(theUserId, theChallenge.id(), theAnswer, isRight);

        if (isRight) {
            addPoints(theUserId, theChallenge.basePoints(), theChallenge.difficulty().name());
            return "✅ CORRECT! +" + theChallenge.basePoints() + " PTS";
        } else {
            return "❌ NOT CORRECT. TIP: INCLUDE THIS: " + theChallenge.correctAnswer();
        }
    }

    private void saveSubmission(long userId, long challengeId, String ans, boolean right) {
        String forInsert = "INSERT INTO submissions(user_id, challenge_id, answer, is_correct) VALUES(?,?,?,?)";
        try (var c = Database.get(); var ps = c.prepareStatement(forInsert)) {
            ps.setLong(1, userId);
            ps.setLong(2, challengeId);
            ps.setString(3, ans == null ? "" : ans);
            ps.setInt(4, right ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("SAVE SUBMISSION FAILED", e); }
    }

    // points
    public int getTotalPoints(String theUserName) {
        long theUserId = users.findByUsername(theUserName).map(UserDao.UserRow::id).orElse(-1L);
        if (theUserId <= 0) return 0;

        String mySql = "SELECT total_points FROM scores WHERE user_id=? AND difficulty='EASY'";
        try (var c = Database.get(); var ps = c.prepareStatement(mySql)) {
            ps.setLong(1, theUserId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException("READ POINTS FAILED", e); }
    }

    // add / upsert points
    public void addPoints(long theUserId, int howMany, String whichDiff) {
        String forUpdate = """
            INSERT INTO scores(user_id, difficulty, total_points)
            VALUES (?,?,?)
            ON CONFLICT(user_id, difficulty)
            DO UPDATE SET total_points = total_points + excluded.total_points
            """;
        try (var c = Database.get(); var ps = c.prepareStatement(forUpdate)) {
            ps.setLong(1, theUserId);
            ps.setString(2, whichDiff);
            ps.setInt(3, howMany);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("ADD POINTS FAILED", e); }
    }

    // reset easy
    public void resetPoints(String theUserName) {
        long theUserId = users.findByUsername(theUserName).map(UserDao.UserRow::id).orElse(-1L);
        if (theUserId <= 0) return;

        try (var c = Database.get()) {
            c.setAutoCommit(false);

            try (var ps = c.prepareStatement("DELETE FROM scores WHERE user_id=? AND difficulty='EASY'")) {
                ps.setLong(1, theUserId);
                ps.executeUpdate();
            }

            List<Long> ids = new ArrayList<>();
            try (var ps = c.prepareStatement("SELECT id FROM challenges WHERE difficulty='EASY'");
                 var rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getLong(1));
            }

            try (var ps = c.prepareStatement("DELETE FROM submissions WHERE user_id=? AND challenge_id=?")) {
                for (long cid : ids) {
                    ps.setLong(1, theUserId);
                    ps.setLong(2, cid);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            c.commit();
        } catch (SQLException e) { throw new RuntimeException("RESET FAILED", e); }
    }

    public boolean hasSolvedBefore(String theUserName, Challenge ch) {
        long uid = users.findByUsername(theUserName)
                .map(UserDao.UserRow::id)
                .orElse(-1L);
        if (uid <= 0) return false;

        String sql = "SELECT 1 FROM submissions WHERE user_id=? AND challenge_id=? AND is_correct=1 LIMIT 1";
        try (var c = Database.get(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, uid);
            ps.setLong(2, ch.id());
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }


    // leaderboards
    public List<String> getLeaderboardLines(String whichDiff, int limit) {
        String mySql = """
            SELECT u.username, s.total_points
            FROM scores s
            JOIN users u ON u.id = s.user_id
            WHERE s.difficulty = ?
            ORDER BY s.total_points DESC, u.username ASC
            LIMIT ?
            """;
        List<String> out = new ArrayList<>();
        try (var c = Database.get(); var ps = c.prepareStatement(mySql)) {
            ps.setString(1, whichDiff);
            ps.setInt(2, Math.max(1, limit));
            try (var rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String who = rs.getString("username");
                    int pts = rs.getInt("total_points");
                    out.add(" > " + rank + "  " + who + "  (" + pts + " pts)");
                    rank++;
                }
            }
        } catch (SQLException e) { throw new RuntimeException("LEADERBOARD FAILED", e); }
        return out;
    }

    // (default EASY)
    public List<String> getLeaderboardLines(int limit) {
        return getLeaderboardLines("EASY", limit);
    }

    public List<String> getLeaderboardEasy(int limit)   { return getLeaderboardLines("EASY",   limit); }
    public List<String> getLeaderboardMedium(int limit) { return getLeaderboardLines("MEDIUM", limit); }
    public List<String> getLeaderboardHard(int limit)   { return getLeaderboardLines("HARD",   limit); }

    // build easy set
    private static List<Challenge> buildEasySet() {
        List<Challenge> myList = new ArrayList<>();

        myList.add(new Challenge(0, Difficulty.EASY, 1, "ARRAY INDEX",
                "LOOP RUNS ONE STEP TOO FAR.",
                """
                function printItems(arr) {
                  for (let i = 0; i <= arr.length; i++) {
                    console.log(arr[i]);
                  }
                }
                """,
                "i < arr.length", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 2, "EQUALITY VS ASSIGNMENT",
                "USE == NOT = IN CONDITION.",
                """
                int x = 5;
                if (x = 10) {
                  System.out.println("BugCo");
                }
                """,
                "if (x == 10)", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 3, "LAST ELEMENT",
                "RETURN LAST ITEM (NOT OUT OF RANGE).",
                """
                function last(arr) {
                  return arr[arr.length];
                }
                """,
                "return arr[arr.length - 1]", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 4, "NULL GUARD",
                "CHECK FOR NULL BEFORE .LENGTH.",
                """
                public int lengthOrZero(String s) {
                  return s.length();
                }
                """,
                "if (s == null) return 0;", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 5, "DIVIDE BY ZERO",
                "AVOID CRASH WHEN b IS ZERO.",
                """
                public int ratio(int a, int b) {
                  return a / b;
                }
                """,
                "if (b == 0) return 0;", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 6, "SWAP VALUES",
                "SWAP a AND b CORRECTLY.",
                """
                int a = 10, b = 20;
                a = b;
                b = a;
                """,
                "int temp = a; a = b; b = temp;", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 7, "CASE CHECK",
                "IGNORE CASE WHEN COMPARE.",
                """
                String cmd = "start";
                if (cmd.equals("START")) {
                  run();
                }
                """,
                "equalsIgnoreCase", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 8, "OFF BY ONE",
                "USE 0..N-1 INDEX IN LOOP.",
                """
                for (int i = 1; i <= arr.length; i++) {
                  System.out.println(arr[i]);
                }
                """,
                "for (int i = 0; i < arr.length; i++)", 10));

        myList.add(new Challenge(0, Difficulty.EASY, 9, "STRING BUILDER",
                "AVOID CONCAT IN LOOP, USE BUILDER.",
                """
                String s = "";
                for (int i = 0; i < n; i++) {
                  s = s + i;
                }
                """,
                "new StringBuilder", 10));

        return myList;
    }
}
