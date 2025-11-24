package main;

import database.DatabaseConnection;
import gui.WelcomeFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 Starting Arlene Jewelry Shop...");
        
        // Initialize database connection
        DatabaseConnection.initializeDatabase();
        
        // Start GUI menggunakan SwingUtilities untuk thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // HAPUS BARIS LookAndFeel - tidak perlu untuk sekarang
                    // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
                    
                    WelcomeFrame welcomeFrame = new WelcomeFrame();
                    welcomeFrame.setVisible(true);
                    
                    System.out.println("✅ GUI started successfully!");
                } catch (Exception e) {
                    System.err.println("❌ Error starting GUI: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Fallback: show error message
                    JOptionPane.showMessageDialog(null, 
                        "Error starting application: " + e.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
