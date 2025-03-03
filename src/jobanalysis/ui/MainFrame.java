package jobanalysis.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

import jobanalysis.ui.panels.ClassificationPanel;
import jobanalysis.ui.panels.FileUploadPanel;
import jobanalysis.ui.panels.LoginPanel;
import jobanalysis.ui.panels.RegisterPanel;
import jobanalysis.ui.panels.ScraperPanel;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel navPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ScraperPanel scraperPanel;
    private FileUploadPanel fileUploadPanel;
    private ClassificationPanel classificationPanel;
    private JPanel chartPanel;          // Changed to JPanel
    private JPanel categoryPanel;       // Changed to JPanel
    private JPanel dashboardPanel;      // Changed to JPanel
    private JPanel jobDisplayPanel;     // Changed to JPanel
    private JPanel statisticPanel;      // Changed to JPanel

    // Color scheme
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color BUTTON_COLOR = new Color(51, 122, 183);
    public static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color LIGHT_GRAY = new Color(240, 240, 240);
    public static final Color NAV_BACKGROUND = new Color(40, 44, 52);
    public static final Color NAV_TEXT = new Color(255, 255, 255);

    // Font definitions
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font NAV_FONT = new Font("Segoe UI", Font.BOLD, 13);

    // Navigation state
    private boolean isLoggedIn = false;

    public MainFrame() {
        setTitle("Job Analysis System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Main content panel with BorderLayout
        JPanel contentPanel = new JPanel(new BorderLayout());
        setContentPane(contentPanel);

        // Create navigation panel
        createNavigationPanel();
        contentPanel.add(navPanel, BorderLayout.NORTH);

        // Initially hide the navigation panel
        navPanel.setVisible(false);

        // Initialize card layout and panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        // Initialize all panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        scraperPanel = new ScraperPanel(this);
        fileUploadPanel = new FileUploadPanel(this);
        classificationPanel = new ClassificationPanel(this);

        // Create temporary panels for the ones that don't exist yet
        chartPanel = createTemporaryPanel("Chart Panel");
        categoryPanel = createTemporaryPanel("Category Panel");
        dashboardPanel = createTemporaryPanel("Dashboard Panel");
        jobDisplayPanel = createTemporaryPanel("Job Display Panel");
        statisticPanel = createTemporaryPanel("Statistic Panel");

        // Add all panels to the card layout
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        mainPanel.add(scraperPanel, "scraper");
        mainPanel.add(fileUploadPanel, "fileupload");
        mainPanel.add(classificationPanel, "classification");
        mainPanel.add(chartPanel, "chart");
        mainPanel.add(categoryPanel, "category");
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(jobDisplayPanel, "jobdisplay");
        mainPanel.add(statisticPanel, "statistic");

        // Show login panel initially
        cardLayout.show(mainPanel, "login");
    }

    // Helper method to create temporary panel placeholders
    private JPanel createTemporaryPanel(String panelName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        JLabel label = new JLabel(panelName + " (Under Development)");
        label.setFont(SUBTITLE_FONT);
        label.setHorizontalAlignment(JLabel.CENTER);

        JPanel centerPanel = createShadowPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(label, BorderLayout.CENTER);

        panel.add(Box.createVerticalStrut(50), BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(Box.createHorizontalStrut(100), BorderLayout.EAST);
        panel.add(Box.createHorizontalStrut(100), BorderLayout.WEST);
        panel.add(Box.createVerticalStrut(100), BorderLayout.SOUTH);

        return panel;
    }

    private void createNavigationPanel() {
        navPanel = new JPanel();
        navPanel.setBackground(NAV_BACKGROUND);
        navPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));

        // Add only the requested navigation buttons
        addNavButton("Job Scraper", "scraper");
        addNavButton("File Upload", "fileupload");
        addNavButton("Classification", "classification");

        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(NAV_FONT);
        logoutButton.setForeground(NAV_TEXT);
        logoutButton.setBackground(DANGER_COLOR);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setMargin(new Insets(5, 15, 5, 15));

        logoutButton.addActionListener(e -> {
            isLoggedIn = false;
            navPanel.setVisible(false);
            cardLayout.show(mainPanel, "login");
        });

        navPanel.add(Box.createHorizontalGlue());
        navPanel.add(logoutButton);
    }

    private void addNavButton(String label, String panelName) {
        JButton navButton = new JButton(label);
        navButton.setFont(NAV_FONT);
        navButton.setForeground(NAV_TEXT);
        navButton.setBackground(NAV_BACKGROUND);
        navButton.setBorderPainted(false);
        navButton.setFocusPainted(false);
        navButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        navButton.setMargin(new Insets(5, 15, 5, 15));

        // Add hover effect
        navButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                navButton.setBackground(PRIMARY_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                navButton.setBackground(NAV_BACKGROUND);
            }
        });

        // Add action listener to navigate to the specified panel
        navButton.addActionListener(e -> cardLayout.show(mainPanel, panelName));

        navPanel.add(navButton);
    }

    public static JButton createStyledButton(String text, String style) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));

        switch (style.toLowerCase()) {
            case "primary":
                styleButtonPrimary(button);
                break;
            case "secondary":
                styleButtonSecondary(button);
                break;
            case "success":
                styleButtonSuccess(button);
                break;
            case "danger":
                styleButtonDanger(button);
                break;
            case "outline":
                styleButtonOutline(button);
                break;
            default:
                styleButtonPrimary(button);
        }

        return button;
    }

    public static JButton createStyledButton(String text) {
        return createStyledButton(text, "primary");
    }

    private static void styleButtonPrimary(JButton button) {
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
    }

    private static void styleButtonSecondary(JButton button) {
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
    }

    private static void styleButtonSuccess(JButton button) {
        button.setBackground(SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
    }

    private static void styleButtonDanger(JButton button) {
        button.setBackground(DANGER_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
    }

    private static void styleButtonOutline(JButton button) {
        button.setBackground(Color.WHITE);
        button.setForeground(BUTTON_COLOR);
        button.setBorder(BorderFactory.createLineBorder(BUTTON_COLOR));
    }

    public static JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(createTextFieldBorder());
        return field;
    }

    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(createTextFieldBorder());
        return field;
    }

    private static Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    public static JScrollPane createStyledScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        return scrollPane;
    }

    // Create styled panel with shadow border
    public static JPanel createShadowPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        return panel;
    }

    // Navigation methods with login check
    public void showLogin() {
        cardLayout.show(mainPanel, "login");
    }

    public void showRegister() {
        cardLayout.show(mainPanel, "register");
    }

    // Method to call when user successfully logs in
    public void loginSuccessful() {
        isLoggedIn = true;
        navPanel.setVisible(true);
        showScraper(); // Go to scraper panel after login
    }

    public void showScraper() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "scraper");
        } else {
            showLogin();
        }
    }

    public void showFileUpload() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "fileupload");
        } else {
            showLogin();
        }
    }

    public void showClassification() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "classification");
        } else {
            showLogin();
        }
    }

    public void showChart() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "chart");
        } else {
            showLogin();
        }
    }

    public void showCategory() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "category");
        } else {
            showLogin();
        }
    }

    public void showDashboard() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "dashboard");
        } else {
            showLogin();
        }
    }

    public void showJobDisplay() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "jobdisplay");
        } else {
            showLogin();
        }
    }

    public void showStatistic() {
        if (isLoggedIn) {
            cardLayout.show(mainPanel, "statistic");
        } else {
            showLogin();
        }
    }
}