package database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import model.Admin;
import model.CartItem;
import model.Customer;
import model.Order;
import model.OrderItem;
import model.Product;
import model.ShoppingCart;
import model.User;
import utils.CurrencyUtils;

public class DatabaseManager {
    private static DatabaseManager instance;
    private UserDAO userDAO;
    private ProductDAO productDAO;
    private CartDAO cartDAO;
    private Map<String, User> users;
    private User currentUser;
    private Map<String, String> orderStatusOverrides;
    private final File orderStatusFile = new File("assets/exports/order-status.properties");
    private final File localCartDir = new File("assets/exports/local-carts");
    private String lastError; // Untuk menyimpan error message terakhir
    
    private DatabaseManager() {
        userDAO = new UserDAO();
        productDAO = new ProductDAO();
        cartDAO = new CartDAO();
        users = new HashMap<>();
        orderStatusOverrides = new HashMap<>();
        loadOrderStatusOverrides();
        loadUsersFromDatabase();
    }

    private void loadOrderStatusOverrides() {
        try {
            if (!orderStatusFile.exists()) return;
            Properties p = new Properties();
            try (FileInputStream fis = new FileInputStream(orderStatusFile)) {
                p.load(fis);
            }
            for (String name : p.stringPropertyNames()) {
                orderStatusOverrides.put(name, p.getProperty(name));
            }
            System.out.println("🔄 Loaded order status overrides: " + orderStatusOverrides.size());
        } catch (IOException e) {
            System.err.println("❌ Failed to load order status overrides: " + e.getMessage());
        }
    }

    private void saveOrderStatusOverrides() {
        try {
            if (!orderStatusFile.getParentFile().exists()) orderStatusFile.getParentFile().mkdirs();
            Properties p = new Properties();
            p.putAll(orderStatusOverrides);
            try (FileOutputStream fos = new FileOutputStream(orderStatusFile)) {
                p.store(fos, "Order status overrides (orderId=status)");
            }
            System.out.println("💾 Saved order status overrides: " + orderStatusOverrides.size());
        } catch (IOException e) {
            System.err.println("❌ Failed to save order status overrides: " + e.getMessage());
        }
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void loadUsersFromDatabase() {
        for (User user : userDAO.getAllUsers()) {
            users.put(user.getUsername(), user);
        }
        System.out.println("✅ Loaded " + users.size() + " users from database");
    }
    
    // ==================== USER MANAGEMENT METHODS ====================
    
    public User login(String username, String password) {
        User user = userDAO.login(username, password);
        if (user != null) {
            currentUser = user;
            users.put(username, user);
            System.out.println("✅ User logged in: " + user.getUserId() + " - " + user.getRole());
        }
        return user;
    }
    
    public boolean registerCustomer(String fullName, String username, String password, 
                                  String email, String phone, String address) {
        
        if (userDAO.usernameExists(username)) {
            return false;
        }
        
        String userId = userDAO.generateUserId("CUSTOMER");
        Customer customer = new Customer(userId, username, password, email, phone, address, fullName);
        
        boolean success = userDAO.registerCustomer(customer);
        
        if (success) {
            users.put(username, customer);
            System.out.println("✅ New user registered: " + username);
        }
        
        return success;
    }
    
    public boolean isUsernameAvailable(String username) {
        return !userDAO.usernameExists(username);
    }
    
    public boolean isEmailAvailable(String email) {
        return !userDAO.emailExists(email);
    }
    
    public void logout() {
        currentUser = null;
    }
    
    // ==================== PRODUCT MANAGEMENT METHODS ====================
    
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    public Product getProductById(String productId) {
        return productDAO.getProductById(productId);
    }
    
    public List<Product> getProductsByCategory(String category) {
        return productDAO.getProductsByCategory(category);
    }
    
    public boolean addProduct(Product product) {
        boolean success = productDAO.addProduct(product);
        if (success) {
            System.out.println("✅ Product added to database: " + product.getProductId());
        } else {
            System.err.println("❌ Failed to add product: " + product.getProductId());
        }
        return success;
    }
    
    public boolean updateProduct(Product product) {
        boolean success = productDAO.updateProduct(product);
        if (success) {
            System.out.println("✅ Product updated in database: " + product.getProductId());
        } else {
            System.err.println("❌ Failed to update product: " + product.getProductId());
        }
        return success;
    }
    
    public boolean deleteProduct(String productId) {
        boolean success = productDAO.deleteProduct(productId);
        if (success) {
            System.out.println("🗑️ Product deleted from database: " + productId);
        } else {
            System.err.println("❌ Failed to delete product: " + productId);
        }
        return success;
    }
    
    public boolean productExists(String productId) {
        return productDAO.productExists(productId);
    }
    
    public String generateProductId(String category) {
        return productDAO.generateProductId(category);
    }
    
    public boolean updateProductStock(String productId, int newStock) {
        return productDAO.updateStock(productId, newStock);
    }
    
    // ==================== SHOPPING CART METHODS ====================
    
    public ShoppingCart getCurrentUserCart() {
        if (currentUser == null) {
            System.err.println("❌ No user logged in");
            return null;
        }
        System.out.println("👤 Getting cart for user: " + currentUser.getUserId());
        ShoppingCart cart = cartDAO.getOrCreateCart(currentUser.getUserId());
        System.out.println("🛒 Cart retrieved: " + (cart != null ? cart.getCartId() : "NULL"));
        // If this is a local cart, attempt to load persisted items from disk
        try {
            if (cart != null && cart.getCartId() != null && cart.getCartId().startsWith("LOCAL_CART_")) {
                List<CartItem> persisted = loadLocalCartItems(currentUser.getUserId());
                if (persisted != null && !persisted.isEmpty()) {
                    cart.setItems(persisted);
                    System.out.println("⚠️ Loaded " + persisted.size() + " items from local cart file for user: " + currentUser.getUserId());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed loading local cart for user: " + currentUser.getUserId() + " - " + e.getMessage());
        }
        return cart;
    }

    public boolean addToCart(Product product, int quantity) {
        System.out.println("=== 🗃️ DATABASE MANAGER ADD TO CART START ===");
        System.out.println("👤 Current User: " + (currentUser != null ? currentUser.getUserId() : "NULL"));
        System.out.println("📦 Product ID: " + product.getProductId());
        System.out.println("📦 Product Name: " + product.getName());
        System.out.println("🔢 Quantity: " + quantity);
        
        if (currentUser == null) {
            System.err.println("❌ ERROR: No user logged in DatabaseManager!");
            return false;
        }
        
        // ✅ STEP 1: PASTIKAN CART ADA
        ShoppingCart cart = getCurrentUserCart();
        if (cart == null) {
            System.err.println("❌ ERROR: Failed to get or create cart");
            return false;
        }
        System.out.println("🛒 Using cart: " + cart.getCartId());

        // If cart is local/in-memory (DB unavailable), handle add-to-cart locally
        if (cart.getCartId() != null && cart.getCartId().startsWith("LOCAL_CART_")) {
            try {
                List<CartItem> items = cart.getItems();
                // Try to find existing item
                CartItem existing = items.stream()
                        .filter(ci -> ci.getProduct().getProductId().equals(product.getProductId()))
                        .findFirst().orElse(null);

                if (existing != null) {
                    int newQty = existing.getQuantity() + quantity;
                    CartItem replacement = new CartItem(existing.getCartItemId(), cart.getCartId(), existing.getProduct(), newQty);
                    items.remove(existing);
                    items.add(replacement);
                } else {
                    int localId = (int) (-(System.currentTimeMillis() % Integer.MAX_VALUE));
                    CartItem newItem = new CartItem(localId, cart.getCartId(), product, quantity);
                    items.add(newItem);
                }
                cart.setItems(items);
                System.out.println("⚠️ Added to local in-memory cart: " + product.getProductId() + " x" + quantity);
                // persist local cart
                saveLocalCart(cart);
                return true;
            } catch (Exception ex) {
                System.err.println("❌ Failed to add to local cart: " + ex.getMessage());
                return false;
            }
        }
        
        // ✅ STEP 2: AMBIL DATA PRODUCT TERBARU DARI DATABASE
        System.out.println("🔄 Getting fresh product data from database...");
        Product currentProduct = productDAO.getProductById(product.getProductId());
        if (currentProduct == null) {
            System.err.println("❌ ERROR: Product not found in database: " + product.getProductId());
            return false;
        }
        
        System.out.println("📦 Product details from DB:");
        System.out.println("   Name: " + currentProduct.getName());
        System.out.println("   Price: " + currentProduct.getPrice());
        System.out.println("   Stock: " + currentProduct.getStock());
        System.out.println("   Category: " + currentProduct.getCategory());
        
        // ✅ STEP 3: VALIDASI STOCK
        if (currentProduct.getStock() < quantity) {
            System.err.println("❌ ERROR: Insufficient stock! Available: " + currentProduct.getStock() + ", Requested: " + quantity);
            return false;
        }
        
        // ✅ STEP 4: VALIDASI HARGA
        if (currentProduct.getPrice() <= 0) {
            System.err.println("❌ ERROR: Invalid price: " + currentProduct.getPrice());
            return false;
        }
        
        // ✅ STEP 5: ADD TO CART VIA CartDAO
        System.out.println("🛒 Calling CartDAO.addToCart()...");
        boolean success = cartDAO.addToCart(cart.getCartId(), product.getProductId(), quantity);
        System.out.println("🛒 CartDAO add result: " + success);
        
        // ✅ STEP 6: UPDATE STOCK JIKA BERHASIL
        if (success) {
            int newStock = currentProduct.getStock() - quantity;
            boolean stockUpdated = productDAO.updateStock(product.getProductId(), newStock);
            System.out.println("📦 Stock update result: " + stockUpdated);
            System.out.println("📦 Stock changed: " + currentProduct.getStock() + " → " + newStock);
            
            if (!stockUpdated) {
                System.err.println("❌ ERROR: Failed to update stock for product: " + product.getProductId());
                // Rollback: remove item from cart
                cartDAO.removeFromCart(cart.getCartId(), product.getProductId());
                return false;
            }
            
            System.out.println("✅ SUCCESS: Added to cart: " + product.getName() + " x" + quantity);
            return true;
        } else {
            System.err.println("❌ ERROR: Failed to add product to cart: " + product.getProductId());
            return false;
        }
    }
    
    public boolean updateCartItem(int cartItemId, int quantity) {
        if (currentUser == null) {
            System.err.println("❌ No user logged in");
            return false;
        }
        
        // Get current cart item to check stock
        ShoppingCart cart = getCurrentUserCart();
        if (cart == null) return false;
        
        CartItem currentItem = cart.getItems().stream()
                .filter(item -> item.getCartItemId() == cartItemId)
                .findFirst()
                .orElse(null);
        
            if (currentItem != null) {
            // ✅ PERBAIKAN: AMBIL DATA PRODUCT TERBARU
            Product currentProduct = productDAO.getProductById(currentItem.getProduct().getProductId());
            if (currentProduct == null) {
                System.err.println("❌ Product not found for update: " + currentItem.getProduct().getProductId());
                return false;
            }
            
            int stockChange = currentItem.getQuantity() - quantity;
            
            // Check if new quantity is valid
            if (currentProduct.getStock() + stockChange < 0) {
                System.err.println("❌ Insufficient stock for update");
                return false;
            }
            
            // If this is a local/in-memory cart, update locally
            if (cart.getCartId() != null && cart.getCartId().startsWith("LOCAL_CART_")) {
                try {
                    List<CartItem> items = cart.getItems();
                    CartItem replacement = new CartItem(currentItem.getCartItemId(), cart.getCartId(), currentItem.getProduct(), quantity);
                    items.remove(currentItem);
                    items.add(replacement);
                    cart.setItems(items);
                    // persist change
                    saveLocalCart(cart);
                    return true;
                } catch (Exception ex) {
                    System.err.println("❌ Failed to update local cart item: " + ex.getMessage());
                    return false;
                }
            }

            boolean success = cartDAO.updateCartItem(cartItemId, quantity);
            if (success) {
                // Update product stock dengan data terbaru
                productDAO.updateStock(currentProduct.getProductId(), currentProduct.getStock() + stockChange);
            }
            return success;
        }
        
        return false;
    }

    public boolean removeFromCart(int cartItemId) {
        if (currentUser == null) {
            System.err.println("❌ No user logged in");
            return false;
        }
        
        // Get item details before removing to restore stock
        ShoppingCart cart = getCurrentUserCart();
        if (cart != null) {
            CartItem itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getCartItemId() == cartItemId)
                    .findFirst()
                    .orElse(null);
            
            if (itemToRemove != null) {
                // ✅ PERBAIKAN: AMBIL DATA PRODUCT TERBARU
                Product currentProduct = productDAO.getProductById(itemToRemove.getProduct().getProductId());
                if (currentProduct != null) {
                    // Restore stock dengan data terbaru
                    productDAO.updateStock(currentProduct.getProductId(), currentProduct.getStock() + itemToRemove.getQuantity());
                }
                // If this is a local cart, remove locally
                if (cart.getCartId() != null && cart.getCartId().startsWith("LOCAL_CART_")) {
                    try {
                        List<CartItem> items = cart.getItems();
                        items.removeIf(ci -> ci.getCartItemId() == cartItemId);
                        cart.setItems(items);
                        // persist change
                        saveLocalCart(cart);
                        return true;
                    } catch (Exception ex) {
                        System.err.println("❌ Failed to remove item from local cart: " + ex.getMessage());
                        return false;
                    }
                }
            }
        }

        return cartDAO.removeFromCart(cartItemId);
    }

    public boolean clearCart() {
        if (currentUser == null) {
            System.err.println("❌ No user logged in");
            return false;
        }
        
        ShoppingCart cart = getCurrentUserCart();
        if (cart == null) {
            return false;
        }
        
        // Restore stock for all items dengan data terbaru
        for (CartItem item : cart.getItems()) {
            Product currentProduct = productDAO.getProductById(item.getProduct().getProductId());
            if (currentProduct != null) {
                productDAO.updateStock(currentProduct.getProductId(), currentProduct.getStock() + item.getQuantity());
            }
        }

        // If local cart, just clear in-memory and remove persisted file
        if (cart.getCartId() != null && cart.getCartId().startsWith("LOCAL_CART_")) {
            String uid = cart.getUserId();
            cart.clear();
            File f = getLocalCartFile(uid);
            if (f.exists()) {
                try {
                    f.delete();
                } catch (Exception ex) {
                    System.err.println("❌ Failed to delete local cart file: " + ex.getMessage());
                }
            }
            return true;
        }

        return cartDAO.clearCart(cart.getCartId());
    }

    public boolean createTransaction(ShoppingCart cart, String paymentMethod) {
        if (currentUser == null || cart == null || cart.isEmpty()) {
            return false;
        }
        
        try {
            System.out.println("💳 Processing transaction...");
            System.out.println("   Cart: " + cart.getCartId());
            System.out.println("   User: " + currentUser.getUsername());
            System.out.println("   Items: " + cart.getTotalItems());
            System.out.println("   Total: " + CurrencyUtils.format(cart.getTotalAmount()));
            System.out.println("   Payment: " + paymentMethod);
            
            // Untuk sementara, langsung clear cart sebagai simulasi transaksi berhasil
            boolean success = clearCart();
            if (success) {
                System.out.println("✅ Transaction completed successfully!");
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing transaction: " + e.getMessage());
            // logged
        }
        
        return false;
    }

    // ---------------- Local cart persistence helpers ----------------
    private File getLocalCartFile(String userId) {
        try {
            if (!localCartDir.exists()) localCartDir.mkdirs();
        } catch (Exception ignored) {}
        return new File(localCartDir, userId + ".cart");
    }

    private void saveLocalCart(ShoppingCart cart) {
        if (cart == null || cart.getUserId() == null) return;
        File f = getLocalCartFile(cart.getUserId());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            for (CartItem item : cart.getItems()) {
                // store: cartItemId,productId,quantity
                bw.write(item.getCartItemId() + "," + item.getProduct().getProductId() + "," + item.getQuantity());
                bw.newLine();
            }
            bw.flush();
            System.out.println("💾 Saved local cart for user: " + cart.getUserId());
        } catch (IOException e) {
            System.err.println("❌ Failed to save local cart for user: " + cart.getUserId() + " - " + e.getMessage());
        }
    }

    private List<CartItem> loadLocalCartItems(String userId) {
        List<CartItem> items = new ArrayList<>();
        File f = getLocalCartFile(userId);
        if (!f.exists()) return items;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                try {
                    int cartItemId = Integer.parseInt(parts[0]);
                    String productId = parts[1];
                    int qty = Integer.parseInt(parts[2]);
                    Product p = productDAO.getProductById(productId);
                    if (p != null) {
                        CartItem ci = new CartItem(cartItemId, "LOCAL_CART_" + userId, p, qty);
                        items.add(ci);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            System.out.println("⚠️ Loaded local cart file for user " + userId + ", items=" + items.size());
        } catch (IOException e) {
            System.err.println("❌ Failed to load local cart file for user: " + userId + " - " + e.getMessage());
        }
        return items;
    }
    
    // ==================== ORDER MANAGEMENT METHODS ====================
    
    public List<Order> getUserOrders() {
        List<Order> orders = new ArrayList<>();
        
        if (currentUser == null) {
            System.err.println("❌ No user logged in");
            return orders;
        }
        
        try {
            System.out.println("📊 Getting user orders for: " + currentUser.getUserId());
            
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                System.err.println("❌ Database connection failed");
                return orders;
            }
            
            // Query untuk mengambil orders dari database
            String orderSql = "SELECT order_id, order_date, total_amount, status, payment_method, shipping_address, notes " +
                            "FROM orders WHERE user_id = ? ORDER BY order_date DESC";
            
            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
                orderStmt.setString(1, currentUser.getUserId());
                
                try (ResultSet orderRs = orderStmt.executeQuery()) {
                    while (orderRs.next()) {
                        Order order = new Order();
                        order.setOrderId(orderRs.getString("order_id"));
                        
                        // Apply any overridden status
                        String status = orderRs.getString("status");
                        if (orderStatusOverrides.containsKey(order.getOrderId())) {
                            status = orderStatusOverrides.get(order.getOrderId());
                        }
                        order.setStatus(status);
                        
                        order.setPaymentMethod(orderRs.getString("payment_method"));
                        order.setShippingAddress(orderRs.getString("shipping_address"));
                        order.setTotalAmount(orderRs.getDouble("total_amount"));
                        order.setNotes(orderRs.getString("notes"));
                        
                        // Parse order date
                        java.sql.Timestamp timestamp = orderRs.getTimestamp("order_date");
                        if (timestamp != null) {
                            order.setOrderDate(new Date(timestamp.getTime()));
                        }
                        
                        // Get order items
                        List<OrderItem> items = getOrderItems(conn, order.getOrderId());
                        order.setItems(items);
                        
                        orders.add(order);
                        System.out.println("✅ Loaded order: " + order.getOrderId() + " with " + items.size() + " items");
                    }
                }
            }
            
            System.out.println("✅ Loaded " + orders.size() + " orders from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting user orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        return orders;
    }
    
    private List<OrderItem> getOrderItems(Connection conn, String orderId) {
        List<OrderItem> items = new ArrayList<>();
        
        try {
            System.out.println("🔍 Fetching order items for order: " + orderId);
            
            String itemSql = "SELECT oi.product_id, oi.quantity, oi.unit_price, " +
                           "p.product_id, p.name, p.category, p.material, p.price, p.stock, " +
                           "p.description, p.image_path, p.weight, p.has_gemstone, p.gemstone_type " +
                           "FROM order_items oi " +
                           "LEFT JOIN products p ON oi.product_id = p.product_id " +
                           "WHERE oi.order_id = ?";
            
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                itemStmt.setString(1, orderId);
                
                try (ResultSet itemRs = itemStmt.executeQuery()) {
                    int count = 0;
                    while (itemRs.next()) {
                        count++;
                        String productId = itemRs.getString("product_id");
                        System.out.println("   📦 Found order item #" + count + ": Product ID = " + productId);
                        
                        // Create product from result set
                        Product product = productDAO.getProductById(productId);
                        if (product == null) {
                            System.out.println("   ⚠️ Product not found in cache, creating from result set");
                            // If product not found, create a basic product from the result set
                            String productName = itemRs.getString("name");
                            if (productName == null || productName.trim().isEmpty()) {
                                productName = "Unknown Product (" + productId + ")";
                                System.err.println("   ⚠️ Product name is null, using default name");
                            }
                            
                            product = new Product(
                                productId,
                                productName,
                                itemRs.getString("category") != null ? itemRs.getString("category") : "Unknown",
                                itemRs.getString("material") != null ? itemRs.getString("material") : "Unknown",
                                itemRs.getDouble("price"),
                                itemRs.getInt("stock"),
                                itemRs.getString("description") != null ? itemRs.getString("description") : "",
                                itemRs.getString("image_path") != null ? itemRs.getString("image_path") : "default_product.png",
                                itemRs.getDouble("weight"),
                                itemRs.getBoolean("has_gemstone"),
                                itemRs.getString("gemstone_type") != null ? itemRs.getString("gemstone_type") : ""
                            );
                            System.out.println("   ✅ Created product from result set: " + productName);
                        }
                        
                        // Double check product name
                        if (product != null && (product.getName() == null || product.getName().trim().isEmpty())) {
                            System.err.println("   ⚠️ Product name is still null after creation!");
                            // Try to get name from result set again
                            String nameFromRs = itemRs.getString("name");
                            if (nameFromRs != null && !nameFromRs.trim().isEmpty()) {
                                // Product doesn't have setName, so we can't fix it here
                                System.err.println("   ⚠️ Cannot set product name - Product class may not have setName method");
                            }
                        }
                        
                        OrderItem item = new OrderItem();
                        item.setProduct(product);
                        item.setQuantity(itemRs.getInt("quantity"));
                        item.setPrice(itemRs.getDouble("unit_price"));
                        
                        items.add(item);
                        String productName = (product != null && product.getName() != null) ? product.getName() : "Unknown Product";
                        System.out.println("   ✅ Added item: " + productName + " x" + item.getQuantity());
                    }
                    
                    System.out.println("📊 Total order items found: " + count);
                    if (count == 0) {
                        System.err.println("⚠️ WARNING: No order items found for order: " + orderId);
                        // Cek apakah order_items ada di database
                        String checkSql = "SELECT COUNT(*) as count FROM order_items WHERE order_id = ?";
                        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                            checkStmt.setString(1, orderId);
                            try (ResultSet checkRs = checkStmt.executeQuery()) {
                                if (checkRs.next()) {
                                    int dbCount = checkRs.getInt("count");
                                    System.err.println("   Database shows " + dbCount + " order_items for this order");
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting order items: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        
        return items;
    }

    /**
     * Update the status of an order (stored in-memory for the sample/demo data).
     */
    public boolean updateOrderStatus(String orderId, String newStatus) {
        if (orderId == null || newStatus == null) return false;
        orderStatusOverrides.put(orderId, newStatus);
        System.out.println("🔁 Order status updated: " + orderId + " -> " + newStatus);
        saveOrderStatusOverrides();
        return true;
    }

    public boolean createOrderFromCart(ShoppingCart cart, String paymentMethod, String shippingAddress, String notes) {
        try {
            if (currentUser == null) {
                System.err.println("❌ No user logged in");
                return false;
            }

            
            
            if (cart == null || cart.isEmpty()) {
                System.err.println("❌ Cart is empty");
                return false;
            }
            
            System.out.println("🛒 Creating order from cart...");
            System.out.println("   User: " + currentUser.getUsername());
            System.out.println("   Payment: " + paymentMethod);
            System.out.println("   Shipping: " + shippingAddress);
            System.out.println("   Items: " + cart.getTotalItems());
            System.out.println("   Total: " + CurrencyUtils.format(cart.getTotalAmount()));
            System.out.println("   Notes: " + (notes != null ? notes : "None"));
            
            // ✅ SIMULASI: CREATE ORDER DI DATABASE
            String orderId = "ORD-" + System.currentTimeMillis();
            System.out.println("   Generated Order ID: " + orderId);
            
            // ✅ CLEAR CART SETELAH ORDER BERHASIL
            boolean clearSuccess = clearCart();
            
            if (clearSuccess) {
                System.out.println("🎉 ORDER CREATED SUCCESSFULLY!");
                System.out.println("📦 Order Details:");
                for (CartItem item : cart.getItems()) {
                    double itemTotal = item.getProduct().getPrice() * item.getQuantity();
                    System.out.println("   - " + item.getProduct().getName() + " x" + item.getQuantity() + " = " + CurrencyUtils.format(itemTotal));
                }
                System.out.println("   💰 Total: " + CurrencyUtils.format(cart.getTotalAmount()));
                System.out.println("   💳 Payment: " + paymentMethod);
                System.out.println("   🏠 Shipping: " + shippingAddress);
                
                return true;
            } else {
                System.err.println("❌ Failed to clear cart after order");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error creating order: " + e.getMessage());
            // logged
            return false;
        }
    }

    // ✅ TAMBAHKAN METHOD INI DI DATABASEMANAGER
public boolean createOrderFromSelectedItems(List<CartItem> selectedItems, String paymentMethod, String shippingAddress, String notes, double totalAmount) {
    lastError = null; // Reset error message
    System.out.println("=== 🛒 CHECKOUT PROCESS START ===");
    System.out.println("📋 Parameters:");
    System.out.println("   Selected Items Count: " + (selectedItems != null ? selectedItems.size() : "NULL"));
    System.out.println("   Payment Method: " + paymentMethod);
    System.out.println("   Shipping Address: " + shippingAddress);
System.out.println("   Total Amount: " + CurrencyUtils.format(totalAmount));
    System.out.println("   Current User: " + (currentUser != null ? currentUser.getUserId() : "NULL"));
    
    if (currentUser == null) {
        System.err.println("❌ No user logged in");
        lastError = "No user logged in. Please login again.";
        return false;
    }
    
    if (selectedItems == null || selectedItems.isEmpty()) {
        System.err.println("❌ No items selected for checkout");
        lastError = "No items selected for checkout.";
        return false;
    }
    
    Connection conn = null;
    try {
        System.out.println("🔌 Getting database connection...");
        conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("❌ Failed to get database connection");
            lastError = "Failed to connect to database. Please check your database connection.";
            return false;
        }
        System.out.println("✅ Database connection obtained");
        
        System.out.println("🔄 Starting transaction...");
        try {
            conn.setAutoCommit(false); // Start transaction
            System.out.println("✅ Transaction started (auto-commit = false)");
        } catch (SQLException e) {
            System.err.println("❌ Failed to set auto-commit to false: " + e.getMessage());
            lastError = "Database connection error: " + e.getMessage();
            return false;
        }
        
        // 1. GENERATE ORDER ID
        String orderId = generateOrderId();
        System.out.println("🆕 Creating order: " + orderId);
        
        // 2. INSERT KE TABEL ORDERS
        String orderSql = "INSERT INTO orders (order_id, user_id, order_date, total_amount, status, payment_method, shipping_address, notes) " +
                         "VALUES (?, ?, NOW(), ?, 'PENDING', ?, ?, ?)";
        
        try (PreparedStatement orderStmt = conn.prepareStatement(orderSql)) {
            orderStmt.setString(1, orderId);
            orderStmt.setString(2, currentUser.getUserId());
            orderStmt.setDouble(3, totalAmount);
            orderStmt.setString(4, paymentMethod);
            orderStmt.setString(5, shippingAddress);
            orderStmt.setString(6, notes != null ? notes : "");
            
            int orderResult = orderStmt.executeUpdate();
            if (orderResult == 0) {
                throw new SQLException("Failed to create order");
            }
            System.out.println("✅ Order created: " + orderId);
        }
        
        // 3. INSERT ORDER ITEMS
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) " +
                        "VALUES (?, ?, ?, ?)";
        
        for (CartItem cartItem : selectedItems) {
            double unitPrice = cartItem.getProduct().getPrice();
            String productId = cartItem.getProduct().getProductId();
            int quantity = cartItem.getQuantity();
            
            // Validasi data sebelum insert
            if (productId == null || productId.trim().isEmpty()) {
                throw new SQLException("Invalid product ID for item: " + cartItem.getProduct().getName());
            }
            if (quantity <= 0) {
                throw new SQLException("Invalid quantity for item: " + cartItem.getProduct().getName());
            }
            if (unitPrice <= 0) {
                throw new SQLException("Invalid unit price for item: " + cartItem.getProduct().getName());
            }
            
            System.out.println("📦 Inserting order item:");
            System.out.println("   Order ID: " + orderId);
            System.out.println("   Product ID: " + productId);
            System.out.println("   Product Name: " + cartItem.getProduct().getName());
            System.out.println("   Quantity: " + quantity);
            System.out.println("   Unit Price: " + unitPrice);
            
            // ✅ CATATAN: Tidak perlu verifikasi product karena sudah valid dari cart
            // Verifikasi product akan membuat connection baru yang bisa mengganggu transaction
            
            try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                itemStmt.setString(1, orderId);
                itemStmt.setString(2, productId);
                itemStmt.setInt(3, quantity);
                itemStmt.setDouble(4, unitPrice);
                
                int itemResult = itemStmt.executeUpdate();
                if (itemResult == 0) {
                    System.err.println("❌ Failed to insert order item - executeUpdate returned 0");
                    throw new SQLException("Failed to add order item: " + cartItem.getProduct().getName());
                }
                System.out.println("✅ Order item inserted successfully: " + cartItem.getProduct().getName() + " x" + quantity);
            } catch (SQLException e) {
                System.err.println("❌ SQL Error inserting order item: " + e.getMessage());
                System.err.println("   SQL State: " + e.getSQLState());
                System.err.println("   Error Code: " + e.getErrorCode());
                System.err.println("   Product ID: " + productId);
                System.err.println("   Order ID: " + orderId);
                e.printStackTrace();
                throw e; // Re-throw untuk rollback transaction
            }
            
            // ✅ CATATAN: Stock sudah dikurangi saat add to cart, jadi tidak perlu dikurangi lagi
        }
        
        // 4. HAPUS ITEMS DARI CART (dalam transaction yang sama, tanpa restore stock)
        String deleteCartItemSql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        for (CartItem cartItem : selectedItems) {
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteCartItemSql)) {
                deleteStmt.setInt(1, cartItem.getCartItemId());
                int deleteResult = deleteStmt.executeUpdate();
                if (deleteResult == 0) {
                    System.err.println("⚠️ Warning: Failed to remove cart item: " + cartItem.getCartItemId());
                    // Tidak throw exception karena order sudah dibuat
                } else {
                    System.out.println("✅ Cart item removed: " + cartItem.getCartItemId());
                }
            }
        }
        
        try {
            conn.commit(); // Commit transaction
            System.out.println("🎉 ORDER COMPLETED: " + orderId + " with " + selectedItems.size() + " items");
            System.out.println("✅ Transaction committed successfully");
        } catch (SQLException e) {
            System.err.println("❌ Failed to commit transaction: " + e.getMessage());
            throw e; // Re-throw untuk masuk ke catch block
        }
        
        // ✅ Reset auto-commit sebelum return (dilakukan di finally block)
        lastError = null; // Clear error on success
        return true;
        
    } catch (SQLException e) {
        String errorMsg = "SQL Error: " + e.getMessage();
        if (e.getSQLState() != null) {
            errorMsg += " (SQL State: " + e.getSQLState() + ")";
        }
        if (e.getErrorCode() != 0) {
            errorMsg += " (Error Code: " + e.getErrorCode() + ")";
        }
        
        System.err.println("❌ SQL Error creating order: " + errorMsg);
        e.printStackTrace();
        
        try {
            if (conn != null) {
                conn.rollback(); // Rollback jika error
                System.out.println("🔄 Transaction rolled back");
            }
        } catch (SQLException ex) {
            System.err.println("❌ Error rolling back: " + ex.getMessage());
            ex.printStackTrace();
        }
        
        // Simpan error message untuk ditampilkan ke user
        lastError = errorMsg;
        return false;
    } catch (Exception e) {
        String errorMsg = "Error: " + e.getMessage();
        System.err.println("❌ General error creating order: " + errorMsg);
        e.printStackTrace();
        
        try {
            if (conn != null) {
                conn.rollback(); // Rollback jika error
                System.out.println("🔄 Transaction rolled back");
            }
        } catch (SQLException ex) {
            System.err.println("❌ Error rolling back: " + ex.getMessage());
        }
        
        // Simpan error message untuk ditampilkan ke user
        lastError = errorMsg;
        return false;
    } finally {
        // Reset auto-commit hanya jika transaction berhasil atau perlu cleanup
        if (conn != null) {
            try {
                // Cek apakah connection masih valid
                if (!conn.isClosed()) {
                    try {
                        conn.setAutoCommit(true);
                        System.out.println("✅ Auto-commit reset to true");
                    } catch (SQLException e) {
                        // Jika gagal reset, mungkin connection sudah ditutup
                        System.out.println("⚠️ Could not reset auto-commit (connection may be closed): " + e.getMessage());
                    }
                } else {
                    System.out.println("⚠️ Connection already closed");
                }
            } catch (SQLException e) {
                System.err.println("❌ Error checking connection status: " + e.getMessage());
            }
            // Jangan close connection karena DatabaseConnection menggunakan connection pooling
        }
        System.out.println("=== 🛒 CHECKOUT PROCESS END ===");
    }
}

// ✅ HELPER METHODS
private String generateOrderId() {
    return "ORD_" + System.currentTimeMillis();
}

private String generateOrderItemId() {
    return "OI_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
}

// ✅ Method ini tidak digunakan lagi karena stock sudah dikurangi saat add to cart
// private void updateProductStock(Connection conn, String productId, int quantitySold) throws SQLException {
//     String sql = "UPDATE products SET stock = stock - ? WHERE product_id = ? AND stock >= ?";
//     
//     try (PreparedStatement stmt = conn.prepareStatement(sql)) {
//         stmt.setInt(1, quantitySold);
//         stmt.setString(2, productId);
//         stmt.setInt(3, quantitySold);
//         
//         int result = stmt.executeUpdate();
//         if (result == 0) {
//             throw new SQLException("Insufficient stock for product: " + productId);
//         }
//         System.out.println("📦 Stock updated: " + productId + " -" + quantitySold);
//     }
// }
    
    // ==================== CATEGORY METHODS ====================
    
    public List<String> getAllCategories() {
        return productDAO.getAllProducts().stream()
                .map(Product::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
    
    // ==================== ADMIN METHODS ====================
    
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    public boolean updateUser(User user) {
        System.out.println("🔧 Updating user: " + user.getUsername());
        return true;
    }
    
    public boolean deleteUser(String userId) {
        System.out.println("🗑️ Deleting user: " + userId);
        return true;
    }
    
    // ==================== UTILITY METHODS ====================
    
    public int getTotalProductsCount() {
        return getAllProducts().size();
    }
    
    public int getTotalUsersCount() {
        return userDAO.getAllUsers().size();
    }
    
    public int getLowStockProductsCount(int threshold) {
        return (int) getAllProducts().stream()
                .filter(product -> product.getStock() <= threshold)
                .count();
    }
    
    public double getTotalInventoryValue() {
        return getAllProducts().stream()
                .mapToDouble(product -> product.getPrice() * product.getStock())
                .sum();
    }
    
    // ==================== USER STATISTICS METHODS ====================
    
    public int getUserTotalOrders() {
        if (currentUser == null) {
            return 0;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return 0;
            }
            
            String sql = "SELECT COUNT(*) as total FROM orders WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, currentUser.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user total orders: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public int getUserTotalItemsPurchased() {
        if (currentUser == null) {
            return 0;
        }
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn == null) {
                return 0;
            }
            
            String sql = "SELECT SUM(oi.quantity) as total_items " +
                        "FROM order_items oi " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "WHERE o.user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, currentUser.getUserId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total_items");
                        return rs.wasNull() ? 0 : total;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user total items purchased: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // ==================== GETTERS ====================
    
    public User getCurrentUser() { 
        return currentUser; 
    }
    
    public boolean isLoggedIn() { 
        return currentUser != null; 
    }
    
    public boolean isAdmin() {
        return currentUser instanceof Admin;
    }
    
    public boolean isCustomer() {
        return currentUser instanceof Customer;
    }
    
    // ==================== ERROR HANDLING ====================
    
    public String getLastError() {
        return lastError;
    }
    
    public void clearLastError() {
        lastError = null;
    }
    
    // ==================== CACHE MANAGEMENT ====================
    
    public void refreshUsersCache() {
        users.clear();
        loadUsersFromDatabase();
        System.out.println("🔄 Users cache refreshed");
    }
    
    public void refreshProductsCache() {
        System.out.println("🔄 Products data refreshed from database");
    }
}