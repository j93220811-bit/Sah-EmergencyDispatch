package hospital.model;

/** Abstract parent for all application users. */
public abstract class User {
    private final String username;
    private final UserRole role;
    protected User(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public abstract boolean canManageResources();
    public boolean canRegisterPatients() { return true; }
}
