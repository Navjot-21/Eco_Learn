package service;

import dao.QuizDAO;
import dao.ScoreDAO;
import dao.UserDAO;
import model.Question;
import model.Score;
import model.User;
import utils.Constants;

import java.sql.SQLException;
import java.util.List;

/**
 * Quiz business logic.
 * Mirrors quizController.js (getQuiz, submitQuiz) from the MERN backend.
 */
public class QuizService {

    private final QuizDAO  quizDAO  = new QuizDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final UserDAO  userDAO  = new UserDAO();

    // ── Fetch ─────────────────────────────────────────────────────────

    /**
     * Returns all questions for a quiz.
     * Equivalent to: Quiz.findById(id) → questions array
     */
    public List<Question> getQuestionsForQuiz(int quizId) {
        try {
            List<Question> questions = quizDAO.getQuestionsByQuizId(quizId);
            if (questions.isEmpty()) {
                throw new IllegalStateException("No questions found for this quiz.");
            }
            return questions;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading quiz questions: " + e.getMessage(), e);
        }
    }

    /** Returns all active quizzes for the dashboard list. */
    public List<Object[]> getAvailableQuizzes() {
        try {
            return quizDAO.getAllActiveQuizzes();
        } catch (SQLException e) {
            throw new RuntimeException("Error loading quizzes: " + e.getMessage(), e);
        }
    }

    // ── Submit ────────────────────────────────────────────────────────

    /**
     * Grades the quiz, awards points, updates level, persists score.
     *
     * @param userId      logged-in user
     * @param quizId      quiz being submitted
     * @param answers     user's answer for each question (index into options 0-3)
     * @param questions   the question list (must match answers length)
     * @return            result summary as Object[]:
     *                    [0] int  earnedPoints
     *                    [1] int  correctAnswers
     *                    [2] int  totalQuestions
     *                    [3] int  newTotalPoints
     *                    [4] int  newLevel
     *                    [5] boolean leveledUp
     */
    public Object[] submitQuiz(int userId, int quizId,
                                int[] answers, List<Question> questions) {
        if (answers.length != questions.size()) {
            throw new IllegalArgumentException("Answers count does not match question count.");
        }

        // 1. Grade
        int earnedPoints    = 0;
        int correctAnswers  = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (answers[i] == questions.get(i).getCorrectOption()) {
                earnedPoints += questions.get(i).getPoints();
                correctAnswers++;
            }
        }

        try {
            // 2. Load current user state
            User user = userDAO.findById(userId);
            if (user == null) throw new RuntimeException("User not found: " + userId);

            int oldLevel          = user.getLevel();
            int newTotalPoints    = user.getPoints() + earnedPoints;
            int newLevel          = Math.max(1, newTotalPoints / Constants.POINTS_PER_LEVEL + 1);
            boolean leveledUp     = newLevel > oldLevel;

            // 3. Persist score
            Score score = new Score(userId, quizId, earnedPoints, correctAnswers, questions.size());
            scoreDAO.saveScore(score);

            // 4. Update user points + level
            userDAO.updatePointsAndLevel(userId, newTotalPoints, newLevel);

            return new Object[]{
                earnedPoints, correctAnswers, questions.size(),
                newTotalPoints, newLevel, leveledUp
            };

        } catch (SQLException e) {
            throw new RuntimeException("Error saving quiz result: " + e.getMessage(), e);
        }
    }

    // ── Admin helpers ─────────────────────────────────────────────────

    /** Creates a quiz shell and then inserts all provided questions. */
    public int createQuizWithQuestions(String title, String description,
                                        String category, String difficulty,
                                        int timeLimit, int createdBy,
                                        List<Question> questions) {
        try {
            int quizId = quizDAO.createQuiz(title, description, category,
                                             difficulty, timeLimit, createdBy);
            if (quizId < 0) throw new RuntimeException("Failed to create quiz.");

            for (Question q : questions) {
                q.setQuizId(quizId);
                quizDAO.addQuestion(q);
            }
            quizDAO.recalcTotalPoints(quizId);
            return quizId;
        } catch (SQLException e) {
            throw new RuntimeException("Error creating quiz: " + e.getMessage(), e);
        }
    }

    /** Adds a single question to an existing quiz. */
    public void addQuestionToQuiz(Question question) {
        try {
            quizDAO.addQuestion(question);
            quizDAO.recalcTotalPoints(question.getQuizId());
        } catch (SQLException e) {
            throw new RuntimeException("Error adding question: " + e.getMessage(), e);
        }
    }
}
