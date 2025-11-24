package model;

import utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private String status;
    private String paymentMethod;
    private String shippingAddress;
    private double totalAmount;
    private Date orderDate;
    private String notes;
    private List<OrderItem> items;

    // Constructor
    public Order() {
        this.orderDate = new Date();
        this.status = "PENDING";
    }

    public Order(String orderId, String userId, String orderDate, double totalAmount, 
             String status, String paymentMethod, String notes) {
    this.orderId = orderId;
    this.status = status;
    this.paymentMethod = paymentMethod;
    this.totalAmount = totalAmount;
    this.notes = notes;
    try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.orderDate = sdf.parse(orderDate);
    } catch (Exception e) {
        this.orderDate = new Date();
    }
}

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    // Utility methods
    public String getFormattedDate() {
        if (orderDate == null) return "N/A";
        return new SimpleDateFormat("dd MMM yyyy HH:mm").format(orderDate);
    }

    public String getFormattedTotal() {
        return CurrencyUtils.format(totalAmount);
    }

    public int getTotalItems() {
        return items != null ? items.size() : 0;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                ", items=" + (items != null ? items.size() : 0) +
                '}';
    }
}