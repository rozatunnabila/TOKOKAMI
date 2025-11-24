package gui;

import database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel {
    private WelcomeFrame parent;
    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextArea addressArea;
    
    public RegisterPanel(WelcomeFrame parent) {
        this.parent = parent;
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Header dengan back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        
        JButton backButton = new JButton("← Back");
        backButton.setBackground(new Color(139, 69, 19));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        backButton.addActionListener(e -> parent.showWelcomePanel());
        
        headerPanel.add(backButton, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Scroll pane for registration form
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Title
        JLabel titleLabel = new JLabel("Join Arlene Family", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel nameLabel = new JLabel("Full Name:*");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        fullNameField = createStyledTextField();
        contentPanel.add(fullNameField, gbc);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel userLabel = new JLabel("Username:*");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        usernameField = createStyledTextField();
        contentPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel passLabel = new JLabel("Password:*");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        passwordField = createStyledPasswordField();
        contentPanel.add(passwordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel confirmLabel = new JLabel("Confirm Password:*");
        confirmLabel.setForeground(Color.WHITE);
        confirmLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        confirmPasswordField = createStyledPasswordField();
        contentPanel.add(confirmPasswordField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel emailLabel = new JLabel("Email:*");
        emailLabel.setForeground(Color.WHITE);
        emailLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        emailField = createStyledTextField();
        contentPanel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setForeground(Color.WHITE);
        phoneLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        phoneField = createStyledTextField();
        contentPanel.add(phoneField, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 7;
        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setForeground(Color.WHITE);
        addressLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(addressLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 7;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setPreferredSize(new Dimension(250, 70));
        addressScroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        contentPanel.add(addressScroll, gbc);
        
        // Register button
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        JButton registerButton = createStyledButton("CREATE ACCOUNT", new Color(0, 100, 0));
        registerButton.addActionListener(new RegisterAction());
        buttonPanel.add(registerButton);
        
        contentPanel.add(buttonPanel, gbc);
        
        // Login link
        gbc.gridy = 9;
        JLabel loginLabel = new JLabel("Already have an account?", JLabel.CENTER);
        loginLabel.setForeground(Color.WHITE);
        loginLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentPanel.add(loginLabel, gbc);
        
        // Swipe instruction
        gbc.gridy = 10;
        JLabel swipeLabel = new JLabel("← Swipe right to login", JLabel.CENTER);
        swipeLabel.setForeground(new Color(255, 215, 0));
        swipeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(swipeLabel, gbc);
        
        scrollPane.setViewportView(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(250, 40));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setPreferredSize(new Dimension(250, 40));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(8, 25, 8, 25)
        ));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fullName = fullNameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();
            
            try {
                // === VALIDATION STEP 1: Check Availability ===
                DatabaseManager db = DatabaseManager.getInstance();
                
                // Check username availability
                if (!db.isUsernameAvailable(username)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Username already exists. Please choose a different username.", 
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check email availability
                if (!db.isEmailAvailable(email)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Email already registered. Please use a different email.", 
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // === VALIDATION STEP 2: Required Fields ===
                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Please fill in all required fields (*)", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // === VALIDATION STEP 3: Password ===
                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Passwords do not match", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (password.length() < 6) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Password must be at least 6 characters", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // === VALIDATION STEP 4: Email Format ===
                if (!email.contains("@") || !email.contains(".")) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Please enter a valid email address", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // === FINAL STEP: Register User ===
                boolean success = db.registerCustomer(fullName, username, password, email, phone, address);
                
                if (success) {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "🎉 Account created successfully!\n\n" +
                        "You can now login with your credentials:\n" +
                        "Username: " + username + "\n" +
                        "Password: " + "••••••••", 
                        "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Clear form
                    clearForm();
                    
                    // Go back to welcome (bisa swipe ke login)
                    parent.showWelcomePanel();
                    
                } else {
                    JOptionPane.showMessageDialog(RegisterPanel.this, 
                        "Registration failed. Please try again.", 
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(RegisterPanel.this, 
                    "Error during registration: " + ex.getMessage(), "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        fullNameField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
    }
} 