package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JDBC connection factory.
 * Each call to {@link #getConnection()} opens a brand-new connection so that
 * concurrent SwingWorker threads never share the same Connection object.
 * The DAO classes already close every connection via try-with-resources.
 *
 * Change DB_URL / DB_USER / DB_PASS to match your MySQL setup.
 */
public class DBConnection {

    // ── Configuration ────────────────────────────────────────────────
    private static final String DB_URL = "mysql_url";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass"; // ← change this if needed
    // ─────────────────────────────────────────────────────────────────

    /** Private constructor prevents direct instantiation. */
    private DBConnection() {
    }

    /**
     * Opens and returns a new {@link Connection} to the database.
     * The caller is responsible for closing the connection
     * (best done via try-with-resources, as all DAOs already do).
     *
     * @throws SQLException if the driver is missing or the DB is unreachable
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC Driver not found. " +
                            "Add mysql-connector-java-8.x.x.jar to lib/ and the classpath.",
                    e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /**
     * No-op kept for API compatibility – individual connections are
     * closed by each DAO via try-with-resources.
     */
    public static void closeConnection() {
        // Nothing to do – connections are managed per-call.
        System.out.println("[DBConnection] closeConnection() called (no-op; connections are per-call).");
    }
}
