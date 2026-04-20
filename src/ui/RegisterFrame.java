package ui;

import controller.AuthController;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Registration screen – converted from React Auth.jsx (register form).
 */
public class RegisterFrame extends JFrame {

    private final AuthController authController = new AuthController();

    private JTextField     nameField;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JComboBox<String> roleCombo;
    private JButton        registerButton;
    private JLabel         errorLabel;
    private JLabel         loadingLabel;

    public RegisterFrame() {
        initFrame();
        buildUI();
    }

    private void initFrame() {
        setTitle(Constants.APP_NAME + " – Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Constants.BG_DARK);
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Constants.BG_DARK);
        add(createCard());
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
        card.setPreferredSize(new Dimension(420, 620));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(35, 40, 35, 40));

        // Header
        JLabel logo = new JLabel("🌱", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.PRIMARY_LIGHT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Join EcoLearn and start earning points!", SwingConstants.CENTER);
        subtitle.setFont(Constants.FONT_SMALL);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error label
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(Constants.FONT_SMALL);
        errorLabel.setForeground(Constants.ERROR);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields
        nameField     = styledTextField("Full Name");
        emailField    = styledTextField("Email Address");
        passwordField = styledPasswordField("Password (min 6 chars)");
        confirmField  = styledPasswordField("Confirm Password");

        // Role selector
        JLabel roleLbl = fieldLabel("👤  Account Type");
        roleCombo = new JComboBox<>(new String[]{"student", "teacher"});
        roleCombo.setFont(Constants.FONT_BODY);
        roleCombo.setBackground(Constants.BG_INPUT);
        roleCombo.setForeground(Constants.TEXT_PRIMARY);
        roleCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, Constants.FIELD_HEIGHT));
        roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        ((JComponent) roleCombo).setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true));

        // Register button
        registerButton = styledButton("Create Account", Constants.PRIMARY, Color.WHITE);
        registerButton.addActionListener(e -> onRegisterClicked());

        // Loading
        loadingLabel = new JLabel(" ", SwingConstants.CENTER);
        loadingLabel.setFont(Constants.FONT_SMALL);
        loadingLabel.setForeground(Constants.TEXT_MUTED);
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Back to login link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel hasAcct = new JLabel("Already have an account?");
        hasAcct.setForeground(Constants.TEXT_SECONDARY);
        hasAcct.setFont(Constants.FONT_SMALL);
        JButton loginLink = new JButton("Sign In");
        loginLink.setFont(Constants.FONT_SMALL);
        loginLink.setForeground(Constants.PRIMARY_LIGHT);
        loginLink.setBorderPainted(false);
        loginLink.setContentAreaFilled(false);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> { new LoginFrame().setVisible(true); dispose(); });
        linkPanel.add(hasAcct);
        linkPanel.add(loginLink);

        // Assemble
        card.add(logo);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(14));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(fieldLabel("👤  Full Name"));
        card.add(Box.createVerticalStrut(5));
        card.add(nameField);
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("📧  Email Address"));
        card.add(Box.createVerticalStrut(5));
        card.add(emailField);
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("🔒  Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));
        card.add(fieldLabel("🔒  Confirm Password"));
        card.add(Box.createVerticalStrut(5));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(10));
        card.add(roleLbl);
        card.add(Box.createVerticalStrut(5));
        card.add(roleCombo);
        card.add(Box.createVerticalStrut(20));
        card.add(registerButton);
        card.add(Box.createVerticalStrut(8));
        card.add(loadingLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(linkPanel);

        return card;
    }

    private void onRegisterClicked() {
        clearError();
        authController.handleRegister(this,
            nameField.getText().trim(),
            emailField.getText().trim(),
            new String(passwordField.getPassword()),
            new String(confirmField.getPassword()),
            (String) roleCombo.getSelectedItem()
        );
    }

    // ── Public API ────────────────────────────────────────────────────

    public void showError(String message) {
        errorLabel.setText("⚠ " + message);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public void setLoading(boolean loading) {
        registerButton.setEnabled(!loading);
        loadingLabel.setText(loading ? "Creating account…" : " ");
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Constants.FONT_LABEL);
        lbl.setForeground(Constants.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        applyInputStyle(tf);
        tf.setForeground(Constants.TEXT_MUTED);
        tf.setText(placeholder);
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

    private JPasswordField styledPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_INPUT);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        applyInputStyle(pf);
        pf.setEchoChar('●');
        return pf;
    }

    private void applyInputStyle(JComponent c) {
        c.setOpaque(false);
        c.setBackground(Constants.BG_INPUT);
        c.setForeground(Constants.TEXT_PRIMARY);
        c.setFont(Constants.FONT_BODY);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        c.setPreferredSize(new Dimension(340, Constants.FIELD_HEIGHT));
        c.setMaximumSize(new Dimension(Short.MAX_VALUE, Constants.FIELD_HEIGHT));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (c instanceof JTextField) ((JTextField) c).setCaretColor(Constants.TEXT_PRIMARY);
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : bg.darker());
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),10,10));
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
        return btn;
    }
}
