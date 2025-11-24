package model;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private List<Object> transactionHistory; // Simplified for now
    private String fullName;
    
    public Customer(String userId, String username, String password, String email, 
                   String phone, String address, String fullName) {
        super(userId, username, password, email, phone, address);
        this.fullName = fullName;
        this.transactionHistory = new ArrayList<>();
    }
    
    @Override
    public String getRole() {
        return "CUSTOMER";
    }
    
    @Override
    public String getDisplayName() {
        return fullName;
    }
    
    public String getFullName() { return fullName; }
}