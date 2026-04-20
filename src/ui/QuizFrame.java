package ui;

import controller.QuizController;
import model.Question;
import model.User;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Quiz screen – converted from React DailyQuiz.jsx.
 * Shows one question at a time with MCQ radio buttons and a timer.
 */
public class QuizFrame extends JFrame {

    private final User           currentUser;
    private final int            quizId;
    private final String         quizTitle;
    private final DashboardFrame dashboardFrame;
    private final QuizController quizController = new QuizController();

    // Quiz state
    private List<Question> questions;
    private int[]          userAnswers;    // -1 = unanswered
    private int            currentIndex = 0;

    // Timer
    private javax.swing.Timer countdownTimer;
    private int               secondsLeft;

    // UI components
    private JLabel       questionCounter;
    private JLabel       timerLabel;
    private JLabel       questionText;
    private ButtonGroup  optionGroup;
    private JRadioButton[] optionButtons;
    private JButton      prevBtn;
    private JButton      nextBtn;
    private JButton      submitBtn;
    private JProgressBar progressBar;
    private JPanel       optionsPanel;

    public QuizFrame(User user, int quizId, String quizTitle, DashboardFrame dashboard) {
        this.currentUser    = user;
        this.quizId         = quizId;
        this.quizTitle      = quizTitle;
        this.dashboardFrame = dashboard;
        initFrame();
        buildUI();
        // Load questions (controller → service → DAO)
        quizController.loadQuiz(this, quizId);
    }

    // ── Frame Setup ───────────────────────────────────────────────────

    private void initFrame() {
        setTitle("📝 " + quizTitle);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confirmExit(); }
        });
        setSize(780, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Constants.BG_DARK);
    }

    // ── UI Construction ───────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        // ── Top bar ───────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Constants.BG_CARD);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("🌿 " + quizTitle);
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.PRIMARY_LIGHT);

        questionCounter = new JLabel("Loading…");
        questionCounter.setFont(Constants.FONT_BODY);
        questionCounter.setForeground(Constants.TEXT_SECONDARY);

        timerLabel = new JLabel("⏱ --:--");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(Constants.ACCENT);

        topBar.add(titleLabel,      BorderLayout.WEST);
        topBar.add(questionCounter, BorderLayout.CENTER);
        topBar.add(timerLabel,      BorderLayout.EAST);

        // ── Progress bar ──────────────────────────────────────────────
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setForeground(Constants.PRIMARY_LIGHT);
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(0, 6));

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(topBar,     BorderLayout.NORTH);
        topSection.add(progressBar, BorderLayout.SOUTH);
        add(topSection, BorderLayout.NORTH);

        // ── Question card ─────────────────────────────────────────────
        JPanel questionCard = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        questionCard.setOpaque(false);
        questionCard.setLayout(new BoxLayout(questionCard, BoxLayout.Y_AXIS));
        questionCard.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel qNumBadge = new JLabel("Question");
        qNumBadge.setFont(Constants.FONT_SMALL);
        qNumBadge.setForeground(Constants.PRIMARY_LIGHT);
        qNumBadge.setAlignmentX(Component.LEFT_ALIGNMENT);

        questionText = new JLabel("<html><div style='width:550px'>Loading question…</div></html>");
        questionText.setFont(new Font("Segoe UI", Font.BOLD, 17));
        questionText.setForeground(Constants.TEXT_PRIMARY);
        questionText.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Options panel
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        optionGroup   = new ButtonGroup();
        optionButtons = new JRadioButton[4];
        String[] labels = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JRadioButton rb = new JRadioButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color bg = isSelected() ? new Color(34, 80, 34) : new Color(40, 40, 40);
                    g2.setColor(bg);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            rb.setOpaque(false);
            rb.setForeground(Constants.TEXT_PRIMARY);
            rb.setFont(Constants.FONT_BODY);
            rb.setBorder(new EmptyBorder(10, 14, 10, 14));
            rb.setMaximumSize(new Dimension(Short.MAX_VALUE, 44));
            rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rb.addActionListener(e -> {
                if (userAnswers != null) userAnswers[currentIndex] = idx;
                optionsPanel.repaint();
            });
            optionGroup.add(rb);
            optionButtons[i] = rb;
            optionsPanel.add(rb);
            optionsPanel.add(Box.createVerticalStrut(8));
        }

        questionCard.add(qNumBadge);
        questionCard.add(Box.createVerticalStrut(10));
        questionCard.add(questionText);
        questionCard.add(Box.createVerticalStrut(22));
        questionCard.add(optionsPanel);

        JPanel cardWrapper = new JPanel(new BorderLayout());
        cardWrapper.setBackground(Constants.BG_DARK);
        cardWrapper.setBorder(new EmptyBorder(16, 20, 8, 20));
        cardWrapper.add(questionCard, BorderLayout.CENTER);
        add(cardWrapper, BorderLayout.CENTER);

        // ── Bottom navigation ─────────────────────────────────────────
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(Constants.BG_CARD);
        navBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        prevBtn   = navButton("◀  Previous");
        nextBtn   = navButton("Next  ▶");
        submitBtn = roundedButton("✔  Submit Quiz", Constants.SUCCESS, Color.WHITE);

        prevBtn.addActionListener(e -> navigate(-1));
        nextBtn.addActionListener(e -> navigate(+1));
        submitBtn.addActionListener(e -> onSubmit());

        prevBtn.setEnabled(false);
        submitBtn.setEnabled(false);

        navBar.add(prevBtn,   BorderLayout.WEST);
        navBar.add(submitBtn, BorderLayout.CENTER);
        navBar.add(nextBtn,   BorderLayout.EAST);
        add(navBar, BorderLayout.SOUTH);
    }

    // ── Public API (called by QuizController) ─────────────────────────

    /** Receives loaded questions from the controller and renders the first one. */
    public void displayQuestions(List<Question> qs) {
        this.questions   = qs;
        this.userAnswers = new int[qs.size()];
        java.util.Arrays.fill(userAnswers, -1);

        // Start 10-min timer
        secondsLeft = 10 * 60;
        startTimer();

        submitBtn.setEnabled(true);
        showQuestion(0);
    }

    public void setSubmitEnabled(boolean enabled) {
        submitBtn.setEnabled(enabled);
    }

    // ── Internal Logic ────────────────────────────────────────────────

    private void showQuestion(int index) {
        if (questions == null || index < 0 || index >= questions.size()) return;
        currentIndex = index;
        Question q   = questions.get(index);

        // Update counter & progress
        questionCounter.setText("Question " + (index + 1) + " / " + questions.size());
        progressBar.setValue((int) (((index + 1) * 100.0) / questions.size()));

        // Update question text
        questionText.setText("<html><div style='width:550px'>" + q.getQuestionText() + "</div></html>");

        // Update options
        optionGroup.clearSelection();
        optionButtons[0].setText("  A  " + q.getOptionA());
        optionButtons[1].setText("  B  " + q.getOptionB());
        optionButtons[2].setText("  C  " + q.getOptionC());
        optionButtons[3].setText("  D  " + q.getOptionD());

        // Restore prior selection
        if (userAnswers[index] >= 0) {
            optionButtons[userAnswers[index]].setSelected(true);
        }

        // Navigation buttons state
        prevBtn.setEnabled(index > 0);
        nextBtn.setEnabled(index < questions.size() - 1);
        optionsPanel.repaint();
    }

    private void navigate(int delta) {
        showQuestion(currentIndex + delta);
    }

    private void onSubmit() {
        if (countdownTimer != null) countdownTimer.stop();
        quizController.submitQuiz(this, currentUser, quizId, userAnswers, questions);
        dashboardFrame.refreshStats();
    }

    private void startTimer() {
        countdownTimer = new javax.swing.Timer(1000, e -> {
            secondsLeft--;
            int min = secondsLeft / 60;
            int sec = secondsLeft % 60;
            timerLabel.setText(String.format("⏱ %02d:%02d", min, sec));
            if (secondsLeft <= 60) timerLabel.setForeground(Constants.ERROR);
            if (secondsLeft <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                JOptionPane.showMessageDialog(this, "⏰ Time's up! Auto-submitting…",
                    "Time Up", JOptionPane.WARNING_MESSAGE);
                onSubmit();
            }
        });
        countdownTimer.start();
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Exit the quiz? Your progress will be lost.", "Exit Quiz",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (countdownTimer != null) countdownTimer.stop();
            dispose();
        }
    }

    // ── Styling Helpers ───────────────────────────────────────────────

    private JButton navButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Constants.FONT_BUTTON);
        btn.setForeground(Constants.TEXT_PRIMARY);
        btn.setBackground(new Color(50, 50, 50));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70), 1, true),
            new EmptyBorder(8, 18, 8, 18)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton roundedButton(String text, Color bg, Color fg) {
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
        return btn;
    }
}
