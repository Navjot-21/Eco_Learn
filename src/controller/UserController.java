package controller;

import model.User;
import service.UserService;
import ui.LeaderboardFrame;

import javax.swing.*;
import java.util.List;

/**
 * Handles user-related UI events: leaderboard, profile, admin actions.
 * Mirrors userController.js from the MERN backend.
 */
public class UserController {

    private final UserService userService = new UserService();

    // ── Leaderboard ───────────────────────────────────────────────────

    /**
     * Loads top-20 users and passes data to LeaderboardFrame.
     * Equivalent to: GET /api/users?sort=points&limit=20
     */
    public void loadLeaderboard(LeaderboardFrame frame) {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<User> doInBackground() {
                return userService.getLeaderboard(20);
            }

            @Override
            protected void done() {
                try {
                    frame.populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame,
                        "Error loading leaderboard: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── Admin ─────────────────────────────────────────────────────────

    /** Deletes a user after admin confirmation. */
    public void deleteUser(JFrame parentFrame, int targetUserId,
                            int adminUserId, Runnable onSuccess) {
        if (targetUserId == adminUserId) {
            JOptionPane.showMessageDialog(parentFrame,
                "You cannot delete your own account.", "Action Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(parentFrame,
            "Are you sure you want to delete this user?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                userService.deleteUser(targetUserId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(parentFrame,
                        "User deleted successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    if (onSuccess != null) onSuccess.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parentFrame,
                        "Error deleting user: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /** Returns all users for the admin panel. */
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
