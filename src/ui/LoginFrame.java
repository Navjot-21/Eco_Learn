package ui;

import controller.AuthController;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login screen – converted from React Auth.jsx (login form).
 * Eco-green dark theme with rounded card layout.
 */
public class LoginFrame extends JFrame {

    private final AuthController authController = new AuthController();

    // UI Components
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         errorLabel;
    private JLabel         loadingLabel;

    public LoginFrame() {
        initFrame();
        buildUI();
    }

    // ── Frame Setup ───────────────────────────────────────────────────

    private void initFrame() {
        setTitle(Constants.APP_NAME + " – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Constants.BG_DARK);
    }

    // ── UI Construction ───────────────────────────────────────────────

    private void buildUI() {
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Constants.BG_DARK);

        JPanel card = createCard();
        add(card);
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 500));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Logo / Title
        JLabel logo = new JLabel("🌿", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 52));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("EcoLearn", SwingConstants.CENTER);
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.PRIMARY_LIGHT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue learning", SwingConstants.CENTER);
        subtitle.setFont(Constants.FONT_SMALL);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error label (hidden by default)
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Constants.FONT_SMALL);
        errorLabel.setForeground(Constants.ERROR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email field
        JLabel emailLbl = fieldLabel("📧  Email Address");
        emailField      = styledTextField("student@example.com");

        // Password field
        JLabel passLbl  = fieldLabel("🔒  Password");
        passwordField   = styledPasswordField();

        // Login button
        loginButton = styledButton("Sign In", Constants.PRIMARY, Color.WHITE);
        loginButton.addActionListener(e -> onLoginClicked());

        // Enter key triggers login
        passwordField.addActionListener(e -> onLoginClicked());
        emailField.addActionListener(e -> passwordField.requestFocus());

        // Loading label
        loadingLabel = new JLabel(" ", SwingConstants.CENTER);
        loadingLabel.setFont(Constants.FONT_SMALL);
        loadingLabel.setForeground(Constants.TEXT_MUTED);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel noAccount = new JLabel("Don't have an account?");
        noAccount.setForeground(Constants.TEXT_SECONDARY);
        noAccount.setFont(Constants.FONT_SMALL);
        JButton regLink = new JButton("Register");
        regLink.setFont(Constants.FONT_SMALL);
        regLink.setForeground(Constants.PRIMARY_LIGHT);
        regLink.setBorderPainted(false);
        regLink.setContentAreaFilled(false);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        linkPanel.add(noAccount);
        linkPanel.add(regLink);

        // Assemble card
        card.add(logo);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(emailLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(emailField);
        card.add(Box.createVerticalStrut(14));
        card.add(passLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(22));
        card.add(loginButton);
        card.add(Box.createVerticalStrut(10));
        card.add(loadingLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(linkPanel);

        return card;
    }

    // ── Event Handlers ────────────────────────────────────────────────

    private void onLoginClicked() {
        clearError();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        authController.handleLogin(this, email, password);
    }

    // ── Public API (called by AuthController) ─────────────────────────

    public void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setForeground(Constants.ERROR);
    }

    public void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loadingLabel.setText(loading ? "Signing in…" : " ");
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    // ── Styling Helpers ───────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Constants.FONT_LABEL);
        lbl.setForeground(Constants.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        styleInputField(tf);
        // Placeholder behaviour
        tf.setForeground(Constants.TEXT_MUTED);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(Constants.TEXT_PRIMARY); }
            }
            public void focusLost(FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Constants.TEXT_MUTED); }
            }
        });
        return tf;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        styleInputField(pf);
        pf.setEchoChar('●');
        return pf;
    }

    private void styleInputField(JComponent field) {
        field.setOpaque(false);
        field.setBackground(Constants.BG_INPUT);
        field.setForeground(Constants.TEXT_PRIMARY);
        field.setFont(Constants.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(320, Constants.FIELD_HEIGHT));
        field.setMaximumSize(new Dimension(Short.MAX_VALUE, Constants.FIELD_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        ((JComponent) field).putClientProperty("caretColor", Constants.TEXT_PRIMARY);
        if (field instanceof JTextField) ((JTextField) field).setCaretColor(Constants.TEXT_PRIMARY);
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : bg.darker());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Constants.FONT_BUTTON);
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, Constants.BUTTON_HEIGHT));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); btn.repaint(); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg);           btn.repaint(); }
        });
        return btn;
    }
}
