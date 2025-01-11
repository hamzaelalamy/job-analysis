package jobanalysis.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import jobanalysis.ui.panels.LoginPanel;
import jobanalysis.ui.panels.RegisterPanel;
import jobanalysis.ui.panels.ScraperPanel;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ScraperPanel scraperPanel;

    // Color scheme
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color BUTTON_COLOR = new Color(51, 122, 183);
    public static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color LIGHT_GRAY = new Color(240, 240, 240);

    // Font definitions
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font HEADING_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public MainFrame() {
        setTitle("Job Analysis System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Initialize card layout and panels
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        scraperPanel = new ScraperPanel(this);

        // Add all panels to the card layout
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        mainPanel.add(scraperPanel, "scraper");

        add(mainPanel);
        showLogin();
    }

    // Enhanced button creation with multiple styles
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

    // Overloaded method for backward compatibility
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

    // Enhanced text field creation
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(createTextFieldBorder());
        return field;
    }

    // Enhanced password field creation
    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(createTextFieldBorder());
        return field;
    }

    // Create consistent text field border
    private static Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    // Create styled scroll pane
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

    // Navigation methods
    public void showLogin() {
        cardLayout.show(mainPanel, "login");
    }

    public void showRegister() {
        cardLayout.show(mainPanel, "register");
    }

    public void showScraper() {
        cardLayout.show(mainPanel, "scraper");
    }
}