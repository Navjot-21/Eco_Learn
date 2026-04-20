package ui;

import controller.UserController;
import model.Question;
import model.User;
import service.QuizService;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Panel – converted from React TeacherDashboard.jsx.
 * Allows admin/teacher to:
 *   • View all users
 *   • Delete a user
 *   • Add a quiz with questions
 */
public class AdminFrame extends JFrame {

    private final User           currentUser;
    private final UserController userController = new UserController();
    private final QuizService    quizService    = new QuizService();

    // Users tab
    private JTable            usersTable;
    private DefaultTableModel usersModel;

    // Quiz creation tab
    private JTextField    quizTitleField;
    private JTextField    quizDescField;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> difficultyCombo;
    private JSpinner      timeLimitSpinner;
    private JTextArea     questionsLog;

    // Temp list of questions being built
    private final List<Question> pendingQuestions = new ArrayList<>();

    public AdminFrame(User currentUser) {
        this.currentUser = currentUser;
        initFrame();
        buildUI();
    }

    // ── Frame Setup ───────────────────────────────────────────────────

    private void initFrame() {
        setTitle("⚙ EcoLearn Admin Panel");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(860, 640);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(Constants.BG_DARK);
    }

    // ── UI Construction ───────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Constants.BG_CARD);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("⚙  Admin Panel");
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.PRIMARY_LIGHT);
        JLabel subtitle = new JLabel("Manage users and quiz content");
        subtitle.setFont(Constants.FONT_SMALL);
        subtitle.setForeground(Constants.TEXT_MUTED);
        JPanel headText = new JPanel();
        headText.setLayout(new BoxLayout(headText, BoxLayout.Y_AXIS));
        headText.setOpaque(false);
        headText.add(title);
        headText.add(Box.createVerticalStrut(4));
        headText.add(subtitle);
        header.add(headText, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Constants.BG_DARK);
        tabs.setForeground(Constants.TEXT_PRIMARY);
        tabs.setFont(Constants.FONT_BODY);
        tabs.addTab("👥  Manage Users", buildUsersTab());
        tabs.addTab("➕  Create Quiz",  buildCreateQuizTab());
        add(tabs, BorderLayout.CENTER);

        // Load users immediately
        loadUsers();
    }

    // ── Users Tab ─────────────────────────────────────────────────────

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Constants.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Name", "Email", "Role", "Points", "Level", "Joined"};
        usersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        styleTable(usersTable);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.getViewport().setBackground(Constants.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50,50,50)));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Bottom button row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        JButton deleteBtn  = actionButton("🗑  Delete User",  Constants.ERROR);
        JButton refreshBtn = actionButton("🔄 Refresh",       new Color(60, 60, 60));

        deleteBtn.addActionListener(e -> onDeleteUser());
        refreshBtn.addActionListener(e -> loadUsers());

        btnRow.add(deleteBtn);
        btnRow.add(refreshBtn);

        // Stats bar
        JLabel statsLbl = new JLabel(" ");
        statsLbl.setFont(Constants.FONT_SMALL);
        statsLbl.setForeground(Constants.TEXT_MUTED);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(btnRow,   BorderLayout.WEST);
        bottom.add(statsLbl, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    // ── Create Quiz Tab ───────────────────────────────────────────────

    private JPanel buildCreateQuizTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(Constants.BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // ── Left: quiz meta ───────────────────────────────────────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(Constants.BG_CARD);
        left.setBorder(titledBorder("Quiz Details"));

        quizTitleField   = adminTextField("Quiz title");
        quizDescField    = adminTextField("Short description (optional)");
        categoryCombo    = new JComboBox<>(Constants.CATEGORIES);
        difficultyCombo  = new JComboBox<>(Constants.DIFFICULTIES);
        timeLimitSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 120, 1));

        styleCombo(categoryCombo);
        styleCombo(difficultyCombo);

        left.add(labelFor("Quiz Title"));        left.add(quizTitleField);  left.add(vgap(8));
        left.add(labelFor("Description"));       left.add(quizDescField);   left.add(vgap(8));
        left.add(labelFor("Category"));          left.add(categoryCombo);   left.add(vgap(8));
        left.add(labelFor("Difficulty"));        left.add(difficultyCombo); left.add(vgap(8));
        left.add(labelFor("Time Limit (min)")); left.add(timeLimitSpinner);left.add(vgap(16));

        // Add question button
        JButton addQBtn = actionButton("➕  Add Question", Constants.PRIMARY);
        addQBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addQBtn.addActionListener(e -> showAddQuestionDialog());
        left.add(addQBtn);
        left.add(vgap(8));

        // Save quiz button
        JButton saveBtn = actionButton("💾  Save Quiz", Constants.SUCCESS);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> onSaveQuiz());
        left.add(saveBtn);

        panel.add(left, BorderLayout.WEST);

        // ── Right: questions log ──────────────────────────────────────
        JPanel right = new JPanel(new BorderLayout());
        right.setBackground(Constants.BG_CARD);
        right.setBorder(titledBorder("Added Questions"));

        questionsLog = new JTextArea("No questions added yet.\nUse 'Add Question' to begin.\n");
        questionsLog.setFont(Constants.FONT_MONO);
        questionsLog.setForeground(Constants.TEXT_SECONDARY);
        questionsLog.setBackground(new Color(28, 28, 28));
        questionsLog.setEditable(false);
        questionsLog.setLineWrap(true);
        questionsLog.setWrapStyleWord(true);
        questionsLog.setBorder(new EmptyBorder(8, 10, 8, 10));

        right.add(new JScrollPane(questionsLog), BorderLayout.CENTER);
        panel.add(right, BorderLayout.CENTER);

        return panel;
    }

    // ── Logic ─────────────────────────────────────────────────────────

    private void loadUsers() {
        SwingWorker<List<User>, Void> worker = new SwingWorker<>() {
            @Override protected List<User> doInBackground() { return userController.getAllUsers(); }
            @Override protected void done() {
                try {
                    List<User> users = get();
                    usersModel.setRowCount(0);
                    for (User u : users) {
                        usersModel.addRow(new Object[]{
                            u.getId(), u.getName(), u.getEmail(),
                            u.getRole(), u.getPoints(), u.getLevel(),
                            u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0,10) : "–"
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminFrame.this,
                        "Error loading users: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void onDeleteUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        int targetId = (int) usersModel.getValueAt(row, 0);
        userController.deleteUser(this, targetId, currentUser.getId(), this::loadUsers);
    }

    private void showAddQuestionDialog() {
        JDialog dialog = new JDialog(this, "Add Question", true);
        dialog.setSize(560, 480);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Constants.BG_CARD);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Constants.BG_CARD);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextArea qText  = new JTextArea(3, 40);
        qText.setLineWrap(true); qText.setWrapStyleWord(true);
        qText.setBackground(Constants.BG_INPUT); qText.setForeground(Constants.TEXT_PRIMARY);
        qText.setFont(Constants.FONT_BODY);

        JTextField optA = adminTextField("Option A");
        JTextField optB = adminTextField("Option B");
        JTextField optC = adminTextField("Option C");
        JTextField optD = adminTextField("Option D");
        JTextField explanation = adminTextField("Explanation (optional)");

        JComboBox<String> correctCombo = new JComboBox<>(new String[]{"A (0)", "B (1)", "C (2)", "D (3)"});
        styleCombo(correctCombo);
        JSpinner pts = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));

        form.add(labelFor("Question Text *")); form.add(new JScrollPane(qText)); form.add(vgap(8));
        form.add(labelFor("Option A *"));      form.add(optA);   form.add(vgap(6));
        form.add(labelFor("Option B *"));      form.add(optB);   form.add(vgap(6));
        form.add(labelFor("Option C *"));      form.add(optC);   form.add(vgap(6));
        form.add(labelFor("Option D *"));      form.add(optD);   form.add(vgap(6));
        form.add(labelFor("Correct Answer")); form.add(correctCombo); form.add(vgap(6));
        form.add(labelFor("Explanation"));    form.add(explanation);  form.add(vgap(6));
        form.add(labelFor("Points"));         form.add(pts);

        JButton addBtn = actionButton("Add Question", Constants.PRIMARY);
        addBtn.addActionListener(e -> {
            if (qText.getText().trim().isEmpty() || optA.getText().trim().isEmpty() ||
                optB.getText().trim().isEmpty()  || optC.getText().trim().isEmpty()  ||
                optD.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in question and all 4 options.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Question q = new Question(0,
                qText.getText().trim(),
                optA.getText().trim(), optB.getText().trim(),
                optC.getText().trim(), optD.getText().trim(),
                correctCombo.getSelectedIndex(),
                explanation.getText().trim(),
                (int) pts.getValue()
            );
            pendingQuestions.add(q);
            questionsLog.append("Q" + pendingQuestions.size() + ": " + q.getQuestionText() + "\n    ✔ Correct: " + q.getCorrectOptionText() + " (" + (int)pts.getValue() + " pts)\n\n");
            dialog.dispose();
        });

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(addBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void onSaveQuiz() {
        String title = quizTitleField.getText().trim();
        if (title.isEmpty()) { JOptionPane.showMessageDialog(this, "Quiz title is required."); return; }
        if (pendingQuestions.isEmpty()) { JOptionPane.showMessageDialog(this, "Add at least one question."); return; }

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() {
                return quizService.createQuizWithQuestions(
                    title,
                    quizDescField.getText().trim(),
                    (String) categoryCombo.getSelectedItem(),
                    (String) difficultyCombo.getSelectedItem(),
                    (int) timeLimitSpinner.getValue(),
                    currentUser.getId(),
                    pendingQuestions
                );
            }
            @Override protected void done() {
                try {
                    int quizId = get();
                    JOptionPane.showMessageDialog(AdminFrame.this,
                        "✅ Quiz created! ID = " + quizId + "\n" + pendingQuestions.size() + " questions saved.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    pendingQuestions.clear();
                    questionsLog.setText("No questions added yet.\n");
                    quizTitleField.setText("");
                    quizDescField.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminFrame.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // ── Styling Helpers ───────────────────────────────────────────────

    private void styleTable(JTable tbl) {
        tbl.setBackground(Constants.BG_CARD);
        tbl.setForeground(Constants.TEXT_PRIMARY);
        tbl.setFont(Constants.FONT_BODY);
        tbl.setRowHeight(36);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 2));
        tbl.setSelectionBackground(Constants.PRIMARY_DARK);
        tbl.setSelectionForeground(Color.WHITE);
        tbl.getTableHeader().setBackground(new Color(25,25,25));
        tbl.getTableHeader().setForeground(Constants.ACCENT);
        tbl.getTableHeader().setFont(Constants.FONT_LABEL);
    }

    private JTextField adminTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(Constants.FONT_BODY);
        tf.setForeground(Constants.TEXT_PRIMARY);
        tf.setBackground(Constants.BG_INPUT);
        tf.setCaretColor(Constants.TEXT_PRIMARY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60,60,60),1,true),
            new EmptyBorder(6,10,6,10)
        ));
        tf.setMaximumSize(new Dimension(Short.MAX_VALUE, Constants.FIELD_HEIGHT));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(Constants.FONT_BODY);
        cb.setBackground(Constants.BG_INPUT);
        cb.setForeground(Constants.TEXT_PRIMARY);
        cb.setMaximumSize(new Dimension(Short.MAX_VALUE, Constants.FIELD_HEIGHT));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JLabel labelFor(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Constants.FONT_LABEL);
        lbl.setForeground(Constants.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    private JButton actionButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? bg : bg.darker());
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),8,8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(Constants.FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private TitledBorder titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60,60,60), 1, true), title);
        tb.setTitleFont(Constants.FONT_LABEL);
        tb.setTitleColor(Constants.PRIMARY_LIGHT);
        return tb;
    }
}
