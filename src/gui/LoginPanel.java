package gui;

import database.DatabaseManager;
import model.User;
import model.Admin;
import model.Customer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel {
    private WelcomeFrame parent;
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginPanel(WelcomeFrame parent) {
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
        
        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Login to Arlene", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        contentPanel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username - LABEL DI KIRI, INPUT DI KANAN (GESER KANAN)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST; // Label di kanan
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Input di kiri
        usernameField = new JTextField(20);
        usernameField.setPreferredSize(new Dimension(250, 40));
        usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(usernameField, gbc);
        
        // Password - LABEL DI KIRI, INPUT DI KANAN (GESER KANAN)
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST; // Label di kanan
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        contentPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST; // Input di kiri
        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        contentPanel.add(passwordField, gbc);
        
        // Login button - DI TENGAH dan DIKECILKAN
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER; // Button di tengah
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        JButton loginButton = createStyledButton("LOGIN", new Color(139, 69, 19));
        loginButton.addActionListener(new LoginAction());
        buttonPanel.add(loginButton);
        
        contentPanel.add(buttonPanel, gbc);
        
        // Register instruction
        gbc.gridy = 4;
        JLabel registerLabel = new JLabel("No account? Swipe left to register", JLabel.CENTER);
        registerLabel.setForeground(new Color(255, 215, 0));
        registerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        contentPanel.add(registerLabel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
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
    
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            try {
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this, 
                        "Please fill in all fields", "Login Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                DatabaseManager db = DatabaseManager.getInstance();
                User user = db.login(username, password);
                
                if (user == null) {
                    JOptionPane.showMessageDialog(LoginPanel.this, 
                        "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // System otomatis detect role dari database
                String userRole = user.getRole();
                String welcomeMessage = "Welcome back, " + user.getDisplayName() + "!\n";
                
                if ("ADMIN".equals(userRole)) {
                    welcomeMessage += "You have ADMIN access with full CRUD privileges.";
                } else {
                    welcomeMessage += "You have CUSTOMER access for shopping.";
                }
                
                JOptionPane.showMessageDialog(LoginPanel.this, 
                    welcomeMessage, "Login Successful", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Navigate to dashboard (nanti akan auto detect role)
                parent.navigateToDashboard();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(LoginPanel.this, 
                    "Error during login: " + ex.getMessage(), "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}