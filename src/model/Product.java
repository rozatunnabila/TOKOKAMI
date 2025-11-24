package model;

import utils.CurrencyUtils;

public class Product {
    private String productId;
    private String name;
    private String category;
    private String material;
    private double price;
    private int stock;
    private String description;
    private String imagePath; // ✅ TAMBAHKAN FIELD INI
    private double weight;
    private boolean hasGemstone;
    private String gemstoneType;
    
    // ✅ CONSTRUCTOR DENGAN IMAGE PATH (11 parameters)
    public Product(String productId, String name, String category, String material, 
                  double price, int stock, String description, String imagePath,
                  double weight, boolean hasGemstone, String gemstoneType) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.material = material;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.imagePath = imagePath;
        this.weight = weight;
        this.hasGemstone = hasGemstone;
        this.gemstoneType = gemstoneType;
    }
    
    // ✅ CONSTRUCTOR TANPA IMAGE PATH (10 parameters - auto generate)
    public Product(String productId, String name, String category, String material, 
                  double price, int stock, String description, double weight, 
                  boolean hasGemstone, String gemstoneType) {
        this(productId, name, category, material, price, stock, description,
             "assets/images/products/" + productId + ".png", // Auto generate path
             weight, hasGemstone, gemstoneType);
    }
    
    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getMaterial() { return material; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; } // ✅ GETTER IMAGE
    public double getWeight() { return weight; }
    public boolean hasGemstone() { return hasGemstone; }
    public String getGemstoneType() { return gemstoneType; }
    
    // Setter untuk image path
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public String getFormattedPrice() {
        return CurrencyUtils.format(price);
    }
    
    @Override
    public String toString() {
        return name + " - " + getFormattedPrice();
    }
}