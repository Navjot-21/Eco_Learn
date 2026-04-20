package ui;

import controller.UserController;
import model.User;
import utils.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Leaderboard screen – converted from React Leaderboard.jsx + src/components/Leaderboard.jsx.
 * Shows top-20 users sorted by total points in a JTable.
 */
public class LeaderboardFrame extends JFrame {

    private final User           currentUser;
    private final UserController userController = new UserController();

    private JTable        table;
    private DefaultTableModel tableModel;
    private JLabel        statusLabel;

    public LeaderboardFrame(User currentUser) {
        this.currentUser = currentUser;
        initFrame();
        buildUI();
        userController.loadLeaderboard(this);
    }

    // ── Frame Setup ───────────────────────────────────────────────────

    private void initFrame() {
        setTitle("🏆 EcoLearn Leaderboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 560);
        setLocationRelativeTo(null);
        setResizable(true);
        getContentPane().setBackground(Constants.BG_DARK);
    }

    // ── UI Construction ───────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        // ── Header ────────────────────────────────────────────────────
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Constants.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(24, 30, 18, 30));

        JLabel trophy = new JLabel("🏆", SwingConstants.CENTER);
        trophy.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        trophy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Global Leaderboard", SwingConstants.CENTER);
        title.setFont(Constants.FONT_TITLE);
        title.setForeground(Constants.ACCENT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Top eco-learners ranked by total points", SwingConstants.CENTER);
        subtitle.setFont(Constants.FONT_SMALL);
        subtitle.setForeground(Constants.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(trophy);
        header.add(Box.createVerticalStrut(6));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        String[] columns = {"🥇 Rank", "👤 Name", "⭐ Points", "📈 Level", "🏅 Badge"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                // Highlight current user's row
                String name = (String) getValueAt(row, 1);
                if (name != null && name.equals(currentUser.getName())) {
                    c.setBackground(new Color(34, 80, 34));
                    c.setForeground(Color.WHITE);
                } else if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Constants.BG_CARD : new Color(36, 36, 36));
                    c.setForeground(Constants.TEXT_PRIMARY);
                }
                return c;
            }
        };

        styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Constants.BG_DARK);
        scrollPane.getViewport().setBackground(Constants.BG_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        add(scrollPane, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Constants.BG_CARD);
        footer.setBorder(new EmptyBorder(12, 20, 12, 20));

        statusLabel = new JLabel("Loading leaderboard…");
        statusLabel.setFont(Constants.FONT_SMALL);
        statusLabel.setForeground(Constants.TEXT_MUTED);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(Constants.FONT_SMALL);
        refreshBtn.setForeground(Constants.PRIMARY_LIGHT);
        refreshBtn.setBackground(new Color(40, 40, 40));
        refreshBtn.setBorderPainted(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            tableModel.setRowCount(0);
            statusLabel.setText("Refreshing…");
            userController.loadLeaderboard(this);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(Constants.FONT_SMALL);
        closeBtn.setForeground(Constants.TEXT_SECONDARY);
        closeBtn.setBackground(new Color(40, 40, 40));
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(refreshBtn);
        btnPanel.add(closeBtn);

        footer.add(statusLabel, BorderLayout.WEST);
        footer.add(btnPanel,    BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    // ── Public API (called by UserController) ─────────────────────────

    /**
     * Populates the JTable with leaderboard data.
     * Called by UserController on the EDT after async load completes.
     */
    public void populateTable(List<User> users) {
        tableModel.setRowCount(0);

        String[] medals = {"🥇", "🥈", "🥉"};

        for (int i = 0; i < users.size(); i++) {
            User u   = users.get(i);
            String rank  = (i < 3) ? medals[i] + " " + (i + 1) : "   " + (i + 1);
            String badge = u.getPoints() >= 500 ? "🌟 Legend" :
                           u.getPoints() >= 200 ? "🔥 Expert" :
                           u.getPoints() >= 100 ? "🌱 Learner" : "🐣 Beginner";

            tableModel.addRow(new Object[]{
                rank, u.getName(), u.getPoints(), "Lv. " + u.getLevel(), badge
            });
        }

        if (users.isEmpty()) {
            tableModel.addRow(new Object[]{"–", "No users found", "–", "–", "–"});
        }

        statusLabel.setText("Showing top " + users.size() + " learners  •  Your rank: " + getRank(users));
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String getRank(List<User> users) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == currentUser.getId()) return "#" + (i + 1);
        }
        return "Unranked";
    }

    private void styleTable(JTable tbl) {
        tbl.setBackground(Constants.BG_CARD);
        tbl.setForeground(Constants.TEXT_PRIMARY);
        tbl.setFont(Constants.FONT_BODY);
        tbl.setRowHeight(42);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 2));
        tbl.setSelectionBackground(Constants.PRIMARY_DARK);
        tbl.setSelectionForeground(Color.WHITE);
        tbl.setFocusable(false);

        JTableHeader th = tbl.getTableHeader();
        th.setBackground(new Color(25, 25, 25));
        th.setForeground(Constants.ACCENT);
        th.setFont(Constants.FONT_LABEL);
        th.setReorderingAllowed(false);
        th.setBorder(BorderFactory.createEmptyBorder());
        th.setPreferredSize(new Dimension(0, 40));

        // Column widths
        int[] widths = {70, 220, 90, 80, 120};
        for (int i = 0; i < widths.length; i++) {
            tbl.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Center columns
        DefaultTableCellRenderer centre = new DefaultTableCellRenderer();
        centre.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 2, 3, 4}) {
            tbl.getColumnModel().getColumn(i).setCellRenderer(centre);
        }
    }
}
