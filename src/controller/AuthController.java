package controller;

import model.User;
import service.AuthService;
import ui.DashboardFrame;
import ui.LoginFrame;
import ui.RegisterFrame;

import javax.swing.*;

/**
 * Handles button-click events for LoginFrame and RegisterFrame.
 * Equivalent to authController.js (register + login) in the MERN backend.
 */
public class AuthController {

    private final AuthService authService = new AuthService();

    // ── Login ─────────────────────────────────────────────────────────

    /**
     * Called when the "Login" button is clicked.
     * Validates, calls AuthService, then opens DashboardFrame.
     */
    public void handleLogin(LoginFrame loginFrame, String email, String password) {
        // Disable button to prevent double-click
        loginFrame.setLoading(true);

        // Run in background to avoid freezing the UI
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.login(email, password);
            }

            @Override
            protected void done() {
                loginFrame.setLoading(false);
                try {
                    User user = get();
                    // Open dashboard and close login window
                    new DashboardFrame(user).setVisible(true);
                    loginFrame.dispose();
                } catch (Exception ex) {
                    String msg = extractMessage(ex);
                    loginFrame.showError(msg);
                }
            }
        };
        worker.execute();
    }

    // ── Register ──────────────────────────────────────────────────────

    /**
     * Called when the "Register" button is clicked.
     * Validates, calls AuthService, then opens DashboardFrame.
     */
    public void handleRegister(RegisterFrame registerFrame,
                                String name, String email,
                                String password, String confirmPassword,
                                String role) {
        registerFrame.setLoading(true);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authService.register(name, email, password, confirmPassword, role);
            }

            @Override
            protected void done() {
                registerFrame.setLoading(false);
                try {
                    User user = get();
                    JOptionPane.showMessageDialog(registerFrame,
                        "🎉 Welcome, " + user.getName() + "! Your account has been created.",
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                    new DashboardFrame(user).setVisible(true);
                    registerFrame.dispose();
                } catch (Exception ex) {
                    String msg = extractMessage(ex);
                    registerFrame.showError(msg);
                }
            }
        };
        worker.execute();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String extractMessage(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof IllegalArgumentException) return cause.getMessage();
        if (ex instanceof java.util.concurrent.ExecutionException && cause != null) {
            return cause.getMessage();
        }
        return ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred.";
    }
}
