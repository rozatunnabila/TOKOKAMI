package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import database.DatabaseManager;
import model.Admin;
import model.Customer;
import model.Order;
import model.OrderItem;
import model.Product;
import model.User;
import utils.CurrencyUtils;

public class AdminDashboard extends JFrame {
    private Admin admin;
    private DatabaseManager db;
    private JTabbedPane tabbedPane;
    private JTable productsTable;
    private DefaultTableModel productsTableModel;
    // Transactions UI
    private DefaultTableModel transactionsTableModel;
    private JTable transactionsTable;
    private JTextArea orderDetailsArea;
    
    // Warna theme admin (biru professional)
    private final Color ADMIN_PRIMARY = new Color(70, 130, 180);    // Steel Blue
    private final Color ADMIN_SECONDARY = new Color(100, 149, 237); // Cornflower Blue
    private final Color ADMIN_ACCENT = new Color(30, 144, 255);     // Dodger Blue
    private final Color ADMIN_DARK = new Color(25, 25, 112);        // Midnight Blue
    private final Color ADMIN_SUCCESS = new Color(34, 139, 34);     // Forest Green
    private final Color ADMIN_DANGER = new Color(220, 20, 60);      // Crimson
    
    public AdminDashboard(Admin admin) {
        this.admin = admin;
        this.db = DatabaseManager.getInstance();
        
        initializeFrame();
        setupUI();
        loadProductsData();
        loadTransactionsData();
    }
    
    private void initializeFrame() {
        setTitle("Arlene Jewelry Shop - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void setupUI() {
        // Main panel dengan background putih
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content - Hanya 3 tab
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(ADMIN_DARK);
        
        tabbedPane.addTab("📦 Manage Products", createProductsPanel());
        tabbedPane.addTab("📊 View Transactions", createTransactionsPanel());
        tabbedPane.addTab("👥 User Management", createUsersPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ADMIN_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        
        // Logo and Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("💎 ARLENE JEWELRY");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subLabel = new JLabel("Admin Panel");
        subLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subLabel.setForeground(new Color(173, 216, 230)); // Light Blue
        subLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        
        // User info and logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + admin.getUsername());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel levelLabel = new JLabel("| Level: " + admin.getAdminLevel());
        levelLabel.setForeground(new Color(173, 216, 230));
        levelLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JButton logoutButton = new JButton("🚪 Logout");
        logoutButton.setBackground(ADMIN_DARK);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        logoutButton.addActionListener(e -> logout());
        
        userPanel.add(welcomeLabel);
        userPanel.add(levelLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(logoutButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JButton addButton = createAdminButton("➕ Add Product", ADMIN_ACCENT);
        JButton editButton = createAdminButton("✏️ Edit Product", ADMIN_SECONDARY);
        JButton deleteButton = createAdminButton("🗑️ Delete Product", ADMIN_DANGER);
        JButton refreshButton = createAdminButton("🔄 Refresh", ADMIN_PRIMARY);

        addButton.addActionListener(e -> showAddProductDialog());
        editButton.addActionListener(e -> editSelectedProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        refreshButton.addActionListener(e -> loadProductsData());

        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);
        toolbar.add(refreshButton);

        // Products table
        String[] columns = {"ID", "Name", "Category", "Material", "Price", "Stock", "Gemstone", "Weight", "Image"};
        productsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua cell tidak editable
            }
        };

        productsTable = new JTable(productsTableModel);
        productsTable.setRowHeight(35);
        productsTable.getTableHeader().setBackground(ADMIN_PRIMARY);
        productsTable.getTableHeader().setForeground(Color.WHITE);
        productsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Center align some columns
        productsTable.getColumnModel().getColumn(4).setCellRenderer(new CenterAlignedRenderer()); // Price
        productsTable.getColumnModel().getColumn(5).setCellRenderer(new CenterAlignedRenderer()); // Stock
        productsTable.getColumnModel().getColumn(7).setCellRenderer(new CenterAlignedRenderer()); // Weight
        productsTable.getColumnModel().getColumn(8).setCellRenderer(new CenterAlignedRenderer()); // Image

        JScrollPane scrollPane = new JScrollPane(productsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Context menu (right-click) for change/remove image
        JPopupMenu productsPopup = new JPopupMenu();
        JMenuItem changeImageItem = new JMenuItem("Change Image");
        JMenuItem removeImageItem = new JMenuItem("Remove Image");
        productsPopup.add(changeImageItem);
        productsPopup.add(removeImageItem);

        changeImageItem.addActionListener(e -> {
            int row = productsTable.getSelectedRow();
            if (row >= 0) changeProductImageAtRow(row);
            else JOptionPane.showMessageDialog(this, "Please select a product row first.", "Info", JOptionPane.INFORMATION_MESSAGE);
        });

        removeImageItem.addActionListener(e -> {
            int row = productsTable.getSelectedRow();
            if (row < 0) { 
                JOptionPane.showMessageDialog(this, "Please select a product row first.", "Info", JOptionPane.INFORMATION_MESSAGE); 
                return; 
            }
            String productId = (String) productsTableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Remove product image and restore default for " + productId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            Product p = db.getProductById(productId);
            if (p == null) return;
            String defaultPath = "assets/images/products/" + productId + ".png";
            p.setImagePath(defaultPath);
            boolean ok = db.updateProduct(p);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Image removed / reset to default.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProductsData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        productsTable.setComponentPopupMenu(productsPopup);

        return panel;
    }
    
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("📊 Transactions Management");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_DARK);
        top.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = createAdminButton("🔄 Refresh", ADMIN_PRIMARY);
        refreshBtn.addActionListener(e -> loadTransactionsData());
        top.add(refreshBtn, BorderLayout.EAST);

        panel.add(top, BorderLayout.NORTH);

        // Table - Kolom Status bisa diedit
        String[] cols = {"Order ID", "Date", "Status", "Payment", "Total"};
        transactionsTableModel = new DefaultTableModel(cols, 0) {
            @Override 
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Hanya kolom Status yang bisa diedit
            }
        };
        
        transactionsTable = new JTable(transactionsTableModel);
        transactionsTable.setRowHeight(34);
        
        // Set custom editor untuk kolom Status
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{
            "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
        });
        transactionsTable.getColumnModel().getColumn(2).setCellEditor(new javax.swing.DefaultCellEditor(statusComboBox));
        
        JScrollPane sp = new JScrollPane(transactionsTable);
        panel.add(sp, BorderLayout.CENTER);

        // Details panel (right)
        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(340, 0));
        right.setBackground(new Color(250, 250, 250));
        right.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel detailsHeader = new JPanel(new BorderLayout());
        detailsHeader.setBackground(new Color(250, 250, 250));
        
        JLabel detailsTitle = new JLabel("Order Details");
        detailsTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JButton updateStatusBtn = createAdminButton("💾 Update Status", ADMIN_SUCCESS);
        updateStatusBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        updateStatusBtn.addActionListener(e -> updateOrderStatus());
        
        detailsHeader.add(detailsTitle, BorderLayout.WEST);
        detailsHeader.add(updateStatusBtn, BorderLayout.EAST);
        
        right.add(detailsHeader, BorderLayout.NORTH);
        
        orderDetailsArea = new JTextArea();
        orderDetailsArea.setEditable(false);
        orderDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        orderDetailsArea.setBackground(new Color(255, 255, 255));
        right.add(new JScrollPane(orderDetailsArea), BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);

        // Load and interactions
        transactionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int r = transactionsTable.getSelectedRow();
                if (r >= 0) {
                    String orderId = (String) transactionsTableModel.getValueAt(r, 0);
                    // find order from db
                    List<Order> orders = db.getUserOrders();
                    Order found = orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
                    if (found != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Order ID: ").append(found.getOrderId()).append('\n');
                        sb.append("Status: ").append(found.getStatus()).append('\n');
                        sb.append("Payment: ").append(found.getPaymentMethod()).append('\n');
                        sb.append("Total: ").append(CurrencyUtils.format(found.getTotalAmount())).append('\n');
                        sb.append("Date: ").append(found.getOrderDate()).append('\n');
                        sb.append("Notes: ").append(found.getNotes() != null ? found.getNotes() : "No notes").append('\n');
                        sb.append("Items:\n");
                        for (OrderItem it : found.getItems()) {
                            sb.append(" - ").append(it.getProduct().getName()).append(" x").append(it.getQuantity()).append(" (").append(CurrencyUtils.format(it.getPrice())).append(")\n");
                        }
                        orderDetailsArea.setText(sb.toString());
                    }
                }
            }
        });

        // Load initial data
        loadTransactionsData();

        // Double-click to view details in dialog
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int r = transactionsTable.getSelectedRow();
                    if (r >= 0) {
                        String orderId = (String) transactionsTableModel.getValueAt(r, 0);
                        List<Order> orders = db.getUserOrders();
                        Order found = orders.stream().filter(o -> o.getOrderId().equals(orderId)).findFirst().orElse(null);
                        if (found != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("=== ORDER DETAILS ===\n\n");
                            sb.append("Order ID: ").append(found.getOrderId()).append('\n');
                            sb.append("Status: ").append(found.getStatus()).append('\n');
                            sb.append("Payment: ").append(found.getPaymentMethod()).append('\n');
                            sb.append("Total: ").append(CurrencyUtils.format(found.getTotalAmount())).append('\n');
                            sb.append("Date: ").append(found.getOrderDate()).append('\n');
                            sb.append("Notes: ").append(found.getNotes() != null ? found.getNotes() : "No notes").append('\n');
                            sb.append("\n=== ORDER ITEMS ===\n");
                            for (OrderItem it : found.getItems()) {
                                sb.append("\n - ").append(it.getProduct().getName()).append("\n");
                                sb.append("   Quantity: ").append(it.getQuantity()).append("\n");
                                sb.append("   Price: ").append(CurrencyUtils.format(it.getPrice())).append("\n");
                                sb.append("   Subtotal: ").append(CurrencyUtils.format(it.getPrice() * it.getQuantity())).append("\n");
                            }
                            JOptionPane.showMessageDialog(AdminDashboard.this, sb.toString(), "Order Details - " + found.getOrderId(), JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        return panel;
    }
    
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("👥 User Management");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(ADMIN_DARK);
        top.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = createAdminButton("🔄 Refresh", ADMIN_PRIMARY);
        top.add(refreshBtn, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        // TANPA ACTIONS COLUMN
        String[] cols = {"User ID", "Username", "Full Name", "Email", "Role"};
        DefaultTableModel usersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { 
                return false; // Semua kolom tidak editable
            }
        };

        JTable usersTable = new JTable(usersModel);
        usersTable.setRowHeight(34);
        usersTable.getTableHeader().setBackground(ADMIN_PRIMARY);
        usersTable.getTableHeader().setForeground(Color.WHITE);
        usersTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        JScrollPane sp = new JScrollPane(usersTable);
        panel.add(sp, BorderLayout.CENTER);

        // Action buttons panel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton editBtn = createAdminButton("✏️ Edit Selected", ADMIN_SECONDARY);
        JButton deleteBtn = createAdminButton("🗑️ Delete Selected", ADMIN_DANGER);
        JButton exportBtn = createAdminButton("📊 Export Users CSV", ADMIN_ACCENT);
        
        bottom.add(editBtn);
        bottom.add(deleteBtn);
        bottom.add(exportBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        // load data
        Runnable usersLoader = () -> {
            try {
                usersModel.setRowCount(0);
                List<User> users = db.getAllUsers();
                for (User u : users) {
                    // TANPA ACTIONS COLUMN
                    usersModel.addRow(new Object[]{
                        u.getUserId(), 
                        u.getUsername(), 
                        u.getDisplayName(), 
                        u.getEmail(), 
                        u.getRole()
                    });
                }
                JOptionPane.showMessageDialog(AdminDashboard.this, 
                    "Loaded " + users.size() + " users", 
                    "Refresh Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                System.err.println("Error loading users: " + ex.getMessage());
                JOptionPane.showMessageDialog(AdminDashboard.this, 
                    "Error loading users: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        refreshBtn.addActionListener(e -> usersLoader.run());
        
        editBtn.addActionListener(e -> {
            int r = usersTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select a user to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String userId = (String) usersModel.getValueAt(r, 0);
            User u = db.getAllUsers().stream().filter(x -> x.getUserId().equals(userId)).findFirst().orElse(null);
            if (u != null) showEditUserDialog(u, usersLoader);
        });

        deleteBtn.addActionListener(e -> {
            int r = usersTable.getSelectedRow();
            if (r == -1) { 
                JOptionPane.showMessageDialog(this, "Please select a user to delete", "No Selection", JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            String userId = (String) usersModel.getValueAt(r, 0);
            String username = (String) usersModel.getValueAt(r, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete user:\n" +
                "Username: " + username + "\n" +
                "User ID: " + userId + "?\n\n" +
                "This action cannot be undone!", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (db.deleteUser(userId)) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    usersLoader.run();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        exportBtn.addActionListener(e -> exportUsersCSV());

        // Load initial data
        usersLoader.run();

        return panel;
    }
    
    private JButton createAdminButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void loadProductsData() {
        try {
            List<Product> products = db.getAllProducts();
            productsTableModel.setRowCount(0); // Clear existing data
            
            // Add products to table
            for (Product product : products) {
                // Tampilkan info gambar di table
                String imageInfo = product.getImagePath() != null ? 
                    "📷 " + new File(product.getImagePath()).getName() : "🖼️ No Image";
                
                productsTableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    product.getCategory(),
                    product.getMaterial(),
                    product.getFormattedPrice(),
                    product.getStock(),
                    product.hasGemstone() ? product.getGemstoneType() : "None",
                    product.getWeight() + "g",
                    imageInfo
                });
            }
            
            System.out.println("✅ Loaded " + products.size() + " products from database");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading products: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading products: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Load orders into transactions table
     */
    private void loadTransactionsData() {
        if (transactionsTableModel == null) return;
        try {
            transactionsTableModel.setRowCount(0);
            List<Order> orders = db.getUserOrders();
            for (Order o : orders) {
                transactionsTableModel.addRow(new Object[]{
                    o.getOrderId(), 
                    o.getOrderDate(), 
                    o.getStatus(), 
                    o.getPaymentMethod(), 
                    CurrencyUtils.format(o.getTotalAmount())
                });
            }
            System.out.println("✅ Loaded " + orders.size() + " transactions from database");
        } catch (Exception e) {
            System.err.println("❌ Error loading transactions: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error loading transactions: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Update status order yang dipilih
     */
    private void updateOrderStatus() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an order to update status", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String orderId = (String) transactionsTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) transactionsTableModel.getValueAt(selectedRow, 2);
        
        // Tampilkan dialog untuk memilih status baru
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{
            "Pending", "Processing", "Shipped", "Delivered", "Cancelled"
        });
        statusCombo.setSelectedItem(currentStatus);
        
        int result = JOptionPane.showConfirmDialog(this,
            new Object[]{"Select new status for order " + orderId + ":", statusCombo},
            "Update Order Status",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String newStatus = (String) statusCombo.getSelectedItem();
            
            // Update status di database
            boolean success = db.updateOrderStatus(orderId, newStatus);
            
            if (success) {
                // Update table
                transactionsTableModel.setValueAt(newStatus, selectedRow, 2);
                
                // Update order details area jika order yang sama masih dipilih
                if (transactionsTable.getSelectedRow() == selectedRow) {
                    List<Order> orders = db.getUserOrders();
                    Order updatedOrder = orders.stream()
                        .filter(o -> o.getOrderId().equals(orderId))
                        .findFirst().orElse(null);
                        
                    if (updatedOrder != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Order ID: ").append(updatedOrder.getOrderId()).append('\n');
                        sb.append("Status: ").append(updatedOrder.getStatus()).append('\n');
                        sb.append("Payment: ").append(updatedOrder.getPaymentMethod()).append('\n');
                        sb.append("Total: ").append(CurrencyUtils.format(updatedOrder.getTotalAmount())).append('\n');
                        sb.append("Date: ").append(updatedOrder.getOrderDate()).append('\n');
                        sb.append("Notes: ").append(updatedOrder.getNotes() != null ? updatedOrder.getNotes() : "No notes").append('\n');
                        sb.append("Items:\n");
                        for (OrderItem it : updatedOrder.getItems()) {
                            sb.append(" - ").append(it.getProduct().getName()).append(" x").append(it.getQuantity()).append(" (").append(CurrencyUtils.format(it.getPrice())).append(")\n");
                        }
                        orderDetailsArea.setText(sb.toString());
                    }
                }
                
                JOptionPane.showMessageDialog(this,
                    "Order status updated successfully!\n" +
                    "Order: " + orderId + "\n" +
                    "New Status: " + newStatus,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to update order status",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportInventoryCSV() {
        try {
            File exportsDir = new File("assets/exports");
            if (!exportsDir.exists()) exportsDir.mkdirs();
            File out = new File(exportsDir, "inventory_" + System.currentTimeMillis() + ".csv");
            try (FileWriter fw = new FileWriter(out)) {
                fw.write("ProductID,Name,Category,Material,Price,Stock,Weight,Gemstone,ImagePath\n");
                for (Product p : db.getAllProducts()) {
                    String line = String.format("%s,%s,%s,%s,%.2f,%d,%.2f,%s,%s\n",
                        p.getProductId(), 
                        p.getName().replaceAll(",", " "), 
                        p.getCategory(),
                        p.getMaterial(),
                        p.getPrice(), 
                        p.getStock(),
                        p.getWeight(),
                        p.hasGemstone() ? p.getGemstoneType() : "None",
                        p.getImagePath() == null ? "" : p.getImagePath());
                    fw.write(line);
                }
            }
            JOptionPane.showMessageDialog(this, 
                "Inventory exported successfully to:\n" + out.getAbsolutePath(), 
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to export CSV: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportUsersCSV() {
        try {
            File exportsDir = new File("assets/exports");
            if (!exportsDir.exists()) exportsDir.mkdirs();
            File out = new File(exportsDir, "users_" + System.currentTimeMillis() + ".csv");
            try (FileWriter fw = new FileWriter(out)) {
                fw.write("UserID,Username,DisplayName,Email,Phone,Address,Role\n");
                for (User u : db.getAllUsers()) {
                    String line = String.format("%s,%s,%s,%s,%s,%s,%s\n",
                        u.getUserId(),
                        u.getUsername(),
                        u.getDisplayName().replaceAll(",", " "),
                        u.getEmail(),
                        u.getPhone() != null ? u.getPhone() : "",
                        u.getAddress() != null ? u.getAddress().replaceAll(",", " ") : "",
                        u.getRole());
                    fw.write(line);
                }
            }
            JOptionPane.showMessageDialog(this, 
                "Users exported successfully to:\n" + out.getAbsolutePath(), 
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to export CSV: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditUserDialog(User user, Runnable refreshCallback) {
        JDialog dialog = new JDialog(this, "Edit User - " + user.getUsername(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;

        content.add(new JLabel("User ID:"), gbc);
        gbc.gridx = 1;
        JTextField userIdField = new JTextField(user.getUserId(), 25);
        userIdField.setEditable(false);
        userIdField.setBackground(new Color(240, 240, 240));
        content.add(userIdField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(user.getUsername(), 25);
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(240, 240, 240));
        content.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Display Name:*"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(user.getDisplayName(), 25);
        content.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Email:*"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(user.getEmail(), 25);
        content.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        JTextField phoneField = new JTextField(user.getPhone() != null ? user.getPhone() : "", 25);
        content.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        content.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        JTextField addressField = new JTextField(user.getAddress() != null ? user.getAddress() : "", 25);
        content.add(addressField, gbc);

        // Role-specific fields
        String specialLabel = null;
        String specialValue = null;
        if (user instanceof Customer) {
            specialLabel = "Full Name:";
            specialValue = ((Customer) user).getFullName();
        } else if (user instanceof Admin) {
            specialLabel = "Admin Level:";
            specialValue = ((Admin) user).getAdminLevel();
        }

        JTextField specialField = null;
        if (specialLabel != null) {
            gbc.gridx = 0; gbc.gridy++;
            content.add(new JLabel(specialLabel), gbc);
            gbc.gridx = 1;
            specialField = new JTextField(specialValue != null ? specialValue : "", 25);
            content.add(specialField, gbc);
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton cancel = createAdminButton("Cancel", Color.GRAY);
        JButton save = createAdminButton("💾 Save Changes", ADMIN_PRIMARY);
        
        buttons.add(cancel);
        buttons.add(save);

        cancel.addActionListener(e -> dialog.dispose());
        
        JTextField finalSpecialField = specialField;
        save.addActionListener(e -> {
            // Validation
            if (nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please fill in all required fields (*)", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Recreate user instance with updated fields
                String password = user.getPassword();
                User updated = null;
                if (user instanceof Customer) {
                    String fullName = finalSpecialField != null ? finalSpecialField.getText().trim() : ((Customer) user).getFullName();
                    updated = new Customer(user.getUserId(), user.getUsername(), password, 
                                         emailField.getText().trim(), 
                                         phoneField.getText().trim(), 
                                         addressField.getText().trim(), 
                                         fullName);
                } else if (user instanceof Admin) {
                    String level = finalSpecialField != null ? finalSpecialField.getText().trim() : ((Admin) user).getAdminLevel();
                    updated = new Admin(user.getUserId(), user.getUsername(), password, 
                                      emailField.getText().trim(), 
                                      phoneField.getText().trim(), 
                                      addressField.getText().trim(), 
                                      level);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Editing this user type is not supported.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                boolean ok = db.updateUser(updated);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "User updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    if (refreshCallback != null) refreshCallback.run();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update user", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(content, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(550, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Product ID (auto-generated)
        contentPanel.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1;
        JTextField idField = new JTextField(20);
        idField.setEditable(false);
        idField.setText(db.generateProductId("Ring"));
        contentPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Product Name
        contentPanel.add(new JLabel("Product Name:*"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        contentPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Category
        contentPanel.add(new JLabel("Category:*"), gbc);
        gbc.gridx = 1;
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Ring", "Necklace", "Bracelet", "Earrings"});
        categoryCombo.addActionListener(e -> {
            String newId = db.generateProductId((String) categoryCombo.getSelectedItem());
            idField.setText(newId);
        });
        contentPanel.add(categoryCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Material
        contentPanel.add(new JLabel("Material:*"), gbc);
        gbc.gridx = 1;
        JComboBox<String> materialCombo = new JComboBox<>(new String[]{"Gold", "Platinum", "Silver", "White Gold", "Pearl"});
        contentPanel.add(materialCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Price
        contentPanel.add(new JLabel("Price (Rp):*"), gbc);
        gbc.gridx = 1;
        JTextField priceField = new JTextField(20);
        contentPanel.add(priceField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Stock
        contentPanel.add(new JLabel("Stock:*"), gbc);
        gbc.gridx = 1;
        JTextField stockField = new JTextField(20);
        contentPanel.add(stockField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Weight
        contentPanel.add(new JLabel("Weight (grams):"), gbc);
        gbc.gridx = 1;
        JTextField weightField = new JTextField(20);
        weightField.setText("0.0");
        contentPanel.add(weightField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Has Gemstone
        contentPanel.add(new JLabel("Has Gemstone:"), gbc);
        gbc.gridx = 1;
        JCheckBox gemstoneCheckbox = new JCheckBox();
        contentPanel.add(gemstoneCheckbox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Gemstone Type
        contentPanel.add(new JLabel("Gemstone Type:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> gemstoneCombo = new JComboBox<>(new String[]{"None", "Diamond", "Sapphire", "Ruby", "Emerald", "Pearl"});
        gemstoneCombo.setEnabled(false);
        contentPanel.add(gemstoneCombo, gbc);

        // Enable/disable gemstone combo based on checkbox
        gemstoneCheckbox.addActionListener(e -> {
            gemstoneCombo.setEnabled(gemstoneCheckbox.isSelected());
            if (!gemstoneCheckbox.isSelected()) {
                gemstoneCombo.setSelectedIndex(0);
            }
        });

        gbc.gridx = 0;
        gbc.gridy++;

        // IMAGE UPLOAD SECTION
        contentPanel.add(new JLabel("Product Image:"), gbc);
        gbc.gridx = 1;
        
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        
        JLabel imagePathLabel = new JLabel("No image selected");
        imagePathLabel.setForeground(Color.GRAY);
        imagePathLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JButton browseButton = new JButton("📁 Browse Image");
        browseButton.setBackground(ADMIN_SECONDARY);
        browseButton.setForeground(Color.WHITE);
        browseButton.setFocusPainted(false);
        browseButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        // Preview panel untuk gambar
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(250, 250, 250));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewPanel.setPreferredSize(new Dimension(150, 150));
        
        JLabel previewLabel = new JLabel("🖼️ Preview", JLabel.CENTER);
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        
        // Variable untuk menyimpan file gambar
        final File[] selectedImageFile = {null};
        
        // Browse button action
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Product Image");
            
            // Filter untuk file gambar
            fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif", "bmp")
            );
            
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile[0] = fileChooser.getSelectedFile();
                imagePathLabel.setText(selectedImageFile[0].getName());
                imagePathLabel.setForeground(ADMIN_SUCCESS);
                
                // Show preview
                try {
                    ImageIcon previewIcon = new ImageIcon(selectedImageFile[0].getAbsolutePath());
                    Image scaledImage = previewIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaledImage));
                    previewLabel.setText("");
                } catch (Exception ex) {
                    previewLabel.setText("❌ Error");
                    previewLabel.setForeground(Color.RED);
                }
            }
        });
        
        imagePanel.add(imagePathLabel, BorderLayout.NORTH);
        imagePanel.add(browseButton, BorderLayout.CENTER);
        imagePanel.add(previewPanel, BorderLayout.SOUTH);
        
        contentPanel.add(imagePanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Description
        contentPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        contentPanel.add(descriptionScroll, gbc);

        gbc.gridheight = 1;
        gbc.gridy += 2;

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton saveButton = createAdminButton("💾 Save Product", ADMIN_ACCENT);
        JButton cancelButton = createAdminButton("Cancel", Color.GRAY);

        saveButton.addActionListener(e -> {
            if (saveProduct(
                idField.getText(),
                nameField.getText(),
                (String) categoryCombo.getSelectedItem(),
                (String) materialCombo.getSelectedItem(),
                priceField.getText(),
                stockField.getText(),
                descriptionArea.getText(),
                weightField.getText(),
                gemstoneCheckbox.isSelected(),
                (String) gemstoneCombo.getSelectedItem(),
                selectedImageFile[0]  // PASS SELECTED IMAGE FILE
            )) {
                dialog.dispose();
                loadProductsData(); // Refresh table
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    private boolean saveProduct(String productId, String name, String category, String material, 
                               String priceStr, String stockStr, String description, 
                               String weightStr, boolean hasGemstone, String gemstoneType,
                               File imageFile) {
        
        // Validation
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (*)", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);
            double weight = weightStr.isEmpty() ? 0.0 : Double.parseDouble(weightStr);

            if (price <= 0 || stock < 0) {
                JOptionPane.showMessageDialog(this, "Price must be positive and stock cannot be negative", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // TENTUKAN IMAGE PATH
            String imagePath;
            if (imageFile != null && imageFile.exists()) {
                // Jika user pilih gambar, copy ke folder products
                imagePath = copyImageToProducts(imageFile, productId);
                System.out.println("✅ Image saved: " + imagePath);
            } else {
                // Jika tidak pilih gambar, gunakan default path
                imagePath = "assets/images/products/" + productId + ".png";
                System.out.println("ℹ️ Using default image path: " + imagePath);
            }

            // GUNAKAN CONSTRUCTOR 11 PARAMETERS DENGAN IMAGE PATH
            Product product = new Product(
                productId, 
                name, 
                category, 
                material, 
                price, 
                stock, 
                description,
                imagePath,
                weight, 
                hasGemstone, 
                gemstoneType
            );

            // Save to database
            boolean success = db.addProduct(product);

            if (success) {
                String message = "Product '" + name + "' added successfully!\n" +
                               "Product ID: " + productId + "\n" +
                               "Image: " + (imageFile != null ? imageFile.getName() : "Default");
                JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add product", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and stock", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    /**
     * Copy image file ke folder products
     */
    private String copyImageToProducts(File sourceFile, String productId) {
        try {
            // Buat folder jika belum ada
            File productsDir = new File("assets/images/products/");
            if (!productsDir.exists()) {
                productsDir.mkdirs();
            }
            
            // Tentukan extension
            String extension = getFileExtension(sourceFile.getName());
            if (extension.isEmpty()) {
                extension = ".png"; // Default extension
            }
            
            // Nama file baru: PRODUCT_ID + extension
            String newFileName = productId + extension;
            File destFile = new File(productsDir, newFileName);
            
            // Copy file
            java.nio.file.Files.copy(
                sourceFile.toPath(), 
                destFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
            return "assets/images/products/" + newFileName;
            
        } catch (Exception e) {
            System.err.println("❌ Error copying image: " + e.getMessage());
            // Fallback ke default path
            return "assets/images/products/" + productId + ".png";
        }
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot == -1 ? "" : fileName.substring(lastDot);
    }

    /**
     * Change product image for the given table row (opens chooser, copies file and updates DB)
     */
    private void changeProductImageAtRow(int row) {
        if (row < 0) return;
        String productId = (String) productsTableModel.getValueAt(row, 0);
        Product p = db.getProductById(productId);
        if (p == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select New Product Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
        int res = fileChooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File selected = fileChooser.getSelectedFile();
        if (selected == null || !selected.exists()) {
            JOptionPane.showMessageDialog(this, "Selected file is not valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // copy file
        String newPath = copyImageToProducts(selected, productId);
        // Provide feedback for debugging
        System.out.println("📁 Copied image to: " + newPath + " (source: " + selected.getAbsolutePath() + ")");

        p.setImagePath(newPath);
        boolean ok = db.updateProduct(p);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Product image updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadProductsData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update product image in database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String productId = (String) productsTableModel.getValueAt(selectedRow, 0);
        Product product = db.getProductById(productId);
        
        if (product != null) {
            showEditProductDialog(product);
        }
    }

    private void showEditProductDialog(Product product) {
        JDialog dialog = new JDialog(this, "Edit Product - " + product.getName(), true);
        dialog.setSize(550, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Product ID (read-only)
        contentPanel.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1;
        JTextField idField = new JTextField(20);
        idField.setEditable(false);
        idField.setText(product.getProductId());
        contentPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Name
        contentPanel.add(new JLabel("Product Name:*"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(product.getName(), 20);
        contentPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Category
        contentPanel.add(new JLabel("Category:*"), gbc);
        gbc.gridx = 1;
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Ring", "Necklace", "Bracelet", "Earrings"});
        categoryCombo.setSelectedItem(product.getCategory());
        contentPanel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Material
        contentPanel.add(new JLabel("Material:*"), gbc);
        gbc.gridx = 1;
        JComboBox<String> materialCombo = new JComboBox<>(new String[]{"Gold", "Platinum", "Silver", "White Gold", "Pearl"});
        materialCombo.setSelectedItem(product.getMaterial());
        contentPanel.add(materialCombo, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Price
        contentPanel.add(new JLabel("Price (Rp):*"), gbc);
        gbc.gridx = 1;
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()), 20);
        contentPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Stock
        contentPanel.add(new JLabel("Stock:*"), gbc);
        gbc.gridx = 1;
        JTextField stockField = new JTextField(String.valueOf(product.getStock()), 20);
        contentPanel.add(stockField, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Weight
        contentPanel.add(new JLabel("Weight (grams):"), gbc);
        gbc.gridx = 1;
        JTextField weightField = new JTextField(String.valueOf(product.getWeight()), 20);
        contentPanel.add(weightField, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Has Gemstone
        contentPanel.add(new JLabel("Has Gemstone:"), gbc);
        gbc.gridx = 1;
        JCheckBox gemstoneCheckbox = new JCheckBox();
        gemstoneCheckbox.setSelected(product.hasGemstone());
        contentPanel.add(gemstoneCheckbox, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Gemstone Type
        contentPanel.add(new JLabel("Gemstone Type:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> gemstoneCombo = new JComboBox<>(new String[]{"None", "Diamond", "Sapphire", "Ruby", "Emerald", "Pearl"});
        gemstoneCombo.setEnabled(product.hasGemstone());
        gemstoneCombo.setSelectedItem(product.hasGemstone() ? product.getGemstoneType() : "None");
        contentPanel.add(gemstoneCombo, gbc);

        gemstoneCheckbox.addActionListener(e -> {
            gemstoneCombo.setEnabled(gemstoneCheckbox.isSelected());
            if (!gemstoneCheckbox.isSelected()) gemstoneCombo.setSelectedIndex(0);
        });

        gbc.gridx = 0; gbc.gridy++;

        // Image upload
        contentPanel.add(new JLabel("Product Image:"), gbc);
        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);

        JLabel imagePathLabel = new JLabel(product.getImagePath() != null ? new File(product.getImagePath()).getName() : "No image selected");
        imagePathLabel.setForeground(Color.GRAY);
        imagePathLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JButton browseButton = new JButton("📁 Browse Image");
        browseButton.setBackground(ADMIN_SECONDARY);
        browseButton.setForeground(Color.WHITE);
        browseButton.setFocusPainted(false);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(new Color(250, 250, 250));
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        previewPanel.setPreferredSize(new Dimension(150, 150));

        JLabel previewLabel = new JLabel("🖼️ Preview", JLabel.CENTER);
        previewLabel.setForeground(Color.GRAY);
        previewLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        previewPanel.add(previewLabel, BorderLayout.CENTER);

        final File[] selectedImageFile = {null};

        // If existing image exists, show preview
        try {
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                ImageIcon previewIcon = new ImageIcon(product.getImagePath());
                Image scaled = previewIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                previewLabel.setIcon(new ImageIcon(scaled));
                previewLabel.setText("");
            }
        } catch (Exception ex) {
            // ignore
        }

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Product Image");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif", "bmp"));
            int result = fileChooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedImageFile[0] = fileChooser.getSelectedFile();
                imagePathLabel.setText(selectedImageFile[0].getName());
                imagePathLabel.setForeground(ADMIN_SUCCESS);
                try {
                    ImageIcon previewIcon = new ImageIcon(selectedImageFile[0].getAbsolutePath());
                    Image scaledImage = previewIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                    previewLabel.setIcon(new ImageIcon(scaledImage));
                    previewLabel.setText("");
                } catch (Exception ex) {
                    previewLabel.setText("❌ Error");
                    previewLabel.setForeground(Color.RED);
                }
            }
        });

        imagePanel.add(imagePathLabel, BorderLayout.NORTH);
        imagePanel.add(browseButton, BorderLayout.CENTER);
        imagePanel.add(previewPanel, BorderLayout.SOUTH);
        contentPanel.add(imagePanel, gbc);

        gbc.gridx = 0; gbc.gridy++;

        // Description
        contentPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridheight = 2;
        JTextArea descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setText(product.getDescription());
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        contentPanel.add(descriptionScroll, gbc);

        gbc.gridheight = 1;
        gbc.gridy += 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton saveButton = createAdminButton("💾 Save Changes", ADMIN_ACCENT);
        JButton cancelButton = createAdminButton("Cancel", Color.GRAY);

        saveButton.addActionListener(e -> {
            // Validate and save
            if (nameField.getText().isEmpty() || priceField.getText().isEmpty() || stockField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields (*)", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double price = Double.parseDouble(priceField.getText());
                int stock = Integer.parseInt(stockField.getText());
                double weight = weightField.getText().isEmpty() ? 0.0 : Double.parseDouble(weightField.getText());

                String imagePath = product.getImagePath();
                if (selectedImageFile[0] != null && selectedImageFile[0].exists()) {
                    imagePath = copyImageToProducts(selectedImageFile[0], product.getProductId());
                }

                Product updated = new Product(
                    product.getProductId(),
                    nameField.getText(),
                    (String) categoryCombo.getSelectedItem(),
                    (String) materialCombo.getSelectedItem(),
                    price,
                    stock,
                    descriptionArea.getText(),
                    imagePath,
                    weight,
                    gemstoneCheckbox.isSelected(),
                    (String) gemstoneCombo.getSelectedItem()
                );

                boolean ok = db.updateProduct(updated);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Product updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadProductsData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update product", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for price and stock", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void deleteSelectedProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String productId = (String) productsTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productsTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + productName + "'?\nThis action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = db.deleteProduct(productId);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Product '" + productName + "' deleted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadProductsData(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete product", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to logout?", "Confirm Logout", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            db.logout();
            this.dispose();
            new WelcomeFrame().setVisible(true);
        }
    }
    
    // Custom cell renderer untuk center alignment
    private class CenterAlignedRenderer extends DefaultTableCellRenderer {
        public CenterAlignedRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
}
