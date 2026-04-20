package dao;

import db.DBConnection;
import model.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for {@code quizzes} and {@code questions} tables.
 * Converted from MongoDB Quiz model + Mongoose queries in quizController.js.
 */
public class QuizDAO {

    // ── Quiz CRUD ─────────────────────────────────────────────────────

    /**
     * Returns all active quizzes as (id, title, category, difficulty, total_points).
     * Used to populate the "Select a Quiz" list on DashboardFrame.
     */
    public List<Object[]> getAllActiveQuizzes() throws SQLException {
        List<Object[]> quizzes = new ArrayList<>();
        String sql = "SELECT id, title, category, difficulty, total_points, time_limit " +
                     "FROM quizzes WHERE is_active = 1 ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                quizzes.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("difficulty"),
                    rs.getInt("total_points"),
                    rs.getInt("time_limit")
                });
            }
        }
        return quizzes;
    }

    /** Returns the title of a quiz by its ID. */
    public String getQuizTitle(int quizId) throws SQLException {
        String sql = "SELECT title FROM quizzes WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("title") : "Unknown Quiz";
            }
        }
    }

    /**
     * Inserts a new quiz.  Returns the generated quiz ID.
     * Used by AdminFrame → Admin creates a quiz shell first.
     */
    public int createQuiz(String title, String description, String category,
                           String difficulty, int timeLimit, int createdBy) throws SQLException {
        String sql = "INSERT INTO quizzes (title, description, category, difficulty, " +
                     "time_limit, is_active, created_by) VALUES (?,?,?,?,?,1,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, category);
            ps.setString(4, difficulty);
            ps.setInt(5, timeLimit);
            ps.setInt(6, createdBy);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /** Updates total_points for a quiz (called after inserting all questions). */
    public void recalcTotalPoints(int quizId) throws SQLException {
        String sql = "UPDATE quizzes SET total_points = " +
                     "(SELECT COALESCE(SUM(points),0) FROM questions WHERE quiz_id = ?) " +
                     "WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setInt(2, quizId);
            ps.executeUpdate();
        }
    }

    /** Soft-deletes a quiz (sets is_active = 0). */
    public void deactivateQuiz(int quizId) throws SQLException {
        String sql = "UPDATE quizzes SET is_active = 0 WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.executeUpdate();
        }
    }

    // ── Question CRUD ─────────────────────────────────────────────────

    /**
     * Fetches all questions for a given quiz.
     * Equivalent to: Quiz.findById(id) with populated questions array.
     */
    public List<Question> getQuestionsByQuizId(int quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE quiz_id = ? ORDER BY id ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) questions.add(mapQuestion(rs));
            }
        }
        return questions;
    }

    /**
     * Inserts a new question into a quiz.
     * Returns the generated question ID.
     */
    public int addQuestion(Question q) throws SQLException {
        String sql = "INSERT INTO questions (quiz_id, question_text, option_a, option_b, " +
                     "option_c, option_d, correct_option, explanation, points) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    q.getQuizId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getOptionA());
            ps.setString(4, q.getOptionB());
            ps.setString(5, q.getOptionC());
            ps.setString(6, q.getOptionD());
            ps.setInt(7,    q.getCorrectOption());
            ps.setString(8, q.getExplanation());
            ps.setInt(9,    q.getPoints());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /** Deletes a question by ID. */
    public void deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            ps.executeUpdate();
        }
    }

    // ── Row Mapper ────────────────────────────────────────────────────

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setOptionA(rs.getString("option_a"));
        q.setOptionB(rs.getString("option_b"));
        q.setOptionC(rs.getString("option_c"));
        q.setOptionD(rs.getString("option_d"));
        q.setCorrectOption(rs.getInt("correct_option"));
        q.setExplanation(rs.getString("explanation"));
        q.setPoints(rs.getInt("points"));
        return q;
    }
}
