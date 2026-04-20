package model;

import java.sql.Timestamp;

/**
 * POJO representing a row in the {@code scores} table.
 * Derived from the {@code completedQuizzes} array embedded in the MongoDB User model.
 */
public class Score {

    private int       id;
    private int       userId;
    private int       quizId;
    private int       score;
    private int       correctAnswers;
    private int       totalQuestions;
    private Timestamp completedAt;

    // Optional denormalised fields populated by JOIN queries
    private String userName;
    private String quizTitle;

    // ── Constructors ──────────────────────────────────────────────────

    public Score() {}

    public Score(int userId, int quizId, int score,
                 int correctAnswers, int totalQuestions) {
        this.userId         = userId;
        this.quizId         = quizId;
        this.score          = score;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int       getId()                         { return id; }
    public void      setId(int id)                   { this.id = id; }

    public int       getUserId()                     { return userId; }
    public void      setUserId(int userId)           { this.userId = userId; }

    public int       getQuizId()                     { return quizId; }
    public void      setQuizId(int quizId)           { this.quizId = quizId; }

    public int       getScore()                      { return score; }
    public void      setScore(int score)             { this.score = score; }

    public int       getCorrectAnswers()             { return correctAnswers; }
    public void      setCorrectAnswers(int ca)       { this.correctAnswers = ca; }

    public int       getTotalQuestions()             { return totalQuestions; }
    public void      setTotalQuestions(int tq)       { this.totalQuestions = tq; }

    public Timestamp getCompletedAt()                { return completedAt; }
    public void      setCompletedAt(Timestamp t)     { this.completedAt = t; }

    public String    getUserName()                   { return userName; }
    public void      setUserName(String userName)    { this.userName = userName; }

    public String    getQuizTitle()                  { return quizTitle; }
    public void      setQuizTitle(String quizTitle)  { this.quizTitle = quizTitle; }

    /** Returns accuracy as a percentage string, e.g. "80%". */
    public String getAccuracy() {
        if (totalQuestions == 0) return "0%";
        return String.format("%.0f%%", (correctAnswers * 100.0) / totalQuestions);
    }

    @Override
    public String toString() {
        return "Score{userId=" + userId + ", quizId=" + quizId +
               ", score=" + score + ", correct=" + correctAnswers +
               "/" + totalQuestions + "}";
    }
}
