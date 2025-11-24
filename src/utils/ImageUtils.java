package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.*;

public class ImageUtils {
    
    /**
     * Get product image dengan debugging yang lebih detail dan multiple fallbacks
     */
    public static ImageIcon getProductImage(String imagePath, int width, int height) {
        System.out.println("🖼️ Loading image: " + imagePath);
        
        try {
            // Jika path null atau kosong, return default
            if (imagePath == null || imagePath.trim().isEmpty()) {
                System.out.println("❌ Image path is null or empty");
                return createDefaultProductIcon(width, height);
            }
        
            File imageFile = new File(imagePath);
            System.out.println("📁 Checking file: " + imageFile.getAbsolutePath());
        
            // Jika file tidak ada, coba berbagai pattern
            if (!imageFile.exists()) {
                System.out.println("🔍 File not found at original path, searching alternatives...");
                
                String fileName = getFileNameFromPath(imagePath);
                String baseName = fileName.replace(".png", "").replace(".jpg", "").replace(".jpeg", "");
                
                // Coba berbagai pattern nama file dan extension
                String[] searchPatterns = {
                    imagePath, // original path
                    "assets/images/products/" + fileName,
                    "assets/images/products/" + baseName + ".jpeg", // Priority: .jpeg
                    "assets/images/products/" + baseName + ".jpg",
                    "assets/images/products/" + baseName + ".png",
                    "assets/images/products/" + baseName.toLowerCase() + ".jpeg",
                    "assets/images/products/" + baseName.toUpperCase() + ".jpeg",
                    "assets/images/products/" + baseName + "_product.jpeg",
                    "assets/images/products/product_" + baseName + ".jpeg",
                    "assets/images/products/" + baseName.replace("ring", "ring") + ".jpeg",
                    "assets/images/products/" + baseName.replace("necklace", "necklace") + ".jpeg",
                    "assets/images/products/" + baseName.replace("bracelet", "bracelet") + ".jpeg",
                    "assets/images/products/" + baseName.replace("earring", "earring") + ".jpeg"
                };
            
                boolean found = false;
                for (String path : searchPatterns) {
                    imageFile = new File(path);
                    System.out.println("   🔍 Trying: " + imageFile.getAbsolutePath());
                    if (imageFile.exists() && imageFile.length() > 0) {
                        System.out.println("   ✅ FOUND: " + path + " (" + imageFile.length() + " bytes)");
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    System.out.println("   ❌ No valid image file found in search patterns");
                }
            }
        
            if (imageFile.exists() && imageFile.length() > 0) {
                System.out.println("✅ SUCCESS: Loading image from: " + imageFile.getAbsolutePath());
                try {
                    // Load dan resize image
                    ImageIcon originalIcon = new ImageIcon(imageFile.getPath());
                    if (originalIcon.getIconWidth() <= 0) {
                        throw new Exception("Invalid image dimensions");
                    }
                    Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                } catch (Exception e) {
                    System.err.println("❌ ERROR processing image: " + e.getMessage());
                    return createDefaultProductIcon(width, height);
                }
            } else {
                System.out.println("❌ FAILED: Image not found or empty, using default icon");
                return createDefaultProductIcon(width, height);
            }
        
        } catch (Exception e) {
            System.err.println("❌ ERROR loading image: " + imagePath + " - " + e.getMessage());
            e.printStackTrace();
            return createDefaultProductIcon(width, height);
        }
    }
    
    /**
     * Buat default product icon yang bagus
     */
    private static ImageIcon createDefaultProductIcon(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints untuk kualitas lebih baik
        setHighQualityRenderingHints(g2d);
        
        // Background gradient emas yang elegan
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(255, 250, 240), // Light gold
            width, height, new Color(245, 222, 179) // Darker gold
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Border emas
        g2d.setColor(new Color(184, 134, 11));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(5, 5, width-10, height-10, 20, 20);
        
        // Icon perhiasan (simbol diamond ring)
        drawJewelryIcon(g2d, width, height);
        
        // Text
        g2d.setColor(new Color(139, 69, 19));
        g2d.setFont(new Font("Serif", Font.BOLD, Math.max(12, width/25)));
        String text = "ARLENE JEWELRY";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int x = (width - textWidth) / 2;
        int y = height - 25;
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        
        return new ImageIcon(image);
    }
    
    /**
     * Set rendering hints untuk kualitas tinggi
     */
    private static void setHighQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }
    
    /**
     * Gambar icon perhiasan yang elegan
     */
    private static void drawJewelryIcon(Graphics2D g2d, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2 - 10;
        int iconSize = Math.min(width, height) / 3;
        
        // Diamond shape
        int[] xPoints = {
            centerX, 
            centerX + iconSize/2, 
            centerX, 
            centerX - iconSize/2
        };
        int[] yPoints = {
            centerY - iconSize/3,
            centerY,
            centerY + iconSize/3,
            centerY
        };
        
        // Fill diamond dengan gradient
        GradientPaint diamondGradient = new GradientPaint(
            centerX - iconSize/2, centerY - iconSize/3, new Color(255, 255, 255),
            centerX + iconSize/2, centerY + iconSize/3, new Color(200, 200, 255)
        );
        g2d.setPaint(diamondGradient);
        g2d.fillPolygon(xPoints, yPoints, 4);
        
        // Diamond border
        g2d.setColor(new Color(100, 100, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolygon(xPoints, yPoints, 4);
        
        // Ring circle
        g2d.setColor(new Color(184, 134, 11));
        g2d.setStroke(new BasicStroke(3));
        int ringSize = iconSize + 10;
        g2d.drawOval(centerX - ringSize/2, centerY - ringSize/2, ringSize, ringSize);
        
        // Sparkle effects
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.5f));
        drawSparkle(g2d, centerX - 20, centerY - 15);
        drawSparkle(g2d, centerX + 15, centerY + 10);
        drawSparkle(g2d, centerX + 10, centerY - 20);
    }
    
    /**
     * Gambar efek sparkle/kilauan
     */
    private static void drawSparkle(Graphics2D g2d, int x, int y) {
        g2d.fillOval(x, y, 4, 4);
        g2d.drawLine(x-4, y, x+4, y);   // Horizontal
        g2d.drawLine(x, y-4, x, y+4);   // Vertical
        g2d.drawLine(x-3, y-3, x+3, y+3); // Diagonal 1
        g2d.drawLine(x-3, y+3, x+3, y-3); // Diagonal 2
    }
    
    /**
     * Cek apakah gambar ada
     */
    public static boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        
        File imageFile = new File(imagePath);
        if (imageFile.exists() && imageFile.length() > 0) {
            return true;
        }
        
        // Cek di product directory dengan berbagai extension
        String fileName = getFileNameFromPath(imagePath);
        String baseName = fileName.replace(".png", "").replace(".jpg", "").replace(".jpeg", "");
        
        String[] extensions = {".jpeg", ".jpg", ".png"};
        for (String ext : extensions) {
            File productFile = new File("assets/images/products/" + baseName + ext);
            if (productFile.exists() && productFile.length() > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Utility untuk extract filename dari path
     */
    private static String getFileNameFromPath(String path) {
        if (path == null) return "default.png";
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash == -1 ? path : path.substring(lastSlash + 1);
    }
    
    /**
     * Buat placeholder image untuk produk baru
     */
    public static ImageIcon createProductPlaceholder(String productName, String category, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // Set rendering hints
        setHighQualityRenderingHints(g2d);
        
        // Background berdasarkan kategori
        Color bgColor = getCategoryColor(category);
        GradientPaint gradient = new GradientPaint(
            0, 0, bgColor.brighter(),
            width, height, bgColor.darker()
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Border
        g2d.setColor(bgColor.darker().darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, width-4, height-4, 15, 15);
        
        // Category icon
        String icon = getCategoryIcon(category);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, Math.min(width/4, 48)));
        FontMetrics fm = g2d.getFontMetrics();
        int iconWidth = fm.stringWidth(icon);
        int iconX = (width - iconWidth) / 2;
        int iconY = height / 3 + fm.getAscent() / 2;
        g2d.drawString(icon, iconX, iconY);
        
        // Product name (truncated if too long)
        g2d.setFont(new Font("SansSerif", Font.BOLD, Math.max(12, width/25)));
        fm = g2d.getFontMetrics();
        String displayName = truncateText(productName, fm, width - 20);
        int nameWidth = fm.stringWidth(displayName);
        int nameX = (width - nameWidth) / 2;
        int nameY = height * 2/3;
        g2d.drawString(displayName, nameX, nameY);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * Warna background berdasarkan kategori
     */
    private static Color getCategoryColor(String category) {
        if (category == null) return new Color(139, 69, 19);
        
        switch (category.toLowerCase()) {
            case "ring": return new Color(184, 134, 11); // Gold
            case "necklace": return new Color(139, 69, 19); // Brown
            case "bracelet": return new Color(192, 192, 192); // Silver
            case "earrings": return new Color(255, 215, 0); // Yellow Gold
            default: return new Color(139, 69, 19); // Default brown
        }
    }
    
    /**
     * Icon berdasarkan kategori
     */
    private static String getCategoryIcon(String category) {
        if (category == null) return "💎";
        
        switch (category.toLowerCase()) {
            case "ring": return "💍";
            case "necklace": return "📿";
            case "bracelet": return "📿";
            case "earrings": return "👂";
            default: return "💎";
        }
    }
    
    /**
     * Potong text jika terlalu panjang
     */
    private static String truncateText(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }
        
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);
        
        for (int i = text.length() - 1; i > 0; i--) {
            String substring = text.substring(0, i);
            if (fm.stringWidth(substring) + ellipsisWidth <= maxWidth) {
                return substring + ellipsis;
            }
        }
        
        return ellipsis;
    }
    
    /**
     * Method untuk scan dan list semua file gambar di products directory
     */
    public static void scanProductsDirectory() {
        System.out.println("\n🔍 SCANNING PRODUCTS DIRECTORY:");
        File productsDir = new File("assets/images/products");
        
        if (!productsDir.exists()) {
            System.out.println("❌ Directory tidak ditemukan: " + productsDir.getAbsolutePath());
            return;
        }
        
        if (!productsDir.isDirectory()) {
            System.out.println("❌ Path bukan directory: " + productsDir.getAbsolutePath());
            return;
        }
        
        File[] files = productsDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".jpeg") || 
            name.toLowerCase().endsWith(".jpg") || 
            name.toLowerCase().endsWith(".png")
        );
        
        if (files == null || files.length == 0) {
            System.out.println("❌ Tidak ada file gambar ditemukan di directory");
            return;
        }
        
        System.out.println("✅ Ditemukan " + files.length + " file gambar:");
        for (File file : files) {
            System.out.println("   📄 " + file.getName() + " (" + file.length() + " bytes)");
        }
    }
    
    /**
     * Method untuk test load specific image
     */
    public static void testImageLoad(String imagePath) {
        System.out.println("\n🧪 TESTING IMAGE LOAD: " + imagePath);
        ImageIcon icon = getProductImage(imagePath, 200, 200);
        if (icon != null && icon.getIconWidth() > 0) {
            System.out.println("✅ TEST SUCCESS: Image loaded successfully");
        } else {
            System.out.println("❌ TEST FAILED: Could not load image");
        }
    }
}