package jobanalysis.ui.panels;
import jobanalysis.ui.MainFrame;  // Ajoutez cette ligne
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import static jobanalysis.ui.MainFrame.*;
import static jobanalysis.ui.MainFrame.BACKGROUND_COLOR;
import static jobanalysis.ui.MainFrame.BUTTON_COLOR;
import static jobanalysis.ui.MainFrame.BUTTON_FONT;
import static jobanalysis.ui.MainFrame.LABEL_FONT;
import static jobanalysis.ui.MainFrame.PRIMARY_COLOR;
import static jobanalysis.ui.MainFrame.TITLE_FONT;

public class DashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel statsPanel;
    private JPanel recentJobsPanel;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 20));
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initializeComponents();
    }

    private void initializeComponents() {
        // Header Panel
        headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Content Panel with GridBagLayout
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();

        // Stats Panel
        statsPanel = createStatsPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(statsPanel, gbc);

        // Recent Jobs Panel
        recentJobsPanel = createRecentJobsPanel();
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(recentJobsPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Titre
        JLabel titleLabel = new JLabel("Tableau de Bord");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);

        // Menu utilisateur
        JPanel userMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userMenu.setBackground(BACKGROUND_COLOR);

        JButton profileButton = createMenuButton("Profile");
        JButton logoutButton = createMenuButton("Déconnexion");

        logoutButton.addActionListener(e -> {
            mainFrame.showLogin();
        });

        userMenu.add(profileButton);
        userMenu.add(logoutButton);
        panel.add(userMenu, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(BACKGROUND_COLOR);

        addStatCard(panel, "Total des offres", "1,234", "📊");
        addStatCard(panel, "Nouvelles aujourd'hui", "56", "🆕");
        addStatCard(panel, "Villes principales", "12", "🏢");
        addStatCard(panel, "Entreprises", "89", "🏢");

        return panel;
    }

    private void addStatCard(JPanel container, String title, String value, String icon) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(LABEL_FONT);
        titleLabel.setForeground(SECONDARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(PRIMARY_COLOR);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);

        container.add(card);
    }

    private JPanel createRecentJobsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Offres Récentes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshButton = createMenuButton("Actualiser");
        headerPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Titre", "Entreprise", "Ville", "Date", "Actions"};
        Object[][] data = {
                {"Développeur Java Senior", "TechMaroc", "Casablanca", "10/01/2025", "Voir"},
                {"Data Analyst", "DataCorp", "Rabat", "10/01/2025", "Voir"},
                {"DevOps Engineer", "CloudTech", "Tanger", "09/01/2025", "Voir"},
                {"Full Stack Developer", "WebSolutions", "Marrakech", "09/01/2025", "Voir"}
        };

        JTable table = new JTable(data, columns);
        table.setRowHeight(40);
        table.setFont(LABEL_FONT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 10));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}