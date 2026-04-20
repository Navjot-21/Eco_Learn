package ui;

import model.User;
import service.QuizService;
import service.UserService;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Main dashboard – converted from React StudentsDashboard.jsx + Home.jsx.
 * Shows user stats, quiz list, and navigation to Leaderboard / Admin.
 */
public class DashboardFrame extends JFrame {

    private final User        currentUser;
    private final QuizService quizService  = new QuizService();
    private final UserService userService  = new UserService();

    // Stat labels updated after quiz completion
    private JLabel pointsLabel;
    private JLabel levelLabel;
    private JLabel quizzesLabel;

    // Quiz list
    private DefaultListModel<String> quizListModel;
    private JList<String>            quizList;
    private java.util.List<Object[]> quizData;   // raw quiz rows from DB

    public DashboardFrame(User user) {
        this.currentUser = user;
        initFrame();
        buildUI();
        loadQuizzes();
    }

    // ── Frame Setup ───────────────────────────────────────────────────

    private void initFrame() {
        setTitle("EcoLearn Dashboard – " + currentUser.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.FRAME_WIDTH, Constants.FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(Constants.BG_DARK);
    }

    // ── UI Construction ───────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Sidebar ───────────────────────────────────────────────────
        JPanel sidebar = buildSidebar();
        add(sidebar, BorderLayout.WEST);

        // ── Main content ──────────────────────────────────────────────
        JPanel content = buildContent();
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Constants.BG_CARD);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 16, 20, 16));

        // Avatar + name
        JLabel avatar = new JLabel("🌿", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(currentUser.getName(), SwingConstants.CENTER);
        nameLabel.setFont(Constants.FONT_SUBTITLE);
        nameLabel.setForeground(Constants.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(currentUser.getRole().toUpperCase(), SwingConstants.CENTER);
        roleLabel.setFont(Constants.FONT_SMALL);
        roleLabel.setForeground(Constants.PRIMARY_LIGHT);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 60));
        sep.setMaximumSize(new Dimension(Short.MAX_VALUE, 1));

        // Navigation buttons
        JButton dashBtn = navButton("🏠  Dashboard",  true);
        JButton lbBtn   = navButton("🏆  Leaderboard", false);
        JButton adminBtn= navButton("⚙  Admin Panel", false);
        JButton logoutBtn=navButton("🚪  Logout",      false);

        dashBtn.addActionListener(e -> refreshStats());
        lbBtn.addActionListener(e -> new LeaderboardFrame(currentUser).setVisible(true));

        adminBtn.setVisible(currentUser.isAdmin());
        adminBtn.addActionListener(e -> new AdminFrame(currentUser).setVisible(true));

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        // Eco tips at bottom
        JLabel tipsTitle = new JLabel("💡 Eco Tip", SwingConstants.CENTER);
        tipsTitle.setFont(Constants.FONT_SMALL);
        tipsTitle.setForeground(Constants.ACCENT);
        tipsTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] tips = {
            "Turn off lights when you leave a room.",
            "Carry a reusable water bottle.",
            "Plant a tree this weekend! 🌳",
            "Reduce, Reuse, Recycle!",
            "Choose public transport when possible."
        };
        String tip = tips[(int)(Math.random() * tips.length)];
        JTextArea tipText = new JTextArea(tip);
        tipText.setFont(Constants.FONT_SMALL);
        tipText.setForeground(Constants.TEXT_SECONDARY);
        tipText.setBackground(new Color(40, 40, 40));
        tipText.setLineWrap(true);
        tipText.setWrapStyleWord(true);
        tipText.setEditable(false);
        tipText.setBorder(new EmptyBorder(8, 8, 8, 8));
        tipText.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(avatar);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(nameLabel);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(dashBtn);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(lbBtn);
        sidebar.add(Box.createVerticalStrut(6));
        if (currentUser.isAdmin()) { sidebar.add(adminBtn); sidebar.add(Box.createVerticalStrut(6)); }
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(tipsTitle);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(tipText);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBackground(Constants.BG_DARK);
        content.setBorder(new EmptyBorder(Constants.PADDING, Constants.PADDING, Constants.PADDING, Constants.PADDING));

        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel welcome = new JLabel("Welcome back, " + currentUser.getName() + "! 🌍");
        welcome.setFont(Constants.FONT_TITLE);
        welcome.setForeground(Constants.TEXT_PRIMARY);

        JLabel date = new JLabel(new java.text.SimpleDateFormat("EEEE, MMM d yyyy")
                                     .format(new java.util.Date()));
        date.setFont(Constants.FONT_SMALL);
        date.setForeground(Constants.TEXT_MUTED);

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);
        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(4));
        welcomePanel.add(date);
        header.add(welcomePanel, BorderLayout.WEST);
        content.add(header, BorderLayout.NORTH);

        // ── Stat cards ────────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(new EmptyBorder(16, 0, 16, 0));

        int completedCount = userService.getCompletedQuizCount(currentUser.getId());

        JPanel pointsCard = statCard("⭐ Total Points", String.valueOf(currentUser.getPoints()), Constants.PRIMARY_LIGHT);
        JPanel levelCard  = statCard("📈 Current Level", "Level " + currentUser.getLevel(), Constants.ACCENT);
        JPanel quizCard   = statCard("📚 Quizzes Done",  String.valueOf(completedCount),      Constants.INFO);

        // Keep references for refresh
        pointsLabel  = (JLabel) ((JPanel) pointsCard.getComponent(1)).getComponent(0);
        levelLabel   = (JLabel) ((JPanel) levelCard .getComponent(1)).getComponent(0);
        quizzesLabel = (JLabel) ((JPanel) quizCard  .getComponent(1)).getComponent(0);

        statsRow.add(pointsCard);
        statsRow.add(levelCard);
        statsRow.add(quizCard);
        // NOTE: statsRow is NOT added to content directly here.
        // It is added inside the 'centre' panel below (a component can only have one parent).

        // ── Centre panel (stats + quiz list) ──────────────────────────
        JPanel centre = new JPanel(new BorderLayout(0, 12));
        centre.setOpaque(false);
        centre.add(statsRow, BorderLayout.NORTH);
        centre.add(buildQuizPanel(), BorderLayout.CENTER);
        content.add(centre, BorderLayout.CENTER);

        return content;
    }

    private JPanel statCard(String title, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.BG_CARD);
                g2.fill(new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),14,14));
                // accent left stripe
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Double(0,0,5,getHeight(),4,4));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Constants.FONT_SMALL);
        titleLbl.setForeground(Constants.TEXT_SECONDARY);

        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLbl.setForeground(accent);

        JPanel valPanel = new JPanel();
        valPanel.setOpaque(false);
        valPanel.setLayout(new BoxLayout(valPanel, BoxLayout.Y_AXIS));
        valPanel.add(valueLbl);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valPanel,  BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel heading = new JLabel("📝 Available Quizzes");
        heading.setFont(Constants.FONT_SUBTITLE);
        heading.setForeground(Constants.TEXT_PRIMARY);
        panel.add(heading, BorderLayout.NORTH);

        // Quiz list
        quizListModel = new DefaultListModel<>();
        quizList = new JList<>(quizListModel);
        quizList.setBackground(Constants.BG_CARD);
        quizList.setForeground(Constants.TEXT_PRIMARY);
        quizList.setFont(Constants.FONT_BODY);
        quizList.setSelectionBackground(Constants.PRIMARY_DARK);
        quizList.setSelectionForeground(Color.WHITE);
        quizList.setFixedCellHeight(44);
        quizList.setBorder(new EmptyBorder(6, 10, 6, 10));

        JScrollPane scrollPane = new JScrollPane(quizList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50,50,50), 1));
        scrollPane.getViewport().setBackground(Constants.BG_CARD);

        // Start Quiz button
        JButton startBtn = roundedButton("▶  Start Selected Quiz", Constants.PRIMARY, Color.WHITE);
        startBtn.addActionListener(e -> onStartQuiz());

        // Double-click also starts
        quizList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onStartQuiz();
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(startBtn,   BorderLayout.SOUTH);
        return panel;
    }

    // ── Logic ─────────────────────────────────────────────────────────

    private void loadQuizzes() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override protected List<Object[]> doInBackground() {
                return quizService.getAvailableQuizzes();
            }
            @Override protected void done() {
                try {
                    quizData = get();
                    quizListModel.clear();
                    for (Object[] row : quizData) {
                        // row: [id, title, category, difficulty, total_points, time_limit]
                        String entry = String.format("%-35s  |  %-12s  |  %-6s  |  %d pts",
                            row[1], row[2], row[3], row[4]);
                        quizListModel.addElement(entry);
                    }
                    if (quizData.isEmpty()) quizListModel.addElement("  No quizzes available. Ask admin to add some!");
                } catch (Exception ex) {
                    quizListModel.addElement("  Error loading quizzes: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onStartQuiz() {
        int idx = quizList.getSelectedIndex();
        if (idx < 0 || quizData == null || idx >= quizData.size()) {
            JOptionPane.showMessageDialog(this, "Please select a quiz first.", "No Quiz Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] row = quizData.get(idx);
        int    quizId    = (int) row[0];
        String quizTitle = (String) row[1];
        new QuizFrame(currentUser, quizId, quizTitle, this).setVisible(true);
    }

    /** Called after returning from QuizFrame to refresh stats. */
    public void refreshStats() {
        try {
            User refreshed = new service.UserService().getUserById(currentUser.getId());
            currentUser.setPoints(refreshed.getPoints());
            currentUser.setLevel(refreshed.getLevel());
            pointsLabel.setText(String.valueOf(currentUser.getPoints()));
            levelLabel .setText("Level " + currentUser.getLevel());
            int done = new service.UserService().getCompletedQuizCount(currentUser.getId());
            quizzesLabel.setText(String.valueOf(done));
        } catch (Exception ignored) {}
    }

    // ── Styling Helpers ───────────────────────────────────────────────

    private JButton navButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(Constants.FONT_BODY);
        btn.setForeground(active ? Constants.PRIMARY_LIGHT : Constants.TEXT_SECONDARY);
        btn.setBackground(active ? new Color(34, 80, 34) : Constants.BG_CARD);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        btn.setBorder(new EmptyBorder(6, 10, 6, 10));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(Constants.PRIMARY_LIGHT); }
            public void mouseExited(MouseEvent e)  { btn.setForeground(active ? Constants.PRIMARY_LIGHT : Constants.TEXT_SECONDARY); }
        });
        return btn;
    }

    private JButton roundedButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
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
        btn.setPreferredSize(new Dimension(0, Constants.BUTTON_HEIGHT));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
