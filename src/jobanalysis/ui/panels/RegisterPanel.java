package jobanalysis.ui.panels;

import javax.swing.*;
import javax.swing.border.*;

import jobanalysis.ui.MainFrame;

import java.awt.*;

public class RegisterPanel extends JPanel {
    private JTextField userField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private MainFrame parent;
    
    public RegisterPanel(MainFrame parent) {
        this.parent = parent;
        this.setLayout(new GridBagLayout());
        this.setBackground(MainFrame.BACKGROUND_COLOR);
        
        // Create a container panel for the registration form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(MainFrame.TITLE_FONT);
        titleLabel.setForeground(MainFrame.PRIMARY_COLOR);
        
        // Form fields
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(MainFrame.LABEL_FONT);
        userField = MainFrame.createStyledTextField();
        
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(MainFrame.LABEL_FONT);
        emailField = MainFrame.createStyledTextField();
        
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(MainFrame.LABEL_FONT);
        passwordField = MainFrame.createStyledPasswordField();
        
        JLabel confirmPassLabel = new JLabel("Confirm Password");
        confirmPassLabel.setFont(MainFrame.LABEL_FONT);
        confirmPasswordField = MainFrame.createStyledPasswordField();
        
        // Buttons
        JButton registerButton = MainFrame.createStyledButton("Register");
        JButton backButton = MainFrame.createStyledButton("Back to Login");
        backButton.setBackground(Color.GRAY);
        
        // Layout
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(titleLabel, gbc);
        
        // Add form fields
        addFormField(formPanel, userLabel, userField, gbc, 1);
        addFormField(formPanel, emailLabel, emailField, gbc, 3);
        addFormField(formPanel, passLabel, passwordField, gbc, 5);
        addFormField(formPanel, confirmPassLabel, confirmPasswordField, gbc, 7);
        
        // Add buttons
        gbc.gridy = 9;
        gbc.insets = new Insets(20, 10, 10, 10);
        formPanel.add(registerButton, gbc);
        
        gbc.gridy = 10;
        gbc.insets = new Insets(10, 10, 10, 10);
        formPanel.add(backButton, gbc);
        
        // Add the form panel to the main panel
        this.add(formPanel);
        
        // Add action listeners
        registerButton.addActionListener(e -> handleRegistration());
        backButton.addActionListener(e -> parent.showLogin());
    }
    
    private void addFormField(JPanel panel, JLabel label, JComponent field, 
                            GridBagConstraints gbc, int row) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);
        
        gbc.gridy = row + 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }
    
    private void handleRegistration() {
        String username = userField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all fields", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, 
                "Passwords do not match", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, 
            "Registration attempted with:\nUsername: " + username + 
            "\nEmail: " + email, 
            "Registration Info", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}