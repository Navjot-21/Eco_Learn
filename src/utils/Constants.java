package utils;

import java.awt.Color;
import java.awt.Font;

/**
 * Application-wide constants – colours, fonts, strings.
 */
public final class Constants {

    private Constants() {}   // utility class – no instances

    // ── App ──────────────────────────────────────────────────────────
    public static final String APP_NAME    = "EcoLearn – Gamified Environmental Learning";
    public static final String APP_VERSION = "1.0.0";

    // ── Roles ────────────────────────────────────────────────────────
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_ADMIN   = "admin";

    // ── Gamification ─────────────────────────────────────────────────
    public static final int POINTS_PER_LEVEL   = 100;   // points needed per level-up
    public static final int STREAK_BONUS_POINTS = 5;    // bonus for daily streak

    // ── Colour Palette (Eco-Green Theme) ─────────────────────────────
    public static final Color PRIMARY        = new Color(34, 139, 34);   // Forest Green
    public static final Color PRIMARY_DARK   = new Color(20,  83, 20);
    public static final Color PRIMARY_LIGHT  = new Color(76, 175, 80);
    public static final Color ACCENT         = new Color(255, 193, 7);   // Amber
    public static final Color BG_DARK        = new Color(18,  18, 18);
    public static final Color BG_CARD        = new Color(30,  30, 30);
    public static final Color BG_INPUT       = new Color(45,  45, 45);
    public static final Color TEXT_PRIMARY   = new Color(240, 240, 240);
    public static final Color TEXT_SECONDARY = new Color(180, 180, 180);
    public static final Color TEXT_MUTED     = new Color(120, 120, 120);
    public static final Color SUCCESS        = new Color(76, 175,  80);
    public static final Color ERROR          = new Color(244,  67,  54);
    public static final Color WARNING        = new Color(255, 152,   0);
    public static final Color INFO           = new Color(33, 150, 243);

    // ── Fonts ─────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  28);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 13);

    // ── UI Dimensions ─────────────────────────────────────────────────
    public static final int FRAME_WIDTH    = 900;
    public static final int FRAME_HEIGHT   = 650;
    public static final int SIDEBAR_WIDTH  = 220;
    public static final int PADDING        = 20;
    public static final int BUTTON_HEIGHT  = 40;
    public static final int FIELD_HEIGHT   = 38;
    public static final int CORNER_RADIUS  = 12;

    // ── Category & Difficulty labels ──────────────────────────────────
    public static final String[] CATEGORIES   = {"environment","science","math","coding","language","general"};
    public static final String[] DIFFICULTIES = {"easy","medium","hard"};
}
