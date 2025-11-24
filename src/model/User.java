package model;

public abstract class User {
    protected String userId;
    protected String username;
    protected String password;
    protected String email;
    protected String phone;
    protected String address;
    
    public User(String userId, String username, String password, String email, String phone, String address) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
    
    // Abstract methods
    public abstract String getRole();
    public abstract String getDisplayName();
    
    // Getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
}