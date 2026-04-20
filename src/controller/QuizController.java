package controller;

import model.Question;
import model.User;
import service.QuizService;
import ui.QuizFrame;

import javax.swing.*;
import java.util.List;

/**
 * Handles quiz-related UI events: loading questions, submitting answers.
 * Converted from quizController.js (getQuiz + submitQuiz).
 */
public class QuizController {

    private final QuizService quizService = new QuizService();

    // ── Load Questions ────────────────────────────────────────────────

    /**
     * Loads questions from DB and passes them to QuizFrame.
     * Called when user clicks "Start Quiz" on DashboardFrame.
     */
    public void loadQuiz(QuizFrame quizFrame, int quizId) {
        SwingWorker<List<Question>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Question> doInBackground() {
                return quizService.getQuestionsForQuiz(quizId);
            }

            @Override
            protected void done() {
                try {
                    List<Question> questions = get();
                    quizFrame.displayQuestions(questions);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(quizFrame,
                        "Failed to load quiz: " + ex.getCause().getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    quizFrame.dispose();
                }
            }
        };
        worker.execute();
    }

    // ── Submit ────────────────────────────────────────────────────────

    /**
     * Grades the quiz and shows a result dialog.
     * Called when user clicks "Submit Quiz" in QuizFrame.
     *
     * @param answers  int[] where answers[i] is the chosen option index (0-3) for question i
     *                 or -1 if not answered
     */
    public void submitQuiz(QuizFrame quizFrame, User currentUser,
                            int quizId, int[] answers, List<Question> questions) {

        // Warn if questions are unanswered
        int unanswered = 0;
        for (int a : answers) if (a < 0) unanswered++;
        if (unanswered > 0) {
            int choice = JOptionPane.showConfirmDialog(quizFrame,
                unanswered + " question(s) are unanswered.\nSubmit anyway?",
                "Confirm Submit", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        quizFrame.setSubmitEnabled(false);

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() {
                // Replace -1 (unanswered) with -99 so they never match correct option
                int[] safeAnswers = answers.clone();
                for (int i = 0; i < safeAnswers.length; i++) {
                    if (safeAnswers[i] < 0) safeAnswers[i] = -99;
                }
                return quizService.submitQuiz(currentUser.getId(), quizId, safeAnswers, questions);
            }

            @Override
            protected void done() {
                try {
                    Object[] result = get();
                    int  earnedPoints   = (int)     result[0];
                    int  correctAnswers = (int)     result[1];
                    int  totalQuestions = (int)     result[2];
                    int  newPoints      = (int)     result[3];
                    int  newLevel       = (int)     result[4];
                    boolean leveledUp   = (boolean) result[5];

                    // Update the in-memory User object
                    currentUser.setPoints(newPoints);
                    currentUser.setLevel(newLevel);

                    // Build result message
                    double pct = totalQuestions > 0
                                 ? (correctAnswers * 100.0) / totalQuestions : 0;
                    String grade = pct >= 80 ? "🏆 Excellent!" :
                                   pct >= 60 ? "✅ Good Job!"  :
                                   pct >= 40 ? "📚 Keep Studying" : "💪 Try Again!";

                    String message = String.format(
                        "%s%n%n" +
                        "✔ Correct:  %d / %d%n" +
                        "⭐ Points Earned:  +%d%n" +
                        "🌿 Total Points:  %d%n" +
                        "📈 Level:  %d%s",
                        grade, correctAnswers, totalQuestions,
                        earnedPoints, newPoints, newLevel,
                        leveledUp ? "\n\n🎉 LEVEL UP!" : ""
                    );

                    JOptionPane.showMessageDialog(quizFrame, message,
                        "Quiz Complete", JOptionPane.INFORMATION_MESSAGE);

                    quizFrame.dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(quizFrame,
                        "Error submitting quiz: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    quizFrame.setSubmitEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
