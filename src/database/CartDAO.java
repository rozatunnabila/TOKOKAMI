package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.CartItem;
import model.Product;
import model.ShoppingCart;

public class CartDAO {
    private static final Logger LOGGER = Logger.getLogger(CartDAO.class.getName());
    
    /**
     * 🛒 MENDAPATKAN ATAU MEMBUAT KERANJANG BELANJA UNTUK USER
     * Method ini akan mencari keranjang yang sudah ada atau membuat yang baru
     * Jika database tidak tersedia, akan membuat local cart sebagai fallback
     */
    public ShoppingCart getOrCreateCart(String userId) {
        System.out.println("🛒 Getting cart for user: " + userId);
        
        //  CEK KONEKSI DATABASE TERLEBIH DAHULU
        // Memastikan koneksi database berfungsi sebelum melanjutkan
        try {
            Connection testConn = DatabaseConnection.getConnection();
            if (testConn == null || testConn.isClosed()) {
                throw new SQLException("Database connection failed");
            }
        } catch (Exception e) {
            // ⚠️ JIKA DATABASE TIDAK TERSEDIA, GUNAKAN LOCAL CART
            System.out.println("⚠️ Database unavailable, creating local cart for user: " + userId);
            return createLocalCart(userId);
        }
        
        // 🔍 CARI KERANJANG YANG SUDAH ADA DI DATABASE
        String cartId = findCartByUserId(userId);
        
        if (cartId == null) {
            // 📝 JIKA TIDAK ADA, BUAT KERANJANG BARU
            cartId = createNewCart(userId);
            if (cartId == null) {
                // ⚠️ JIKA GAGAL BUAT KERANJANG DI DATABASE, GUNAKAN LOCAL CART
                System.out.println("⚠️ Failed to create database cart, falling back to local cart");
                return createLocalCart(userId);
            }
            System.out.println("✅ Created new cart: " + cartId);
        } else {
            System.out.println("✅ Found existing cart: " + cartId);
        }
        
        // 🛍️ LOAD ITEM-ITEM DALAM KERANJANG
        ShoppingCart cart = new ShoppingCart(cartId, userId);
        List<CartItem> items = getCartItems(cartId);
        cart.setItems(items);
        
        System.out.println("🛒 Cart loaded with " + items.size() + " items");
        return cart;
    }
    
    /**
     * 🆕 METHOD BARU: MEMBUAT LOCAL CART
     * Digunakan ketika database tidak tersedia sebagai fallback mechanism
     * Local cart disimpan di memory saja, tidak di database
     */
    private ShoppingCart createLocalCart(String userId) {
        // 🆔 GENERATE ID UNIK UNTUK LOCAL CART
        String localCartId = "LOCAL_CART_" + userId + "_" + System.currentTimeMillis();
        ShoppingCart cart = new ShoppingCart(localCartId, userId);
        cart.setItems(new ArrayList<>()); // Keranjang kosong
        System.out.println("🛒 Created local cart: " + localCartId);
        return cart;
    }
    
    /**
     * 🔍 MENCARI KERANJANG BERDASARKAN USER ID
     * Method private untuk mencari cart_id dari database berdasarkan user_id
     */
    private String findCartByUserId(String userId) {
        String sql = "SELECT cart_id FROM shopping_carts WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("cart_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding cart: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error finding cart: " + e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 📝 MEMBUAT KERANJANG BARU DI DATABASE
     * Method private untuk membuat record keranjang baru di tabel shopping_carts
     */
    private String createNewCart(String userId) {
        // 🆔 GENERATE CART ID UNIK
        String cartId = "CART_" + userId + "_" + System.currentTimeMillis();
        String sql = "INSERT INTO shopping_carts (cart_id, user_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();
            return cartId;
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating cart: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error creating cart: " + e.getMessage(), e);
        }
        return null; // Return null jika gagal
    }
    
    /**
     * 🔧 METHOD HELPER BARU: EKSTRAK USER ID DARI CART ID
     * Berguna untuk mendapatkan user_id dari format cart_id yang sudah ada
     * Format: CART_USERID_TIMESTAMP atau LOCAL_CART_USERID_TIMESTAMP
     */
    private String extractUserIdFromCartId(String cartId) {
        // Format: CART_USERID_TIMESTAMP atau LOCAL_CART_USERID_TIMESTAMP
        if (cartId.startsWith("CART_")) {
            String[] parts = cartId.split("_");
            if (parts.length >= 3) {
                return parts[1]; // user ID adalah bagian kedua
            }
        } else if (cartId.startsWith("LOCAL_CART_")) {
            String[] parts = cartId.split("_");
            if (parts.length >= 4) {
                return parts[2]; // user ID adalah bagian ketiga untuk LOCAL_CART
            }
        }
        System.err.println("❌ Invalid cart ID format: " + cartId);
        return null;
    }
    
    /**
     ✅ METHOD addToCart YANG SUDAH DIPERBAIKI
     * Menambahkan produk ke keranjang dengan berbagai validasi:
     * 1. Cek apakah ini local cart
     * 2. Cek apakah keranjang ada di database
     * 3. Cek apakah produk ada
     * 4. Update quantity jika item sudah ada, atau insert baru
     */
    public boolean addToCart(String cartId, String productId, int quantity) {
        System.out.println("🛒 DEBUG - Adding to cart:");
        System.out.println("   Cart ID: " + cartId);
        System.out.println("   Product ID: " + productId);
        System.out.println("   Quantity: " + quantity);
        
        // ✅ CEK JIKA INI LOCAL CART
        // Local cart tidak disimpan di database, jadi operasi tidak didukung
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            System.out.println("⚠️ This is a local cart, addToCart operation not supported in database");
            return false; // Local cart ditangani oleh DatabaseManager
        }
        
        // ✅ PERBAIKAN: JIKA CART TIDAK ADA, BUAT BARU
        // Handle kasus dimana cart_id tidak ditemukan di database
        if (!cartExists(cartId)) {
            System.out.println("🛒 Cart not found, extracting user ID and creating new cart...");
            String userId = extractUserIdFromCartId(cartId);
            if (userId == null) {
                System.err.println("❌ Cannot extract user ID from cart ID: " + cartId);
                return false;
            }
            String newCartId = createNewCart(userId);
            if (newCartId == null) {
                System.err.println("❌ Failed to create new cart in database");
                return false;
            }
            System.out.println("✅ Created new cart: " + newCartId);
            cartId = newCartId; // Gunakan cart_id yang baru
        }
        
        // 🔍 CEK APAKAH PRODUK ADA DI DATABASE
        if (!productExists(productId)) {
            System.err.println("❌ Product does not exist: " + productId);
            return false;
        }
        
        // 🔍 CEK APAKAH ITEM SUDAH ADA DI KERANJANG
        Integer existingItemId = findCartItem(cartId, productId);
        
        if (existingItemId != null) {
            // 📈 JIKA SUDAH ADA, UPDATE QUANTITY
            System.out.println("🛒 Item exists, updating quantity...");
            return updateCartItemQuantity(existingItemId, quantity);
        } else {
            // 🆕 JIKA BARU, INSERT ITEM BARU
            System.out.println("🛒 New item, inserting...");
            return insertCartItem(cartId, productId, quantity);
        }
    }
    
    // ==================== HELPER METHODS UNTUK DEBUGGING ====================
    
    /**
     * 🔍 CEK APAKAH KERANJANG ADA DI DATABASE
     */
    private boolean cartExists(String cartId) {
        // Jika local cart, return false karena tidak ada di database
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM shopping_carts WHERE cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    System.out.println("🛒 Cart exists check: " + cartId + " = " + exists);
                    return exists;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking cart existence: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error checking cart existence: " + e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * 🔍 CEK APAKAH PRODUK ADA DI DATABASE
     */
    private boolean productExists(String productId) {
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    boolean exists = rs.getInt(1) > 0;
                    System.out.println("📦 Product exists check: " + productId + " = " + exists);
                    return exists;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking product existence: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error checking product existence: " + e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * 🔍 MENCARI ITEM DI KERANJANG BERDASARKAN CART_ID DAN PRODUCT_ID
     */
    private Integer findCartItem(String cartId, String productId) {
        // Jika local cart, return null
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            return null;
        }
        
        String sql = "SELECT cart_item_id FROM cart_items WHERE cart_id = ? AND product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            pstmt.setString(2, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cart_item_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error finding cart item: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error finding cart item: " + e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 📝 INSERT ITEM BARU KE KERANJANG
     */
    private boolean insertCartItem(String cartId, String productId, int quantity) {
        // Jika local cart, return false
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            return false;
        }
        
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            pstmt.setString(2, productId);
            pstmt.setInt(3, quantity);
            
            int result = pstmt.executeUpdate();
            boolean success = result > 0;
            System.out.println("✅ Insert cart item: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ Error adding to cart: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error adding to cart: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 📈 UPDATE QUANTITY ITEM YANG SUDAH ADA DI KERANJANG
     */
    private boolean updateCartItemQuantity(int cartItemId, int additionalQuantity) {
        String sql = "UPDATE cart_items SET quantity = quantity + ? WHERE cart_item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, additionalQuantity);
            pstmt.setInt(2, cartItemId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating cart item quantity: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error updating cart item quantity: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * ✏️ UPDATE QUANTITY ITEM DI KERANJANG
     * Jika quantity <= 0, item akan dihapus dari keranjang
     */
    public boolean updateCartItem(int cartItemId, int quantity) {
        System.out.println("🛒 Updating cart item: " + cartItemId + " to quantity: " + quantity);
        
        // 🗑️ JIKA QUANTITY <= 0, HAPUS ITEM DARI KERANJANG
        if (quantity <= 0) {
            return removeFromCart(cartItemId);
        }
        
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, cartItemId);
            
            int result = pstmt.executeUpdate();
            boolean success = result > 0;
            System.out.println("✅ Update cart item: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating cart item: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error updating cart item: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 🗑️ MENGHAPUS ITEM DARI KERANJANG BERDASARKAN CART_ITEM_ID
     */
    public boolean removeFromCart(int cartItemId) {
        System.out.println("🛒 Removing cart item: " + cartItemId);
        
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cartItemId);
            int result = pstmt.executeUpdate();
            boolean success = result > 0;
            System.out.println("✅ Remove from cart: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ Error removing from cart: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error removing from cart: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 🗑️ METHOD BARU: Remove by cart_id dan product_id 
     * Berguna untuk rollback operation atau menghapus item spesifik
     */
    public boolean removeFromCart(String cartId, String productId) {
        // Jika local cart, return false
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            return false;
        }
        
        System.out.println("🛒 Removing item from cart - Cart: " + cartId + ", Product: " + productId);
        
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            pstmt.setString(2, productId);
            
            int result = pstmt.executeUpdate();
            boolean success = result > 0;
            System.out.println("✅ Remove from cart by product: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ Error removing from cart: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error removing from cart: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 🧹 MENGHAPUS SEMUA ITEM DALAM KERANJANG
     * Biasanya dipanggil setelah checkout berhasil
     */
    public boolean clearCart(String cartId) {
        // Jika local cart, return false
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            return false;
        }
        
        System.out.println("🛒 Clearing cart: " + cartId);
        
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            int result = pstmt.executeUpdate();
            boolean success = result > 0;
            System.out.println("✅ Clear cart: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ Error clearing cart: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error clearing cart: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 📋 MENDAPATKAN SEMUA ITEM DALAM KERANJANG
     * Method ini melakukan JOIN dengan tabel products untuk mendapatkan detail produk
     */
    private List<CartItem> getCartItems(String cartId) {
        // Jika local cart, return empty list
        if (cartId != null && cartId.startsWith("LOCAL_CART_")) {
            return new ArrayList<>();
        }
        
        List<CartItem> items = new ArrayList<>();
        String sql = "SELECT ci.cart_item_id, ci.cart_id, ci.quantity, " +
                    "p.product_id, p.name, p.category, p.material, p.price, p.stock, " +
                    "p.description, p.image_path, p.weight, p.has_gemstone, p.gemstone_type " +
                    "FROM cart_items ci " +
                    "JOIN products p ON ci.product_id = p.product_id " +
                    "WHERE ci.cart_id = ? " +
                    "ORDER BY ci.cart_item_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cartId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // 🏷️ BUAT OBJEK PRODUCT DARI DATA DATABASE
                    Product product = new Product(
                        rs.getString("product_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("material"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("description"),
                        rs.getString("image_path"),
                        rs.getDouble("weight"),
                        rs.getBoolean("has_gemstone"),
                        rs.getString("gemstone_type")
                    );
                    
                    // 🛍️ BUAT OBJEK CART ITEM
                    CartItem item = new CartItem(
                        rs.getInt("cart_item_id"),
                        rs.getString("cart_id"),
                        product,
                        rs.getInt("quantity")
                    );
                    
                    items.add(item);
                    System.out.println("📦 Cart item: " + product.getName() + " x" + item.getQuantity());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting cart items: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "❌ Error getting cart items: " + e.getMessage(), e);
        }
        
        return items;
    }
}
