package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Product;

public class ProductDAO {
    
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
    
    public Product getProductById(String productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting product: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting products by category: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
    
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (product_id, name, category, material, price, stock, description, image_path, weight, has_gemstone, gemstone_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getProductId());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setString(4, product.getMaterial());
            pstmt.setDouble(5, product.getPrice());
            pstmt.setInt(6, product.getStock());
            pstmt.setString(7, product.getDescription());
            pstmt.setString(8, product.getImagePath());
            pstmt.setDouble(9, product.getWeight());
            pstmt.setBoolean(10, product.hasGemstone());
            pstmt.setString(11, product.getGemstoneType());
            
            int result = pstmt.executeUpdate();
            System.out.println("✅ Product added to database: " + product.getProductId());
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error adding product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name=?, category=?, material=?, price=?, stock=?, description=?, image_path=?, weight=?, has_gemstone=?, gemstone_type=? " +
                    "WHERE product_id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getCategory());
            pstmt.setString(3, product.getMaterial());
            pstmt.setDouble(4, product.getPrice());
            pstmt.setInt(5, product.getStock());
            pstmt.setString(6, product.getDescription());
            pstmt.setString(7, product.getImagePath());
            pstmt.setDouble(8, product.getWeight());
            pstmt.setBoolean(9, product.hasGemstone());
            pstmt.setString(10, product.getGemstoneType());
            pstmt.setString(11, product.getProductId());
            
            int result = pstmt.executeUpdate();
            System.out.println("✅ Product updated in database: " + product.getProductId());
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteProduct(String productId) {
        String sql = "DELETE FROM products WHERE product_id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            int result = pstmt.executeUpdate();
            System.out.println("🗑️ Product deleted from database: " + productId);
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting product: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean productExists(String productId) {
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking product existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    public String generateProductId(String category) {
        String prefix = getCategoryPrefix(category);
        String sql = "SELECT COUNT(*) FROM products WHERE product_id LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, prefix + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1) + 1;
                    return prefix + String.format("%03d", count);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error generating product ID: " + e.getMessage());
            e.printStackTrace();
        }
        return prefix + "001";
    }
    
    public boolean updateStock(String productId, int newStock) {
        String sql = "UPDATE products SET stock = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newStock);
            pstmt.setString(2, productId);
            
            int result = pstmt.executeUpdate();
            System.out.println("📦 Stock updated for product " + productId + ": " + newStock);
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating stock: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE name LIKE ? OR description LIKE ? OR category LIKE ? ORDER BY name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchTerm = "%" + keyword + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setString(3, searchTerm);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
    
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock <= ? ORDER BY stock ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, threshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting low stock products: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
    
    private String getCategoryPrefix(String category) {
        if (category == null) return "P";
        
        switch (category.toLowerCase()) {
            case "ring": return "R";
            case "necklace": return "N";
            case "bracelet": return "B";
            case "earrings": return "E";
            default: return "P";
        }
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        try {
            // ✅ AMBIL SEMUA 11 PARAMETERS YANG DIBUTUHKAN
            String productId = rs.getString("product_id");
            String name = rs.getString("name");
            String category = rs.getString("category");
            String material = rs.getString("material");
            double price = rs.getDouble("price");
            int stock = rs.getInt("stock");
            String description = rs.getString("description");
            String imagePath = rs.getString("image_path");
            double weight = rs.getDouble("weight");
            boolean hasGemstone = rs.getBoolean("has_gemstone");
            String gemstoneType = rs.getString("gemstone_type");
            
            System.out.println("✅ Creating product: " + name + " | Image: " + imagePath);
            
            // ✅ GUNAKAN CONSTRUCTOR 11 PARAMETERS
            return new Product(
                productId,
                name,
                category, 
                material,
                price,
                stock,
                description != null ? description : "",
                imagePath != null ? imagePath : "default_product.png",
                weight,
                hasGemstone,
                gemstoneType != null ? gemstoneType : ""
            );
            
        } catch (Exception e) {
            System.err.println("❌ CRITICAL ERROR mapping product: " + e.getMessage());
            throw new SQLException("Failed to map product: " + e.getMessage());
        }
    }

} 