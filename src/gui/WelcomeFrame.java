package gui;

import javax.swing.*;

import database.DatabaseManager;
import model.Admin;
import model.Customer;
import model.User;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WelcomeFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private int dragStartX;
    private boolean isDragging = false;
    
    public WelcomeFrame() {
        setupUI();
    }
    
    private void setupUI() {
        setTitle("Arlene Jewelry Shop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel dengan card layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setOpaque(false);
        
        // Add panels
        mainPanel.add(createWelcomePanel(), "WELCOME");
        mainPanel.add(new LoginPanel(this), "LOGIN");
        mainPanel.add(new RegisterPanel(this), "REGISTER");
        
        // Set background
        setContentPane(new BackgroundPanel());
        add(mainPanel, BorderLayout.CENTER);
        
        // Add drag listener
        setupDragListener();
        
        // Show welcome panel pertama
        cardLayout.show(mainPanel, "WELCOME");
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(100, 100, 50, 100)); // ↑ Diperbesar atasnya
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        
        // Title - dipindah lebih ke atas
        JLabel titleLabel = new JLabel("💎 ARLENE JEWELRY", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridy = 0;
        panel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Luxury & Elegance Since 1985", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Serif", Font.ITALIC, 20));
        subtitleLabel.setForeground(new Color(255, 215, 0));
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);
        
        // Spacer
        gbc.gridy = 2;
        panel.add(Box.createVerticalStrut(50), gbc);
        
        // Instruction - HANYA SWIPE, TANPA TOMBOL
        JLabel instructionLabel = new JLabel("← Swipe right to login | Swipe left to register →", JLabel.CENTER);
        instructionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        instructionLabel.setForeground(Color.WHITE);
        gbc.gridy = 3;
        gbc.insets = new Insets(40, 10, 10, 10);
        panel.add(instructionLabel, gbc);
        
        return panel;
    }
    
    private void setupDragListener() {
        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartX = e.getX();
                isDragging = true;
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    int dragDistance = e.getX() - dragStartX;
                    if (Math.abs(dragDistance) > 30) {
                        if (dragDistance > 0) {
                            // Drag ke kanan -> Show Login
                            showLoginPanel();
                        } else {
                            // Drag ke kiri -> Show Register
                            showRegisterPanel();
                        }
                    }
                    isDragging = false;
                }
            }
        };
        
        mainPanel.addMouseListener(dragAdapter);
    }
    
    // SIMPLE METHODS
    public void showLoginPanel() {
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    public void showRegisterPanel() {
        cardLayout.show(mainPanel, "REGISTER");
    }
    
    public void showWelcomePanel() {
        cardLayout.show(mainPanel, "WELCOME");
    }
    
    public void navigateToDashboard() {
        User currentUser = DatabaseManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            this.dispose();
            
            if (currentUser instanceof Admin) {
                new AdminDashboard((Admin) currentUser).setVisible(true);
            } else if (currentUser instanceof Customer) {
                new CustomerDashboard((Customer) currentUser).setVisible(true);
            }
        }
    }
    
    // Background panel dengan gambar
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        
        public BackgroundPanel() {
            try {
                backgroundImage = new ImageIcon("assets/images/bck.jpg").getImage();
            } catch (Exception e) {
                System.err.println("❌ Error loading background image: " + e.getMessage());
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(245, 222, 179),
                    getWidth(), getHeight(), new Color(139, 69, 19)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
            // Overlay gelap untuk readability
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}