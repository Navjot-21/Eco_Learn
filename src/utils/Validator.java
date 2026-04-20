package utils;

/**
 * Input validation helpers – mirrors the validation logic from the MERN backend.
 */
public final class Validator {

    private Validator() {}

    /** Checks that a string is non-null and not blank. */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates email format (same regex used in the Mongoose User model).
     * Pattern: word chars, optional dots/dashes, @, domain, TLD (2-3 chars).
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        String regex = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+$";
        return email.matches(regex);
    }

    /** Password must be at least 6 characters (matches Mongoose minlength:6). */
    public static boolean isValidPassword(String password) {
        return isNotEmpty(password) && password.length() >= 6;
    }

    /** Name must be non-empty and ≤50 characters. */
    public static boolean isValidName(String name) {
        return isNotEmpty(name) && name.trim().length() <= 50;
    }

    /** Checks two password strings match (used during registration). */
    public static boolean passwordsMatch(String p1, String p2) {
        return p1 != null && p1.equals(p2);
    }

    /**
     * Convenience method: validates all registration fields.
     * Returns a human-readable error message or null if valid.
     */
    public static String validateRegistration(String name, String email,
                                               String password, String confirmPassword) {
        if (!isValidName(name))
            return "Name is required and must be ≤ 50 characters.";
        if (!isValidEmail(email))
            return "Please enter a valid email address.";
        if (!isValidPassword(password))
            return "Password must be at least 6 characters.";
        if (!passwordsMatch(password, confirmPassword))
            return "Passwords do not match.";
        return null;   // all valid
    }

    /**
     * Validates login fields.
     * Returns error message or null if valid.
     */
    public static String validateLogin(String email, String password) {
        if (!isValidEmail(email))
            return "Please enter a valid email address.";
        if (!isNotEmpty(password))
            return "Password is required.";
        return null;
    }
}
