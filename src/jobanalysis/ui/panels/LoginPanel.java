package jobanalysis.ui.panels;

import javax.swing.*;
import javax.swing.border.*;

import jobanalysis.ui.MainFrame;

import java.awt.*;

import jobanalysis.services.AuthService;

public class LoginPanel extends JPanel {
    private JTextField userField;
    private JPasswordField passwordField;
    private MainFrame parent;
    private AuthService authService;
    
    public LoginPanel(MainFrame parent) {
        this.parent = parent;
        this.setLayout(new GridBagLayout());
        this.setBackground(MainFrame.BACKGROUND_COLOR);
        this.authService = new AuthService();
        
        // Create a container panel for the login form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(MainFrame.TITLE_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Please login to your account");
        subtitleLabel.setFont(MainFrame.LABEL_FONT);
        subtitleLabel.setForeground(Color.GRAY);
        
        // Form fields
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(MainFrame.LABEL_FONT);
        userField = MainFrame.createStyledTextField();
        
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(MainFrame.LABEL_FONT);
        passwordField = MainFrame.createStyledPasswordField();
        
        // Buttons
        JButton loginButton = MainFrame.createStyledButton("Login");
        JButton registerButton = MainFrame.createStyledButton("Create Account");
        registerButton.setBackground(Color.GRAY);
        
        // Layout components
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 10, 20, 10);
        formPanel.add(subtitleLabel, gbc);
        
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        formPanel.add(userLabel, gbc);
        
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(userField, gbc);
        
        gbc.gridy = 4;
        gbc.insets = new Insets(15, 10, 5, 10);
        formPanel.add(passLabel, gbc);
        
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 20, 10);
        formPanel.add(passwordField, gbc);
        
        gbc.gridy = 6;
        gbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(loginButton, gbc);
        
        gbc.gridy = 7;
        formPanel.add(registerButton, gbc);
        
        // Add the form panel to the main panel
        this.add(formPanel);
        
        // Add action listeners
        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> parent.showRegister());
    }
    
    private void handleLogin() {
        String username = userField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all fields", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (authService.login(username, password)) {
            //parent.showMainContent();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Invalid username or password", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}