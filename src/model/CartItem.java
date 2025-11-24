package model;

public class CartItem {
    private int cartItemId;
    private String cartId;
    private Product product;
    private int quantity;
    
    public CartItem(int cartItemId, String cartId, Product product, int quantity) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.product = product;
        this.quantity = quantity;
    }
    
    public int getCartItemId() { return cartItemId; }
    public String getCartId() { return cartId; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
}