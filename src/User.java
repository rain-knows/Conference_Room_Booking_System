public class User {
    private int userId;
    private String username;
    private String password; // It's better to not store raw password here in a real app
    private String role; // e.g., "admin", "user"
    private String email;
    private String phone;

    public User(int userId, String username, String role, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.phone = phone;
    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    // isAdmin check
    public boolean isAdmin() {
        if (this.role == null) {
            return false;
        }
        String trimmedRole = this.role.trim();
        return "admin".equalsIgnoreCase(trimmedRole) || "SYSTEM_ADMIN".equalsIgnoreCase(trimmedRole);
    }
}