public class User {
    private int userId;
    private String username;
    private String password; // It's better to not store raw password here in a real app
    private String role; // e.g., "NORMAL_EMPLOYEE", "LEADER", "SYSTEM_ADMIN"
    private String email;
    private String phone;
    private boolean active; // 账户是否激活

    public User(int userId, String username, String role, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.active = true; // 默认激活
    }

    public User(int userId, String username, String role, String email, String phone, boolean active) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // isAdmin check
    public boolean isAdmin() {
        if (this.role == null) {
            return false;
        }
        String trimmedRole = this.role.trim();
        return "SYSTEM_ADMIN".equalsIgnoreCase(trimmedRole);
    }

    // 检查是否为系统管理员
    public boolean isSystemAdmin() {
        return "SYSTEM_ADMIN".equals(this.role);
    }

    // 检查是否为领导
    public boolean isLeader() {
        return "LEADER".equals(this.role);
    }

    // 检查是否为普通员工
    public boolean isNormalEmployee() {
        return "NORMAL_EMPLOYEE".equals(this.role);
    }
}