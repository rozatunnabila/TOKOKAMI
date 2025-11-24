package model;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private String cartId;
    private String userId;
    private List<CartItem> items;
    
    public ShoppingCart(String cartId, String userId) {
        this.cartId = cartId;
        this.userId = userId;
        this.items = new ArrayList<>();
    }
    
    public String getCartId() { return cartId; }
    public String getUserId() { return userId; }
    public List<CartItem> getItems() { return items; }
    
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    
    public double getTotalAmount() {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
    
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    public void clear() {
        items.clear();
    }
    
    public boolean isEmpty() {
        return items.isEmpty();
    }
}