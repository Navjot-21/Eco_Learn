package model;

import java.sql.Timestamp;

/**
 * POJO representing a row in the {@code users} table.
 * Converted from the MongoDB User model in the MERN project.
 */
public class User {

    private int       id;
    private String    name;
    private String    email;
    private String    password;      // stored as BCrypt hash
    private String    role;          // "student" | "teacher" | "admin"
    private int       points;
    private int       level;
    private int       streak;
    private Timestamp lastActive;
    private Timestamp createdAt;

    // ── Constructors ──────────────────────────────────────────────────

    public User() {}

    /** Constructor for creating a new user (registration). */
    public User(String name, String email, String password, String role) {
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.role     = role;
        this.points   = 0;
        this.level    = 1;
        this.streak   = 0;
    }

    /** Full constructor for reading from DB. */
    public User(int id, String name, String email, String password,
                String role, int points, int level, int streak,
                Timestamp lastActive, Timestamp createdAt) {
        this.id         = id;
        this.name       = name;
        this.email      = email;
        this.password   = password;
        this.role       = role;
        this.points     = points;
        this.level      = level;
        this.streak     = streak;
        this.lastActive = lastActive;
        this.createdAt  = createdAt;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public int       getId()           { return id; }
    public void      setId(int id)     { this.id = id; }

    public String    getName()                  { return name; }
    public void      setName(String name)       { this.name = name; }

    public String    getEmail()                 { return email; }
    public void      setEmail(String email)     { this.email = email; }

    public String    getPassword()              { return password; }
    public void      setPassword(String pass)   { this.password = pass; }

    public String    getRole()                  { return role; }
    public void      setRole(String role)       { this.role = role; }

    public int       getPoints()                { return points; }
    public void      setPoints(int points)      { this.points = points; }

    public int       getLevel()                 { return level; }
    public void      setLevel(int level)        { this.level = level; }

    public int       getStreak()                { return streak; }
    public void      setStreak(int streak)      { this.streak = streak; }

    public Timestamp getLastActive()            { return lastActive; }
    public void      setLastActive(Timestamp t) { this.lastActive = t; }

    public Timestamp getCreatedAt()             { return createdAt; }
    public void      setCreatedAt(Timestamp t)  { this.createdAt = t; }

    // ── Helpers ───────────────────────────────────────────────────────

    public boolean isAdmin()   { return "admin".equals(role);   }
    public boolean isTeacher() { return "teacher".equals(role); }
    public boolean isStudent() { return "student".equals(role); }

    /** Points needed to reach the next level. */
    public int pointsToNextLevel() {
        return (level * utils.Constants.POINTS_PER_LEVEL) - points;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', role='" + role +
               "', points=" + points + ", level=" + level + "}";
    }
}
