package service;

import dao.ScoreDAO;
import dao.UserDAO;
import model.Score;
import model.User;

import java.sql.SQLException;
import java.util.List;

/**
 * User-related business logic:
 * leaderboard, profile, admin user management.
 * Mirrors userController.js and statsController.js from the MERN backend.
 */
public class UserService {

    private final UserDAO  userDAO  = new UserDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();

    // ── Leaderboard ───────────────────────────────────────────────────

    /**
     * Returns top N users sorted by points.
     * Equivalent to: User.find().sort({ points: -1 }).limit(n)
     */
    public List<User> getLeaderboard(int limit) {
        try {
            return userDAO.getLeaderboard(limit);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching leaderboard: " + e.getMessage(), e);
        }
    }

    // ── Profile ───────────────────────────────────────────────────────

    /** Returns the full User object for a given ID. */
    public User getUserById(int userId) {
        try {
            User user = userDAO.findById(userId);
            if (user == null) throw new RuntimeException("User not found: " + userId);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user: " + e.getMessage(), e);
        }
    }

    /** Returns all quiz attempts by a user. */
    public List<Score> getUserHistory(int userId) {
        try {
            return scoreDAO.getScoresByUser(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching history: " + e.getMessage(), e);
        }
    }

    /** Returns how many distinct quizzes this user has completed. */
    public int getCompletedQuizCount(int userId) {
        try {
            return scoreDAO.countCompletedQuizzes(userId);
        } catch (SQLException e) {
            return 0;
        }
    }

    // ── Admin ─────────────────────────────────────────────────────────

    /** Returns all users (for admin panel). */
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching users: " + e.getMessage(), e);
        }
    }

    /** Deletes a user by ID. Admins only. */
    public void deleteUser(int userId) {
        try {
            userDAO.deleteUser(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    /** Updates display name. */
    public void updateName(int userId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        try {
            userDAO.updateProfile(userId, newName.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Error updating profile: " + e.getMessage(), e);
        }
    }
}
