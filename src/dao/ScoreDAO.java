package dao;

import db.DBConnection;
import model.Score;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for the {@code scores} table.
 * Mirrors the completedQuizzes logic embedded in the Mongoose User model.
 */
public class ScoreDAO {

    // ── Create ────────────────────────────────────────────────────────

    /**
     * Saves a quiz result.
     * Equivalent to: user.completedQuizzes.push({ quiz, score, completedAt })
     */
    public void saveScore(Score score) throws SQLException {
        String sql = "INSERT INTO scores (user_id, quiz_id, score, correct_answers, total_questions) " +
                     "VALUES (?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, score.getUserId());
            ps.setInt(2, score.getQuizId());
            ps.setInt(3, score.getScore());
            ps.setInt(4, score.getCorrectAnswers());
            ps.setInt(5, score.getTotalQuestions());
            ps.executeUpdate();
        }
    }

    // ── Read ──────────────────────────────────────────────────────────

    /**
     * Returns all scores for a user, newest first.
     * Joins with quizzes to populate quizTitle.
     */
    public List<Score> getScoresByUser(int userId) throws SQLException {
        List<Score> scores = new ArrayList<>();
        String sql = "SELECT s.*, q.title AS quiz_title " +
                     "FROM scores s JOIN quizzes q ON s.quiz_id = q.id " +
                     "WHERE s.user_id = ? ORDER BY s.completed_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) scores.add(mapRow(rs, true));
            }
        }
        return scores;
    }

    /**
     * Returns the global leaderboard:
     * users ordered by their total accumulated points (SUM of scores).
     * Used by LeaderboardFrame.
     */
    public List<Score> getGlobalLeaderboard(int limit) throws SQLException {
        List<Score> entries = new ArrayList<>();
        // Only select what is needed — no correct_answers/total_questions in this query
        String sql = "SELECT u.id AS user_id, u.points AS score, u.name AS user_name " +
                     "FROM users u ORDER BY u.points DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Score s = new Score();
                    s.setUserId(rs.getInt("user_id"));
                    s.setScore(rs.getInt("score"));
                    s.setUserName(rs.getString("user_name"));
                    entries.add(s);
                }
            }
        }
        return entries;
    }

    /** Returns the best score a user achieved for a specific quiz. */
    public Score getBestScore(int userId, int quizId) throws SQLException {
        String sql = "SELECT s.*, q.title AS quiz_title " +
                     "FROM scores s JOIN quizzes q ON s.quiz_id = q.id " +
                     "WHERE s.user_id = ? AND s.quiz_id = ? " +
                     "ORDER BY s.score DESC LIMIT 1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs, true);
            }
        }
        return null;
    }

    /** Count how many quizzes a user has completed. */
    public int countCompletedQuizzes(int userId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT quiz_id) FROM scores WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // ── Row Mapper ────────────────────────────────────────────────────

    private Score mapRow(ResultSet rs, boolean withJoin) throws SQLException {
        Score s = new Score();
        s.setId(rs.getInt("id"));         // fixed: use the score row's own primary key
        s.setUserId(rs.getInt("user_id"));
        s.setQuizId(rs.getInt("quiz_id"));
        s.setScore(rs.getInt("score"));
        s.setCorrectAnswers(rs.getInt("correct_answers"));
        s.setTotalQuestions(rs.getInt("total_questions"));
        s.setCompletedAt(rs.getTimestamp("completed_at"));
        if (withJoin) s.setQuizTitle(rs.getString("quiz_title"));
        return s;
    }
}
