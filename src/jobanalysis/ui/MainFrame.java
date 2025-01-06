package jobanalysis.ui;

import jobanalysis.ui.panels.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    
 // Define common colors and fonts that can be used across panels
    public static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    public static final Color BUTTON_COLOR = new Color(51, 122, 183);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    
    public MainFrame() {
        this.setTitle("Job Analysis System");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        // Initialize all panels
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        //scrapingPanel = new ScrapingPanel();
        //mlPanel = new MLPanel();
        //dashboardPanel = new DashboardPanel();
        
        // Add all panels to the card layout
        mainPanel.add(loginPanel, "login");
        mainPanel.add(registerPanel, "register");
        //mainPanel.add(scrapingPanel, "scraping");
        //mainPanel.add(mlPanel, "ml");
        //mainPanel.add(dashboardPanel, "dashboard");
        
        this.add(mainPanel);
        
        // Start with login panel
        showLogin();
    }
    
    public static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));
        return button;
    }
    
 // Helper method to style a text field
    public static JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 35));
        return field;
    }
    
    // Helper method to style a password field
    public static JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(LABEL_FONT);
        field.setPreferredSize(new Dimension(200, 35));
        return field;
    }
    
    public void showLogin() {
        cardLayout.show(mainPanel, "login");
    }
    
    public void showRegister() {
        cardLayout.show(mainPanel, "register");
    }

}
