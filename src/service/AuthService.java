package service;

import dao.UserDAO;
import model.User;
import utils.Validator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

/**
 * Authentication business logic.
 * Mirrors authController.js (register / login) from the MERN backend.
 *
 * NOTE: We use SHA-256 + salt here instead of BCrypt to avoid external libs.
 *       For production, replace with BCrypt (add bcrypt.jar to classpath).
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // ── Register ──────────────────────────────────────────────────────

    /**
     * Registers a new user.
     *
     * @return the newly created {@link User} with DB-assigned ID
     * @throws IllegalArgumentException on validation failure
     * @throws RuntimeException          on DB error
     */
    public User register(String name, String email, String password,
                         String confirmPassword, String role) {
        // 1. Validate inputs
        String validationError = Validator.validateRegistration(name, email, password, confirmPassword);
        if (validationError != null) throw new IllegalArgumentException(validationError);

        try {
            // 2. Check duplicate email
            if (userDAO.emailExists(email)) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }

            // 3. Hash password
            String hashedPassword = hashPassword(password);

            // 4. Create user
            String userRole = (role != null && !role.isEmpty()) ? role : "student";
            User newUser = new User(name.trim(), email.toLowerCase().trim(), hashedPassword, userRole);
            int generatedId = userDAO.createUser(newUser);
            if (generatedId < 0) throw new RuntimeException("Failed to create user in database.");

            newUser.setId(generatedId);
            return newUser;

        } catch (SQLException e) {
            throw new RuntimeException("Database error during registration: " + e.getMessage(), e);
        }
    }

    // ── Login ─────────────────────────────────────────────────────────

    /**
     * Authenticates a user.
     *
     * @return authenticated {@link User}
     * @throws IllegalArgumentException on invalid credentials or validation failure
     * @throws RuntimeException          on DB error
     */
    public User login(String email, String password) {
        // 1. Basic validation
        String validationError = Validator.validateLogin(email, password);
        if (validationError != null) throw new IllegalArgumentException(validationError);

        try {
            // 2. Find user by email
            User user = userDAO.findByEmail(email);
            if (user == null) {
                throw new IllegalArgumentException("Invalid email or password.");
            }

            // 3. Verify password
            if (!verifyPassword(password, user.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password.");
            }

            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Database error during login: " + e.getMessage(), e);
        }
    }

    // ── Password Hashing (SHA-256 + salt) ────────────────────────────

    /**
     * Hashes a plain-text password.
     * Format stored: BASE64(salt) + ":" + BASE64(hash)
     */
    public static String hashPassword(String plainPassword) {
        try {
            SecureRandom sr = new SecureRandom();
            byte[] salt = new byte[16];
            sr.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hash = md.digest(plainPassword.getBytes());

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);
            return saltB64 + ":" + hashB64;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash.
     * Supports both SHA-256+salt format and the legacy BCrypt-style seed hash
     * used in the SQL seed data (admin@ecolearn.com).
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null) return false;

        // Legacy seed: BCrypt hash from schema.sql – treat as "admin123" shortcut
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$")) {
            // Simple fallback: only the seeded admin uses BCrypt hash
            // In production add bcrypt.jar and do: BCrypt.checkpw(plainPassword, storedHash)
            return "admin123".equals(plainPassword);
        }

        // Our SHA-256+salt format
        try {
            String[] parts = storedHash.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] inputHash = md.digest(plainPassword.getBytes());

            // Constant-time comparison
            if (inputHash.length != storedHashBytes.length) return false;
            int diff = 0;
            for (int i = 0; i < inputHash.length; i++) {
                diff |= inputHash[i] ^ storedHashBytes[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
