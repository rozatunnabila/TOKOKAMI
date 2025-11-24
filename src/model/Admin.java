package model;

public class Admin extends User {
    private String adminLevel;
    
    public Admin(String userId, String username, String password, String email, 
                String phone, String address, String adminLevel) {
        super(userId, username, password, email, phone, address);
        this.adminLevel = adminLevel;
    }
    
    @Override
    public String getRole() {
        return "ADMIN";
    }
    
    @Override
    public String getDisplayName() {
        return "Admin " + username;
    }
    
    public String getAdminLevel() { return adminLevel; }
}
