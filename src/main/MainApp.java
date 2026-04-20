package main;

import db.DBConnection;
import ui.LoginFrame;
import utils.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point.
 *
 * How to compile & run:
 * ─────────────────────────────────────────────────────────────────────
 * 1. Add mysql-connector-java-8.x.x.jar to the classpath.
 * 2. Run the SQL script: database/schema.sql in MySQL Workbench or CLI.
 * 3. Update DB_USER / DB_PASS in src/db/DBConnection.java if needed.
 *
 * Compile (from project root):
 * javac -cp "lib/mysql-connector-java-8.x.x.jar" -d out \
 * src/**​/*.java src/main/MainApp.java
 *
 * Run:
 * java -cp "out;lib/mysql-connector-java-8.x.x.jar" main.MainApp
 * (Windows: use ; as separator. Linux/Mac: use :)
 *
 * Default Admin credentials:
 * Email: admin@ecolearn.com
 * Password: admin123
 * ─────────────────────────────────────────────────────────────────────
 */
public class MainApp {

    public static void main(String[] args) {

        // ── 1. Set Look & Feel ─────────────────────────────────────────
        configureLookAndFeel();

        // ── 2. Test DB connection ──────────────────────────────────────
        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "❌ Cannot connect to MySQL!\n\n" +
                            "Please ensure:\n" +
                            "  • MySQL is running on localhost:3306\n" +
                            "  • Database 'ecolearn_db' exists (run schema.sql)\n" +
                            "  • Credentials in DBConnection.java are correct\n\n" +
                            "Error: " + e.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // ── 3. Launch Login screen on the EDT ─────────────────────────
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });

        // ── 4. Shutdown hook – close DB on exit ────────────────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DBConnection.closeConnection();
            System.out.println("[MainApp] Shutdown complete.");
        }));
    }

    private static void configureLookAndFeel() {
        // Attempt to use system font rendering on all platforms
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Try Nimbus for a slightly less dated Swing look
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());

                    // Override Nimbus defaults to match our dark theme
                    UIManager.put("control", Constants.BG_CARD);
                    UIManager.put("info", Constants.BG_DARK);
                    UIManager.put("nimbusBase", Constants.PRIMARY_DARK);
                    UIManager.put("nimbusAlertYellow", Constants.ACCENT);
                    UIManager.put("nimbusDisabledText", Constants.TEXT_MUTED);
                    UIManager.put("nimbusFocus", Constants.PRIMARY_LIGHT);
                    UIManager.put("nimbusGreen", Constants.SUCCESS);
                    UIManager.put("nimbusOrange", Constants.WARNING);
                    UIManager.put("nimbusRed", Constants.ERROR);
                    UIManager.put("nimbusSelectedText", Color.WHITE);
                    UIManager.put("nimbusSelectionBackground", Constants.PRIMARY_DARK);
                    UIManager.put("text", Constants.TEXT_PRIMARY);
                    break;
                }
            }
        } catch (Exception e) {
            // Fall back to system default look-and-feel silently
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }

        // Global font override
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, Constants.FONT_BODY);
            }
        }
    }
}
