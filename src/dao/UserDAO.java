package dao;

import db.DBConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-Access Object for the {@code users} table.
 * Converts Express/Mongoose user queries to JDBC PreparedStatements.
 */
public class UserDAO {

    // ── Create ────────────────────────────────────────────────────────

    /**
     * Inserts a new user and returns the generated ID.
     * Equivalent to: User.create({ name, email, password, role })
     */
    public int createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail().toLowerCase());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // ── Read ──────────────────────────────────────────────────────────

    /** Fetches a user by email – used during login. */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Fetches a user by ID. */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /** Returns all users – used by Admin panel. */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY points DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        }
        return users;
    }

    /**
     * Returns users sorted by total points for the leaderboard.
     * Equivalent to: User.find().sort({ points: -1 })
     */
    public List<User> getLeaderboard(int limit) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY points DESC LIMIT ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapRow(rs));
            }
        }
        return users;
    }

    // ── Update ────────────────────────────────────────────────────────

    /** Updates points and level after a quiz submission. */
    public void updatePointsAndLevel(int userId, int points, int level) throws SQLException {
        String sql = "UPDATE users SET points = ?, level = ?, last_active = NOW() WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, level);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    /** Updates basic profile info. */
    public void updateProfile(int userId, String name) throws SQLException {
        String sql = "UPDATE users SET name = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Deletes a user by ID (admin function). */
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /** Checks if an email already exists (for registration validation). */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Row Mapper ────────────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getInt("points"),
            rs.getInt("level"),
            rs.getInt("streak"),
            rs.getTimestamp("last_active"),
            rs.getTimestamp("created_at")
        );
    }
}
