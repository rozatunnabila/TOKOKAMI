package gui;

import database.DatabaseManager;
import model.User;
import model.Customer;
import model.OrderItem;
import model.Product;
import model.ShoppingCart;
import model.CartItem;
import utils.CurrencyUtils;
import utils.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.Order;

public class CustomerDashboard extends JFrame {
    private User user;
    private DatabaseManager db;
    private JTabbedPane tabbedPane;
    private JPanel productsPanel;
    private String currentCategory = "All";
    
    // Cart related variables
    private JPanel cartItemsPanel;
    private JPanel summaryPanel;
    private JLabel totalItemsLabel;
    private JLabel subtotalLabel;
    private JLabel shippingLabel;
    private JLabel totalLabel;
    private JPanel ordersPanel;

    // ✅ FIX: Inisialisasi selectedCartItems
    private Map<Integer, Boolean> selectedCartItems = new HashMap<>();
    private JLabel selectedItemsLabel;
    
    public CustomerDashboard(User user) {
        this.user = user;
        this.db = DatabaseManager.getInstance();
        
        initializeFrame();
        setupUI();
        
        // Tunda loading produk sampai UI ter-render sempurna
        SwingUtilities.invokeLater(() -> {
            loadProductsByCategory("All");
        });
    }
    
    private void initializeFrame() {
        setTitle("Arlene Jewelry Shop - Customer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private void setupUI() {
        // Create main panel with elegant background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Elegant light gold to white gradient
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 250, 240),
                    getWidth(), getHeight(), new Color(255, 255, 255)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabbed content
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(250, 250, 250));
        tabbedPane.setForeground(new Color(139, 69, 19));
        
        tabbedPane.addTab("🏠 Home", createHomePanel());
        tabbedPane.addTab("💎 Products", createProductsPanel());
        tabbedPane.addTab("🛒 Cart", createCartPanel());
        tabbedPane.addTab("📊 Orders", createHistoryPanel());
        tabbedPane.addTab("👤 Profile", createProfilePanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(139, 69, 19));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 70));
        
        // Logo and Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("💎 ARLENE JEWELRY");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subLabel = new JLabel("Customer Dashboard");
        subLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        subLabel.setForeground(new Color(255, 215, 0));
        subLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subLabel);
        
        // User info and logout
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        String displayName = (user instanceof Customer) ? ((Customer) user).getFullName() : user.getUsername();
        JLabel welcomeLabel = new JLabel("Welcome, " + displayName);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JButton logoutButton = new JButton("🚪 Logout");
        logoutButton.setBackground(new Color(205, 133, 63));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        logoutButton.addActionListener(e -> logout());
        
        userPanel.add(welcomeLabel);
        userPanel.add(Box.createHorizontalStrut(20));
        userPanel.add(logoutButton);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createHomePanel() {
        // Main panel dengan scroll
        JPanel homePanel = new JPanel(new BorderLayout());
        homePanel.setBackground(Color.WHITE);
        
        // Scroll pane untuk seluruh content
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Content panel yang bisa di-scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Hero Section dengan Banner Image
        JPanel heroPanel = createBannerSection();
        contentPanel.add(heroPanel);
        
        // Featured Categories
        JPanel categoriesPanel = createCategoriesSection();
        contentPanel.add(categoriesPanel);
        
        // Featured Products
        JPanel featuredPanel = createFeaturedProductsSection();
        contentPanel.add(featuredPanel);
        
        // Add some spacing at the bottom
        contentPanel.add(Box.createVerticalStrut(30));
        
        scrollPane.setViewportView(contentPanel);
        homePanel.add(scrollPane, BorderLayout.CENTER);
        
        return homePanel;
    }

    private JPanel createBannerSection() {
        JPanel heroPanel = new JPanel(new BorderLayout());
        heroPanel.setPreferredSize(new Dimension(getWidth(), 400));
        heroPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        heroPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        heroPanel.setBackground(Color.WHITE);
        
        try {
            // Cari file banner image
            String bannerPath = findBannerImage();
            
            if (bannerPath != null) {
                // Load banner image dengan proper scaling
                ImageIcon bannerIcon = new ImageIcon(bannerPath);
                Image originalImage = bannerIcon.getImage();
                
                // Calculate scaled dimensions to maintain aspect ratio and fill width
                int panelWidth = getWidth();
                int panelHeight = 400;
                
                // Calculate scaled height based on aspect ratio
                double aspectRatio = (double) originalImage.getWidth(null) / originalImage.getHeight(null);
                int scaledWidth = panelWidth;
                int scaledHeight = (int) (panelWidth / aspectRatio);
                
                // If calculated height is less than panel height, adjust to fill height
                if (scaledHeight < panelHeight) {
                    scaledHeight = panelHeight;
                    scaledWidth = (int) (panelHeight * aspectRatio);
                }
                
                Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledBanner = new ImageIcon(scaledImage);
                
                JLabel bannerLabel = new JLabel(scaledBanner);
                bannerLabel.setHorizontalAlignment(JLabel.LEFT);
                bannerLabel.setVerticalAlignment(JLabel.TOP);
                
                // Create a container panel to ensure left alignment
                JPanel imageContainer = new JPanel(new BorderLayout());
                imageContainer.setBackground(Color.WHITE);
                imageContainer.add(bannerLabel, BorderLayout.WEST);
                
                heroPanel.add(imageContainer, BorderLayout.CENTER);
                
            } else {
                // Fallback ke banner default yang simple
                createSimpleBanner(heroPanel);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error loading banner image: " + e.getMessage());
            e.printStackTrace();
            createSimpleBanner(heroPanel);
        }
        
        return heroPanel;
    }

    private void createSimpleBanner(JPanel heroPanel) {
        // Buat banner default yang simple dan clean dengan alignment kiri
        JLabel simpleBanner = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Simple gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(139, 69, 19),
                    getWidth(), getHeight(), new Color(101, 67, 33)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Simple jewelry icon di kiri
                g2d.setColor(new Color(255, 215, 0));
                g2d.setFont(new Font("Serif", Font.BOLD, 80));
                FontMetrics fm = g2d.getFontMetrics();
                String icon = "💎";
                int x = 50;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(icon, x, y);
                
                // Add text to the right of the icon
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Serif", Font.BOLD, 36));
                String text = "Arlene Jewelry Shop";
                int textX = x + fm.stringWidth(icon) + 20;
                int textY = y;
                g2d.drawString(text, textX, textY);
                
                g2d.setFont(new Font("SansSerif", Font.ITALIC, 18));
                String subText = "Luxury Jewelry Collection";
                FontMetrics subFm = g2d.getFontMetrics();
                int subTextY = textY + subFm.getHeight() + 10;
                g2d.drawString(subText, textX, subTextY);
            }
        };
        
        simpleBanner.setPreferredSize(new Dimension(getWidth(), 400));
        simpleBanner.setHorizontalAlignment(JLabel.LEFT);
        
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.add(simpleBanner, BorderLayout.WEST);
        
        heroPanel.add(container, BorderLayout.CENTER);
    }

    private String findBannerImage() {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".bmp", ".gif"};
        
        // Cari file banner di berbagai lokasi
        String[] searchPaths = {
            "assets/images/banner",
            "assets/images/promo", 
            "assets/images/main",
            "assets/images/hero",
            "assets/images/welcome",
            "assets/images/bck",
            "images/banner",
            "images/promo",
            "banner",
            "promo"
        };
        
        for (String path : searchPaths) {
            for (String ext : imageExtensions) {
                String testPath = path + ext;
                if (new File(testPath).exists()) {
                    System.out.println("✅ Found banner image: " + testPath);
                    return testPath;
                }
            }
        }
        
        // Coba cari file dengan pattern banner*
        File assetsDir = new File("assets/images");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            File[] files = assetsDir.listFiles((dir, name) -> 
                name.toLowerCase().startsWith("banner") || 
                name.toLowerCase().startsWith("promo") ||
                name.toLowerCase().startsWith("main") ||
                name.toLowerCase().startsWith("hero")
            );
            
            if (files != null && files.length > 0) {
                System.out.println("✅ Found banner image: " + files[0].getPath());
                return files[0].getPath();
            }
        }
        
        System.out.println("❌ No banner image found, using default banner");
        return null;
    }

    private JPanel createCategoriesSection() {
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBackground(Color.WHITE);
        categoriesPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 30, 50));
        categoriesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        // Section title
        JLabel titleLabel = new JLabel("🛍️ Shop by Category", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(139, 69, 19));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Categories buttons container
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String[] categories = {"All", "Rings", "Necklaces", "Bracelets", "Earrings"};
        for (String category : categories) {
            JButton categoryBtn = createCategoryButton(category);
            buttonsPanel.add(categoryBtn);
            buttonsPanel.add(Box.createHorizontalStrut(15));
        }
        
        categoriesPanel.add(titleLabel);
        categoriesPanel.add(buttonsPanel);
        
        return categoriesPanel;
    }

    private JPanel createFeaturedProductsSection() {
        JPanel featuredPanel = new JPanel();
        featuredPanel.setLayout(new BoxLayout(featuredPanel, BoxLayout.Y_AXIS));
        featuredPanel.setBackground(Color.WHITE);
        featuredPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 50, 50));
        
        // Section title
        JLabel featuredLabel = new JLabel("✨ Featured Products", JLabel.CENTER);
        featuredLabel.setFont(new Font("Serif", Font.BOLD, 28));
        featuredLabel.setForeground(new Color(139, 69, 19));
        featuredLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        featuredLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        // Products grid dengan GridLayout untuk 2 baris x 4 kolom
        JPanel featuredGrid = new JPanel(new GridLayout(2, 4, 20, 20));
        featuredGrid.setBackground(Color.WHITE);
        featuredGrid.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Load featured products dari database - ambil 8 produk pertama
        List<Product> allProducts = getProductsFromDatabase();
        int productCount = Math.min(8, allProducts.size());
        List<Product> featuredProducts = allProducts.subList(0, productCount);
        
        for (Product product : featuredProducts) {
            JPanel productCard = createProductCard(product, true);
            featuredGrid.add(productCard);
        }
        
        // Jika produk kurang dari 8, tambahkan panel kosong
        for (int i = featuredProducts.size(); i < 8; i++) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Color.WHITE);
            featuredGrid.add(emptyPanel);
        }
        
        featuredPanel.add(featuredLabel);
        featuredPanel.add(featuredGrid);
        
        // View All button
        JPanel viewAllPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        viewAllPanel.setBackground(Color.WHITE);
        viewAllPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton viewAllButton = createStyledButton("View All Products →", new Color(139, 69, 19));
        viewAllButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        viewAllButton.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        viewAllButton.addActionListener(e -> {
            tabbedPane.setSelectedIndex(1);
            loadProductsByCategory("All");
        });
        
        viewAllPanel.add(viewAllButton);
        featuredPanel.add(viewAllPanel);
        
        return featuredPanel;
    }
    
    private JButton createCategoryButton(String category) {
        JButton button = new JButton(getCategoryIcon(category) + " " + category);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(139, 69, 19));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            // Switch to Products tab and filter by category
            tabbedPane.setSelectedIndex(1);
            loadProductsByCategory(category);
        });
        
        return button;
    }
    
    private String getCategoryIcon(String category) {
        switch (category) {
            case "Rings": return "💍";
            case "Necklaces": return "📿";
            case "Bracelets": return "📿";
            case "Earrings": return "👂";
            case "All": return "💎";
            default: return "✨";
        }
    }
    
    private JPanel createProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Categories Navigation
        JPanel categoriesNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        categoriesNav.setBackground(new Color(250, 245, 240));
        categoriesNav.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        String[] categories = {"All", "Rings", "Necklaces", "Bracelets", "Earrings"};
        for (String category : categories) {
            JButton catButton = createCategoryNavButton(category);
            categoriesNav.add(catButton);
        }
        
        // Search and filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JTextField searchField = new JTextField(25);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        JComboBox<String> sortCombo = new JComboBox<>(new String[]{"Price: Low to High", "Price: High to Low", "Newest First"});
        
        JButton searchButton = createStyledButton("🔍 Search", new Color(139, 69, 19));
        JButton clearButton = createStyledButton("Clear", new Color(100, 100, 100));
        
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                searchProducts(searchText);
            } else {
                loadProductsByCategory(currentCategory);
            }
        });
        
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadProductsByCategory(currentCategory);
        });
        
        filterPanel.add(new JLabel("Search:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Sort by:"));
        filterPanel.add(sortCombo);
        filterPanel.add(searchButton);
        filterPanel.add(clearButton);
        
        // Products panel
        productsPanel = new JPanel(new GridLayout(0, 4, 25, 25));
        productsPanel.setBackground(Color.WHITE);
        productsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Top panel untuk kategori dan filter
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(categoriesNav, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createCategoryNavButton(String category) {
        JButton button = new JButton(category);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(currentCategory.equals(category) ? new Color(139, 69, 19) : new Color(200, 200, 200));
        button.setForeground(currentCategory.equals(category) ? Color.WHITE : Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addActionListener(e -> {
            currentCategory = category;
            loadProductsByCategory(category);
            // Update all buttons
            Component[] comps = button.getParent().getComponents();
            for (Component comp : comps) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    boolean isActive = btn.getText().equals(category);
                    btn.setBackground(isActive ? new Color(139, 69, 19) : new Color(200, 200, 200));
                    btn.setForeground(isActive ? Color.WHITE : Color.BLACK);
                }
            }
        });
        
        return button;
    }
    
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("🛒 My Shopping Cart");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(139, 69, 19));
        
        JButton refreshButton = createStyledButton("🔄 Refresh Cart", new Color(139, 69, 19));
        refreshButton.addActionListener(e -> loadCartData());
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // Cart content panel
        JPanel cartContentPanel = new JPanel(new BorderLayout());
        cartContentPanel.setBackground(Color.WHITE);

        // Cart items panel
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Color.WHITE);
        cartItemsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(800, 500));

        // Summary panel
        summaryPanel = createSummaryPanel();

        cartContentPanel.add(scrollPane, BorderLayout.CENTER);
        cartContentPanel.add(summaryPanel, BorderLayout.SOUTH);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(cartContentPanel, BorderLayout.CENTER);

        // Load initial cart data
        loadCartData();

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
    
        // Summary details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        detailsPanel.setBackground(new Color(250, 250, 250));
    
        // Selected items label
        selectedItemsLabel = new JLabel("0 items selected");
        selectedItemsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        selectedItemsLabel.setForeground(new Color(139, 69, 19));
        
        totalItemsLabel = new JLabel("0 items");
        totalItemsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        subtotalLabel = new JLabel(CurrencyUtils.format(0));
        subtotalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        shippingLabel = new JLabel(CurrencyUtils.format(0));
        shippingLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        totalLabel = new JLabel(CurrencyUtils.format(0));
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 100, 0));
    
        // Tambah row untuk selected items
        detailsPanel.add(new JLabel("Selected Items:"));
        detailsPanel.add(selectedItemsLabel);
        detailsPanel.add(new JLabel("Total Items:"));
        detailsPanel.add(totalItemsLabel);
        detailsPanel.add(new JLabel("Subtotal:"));
        detailsPanel.add(subtotalLabel);
        detailsPanel.add(new JLabel("Shipping:"));
        detailsPanel.add(shippingLabel);
        detailsPanel.add(new JLabel("Total Amount:"));
        detailsPanel.add(totalLabel);
    
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(250, 250, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
    
        // Select All / Deselect All button
        JButton selectAllButton = createStyledButton("✅ Select All", new Color(70, 130, 180));
        JButton clearButton = createStyledButton("🗑️ Clear Cart", new Color(220, 20, 60));
        JButton checkoutButton = createStyledButton("💳 Checkout Selected", new Color(0, 100, 0));
    
        selectAllButton.addActionListener(e -> toggleSelectAll());
        clearButton.addActionListener(e -> clearCart());
        checkoutButton.addActionListener(e -> checkoutSelectedItems());
    
        buttonPanel.add(selectAllButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(checkoutButton);
    
        panel.add(detailsPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        return panel;
    }

    private void toggleSelectAll() {
        ShoppingCart cart = db.getCurrentUserCart();
        if (cart == null || cart.isEmpty()) return;
        
        boolean allSelected = cart.getItems().stream()
                .allMatch(item -> selectedCartItems.getOrDefault(item.getCartItemId(), false));
        
        // Jika semua selected, deselect semua. Jika tidak, select semua.
        boolean newSelection = !allSelected;
        
        for (CartItem item : cart.getItems()) {
            selectedCartItems.put(item.getCartItemId(), newSelection);
        }
        
        // Refresh cart display
        loadCartData();
    }

    private void checkoutSelectedItems() {
    ShoppingCart cart = db.getCurrentUserCart();
    if (cart == null || cart.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Your cart is empty. Please add some items first.", 
            "Empty Cart", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    // Filter hanya items yang dipilih
    List<CartItem> selectedItems = cart.getItems().stream()
            .filter(item -> selectedCartItems.getOrDefault(item.getCartItemId(), false))
            .collect(Collectors.toList());
    
    if (selectedItems.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Please select at least one item to checkout.", 
            "No Items Selected", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Hitung total untuk selected items saja
    double selectedTotal = selectedItems.stream()
            .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    
    // Tampilkan dialog checkout
    String[] paymentMethods = {"QRIS", "COD", "Transfer"};
    String selectedMethod = (String) JOptionPane.showInputDialog(this,
        "Select payment method for " + selectedItems.size() + " selected items:\nTotal Amount: " + CurrencyUtils.format(selectedTotal),
        "Checkout Selected Items",
        JOptionPane.QUESTION_MESSAGE, null, paymentMethods, paymentMethods[0]);
    
    if (selectedMethod != null) {
        // Get shipping address
        String defaultAddress = "Jl. Test Alamat No. 123, Jakarta";
        String shippingAddress = (String) JOptionPane.showInputDialog(this,
            "Enter shipping address:",
            "Shipping Address",
            JOptionPane.QUESTION_MESSAGE, null, null, defaultAddress);
        
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Shipping address is required!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get notes (optional)
        String notes = JOptionPane.showInputDialog(this,
            "Additional notes (optional):",
            "Order Notes", 
            JOptionPane.QUESTION_MESSAGE);

        // Confirm checkout
        int confirm = JOptionPane.showConfirmDialog(this,
            "CONFIRM CHECKOUT FOR SELECTED ITEMS:\n\n" +
            "📦 Selected Items (" + selectedItems.size() + "):\n" + getItemsDetails(selectedItems) +
            "💰 Total Amount: " + CurrencyUtils.format(selectedTotal) + "\n" +
            "💳 Payment Method: " + selectedMethod + "\n" +
            "🏠 Shipping Address: " + shippingAddress + "\n\n" +
            "Only selected items will be purchased.",
            "Confirm Checkout Selected Items", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // ✅ PANGGIL METHOD DATABASE YANG BENAR
                System.out.println("🛒 Starting checkout process...");
                System.out.println("   Selected items: " + selectedItems.size());
                System.out.println("   Total amount: " + CurrencyUtils.format(selectedTotal));
                
                boolean success = db.createOrderFromSelectedItems(selectedItems, selectedMethod, shippingAddress, notes, selectedTotal);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "🎉 ORDER PLACED SUCCESSFULLY!\n\n" +
                        "Thank you for your purchase!\n" +
                        "📦 Items Ordered: " + selectedItems.size() + " products\n" +
                    "💰 Order Total: " + CurrencyUtils.format(selectedTotal) + "\n" +
                        "💳 Payment Method: " + selectedMethod + "\n" +
                        "🏠 Shipping to: " + shippingAddress + "\n\n" +
                        "Selected items have been removed from cart.",
                        "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    
                    // ✅ REFRESH DATA
                    loadCartData(); // Refresh cart
                    loadOrderHistory(); // Refresh orders
                    tabbedPane.setSelectedIndex(3); // Switch to Orders tab
                    
            } else {
                String errorMsg = db.getLastError();
                String displayMsg = "❌ Failed to process order.\n\n";
                
                if (errorMsg != null && !errorMsg.isEmpty()) {
                    displayMsg += "Error Details:\n" + errorMsg + "\n\n";
                } else {
                    displayMsg += "Please check the console for error details.\n\n";
                }
                
                displayMsg += "Make sure:\n" +
                             "• Database is connected\n" +
                             "• Products are available\n" +
                             "• Cart items are valid\n\n" +
                             "Please try again or contact support.";
                
                JOptionPane.showMessageDialog(this,
                    displayMsg, 
                    "Checkout Error", JOptionPane.ERROR_MESSAGE);
                
                db.clearLastError();
            }
            } catch (Exception e) {
                System.err.println("❌ Exception during checkout: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "❌ Error during checkout:\n\n" +
                    e.getMessage() + "\n\n" +
                    "Please check the console for details.",
                    "Checkout Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

    private String getItemsDetails(List<CartItem> items) {
        StringBuilder details = new StringBuilder();
        for (CartItem item : items) {
            details.append("• ").append(item.getProduct().getName())
                .append(" x").append(item.getQuantity())
                .append(" - ").append(item.getProduct().getFormattedPrice())
                .append("\n");
        }
        return details.toString();
    }

    // ✅ FIX: Tambahkan method createOrderForSelectedItems yang hilang
    private boolean createOrderForSelectedItems(List<CartItem> selectedItems, String paymentMethod, String shippingAddress, String notes) {
        try {
            // Hitung total untuk selected items
            double totalAmount = selectedItems.stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
            
            // Simulasi create order - hapus selected items dari cart
            for (CartItem item : selectedItems) {
                db.removeFromCart(item.getCartItemId());
            }
            
            System.out.println("✅ Created order for " + selectedItems.size() + " selected items");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Error creating order for selected items: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadCartData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private ShoppingCart cart;
            private double subtotal;
            private double shipping;
            private double total;
            private int totalItems;

            @Override
            protected Void doInBackground() throws Exception {
                cart = db.getCurrentUserCart();
                if (cart != null) {
                    subtotal = cart.getTotalAmount();
                    shipping = subtotal > 0 ? 10.00 : 0.00;
                    total = subtotal + shipping;
                    totalItems = cart.getTotalItems();
                    System.out.println("🛒 Cart loaded: " + totalItems + " items, Total: " + CurrencyUtils.format(total));
                } else {
                    System.out.println("🛒 Cart is empty or not found");
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    cartItemsPanel.removeAll();
                    selectedCartItems = new HashMap<>(); // Reset selected items
                    
                    if (cart == null || cart.isEmpty()) {
                        // Show empty cart message
                        JPanel emptyPanel = new JPanel(new BorderLayout());
                        emptyPanel.setBackground(Color.WHITE);
                        emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
                        
                        JLabel emptyLabel = new JLabel("Your shopping cart is empty", JLabel.CENTER);
                        emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
                        emptyLabel.setForeground(Color.GRAY);
                        
                        JLabel suggestionLabel = new JLabel("Browse our products and add some items to get started!", JLabel.CENTER);
                        suggestionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                        suggestionLabel.setForeground(Color.LIGHT_GRAY);
                        
                        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
                        emptyPanel.add(suggestionLabel, BorderLayout.SOUTH);
                        
                        cartItemsPanel.add(emptyPanel);
                    } else {
                        // Add cart items dengan checkbox
                        for (CartItem item : cart.getItems()) {
                            JPanel itemPanel = createCartItemPanel(item);
                            cartItemsPanel.add(itemPanel);
                            cartItemsPanel.add(Box.createVerticalStrut(10));
                        }
                        
                        // Initialize semua item sebagai selected
                        for (CartItem item : cart.getItems()) {
                            selectedCartItems.put(item.getCartItemId(), true);
                        }
                    }

                    updateCartSummary(); // Update summary dengan selected items
                    cartItemsPanel.revalidate();
                    cartItemsPanel.repaint();

                } catch (Exception e) {
                    System.err.println("❌ Error loading cart: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(CustomerDashboard.this,
                        "Error loading cart: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private JPanel createCartItemPanel(CartItem item) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
    
        Product product = item.getProduct();
    
        // LEFT SIDE: Checkbox + Product info
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
    
        // CHECKBOX untuk select item
        JCheckBox selectCheckbox = new JCheckBox();
        selectCheckbox.setSelected(selectedCartItems.getOrDefault(item.getCartItemId(), true)); // Default selected
        selectCheckbox.setBackground(Color.WHITE);
        selectCheckbox.addActionListener(e -> {
            selectedCartItems.put(item.getCartItemId(), selectCheckbox.isSelected());
            updateCartSummary(); // Update summary ketika selection berubah
        });
    
        // Product image
        JLabel imageLabel = new JLabel();
        ImageIcon productImage = ImageUtils.getProductImage(product.getImagePath(), 80, 80);
        imageLabel.setIcon(productImage);
    
        // Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
    
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    
        JLabel priceLabel = new JLabel("Price: " + product.getFormattedPrice());
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        priceLabel.setForeground(new Color(0, 100, 0));
    
        JLabel materialLabel = new JLabel(product.getMaterial() + " • " + product.getCategory());
        materialLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        materialLabel.setForeground(Color.GRAY);
    
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(materialLabel);
    
        // Panel untuk checkbox + image
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.add(selectCheckbox, BorderLayout.WEST);
        selectionPanel.add(Box.createHorizontalStrut(10));
        selectionPanel.add(imageLabel, BorderLayout.CENTER);
    
        leftPanel.add(selectionPanel, BorderLayout.WEST);
        leftPanel.add(Box.createHorizontalStrut(15));
        leftPanel.add(infoPanel, BorderLayout.CENTER);
    
        // RIGHT SIDE: Quantity controls and remove button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    
        // Quantity controls
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        quantityPanel.setBackground(Color.WHITE);
    
        JButton decreaseBtn = new JButton("-");
        decreaseBtn.setPreferredSize(new Dimension(30, 25));
        decreaseBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        decreaseBtn.addActionListener(e -> updateQuantity(item, item.getQuantity() - 1));
    
        JLabel quantityLabel = new JLabel(String.valueOf(item.getQuantity()));
        quantityLabel.setPreferredSize(new Dimension(40, 25));
        quantityLabel.setHorizontalAlignment(JLabel.CENTER);
        quantityLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        quantityLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    
        JButton increaseBtn = new JButton("+");
        increaseBtn.setPreferredSize(new Dimension(30, 25));
        increaseBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        increaseBtn.addActionListener(e -> updateQuantity(item, item.getQuantity() + 1));
    
        quantityPanel.add(decreaseBtn);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(increaseBtn);
    
        // Remove button
        JButton removeBtn = createStyledButton("🗑️ Remove", new Color(220, 20, 60));
        removeBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        removeBtn.setPreferredSize(new Dimension(100, 25));
        removeBtn.addActionListener(e -> removeItem(item));
    
        // Total price for this item
        double itemTotal = product.getPrice() * item.getQuantity();
        JLabel itemTotalLabel = new JLabel("Total: " + CurrencyUtils.format(itemTotal));
        itemTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        itemTotalLabel.setForeground(new Color(139, 69, 19));
    
        rightPanel.add(quantityPanel);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(removeBtn);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(itemTotalLabel);
    
        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
    
        return panel;
    }

    private void updateCartSummary() {
        ShoppingCart cart = db.getCurrentUserCart();
        if (cart == null || cart.isEmpty()) {
            return;
        }
        
        int totalSelectedItems = 0;
        double selectedSubtotal = 0.0;
        
        // Hitung hanya items yang dipilih
        for (CartItem item : cart.getItems()) {
            if (selectedCartItems.getOrDefault(item.getCartItemId(), true)) {
                totalSelectedItems += item.getQuantity();
                selectedSubtotal += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        
        double shipping = selectedSubtotal > 0 ? 10.00 : 0.00;
        double total = selectedSubtotal + shipping;
        
        // Update labels
        selectedItemsLabel.setText(totalSelectedItems + " items selected");
        totalItemsLabel.setText(totalSelectedItems + " items");
        subtotalLabel.setText(CurrencyUtils.format(selectedSubtotal));
        shippingLabel.setText(CurrencyUtils.format(shipping));
        totalLabel.setText(CurrencyUtils.format(total));
    }

    private void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(item);
            return;
        }

        // Check stock availability
        int availableStock = item.getProduct().getStock() + item.getQuantity();
        if (newQuantity > availableStock) {
            JOptionPane.showMessageDialog(this,
                "Only " + availableStock + " items available in stock",
                "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = db.updateCartItem(item.getCartItemId(), newQuantity);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Quantity updated successfully!", "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            loadCartData(); // Refresh cart display
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update quantity", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeItem(CartItem item) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove '" + item.getProduct().getName() + "' from cart?",
            "Confirm Removal", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = db.removeFromCart(item.getCartItemId());
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Item removed from cart", "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadCartData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to remove item", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearCart() {
        ShoppingCart cart = db.getCurrentUserCart();
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Cart is already empty", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Clear all items from cart? This action cannot be undone.",
            "Confirm Clear Cart", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = db.clearCart();
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Cart cleared successfully", "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadCartData();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to clear cart", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadOrderHistory() {
        System.out.println("🔄 START loadOrderHistory()");
        
        if (ordersPanel == null) {
            System.err.println("❌ ordersPanel is NULL!");
            return;
        }

        SwingWorker<List<Order>, Void> worker = new SwingWorker<List<Order>, Void>() {
            @Override
            protected List<Order> doInBackground() throws Exception {
                try {
                    System.out.println("📊 Fetching orders from database...");
                    List<Order> orders = db.getUserOrders();
                    System.out.println("✅ Database returned: " + (orders != null ? orders.size() : "NULL") + " orders");
                    
                    if (orders == null) {
                        System.out.println("⚠️ Orders is null, returning empty list");
                        return new ArrayList<>();
                    }
                    return orders;
                    
                } catch (Exception e) {
                    System.err.println("❌ Error in doInBackground: " + e.getMessage());
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Order> orders = get();
                    System.out.println("🎯 Processing " + orders.size() + " orders in UI");
                    
                    // Clear existing content
                    ordersPanel.removeAll();
                    
                    if (orders.isEmpty()) {
                        System.out.println("📭 No orders found, showing empty state");
                        JPanel emptyPanel = createEmptyOrdersPanel();
                        ordersPanel.add(emptyPanel);
                    } else {
                        System.out.println("🎨 Creating order cards...");
                        
                        for (Order order : orders) {
                            JPanel orderCard = createOrderCard(order);
                            ordersPanel.add(orderCard);
                            ordersPanel.add(Box.createVerticalStrut(15));
                        }
                        
                        System.out.println("✅ Created " + orders.size() + " order cards");
                    }

                    // Refresh UI
                    ordersPanel.revalidate();
                    ordersPanel.repaint();
                    System.out.println("✅ Order history UI updated successfully");

                } catch (Exception e) {
                    System.err.println("❌ Error in done(): " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error in UI
                    ordersPanel.removeAll();
                    JLabel errorLabel = new JLabel(
                        "<html><div style='text-align: center; color: red; padding: 20px;'>" +
                        "❌ Error loading orders<br>" +
                        "<small>Please try again later</small>" +
                        "</div></html>", 
                        JLabel.CENTER
                    );
                    errorLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                    ordersPanel.add(errorLabel);
                    ordersPanel.revalidate();
                }
            }
        };
        
        worker.execute();
        System.out.println("🔄 END loadOrderHistory() - worker started");
    }

    private JPanel createEmptyOrdersPanel() {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        
        JLabel emptyLabel = new JLabel("You haven't placed any orders yet", JLabel.CENTER);
        emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 18));
        emptyLabel.setForeground(Color.GRAY);
        
        JLabel suggestionLabel = new JLabel("Complete a purchase to see your order history here!", JLabel.CENTER);
        suggestionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        suggestionLabel.setForeground(Color.LIGHT_GRAY);
        
        emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        emptyPanel.add(suggestionLabel, BorderLayout.SOUTH);
        
        return emptyPanel;
    }

    private JPanel createOrderCard(Order order) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Left side: Order info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);

        // HANDLE POTENTIAL NULL VALUES
        String orderId = (order.getOrderId() != null) ? order.getOrderId() : "N/A";
        String status = (order.getStatus() != null) ? order.getStatus() : "UNKNOWN";
        String paymentMethod = (order.getPaymentMethod() != null) ? order.getPaymentMethod() : "N/A";
        String date = (order.getFormattedDate() != null) ? order.getFormattedDate() : "N/A";
        String total = (order.getFormattedTotal() != null) ? order.getFormattedTotal() : CurrencyUtils.format(0);
        int itemCount = (order.getItems() != null) ? order.getItems().size() : 0;

        JLabel orderIdLabel = new JLabel("Order #" + orderId);
        orderIdLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        orderIdLabel.setForeground(new Color(139, 69, 19));

        JLabel dateLabel = new JLabel("Date: " + date);
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);

        JLabel statusLabel = new JLabel("Status: " + status);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(getStatusColor(status));

        JLabel paymentLabel = new JLabel("Payment: " + paymentMethod);
        paymentLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel itemsLabel = new JLabel("Items: " + itemCount + " products");
        itemsLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        leftPanel.add(orderIdLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(dateLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(statusLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(paymentLabel);
        leftPanel.add(Box.createVerticalStrut(4));
        leftPanel.add(itemsLabel);

        // Right side: Total amount and view details button
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel totalLabel = new JLabel("Total: " + total);
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 100, 0));

        JButton detailsButton = createStyledButton("📋 View Details", new Color(139, 69, 19));
        detailsButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        detailsButton.addActionListener(e -> showOrderDetails(order));

        rightPanel.add(totalLabel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(detailsButton);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private Color getStatusColor(String status) {
        if (status == null) return Color.GRAY;
        
        switch (status.toUpperCase()) {
            case "PENDING": return Color.ORANGE;
            case "PROCESSING": return Color.BLUE;
            case "SHIPPED": return new Color(0, 100, 0); // Dark green
            case "DELIVERED": return new Color(0, 150, 0); // Green
            case "CANCELLED": return Color.RED;
            default: return Color.GRAY;
        }
    }

    private void showOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();
        details.append("📦 ORDER DETAILS\n\n");
        details.append("Order ID: ").append(order.getOrderId()).append("\n");
        details.append("Date: ").append(order.getFormattedDate()).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        details.append("Payment: ").append(order.getPaymentMethod()).append("\n");
        
        if (order.getShippingAddress() != null) {
            details.append("Shipping: ").append(order.getShippingAddress()).append("\n");
        }
        
        details.append("\nITEMS:\n");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                if (item != null && item.getProduct() != null) {
                    String productName = item.getProduct().getName() != null ? item.getProduct().getName() : "Unknown Product";
                    details.append("• ").append(productName)
                           .append(" x").append(item.getQuantity())
                           .append(" - ").append(item.getFormattedSubtotal()).append("\n");
                } else {
                    details.append("• Invalid item data\n");
                }
            }
        } else {
            details.append("• No items found\n");
            // Debug info
            System.out.println("⚠️ Order " + order.getOrderId() + " has no items (items is " + 
                             (order.getItems() == null ? "null" : "empty") + ")");
        }
        
        details.append("\nTOTAL: ").append(order.getFormattedTotal());
        
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            details.append("\n\nNotes: ").append(order.getNotes());
        }
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JOptionPane.showMessageDialog(this, scrollPane, 
            "Order Details - " + order.getOrderId(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void checkout() {
        ShoppingCart cart = db.getCurrentUserCart();
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Your cart is empty. Please add some items first.", 
                "Empty Cart", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
    
        // Show payment method selection
        String[] paymentMethods = {"QRIS", "COD", "Transfer"};
        String selectedMethod = (String) JOptionPane.showInputDialog(this,
            "Select payment method:\nTotal Amount: " + CurrencyUtils.format(cart.getTotalAmount()),
            "Checkout - Payment Method",
            JOptionPane.QUESTION_MESSAGE, null, paymentMethods, paymentMethods[0]);
        
        if (selectedMethod != null) {
            // Get shipping address
            String defaultAddress = "Jl. Test Alamat No. 123, Jakarta";
            String shippingAddress = (String) JOptionPane.showInputDialog(this,
                "Enter shipping address:",
                "Shipping Address",
                JOptionPane.QUESTION_MESSAGE, null, null, defaultAddress);
            
            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Shipping address is required!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            // Get notes (optional)
            String notes = JOptionPane.showInputDialog(this,
                "Additional notes (optional):",
                "Order Notes", 
                JOptionPane.QUESTION_MESSAGE);
    
            // Confirm checkout
            int confirm = JOptionPane.showConfirmDialog(this,
                "CONFIRM YOUR ORDER:\n\n" +
                "📦 Total Items: " + cart.getTotalItems() + "\n" +
                "💰 Total Amount: " + CurrencyUtils.format(cart.getTotalAmount()) + "\n" +
                "💳 Payment Method: " + selectedMethod + "\n" +
                "🏠 Shipping Address: " + shippingAddress + "\n\n" +
                "This will create your order and clear the cart.",
                "Confirm Checkout", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = db.createOrderFromCart(cart, selectedMethod, shippingAddress, notes);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "🎉 ORDER PLACED SUCCESSFULLY!\n\n" +
                        "Thank you for your purchase!\n" +
                        "📦 Order Total: " + CurrencyUtils.format(cart.getTotalAmount()) + "\n" +
                        "💳 Payment Method: " + selectedMethod + "\n" +
                        "🏠 Shipping to: " + shippingAddress + "\n\n" +
                        "You can view your order history in the Orders tab.",
                        "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    
                    loadCartData(); // Refresh to show empty cart
                    
                    // Auto-switch to Orders tab
                    tabbedPane.setSelectedIndex(3);
                    loadOrderHistory(); // Refresh order history
                    
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to process order. Please try again.", 
                        "Checkout Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("📊 My Orders");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(139, 69, 19));
        
        JButton refreshButton = createStyledButton("🔄 Refresh", new Color(139, 69, 19));
        refreshButton.addActionListener(e -> {
            System.out.println("🔄 Manual refresh triggered");
            loadOrderHistory();
        });
    
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);
    
        // BUAT ORDERS PANEL YANG BENAR
        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(Color.WHITE);
        ordersPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(800, 500));
    
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
    
        // Load initial order data
        System.out.println("🚀 Initializing orders tab...");
        loadOrderHistory();
    
        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 12, 12, 12);
        
        // Profile title
        JLabel titleLabel = new JLabel("👤 My Profile", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(139, 69, 19));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Profile fields with better styling
        String fullName = (user instanceof Customer) ? ((Customer) user).getFullName() : user.getUsername();
        addProfileField(panel, gbc, "Full Name:", fullName, 1);
        addProfileField(panel, gbc, "Username:", user.getUsername(), 2);
        addProfileField(panel, gbc, "Email:", user.getEmail(), 3);
        addProfileField(panel, gbc, "Phone:", user.getPhone(), 4);
        addProfileField(panel, gbc, "Address:", user.getAddress(), 5);
        addProfileField(panel, gbc, "Member Since:", "2024", 6);
        
        // Load statistics from database
        int totalOrders = db.getUserTotalOrders();
        int totalItemsPurchased = db.getUserTotalItemsPurchased();
        
        addProfileField(panel, gbc, "Total Orders:", String.valueOf(totalOrders), 7);
        addProfileField(panel, gbc, "Total Items Purchased:", String.valueOf(totalItemsPurchased), 8);
        
        return panel;
    }
    
    private void addProfileField(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        fieldLabel.setForeground(new Color(100, 100, 100));
        panel.add(fieldLabel, gbc);
        
        gbc.gridx = 1;
        JTextField valueField = new JTextField(value);
        valueField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        valueField.setBackground(new Color(250, 250, 250));
        valueField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        valueField.setEditable(false);
        panel.add(valueField, gbc);
    }
    
    // METHOD YANG PALING PENTING: LOAD PRODUCTS DARI DATABASE
    private List<Product> getProductsFromDatabase() {
        System.out.println("📦 Loading products from database...");
        List<Product> products = db.getAllProducts();
        System.out.println("✅ Loaded " + products.size() + " products from database");
        
        // Debug: print semua produk dan kategorinya
        for (Product product : products) {
            System.out.println("   - " + product.getProductId() + ": " + product.getName() + 
                             " | Category: " + product.getCategory() + 
                             " | Stock: " + product.getStock());
        }
        
        return products;
    }
    
    // PERBAIKI METHOD LOAD PRODUCTS BY CATEGORY
private void loadProductsByCategory(String category) {
    System.out.println("🛍️ Loading products for category: " + category);
    
    // BERSIHKAN PRODUCTS PANEL
    productsPanel.removeAll();
    
    List<Product> filteredProducts;
    
    if ("All".equals(category)) {
        filteredProducts = getProductsFromDatabase();
    } else {
        // Convert display category to database category
        String dbCategory = convertToDbCategory(category);
        System.out.println("🔍 Filtering by database category: " + dbCategory);
        filteredProducts = getProductsFromDatabase().stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(dbCategory))
            .collect(Collectors.toList());
    } // ✅ FIX: TUTUP ELSE DI SINI
    
    System.out.println("✅ Found " + filteredProducts.size() + " products for category: " + category);
    
    // TAMBAHKAN PRODUCT CARDS
    for (Product product : filteredProducts) {
        JPanel productCard = createProductCard(product, false);
        productsPanel.add(productCard);
    }
    
    // JIKA TIDAK ADA PRODUK
    if (filteredProducts.isEmpty()) {
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(Color.WHITE);
        emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        
        JLabel noProducts = new JLabel(
            "<html><div style='text-align: center; color: gray;'>" +
            "No products found in " + category + " category<br>" +
            "<small>Please check back later</small>" +
            "</div></html>", 
            JLabel.CENTER
        );
        noProducts.setFont(new Font("SansSerif", Font.ITALIC, 16));
        emptyPanel.add(noProducts, BorderLayout.CENTER);
        productsPanel.add(emptyPanel);
    }
    
    // REVALIDATE DAN REPAINT
    productsPanel.revalidate();
    productsPanel.repaint();
    
    System.out.println("🎉 Products display updated for category: " + category);
}
    
    // METHOD KONVERSI CATEGORY
    private String convertToDbCategory(String displayCategory) {
    if (displayCategory == null) return "Ring";
    
    switch (displayCategory) {
        case "Rings": 
            return "Ring";
        case "Necklaces": 
            return "Necklace";
        case "Bracelets": 
            return "Bracelet";
        case "Earrings": 
            return "Earrings";
        default: 
            return "Ring";
    }
}
    
    private void searchProducts(String searchText) {
        System.out.println("🔍 Searching for: " + searchText);
        
        productsPanel.removeAll();
        
        List<Product> allProducts = getProductsFromDatabase();
        List<Product> searchResults = allProducts.stream()
            .filter(p -> p.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        p.getCategory().toLowerCase().contains(searchText.toLowerCase()) ||
                        p.getMaterial().toLowerCase().contains(searchText.toLowerCase()) ||
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchText.toLowerCase())))
            .collect(Collectors.toList());
        
        System.out.println("✅ Found " + searchResults.size() + " search results");
        
        for (Product product : searchResults) {
            JPanel productCard = createProductCard(product, false);
            productsPanel.add(productCard);
        }
        
        if (searchResults.isEmpty()) {
            JLabel noResults = new JLabel("No products found for: '" + searchText + "'", JLabel.CENTER);
            noResults.setFont(new Font("SansSerif", Font.ITALIC, 16));
            noResults.setForeground(Color.GRAY);
            productsPanel.add(noResults);
        }
        
        productsPanel.revalidate();
        productsPanel.repaint();
    }
    
    private JPanel createProductCard(Product product, boolean isFeatured) {
        int width = isFeatured ? 280 : 300;
        int height = isFeatured ? 380 : 420;
        
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(width, height));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Debug info
        System.out.println("🛍️ Creating card for: " + product.getName() + " | Category: " + product.getCategory());
        
        // Product image
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(width - 30, isFeatured ? 180 : 220));
        
        ImageIcon productImage = ImageUtils.getProductImage(product.getImagePath(), width - 30, isFeatured ? 180 : 220);
        imageLabel.setIcon(productImage);
        
        // Product info - GUNAKAN BOX LAYOUT UNTUK FLEXIBILITAS LEBIH BAIK
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        
        JLabel nameLabel = new JLabel("<html><div style='width:" + (width - 40) + "px'>" + product.getName() + "</div></html>");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, isFeatured ? 14 : 15));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel materialLabel = new JLabel("💎 " + product.getMaterial() + " • " + product.getCategory());
        materialLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        materialLabel.setForeground(Color.GRAY);
        materialLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel(product.getFormattedPrice());
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, isFeatured ? 16 : 18));
        priceLabel.setForeground(new Color(0, 100, 0));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stockLabel = new JLabel("📦 Stock: " + product.getStock() + " available");
        stockLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        stockLabel.setForeground(product.getStock() > 0 ? new Color(0, 100, 0) : Color.RED);
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(materialLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(stockLabel);
        
        if (!isFeatured && product.hasGemstone()) {
            JLabel gemstoneLabel = new JLabel("✨ " + product.getGemstoneType() + " Gemstone");
            gemstoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            gemstoneLabel.setForeground(new Color(139, 69, 19));
            gemstoneLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(gemstoneLabel);
        }
        
        // Add to cart button
        JButton addToCartButton = new JButton(isFeatured ? "🛒 Add to Cart" : "Add to Cart");
        addToCartButton.setBackground(new Color(139, 69, 19));
        addToCartButton.setForeground(Color.WHITE);
        addToCartButton.setFocusPainted(false);
        addToCartButton.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        addToCartButton.setFont(new Font("SansSerif", Font.BOLD, isFeatured ? 12 : 13));
        addToCartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        addToCartButton.addActionListener(e -> addToCart(product));
        
        card.add(imageLabel, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(addToCartButton, BorderLayout.SOUTH);
        
        return card;
    }
    
    // PERBAIKI ADD TO CART METHOD
    private void addToCart(Product product) {
        System.out.println("=== 🎯 CUSTOMER DASHBOARD ADD TO CART CALLED ===");
        System.out.println("📦 Product: " + product.getProductId() + " - " + product.getName());
        System.out.println("👤 Current user: " + (user != null ? user.getUsername() : "NULL"));
        
        System.out.println("=== 🛒 DEBUG ADD TO CART START ===");
        System.out.println("👤 Current user: " + (user != null ? user.getUsername() : "NULL"));
        System.out.println("📦 Product: " + product.getProductId() + " - " + product.getName());
        System.out.println("💰 Price: " + product.getPrice());
        System.out.println("📊 Stock: " + product.getStock());
        
        // Cek apakah user sudah login di DatabaseManager
        if (db.getCurrentUser() == null) {
            System.err.println("❌ ERROR: No user logged in DatabaseManager!");
            JOptionPane.showMessageDialog(this, 
                "Session error. Please logout and login again.", 
                "Session Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("✅ User is logged in DatabaseManager: " + db.getCurrentUser().getUserId());
        
        // Refresh product data dari database untuk mendapatkan stock terbaru
        System.out.println("🔄 Getting fresh product data from database...");
        Product currentProduct = db.getProductById(product.getProductId());
        if (currentProduct == null) {
            System.err.println("❌ ERROR: Product not found in database!");
            JOptionPane.showMessageDialog(this, 
                "Product not found in database!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("✅ Product found in database:");
        System.out.println("   Name: " + currentProduct.getName());
        System.out.println("   Price: " + currentProduct.getPrice());
        System.out.println("   Stock: " + currentProduct.getStock());
        
        if (currentProduct.getStock() <= 0) {
            System.err.println("❌ ERROR: Product out of stock!");
            JOptionPane.showMessageDialog(this, 
                "Sorry, this product is out of stock!", 
                "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Simple quantity input
        String quantityStr = JOptionPane.showInputDialog(this, 
            "How many '" + currentProduct.getName() + "' would you like to add?\n\n" +
            "Available stock: " + currentProduct.getStock() + " units\n" +
            "Price: " + currentProduct.getFormattedPrice() + " per unit", 
            "1");
        
        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr.trim());
                
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Please enter a positive number", 
                        "Invalid Quantity", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (quantity > currentProduct.getStock()) {
                    JOptionPane.showMessageDialog(this, 
                        "Only " + currentProduct.getStock() + " items available in stock", 
                        "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                System.out.println("🛒 Attempting to add to cart:");
                System.out.println("   Product: " + currentProduct.getName() + " x" + quantity);
                System.out.println("   User ID: " + db.getCurrentUser().getUserId());
                
                // PANGGIL METHOD ADD TO CART DARI DATABASE MANAGER
                boolean success = db.addToCart(currentProduct, quantity);
                
                System.out.println("✅ Add to cart result: " + success);
                
                if (success) {
                    System.out.println("🎉 SUCCESS: Item added to cart!");
                    
                    // TAMPILKAN DIALOG KONFIRMASI DENGAN OPSI
                    Object[] options = {"Continue Shopping", "View Cart"};
                    int choice = JOptionPane.showOptionDialog(this,
                        "✅ Successfully added to cart!\n\n" +
                        currentProduct.getName() + " x" + quantity + "\n" +
                        "Total: " + CurrencyUtils.format(currentProduct.getPrice() * quantity) + "\n\n" +
                        "What would you like to do next?",
                        "Added to Cart",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[1]);
                    
                    // Refresh products to update stock display
                    loadProductsByCategory(currentCategory);
                    
                    // Jika user pilih "View Cart" atau close dialog, arahkan ke cart
                    if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {
                        tabbedPane.setSelectedIndex(2); // Switch to cart tab
                        loadCartData(); // Refresh cart data
                    }
                    
                } else {
                    System.err.println("❌ ERROR: Failed to add item to cart");
                    JOptionPane.showMessageDialog(this, 
                        "Failed to add item to cart. Please try again.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                System.err.println("❌ ERROR: Invalid quantity input");
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid number", 
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("ℹ️ User cancelled quantity input");
        }
        System.out.println("=== 🛒 DEBUG ADD TO CART END ===");
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
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
}
