package model;

import utils.CurrencyUtils;

public class OrderItem {
    private Product product;
    private int quantity;
    private double price;

    // Constructor
    public OrderItem() {}

    public OrderItem(Product product, int quantity, double price) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // Utility methods
    public double getSubtotal() {
        return price * quantity;
    }

    public String getFormattedSubtotal() {
        return CurrencyUtils.format(getSubtotal());
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "product=" + (product != null ? product.getName() : "null") +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}